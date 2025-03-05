import { AbstractControl, FormControl, UntypedFormArray, UntypedFormControl, UntypedFormGroup, ValidatorFn, Validators } from '@angular/forms';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { isNullOrUndefined } from '@app/utilities/enhancers/utils';
import { PlanBlueprintSystemFieldType } from '@app/core/common/enum/plan-blueprint-system-field-type';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { FormService } from '../form-service';

export type MarkedValidatorFn = ValidatorFn & { type: string, metadata: unknown };

// **Workaround for Angular limitation:**
// Angular's 'hasValidator' method has a limitation https://v17.angular.io/api/forms/AbstractControl#hasValidator 
// when counting different controls of the same validator
// To address this we followed the approach discussed here https://github.com/angular/angular/issues/54305 
export class CustomValidators {

	static urlRegex = /^(?:http(s)?:\/\/)[\w.-]+(?:\.[\w\.-]+)+[\w\-\._~:\/?#[\]@!\$&'\(\)\*\+,;=.]+$/;

	static extendValidatorWithMetadata(validatorFn: ValidatorFn, type: string, metadata?: unknown): MarkedValidatorFn {
		const fn = validatorFn as MarkedValidatorFn;
		fn.type = type;
		fn.metadata = metadata;
		return fn;
	}

	static required = (): MarkedValidatorFn => this.extendValidatorWithMetadata(Validators.required, 'required');

	static RequiredWithVisibilityRulesValidator(visibilityRulesService: VisibilityRulesService, visibilityRulesKey: string) {
		return this.extendValidatorWithMetadata((control: UntypedFormControl): { [key: string]: any } => {

			if (visibilityRulesService.isVisibleMap[visibilityRulesKey] ?? true) {
				return Validators.required(control);
			}
			FormService.removeError(control, 'required');
			return null;
		}, 'RequiredWithVisibilityRulesValidator');
	}

	static UrlWithVisibilityRulesValidator(visibilityRulesService: VisibilityRulesService, visibilityRulesKey: string) {
		return this.extendValidatorWithMetadata((control: UntypedFormControl): { [key: string]: any } => {

			if (visibilityRulesService.isVisibleMap[visibilityRulesKey] ?? true) {
				return Validators.pattern(CustomValidators.urlRegex);
			}
			FormService.removeError(control, 'pattern');
			return null;
		}, 'UrlWithVisibilityRulesValidator');
	}
}

export function BackendErrorValidator(errorModel: ValidationErrorModel, propertyName: string): ValidatorFn {
	return (control: AbstractControl): { [key: string]: any } => {
		const error: string = errorModel.getError(propertyName);
		return error ? { 'backendError': { message: error } } : null;
	};
}

export function E164PhoneValidator(): ValidatorFn {
	return Validators.pattern('^\\+?[1-9]\\d{1,14}$');
}

// Getter is required because the index of each element is not fixed (array does not always follow LIFO)
export function BackendArrayErrorValidator(errorModel: ValidationErrorModel, propertyNameGetter: () => string): ValidatorFn {
	return (control: AbstractControl): { [key: string]: any } => {
		const error: string = errorModel.getError(propertyNameGetter());
		return error ? { 'backendError': { message: error } } : null;
	};
}

export function CustomErrorValidator(errorModel: ValidationErrorModel, propertyNames: string[]): ValidatorFn {
	return (control: AbstractControl): { [key: string]: any } => {
		const error: string = errorModel.getErrors(propertyNames);
		return error ? { 'customError': { message: error } } : null;
	};
}

export function RequiredWithVisibilityRulesValidator(visibilityRulesService: VisibilityRulesService, visibilityRulesKey: string) {
	return (control: UntypedFormControl): { [key: string]: any } => {

		if (visibilityRulesService.isVisibleMap[visibilityRulesKey] ?? true) {
			return Validators.required(control);
		}
		FormService.removeError(control, 'required');
		return null;
	};
}

export function ValidateIfVisible(visibilityRulesService: VisibilityRulesService, visibilityRulesKey: string, validator: ValidatorFn, errorName: string) {
    return (control: UntypedFormControl): { [key: string]: any } => {
        if (visibilityRulesService.isVisibleMap[visibilityRulesKey] ?? true) {
			return validator(control);
		}
		FormService.removeError(control, errorName);
		return null;
    };
}

export function UrlValidator(): ValidatorFn {
	return Validators.pattern(CustomValidators.urlRegex);
}


export function DateValidator(): ValidatorFn {
	return (control: AbstractControl): { [key: string]: any } => {
		if (control.value) {
			const dateString: string[] = control.value.split('-');
			const yearString = dateString.length > 0 ? dateString[0].replace(/_/g, '') : null;
			const monthString = dateString.length > 1 ? dateString[1].replace(/_/g, '') : null;
			const dayString = dateString.length > 2 ? dateString[2].replace(/_/g, '') : null;
			let yearParsed: number = null;
			let monthParsed: number = null;
			let dayParsed: number = null;

			if (!isNullOrUndefined(yearString) && yearString.length === 4 && !Number.isNaN(Number(yearString))) {
				yearParsed = Number(yearString);
			}
			if (!isNullOrUndefined(monthString) && monthString.length > 0 && !Number.isNaN(Number(monthString))) {
				monthParsed = Number(monthString);
			}
			if (!isNullOrUndefined(dayString) && dayString.length > 0 && !Number.isNaN(Number(dayString))) {
				dayParsed = Number(dayString);
			}

			if ((dayParsed && (!monthParsed || !yearParsed)) || (monthParsed && !yearParsed) || !yearParsed) {
				return { 'invalidDate': true };
			}

			const current_date = new Date();
			yearParsed = yearParsed ? yearParsed : current_date.getFullYear();
			monthParsed = monthParsed ? monthParsed - 1 : current_date.getMonth();
			dayParsed = dayParsed ? dayParsed : 1;

			//due to autocorrection of Date objects
			const d = new Date(yearParsed, monthParsed, dayParsed);
			if (d.getFullYear() === yearParsed && d.getMonth() === monthParsed && d.getDate() === dayParsed) {
				return null;
			}
			return { 'invalidDate': true };
		}
		return null;
	};
}

export function DateFromToValidator(): ValidatorFn {
	return (control: AbstractControl): { [key: string]: any } => {
		if (control.get('fromTime').value && control.get('toTime').value) {
			const fromDate = new Date(control.get('fromTime').value);
			const toDate = new Date(control.get('toTime').value);
			if (fromDate <= toDate) { return null; }
			return { 'invalidFromToDate': true };
		}
		return null;
	};
}

export function EmailMatchValidator(): ValidatorFn {
	return (control: AbstractControl): { [key: string]: any } => {
		return control.get('email').value === control.get('emailConfirm').value ? null : { 'emailMismatch': true };
	};
}

export function PasswordMatchValidator(passwordControlName: string, repeatPasswordControlName: string): ValidatorFn {
	return (control: AbstractControl): { [key: string]: any } => {
		const passwordControl = control.get(passwordControlName);
		const passwordRepeatControl = control.get(repeatPasswordControlName);

		if (passwordControl && passwordControl.value && passwordRepeatControl && passwordRepeatControl.value && passwordControl.value !== passwordRepeatControl.value) {
			return { 'passwordMismatch': true };
		}
		return null;
	};
}

export function PlanBlueprintSystemFieldRequiredValidator(): ValidatorFn {
	return (control: AbstractControl): { [key: string]: any } => {
		let foundTitle = false;
		let foundDescription = false;
		let foundLanguage = false;
		let foundAccess = false;

		const sectionsFormArray = (control as UntypedFormArray);
		if (sectionsFormArray.controls != null && sectionsFormArray.controls.length > 0) {
			sectionsFormArray.controls.forEach((section, index) => {
				const fieldsFormArray = section.get('fields') as UntypedFormArray;
				if (fieldsFormArray && fieldsFormArray.length > 0) {
					if (fieldsFormArray.controls.some(y => (y as UntypedFormGroup).get('systemFieldType')?.value === PlanBlueprintSystemFieldType.Title)) {
						foundTitle = true;
					}
					if (fieldsFormArray.controls.some(y => (y as UntypedFormGroup).get('systemFieldType')?.value === PlanBlueprintSystemFieldType.Description)) {
						foundDescription = true;
					}
					if (fieldsFormArray.controls.some(y => (y as UntypedFormGroup).get('systemFieldType')?.value === PlanBlueprintSystemFieldType.Language)) {
						foundLanguage = true;
					}
					if (fieldsFormArray.controls.some(y => (y as UntypedFormGroup).get('systemFieldType')?.value === PlanBlueprintSystemFieldType.AccessRights)) {
						foundAccess = true;
					}
				}
			});
		}
		return foundTitle && foundDescription && foundAccess && foundLanguage ? null : { 'planBlueprintSystemFieldRequired': true };

	};
}

export function JsonValidator(): ValidatorFn {
    return (control: FormControl<string>): { [key: string]: any} => {
        if(!control?.value){ return;}
        try {
            const value = CleanupJsonString(control.value);
            const json = JSON.parse(value);
        }
        catch (error){
            return {'invalidJson': true};
        }
        return;
    }
}

export function CleanupJsonString(input: string): string {
    return input.replace(/(\r\n\s|\n|\r|\s)/gm, "");
}