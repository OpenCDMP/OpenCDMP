import { OnInit, Directive } from '@angular/core';
import { AbstractControl, FormGroup, UntypedFormArray, UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { BaseComponent } from '@common/base/base.component';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';

@Directive()
export class BaseCriteriaComponent<T extends { [K in keyof T]: AbstractControl<any, any>; }> extends BaseComponent implements OnInit {

	public refreshCallback: Function = null;
	public formGroup: FormGroup<T> = null;

	constructor(
		public validationErrorModel: ValidationErrorModel,
	) {
		super();
	}

	ngOnInit() {
		if (this.validationErrorModel == null) { this.validationErrorModel = new ValidationErrorModel(); }
	}

	controlModified(): void {
		this.clearErrorModel();
		if (!this.isFormValid()) { return; }
		if (this.refreshCallback != null) { this.refreshCallback(); }
	}

	public isFormValid(): boolean {
		this.touchAllFormFields(this.formGroup);
		this.validateAllFormFields(this.formGroup);
		return this.formGroup.valid;
	}

	public getFormData(): any {
		return this.formGroup.value;
	}

	public getFormControl(controlName: string): AbstractControl {
		return this.formGroup.get(controlName);
	}

	public disableFormFields(formControl: AbstractControl) {
		formControl.disable();
	}

	public validateAllFormFields(formControl: AbstractControl) {
		if (formControl instanceof UntypedFormControl) {
			formControl.updateValueAndValidity({ emitEvent: false });
		} else if (formControl instanceof UntypedFormGroup) {
			Object.keys(formControl.controls).forEach(item => {
				const control = formControl.get(item);
				this.validateAllFormFields(control);
			});
		} else if (formControl instanceof UntypedFormArray) {
			formControl.controls.forEach(item => {
				this.validateAllFormFields(item);
			});
		}
	}

	public touchAllFormFields(formControl: AbstractControl) {
		if (formControl instanceof UntypedFormControl) {
			formControl.markAsTouched();
		} else if (formControl instanceof UntypedFormGroup) {
			Object.keys(formControl.controls).forEach(item => {
				const control = formControl.get(item);
				this.touchAllFormFields(control);
			});
		} else if (formControl instanceof UntypedFormArray) {
			formControl.controls.forEach(item => {
				this.touchAllFormFields(item);
			});
		}
	}

	setRefreshCallback(callback: Function): void {
		this.refreshCallback = callback;
	}

	onCallbackError(error: any) {
		this.setErrorModel(error.error);
		this.validateAllFormFields(this.formGroup);
	}

	public setErrorModel(validationErrorModel: ValidationErrorModel) {
		Object.keys(validationErrorModel).forEach(item => {
			(<any>this.validationErrorModel)[item] = (<any>validationErrorModel)[item];
		});
	}

	public clearErrorModel() {
		Object.keys(this.validationErrorModel).forEach(item => {
			(<any>this.validationErrorModel)[item] = '';
		});
	}
}
