import { AbstractControl, FormArray, FormControl, FormGroup, UntypedFormArray, UntypedFormControl, ValidatorFn, Validators } from '@angular/forms';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { SystemFieldType } from '@app/core/common/enum/system-field-type';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { FormService } from '../form-service';
import { PlanUserRole } from '@app/core/common/enum/plan-user-role';
import { PlanBlueprintRootSectionForm } from '@app/ui/admin/plan-blueprint/editor/plan-blueprint-editor.model';
import { TemplateFieldForm } from '@app/ui/admin/description-template/editor/description-template-editor.model';

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

			if (yearString?.length === 4 && !Number.isNaN(Number(yearString))) {
				yearParsed = Number(yearString);
			}
			if (monthString && monthString.length > 0 && !Number.isNaN(Number(monthString))) {
				monthParsed = Number(monthString);
			}
			if (dayString && dayString.length > 0 && !Number.isNaN(Number(dayString))) {
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

export function MultiplicityMinMaxValidator(): ValidatorFn {
	return (control: AbstractControl): { [key: string]: any } => {
		const min = control.get('min').value;
		const max = control.get('max').value;
		if (min != null && max != null && min > max) { return { 'multiplicityMinAboveMax': true }; }
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
	return (formArray: FormArray<FormGroup<PlanBlueprintRootSectionForm>>): { [key: string]: any } => {
		let foundTitle = false;
		let foundDescription = false;
		let foundLanguage = false;
		let foundAccess = false;

		const checkFieldsArray = (fieldsFormArray: FormArray<FormGroup<TemplateFieldForm>>) => {
			if (fieldsFormArray && fieldsFormArray.length > 0) {
				if (fieldsFormArray.controls.some(y => y.get('data')?.get('systemFieldType')?.value === SystemFieldType.PlanTitle)) {
					foundTitle = true;
				}
				if (fieldsFormArray.controls.some(y => y.get('data')?.get('systemFieldType')?.value === SystemFieldType.PlanDescription)) {
					foundDescription = true;
				}
				if (fieldsFormArray.controls.some(y => y.get('data')?.get('systemFieldType')?.value === SystemFieldType.PlanLanguage)) {
					foundLanguage = true;
				}
				if (fieldsFormArray.controls.some(y => y.get('data')?.get('systemFieldType')?.value === SystemFieldType.PlanAccessRights)) {
					foundAccess = true;
				}
			}
		};

		const checkSection = (section: AbstractControl) => {
			const fieldSetsFormArray = section.get('fieldSets') as UntypedFormArray;
			if (fieldSetsFormArray && fieldSetsFormArray.length > 0) {
				fieldSetsFormArray.controls.forEach(fieldSet => {
					checkFieldsArray(fieldSet.get('fields') as UntypedFormArray);
				});
			}
			const subsectionsFormArray = section.get('sections') as UntypedFormArray;
			if (subsectionsFormArray && subsectionsFormArray.length > 0) {
				subsectionsFormArray.controls.forEach(sub => checkSection(sub));
			}
		};

		if (formArray.controls != null && formArray.controls.length > 0) {
			formArray.controls.forEach(section => checkSection(section));
		}
		return foundTitle && foundDescription && foundAccess && foundLanguage ? null : {
            'planBlueprintSystemFieldRequired': true,
            'titleRequired': !foundTitle,
            'descriptionRequired': !foundDescription,
            'accessRequired': !foundAccess,
            'languageRequired': !foundLanguage
        };

	};
}

export function PlanBlueprintHasDescriptionTemplatesValidator(): ValidatorFn {
	return (formArray: FormArray<FormGroup<PlanBlueprintRootSectionForm>>): { [key: string]: any } => {
		const sectionHasTemplates = (section: AbstractControl): boolean => {
			if (section.get('hasTemplates')?.value === true) { return true; }
			const subsectionsFormArray = section.get('sections') as UntypedFormArray;
			if (subsectionsFormArray && subsectionsFormArray.length > 0) {
				return subsectionsFormArray.controls.some(sub => sectionHasTemplates(sub));
			}
			return false;
		};

		const foundDescriptionTemplates = formArray.controls?.some(section => sectionHasTemplates(section)) ?? false;

		return foundDescriptionTemplates ? null : { 'planBlueprintDescriptionTemplatesRequired': true };
	};
}

export function DescriptionTemplateSystemFieldRequiredValidator(): ValidatorFn {
	return (formArray: FormArray<FormGroup<PlanBlueprintRootSectionForm>>): { [key: string]: any } => {
		let foundTitle = false;

		const checkFieldsArray = (fieldsFormArray: FormArray<FormGroup<TemplateFieldForm>>) => {
			if (fieldsFormArray && fieldsFormArray.length > 0) {
				if (fieldsFormArray.controls.some(y => y.get('data')?.get('systemFieldType')?.value === SystemFieldType.PlanDescriptionTitle)) {
					foundTitle = true;
				}
			}
		};

		const checkSection = (section: AbstractControl) => {
			const fieldSetsFormArray = section.get('fieldSets') as UntypedFormArray;
			if (fieldSetsFormArray && fieldSetsFormArray.length > 0) {
				fieldSetsFormArray.controls.forEach(fieldSet => {
					checkFieldsArray(fieldSet.get('fields') as UntypedFormArray);
				});
			}
			const subsectionsFormArray = section.get('sections') as UntypedFormArray;
			if (subsectionsFormArray && subsectionsFormArray.length > 0) {
				subsectionsFormArray.controls.forEach(sub => checkSection(sub));
			}
		};

		if (formArray.controls != null && formArray.controls.length > 0) {
			formArray.controls.forEach(section => checkSection(section));
		}
		return foundTitle ? null : { 'systemFieldRequired': true };

	};
}

export function PlanOwnerRequiredValidator(): ValidatorFn {
	return (control: AbstractControl): { [key: string]: any } => {

		let foundOwner = false;

		const usersFormArray = (control as UntypedFormArray);
		if (usersFormArray.controls != null && usersFormArray.controls.length > 0) {
			usersFormArray.controls.forEach((user, index) => {
				if ( (user.get('user').value != null || user.get('email').value?.length > 0) && user.get('role').value === PlanUserRole.Owner && (user.get('sectionId').value == null || user.get('sectionId').value?.length == 0)) {
					foundOwner = true;
				}
			});
		}
		return foundOwner ? null : { 'planOwnerRequired': true };

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