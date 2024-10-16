import { Injectable } from '@angular/core';
import { AbstractControl, UntypedFormArray, UntypedFormControl, UntypedFormGroup } from '@angular/forms';

@Injectable()
export class FormService {

	constructor() {
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
			formControl.updateValueAndValidity({ emitEvent: false })
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

	public removeAllBackEndErrors(formControl: AbstractControl) {
		if (formControl instanceof UntypedFormControl) {
			FormService.removeError(formControl, 'backendError');
		} else if (formControl instanceof UntypedFormGroup) {
			Object.keys(formControl.controls).forEach(item => {
				const control = formControl.get(item);
				this.removeAllBackEndErrors(control);
			});
		} else if (formControl instanceof UntypedFormArray) {
			formControl.controls.forEach(item => {
				this.removeAllBackEndErrors(item);
			});
		}
	}

	public static removeError(control: UntypedFormControl, error: string) {
		const err = control.errors;
		if (err && err[error]) {
			delete err[error];
			if (!Object.keys(err).length) {
				control.setErrors(null); // Make control Valid again
			} else {
				control.setErrors(err); // if there are others errors left attach them back to the control
			}
		}
	}

	public getValue(control: AbstractControl) {
		return JSON.parse(JSON.stringify(control)); //Used to deep copy formGroup.
	}
}
