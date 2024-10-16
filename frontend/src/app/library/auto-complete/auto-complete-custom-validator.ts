import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function ValidJsonValidator(): ValidatorFn {
	return (control: AbstractControl): ValidationErrors | null => {
		try {
			if (typeof control.value !== 'object') { JSON.parse(control.value); }
		} catch (e) {
			return { 'invalidJson': { value: true } };
		}
		return null;
	};
}

