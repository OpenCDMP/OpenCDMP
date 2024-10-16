import { ChangeDetectorRef, Component, Input, OnInit, SimpleChanges } from '@angular/core';
import { UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { DescriptionTemplateSelectDataEditorModel, DescriptionTemplateSelectOptionEditorModel } from '../../../description-template-editor.model';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { BaseComponent } from '@common/base/base.component';

@Component({
	selector: 'app-description-template-editor-select-field-component',
	styleUrls: ['./description-template-editor-select-field.component.scss'],
	templateUrl: './description-template-editor-select-field.component.html'
})
export class DescriptionTemplateEditorSelectFieldComponent extends BaseComponent implements OnInit {

	@Input() form: UntypedFormGroup;
	@Input() viewOnly: boolean = false;
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
										visibilityRule.get('textValue').setValue(newValue);
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
		const selectOptions: DescriptionTemplateSelectOptionEditorModel = new DescriptionTemplateSelectOptionEditorModel(this.validationErrorModel);
		const selectOptionsArray = this.form.get('data').get('options') as UntypedFormArray;
		selectOptionsArray.push(selectOptions.buildForm({rootPath: this.validationRootPath + 'data.options[' + selectOptionsArray.length + '].'}));
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
			
			DescriptionTemplateSelectDataEditorModel.reapplySelectValidators({
				formGroup: this.form?.get('data') as UntypedFormGroup,
				rootPath: `${this.validationRootPath}data.`,
				validationErrorModel: this.validationErrorModel
			});

			this.form.get('data').get('options').markAsDirty();
		}
	}
}
