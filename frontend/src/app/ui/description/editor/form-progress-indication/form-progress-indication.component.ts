import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { AbstractControl, UntypedFormArray, UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { BaseComponent } from '@common/base/base.component';
import { MarkedValidatorFn } from '@common/forms/validation/custom-validator';
import { takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { DescriptionEditorModel } from '../description-editor.model';

@Component({
    selector: 'app-form-progress-indication',
    templateUrl: './form-progress-indication.component.html',
    styleUrls: ['./form-progress-indication.component.scss'],
    standalone: false
})
export class FormProgressIndicationComponent extends BaseComponent implements OnInit, OnChanges {
	@Input() formGroup: UntypedFormGroup;
	@Input() public progressValueAccuracy = 2;
	@Input() checkVisibility: boolean = false;
	determinateProgressValue: number;
	progressSoFar: number;
	total: number;
	percent: number;
	fieldTypes: string[] = ['dateValue', 'booleanValue', 'externalIdentifier.identifier', 'externalIdentifier.type', 'reference', 'references', 'tags', 'textListValue', 'textValue'];

	constructor(private visibilityRulesService: VisibilityRulesService) { super(); }

	public value = 0;
	ngOnInit() {
		this.init();
	}

	ngOnChanges(changes: SimpleChanges) {
		if (changes.formGroup) {
			this.init();
		}
	}

	init() {
		setTimeout(() => { this.calculateValueForProgressbar(); });
		this.formGroup
			.valueChanges
			.pipe(takeUntil(this._destroyed))
			.subscribe(control => {
				setTimeout(() => { this.calculateValueForProgressbar(); });
			});
	}

	calculateValueForProgressbar() {
		if (this.formGroup.disabled) return;

		this.progressSoFar = this.countCompletedRequiredBaseFields(this.formGroup) + this.countRequiredFields(this.formGroup.get('properties'), this.checkVisibility, true);
		this.total = 2 + this.countRequiredFields(this.formGroup.get('properties'), this.checkVisibility); // main info contains two required fields: label and descriptionTemplateId
		this.percent = (this.progressSoFar / this.total) * 100;
		this.value = Number.parseFloat(this.percent.toPrecision(this.progressValueAccuracy));
	}

	countCompletedRequiredBaseFields(formControl: AbstractControl): number {
		let count = 0;
		const baseInfoControlNames: string[] = [nameof<DescriptionEditorModel>(x => x.label), nameof<DescriptionEditorModel>(x => x.descriptionTemplateId)];
		baseInfoControlNames.forEach((name: string) => {
			if (this.formGroup.get(name)?.valid) count += 1;
		});

		return count;
	}

	countRequiredFields(formControl: AbstractControl, checkVisibility = false, countCompletedFields = false): number {
		let valueCurrent = 0;
		if (formControl instanceof UntypedFormGroup) {
			if (!checkVisibility || (!formControl.get('id')?.value || (this.visibilityRulesService.isVisibleMap[formControl.get('id').value] ?? true))) {
				Object.keys(formControl.controls).forEach(item => {
					const control = formControl.get(item);
					valueCurrent = valueCurrent + this.countRequiredFields(control, checkVisibility, countCompletedFields);
				});
			}
		} else if (formControl instanceof UntypedFormArray) {
			formControl.controls.forEach(item => {
				valueCurrent = valueCurrent + this.countRequiredFieldsByFieldset(item.get('ordinal').value, item.get('fields') as UntypedFormGroup, countCompletedFields);
			});
		}
		return valueCurrent;
	}

	countRequiredFieldsByFieldset(ordinal: number, fieldsFormGroup: UntypedFormGroup, filterValid: boolean = false): number {
		let fieldsCount: number = 0;
		const fieldSetNames = Object.keys(fieldsFormGroup.controls);
		for (let item of fieldSetNames) {
			if (!this.checkVisibility || this.visibilityRulesService.isVisible(item, ordinal)) {
				const fieldControl = fieldsFormGroup.get(item);
				const fieldNames = Object.keys((fieldControl as UntypedFormGroup).controls);
				for (let fieldType of fieldNames) {
					const typedControl = fieldControl.get(fieldType);
					let controlFilter: boolean = this.controlRequired(typedControl) && this.controlEnabled(typedControl);
					if (filterValid) controlFilter = controlFilter && typedControl.valid;

					if (controlFilter) {
						fieldsCount++;
						break;
					}
				}
			}
		}
		return fieldsCount;
	}

	//////////////////////////////////////////////////////////////////////

	countFormControlsWithValueForProgress(formControl: AbstractControl): number {
		let valueCurent = 0;
		if (formControl instanceof UntypedFormGroup) {
			if (this.checkFormsIfIsFieldsAndVisible(formControl) && this.checkIfIsRequired((formControl as UntypedFormGroup))) {
				if (this.hasValue(formControl))
					valueCurent++;
			}
			if (this.chechFieldIfIsFieldSetAndVisible((formControl as UntypedFormGroup)) && this.checkIfIsRequired((formControl as UntypedFormGroup))) {
				valueCurent = valueCurent + this.fieldSetsGetChildsForProgress(formControl);
			} else {
				Object.keys(formControl.controls).forEach(item => {
					const control = formControl.get(item);
					valueCurent = valueCurent + this.countFormControlsWithValueForProgress(control);
				});
			}
		} else if (formControl instanceof UntypedFormArray) {
			formControl.controls.forEach(item => {
				valueCurent = valueCurent + this.countFormControlsWithValueForProgress(item);
			});
		}
		return valueCurent;
	}
	private hasValue(formGroup: UntypedFormGroup): boolean {
		return formGroup.get('value').valid && formGroup.get('value').value != null && formGroup.get('value').value !== '' && (this.visibilityRulesService.isVisibleMap[formGroup.get('id').value] ?? true);
	}

	private fieldSetsGetChildsForProgress(formGroup: UntypedFormGroup): number {
		let valueCurent = 0;
		if (this.visibilityRulesService.isVisibleMap[formGroup.get('id').value] ?? true) {
			(formGroup.get('fields') as UntypedFormArray).controls.forEach((element: UntypedFormGroup) => {
				valueCurent = valueCurent + this.countFormControlsWithValueForProgress(element);
			});

			(formGroup.get('multiplicityItems') as UntypedFormArray).controls.forEach((element: UntypedFormGroup) => {
				valueCurent = valueCurent + this.countFormControlsWithValueForProgress(element);
			});
		}
		return valueCurent;
	}

	private checkIfIsRequired(formControl: UntypedFormGroup): boolean {
		return !!(formControl.get('validationRequired') && formControl.get('validationRequired').value);

	}

	private checkFormsIfIsFieldsAndVisible(formControl: UntypedFormGroup): boolean {
		if (formControl.contains('id') && formControl.contains('value')) {
			return true;
		}
		return false;
	}
	private chechFieldIfIsFieldSetAndVisible(formControl: UntypedFormGroup): boolean {
		if (formControl.contains('id') && formControl.contains('fields')) {
			return true;
		}
		return false;
	}

	CountFormControlDepthLengthFotTotal(formControl: AbstractControl): number {
		let valueCurent = 0;
		if (formControl instanceof UntypedFormArray) {
			formControl.controls.forEach(item => {
				valueCurent = valueCurent + this.CountFormControlDepthLengthFotTotal(item);
			});
		} else if (formControl instanceof UntypedFormGroup) {
			if ((formControl as UntypedFormGroup).contains('id') && (formControl as UntypedFormGroup).contains('value') && (this.visibilityRulesService.isVisibleMap[(formControl as UntypedFormGroup).get('id').value] ?? true) && this.checkIfIsRequired((formControl as UntypedFormGroup))) {
				valueCurent++;
			} else if ((formControl as UntypedFormGroup).contains('id') && (formControl as UntypedFormGroup).contains('fields')) {
				valueCurent = valueCurent + this.fieldSetsGetChildsForTotal(formControl);
			} else {
				Object.keys(formControl.controls).forEach(item => {
					const control = formControl.get(item);
					valueCurent = valueCurent + this.CountFormControlDepthLengthFotTotal(control);
				});
			}
		}

		return valueCurent;
	}

	private fieldSetsGetChildsForTotal(formGroup: UntypedFormGroup): number {
		let valueCurent = 0;
		if (this.visibilityRulesService.isVisibleMap[formGroup.get('id').value] ?? true) {
			(formGroup.get('fields') as UntypedFormArray).controls.forEach((element: UntypedFormGroup) => {
				valueCurent = valueCurent + this.CountFormControlDepthLengthFotTotal(element);
			});
			(formGroup.get('multiplicityItems') as UntypedFormArray).controls.forEach((element: UntypedFormGroup) => {
				valueCurent = valueCurent + this.CountFormControlDepthLengthFotTotal(element);
			});
		}
		return valueCurent;
	}

	countFormControlsValidForProgress(formControl: AbstractControl): number {
		let valueCurrent = 0;
		if (formControl instanceof UntypedFormControl) {
			if (this.controlRequired(formControl) && this.controlEnabled(formControl) && formControl.valid) {
				valueCurrent++;
			}
		} else if (formControl instanceof UntypedFormGroup) {
			Object.keys(formControl.controls).forEach(item => {
				const control = formControl.get(item);
				valueCurrent = valueCurrent + this.countFormControlsValidForProgress(control);
			});
		} else if (formControl instanceof UntypedFormArray) {
			formControl.controls.forEach(item => {
				valueCurrent = valueCurrent + this.countFormControlsValidForProgress(item);
			});
		}
		return valueCurrent;
	}

	controlRequired(formControl: AbstractControl) {
		if (formControl.validator) {
			const validators = (formControl as AbstractControl & { _rawValidators: MarkedValidatorFn[] })._rawValidators;
			return validators.some(validator => validator.type === 'RequiredWithVisibilityRulesValidator');
		}

		return false;
	}

	controlEnabled(formControl: AbstractControl) {
		if (formControl.enabled) {
			return true;
		} else { return false }
	}
}
