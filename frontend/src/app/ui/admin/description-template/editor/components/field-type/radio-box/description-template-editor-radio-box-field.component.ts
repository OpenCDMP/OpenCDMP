import { Component, Input, OnInit, SimpleChanges } from '@angular/core';
import { UntypedFormArray, UntypedFormBuilder, UntypedFormGroup } from '@angular/forms';
import { DescriptionTemplateRadioBoxDataEditorModel, DescriptionTemplateRadioBoxOptionEditorModel } from '../../../description-template-editor.model';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { BaseComponent } from '@common/base/base.component';

@Component({
	selector: 'app-description-template-editor-radio-box-field-component',
	styleUrls: ['./description-template-editor-radio-box-field.component.scss'],
	templateUrl: './description-template-editor-radio-box-field.component.html'
})
export class DescriptionTemplateEditorRadioBoxFieldComponent extends BaseComponent implements OnInit {

	@Input() form: UntypedFormGroup;
	@Input() validationErrorModel: ValidationErrorModel;
	@Input() validationRootPath: string;

	ngOnInit() {
		this.applyNewVisibilityValueListener();
	}

	ngOnChanges(changes: SimpleChanges) {
		this.applyNewVisibilityValueListener();
	}

	applyNewVisibilityValueListener() {
		(this.form.get('data').get('options') as UntypedFormArray).controls?.forEach(
			(option, index) => {
				let oldValue = option.get('value').value || null; 
				option.get('value').valueChanges
				.pipe(takeUntil(this._destroyed), debounceTime(200), distinctUntilChanged())
				.subscribe(newValue => {
					if (newValue) {
						(this.form.get('visibilityRules') as UntypedFormArray).controls?.forEach(
							(visibilityRule, index) => {
								if (visibilityRule.get('textValue').value == oldValue){
									setTimeout( () => {
										if (newValue == null){
											visibilityRule.get('textValue').setValue(null);
										} else {
											visibilityRule.get('textValue').setValue(newValue);
										}
										visibilityRule.updateValueAndValidity();
										oldValue = newValue;
									}, 100);
								}
							}
							
						)							
					}
				});
			}
		)
	}

	addNewRow() {
		const radioListOptions: DescriptionTemplateRadioBoxOptionEditorModel = new DescriptionTemplateRadioBoxOptionEditorModel(this.validationErrorModel);
		const selectOptionsArray = this.form.get('data').get('options') as UntypedFormArray;

		if (!selectOptionsArray) { (<UntypedFormGroup>this.form.get('data')).addControl('options', new UntypedFormBuilder().array([])); }
		selectOptionsArray.push(radioListOptions.buildForm({rootPath: this.validationRootPath + 'data.options[' + selectOptionsArray.length + '].'}));
		this.applyNewVisibilityValueListener();
	}

	deleteRow(intex: number) {
		if (this.form.get('data').get('options')) { 
			const control = (this.form.get('data').get('options') as UntypedFormArray).at(intex) as UntypedFormGroup;
			if (control && control.get('value').value != null){
				(this.form.get('visibilityRules') as UntypedFormArray).controls?.forEach(
					(visibilityRule, index) => {
						if (visibilityRule.get('textValue').value == control.get('value').value){
							setTimeout( () => {
								visibilityRule.get('textValue').setValue(null);
								visibilityRule.updateValueAndValidity();
							}, 100);							
						}
					}				
				);		
			}

			(<UntypedFormArray>this.form.get('data').get('options')).removeAt(intex); 
			
			DescriptionTemplateRadioBoxDataEditorModel.reapplyRadioBoxValidators({
				formGroup: this.form.get('data') as UntypedFormGroup,
				rootPath: `${this.validationRootPath}data.`,
				validationErrorModel: this.validationErrorModel
			});

			this.form.get('data').get('options').markAsDirty();
		}
	}
}
