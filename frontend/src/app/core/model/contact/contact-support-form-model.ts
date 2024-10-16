import { UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";

export interface ContactSupportPersist{
	subject: string,
	description: string;
}

export interface PublicContactSupportPersist{
	fullName: string,
	email: string,
	affiliation: string;
	message: string
}

export class ContactEmailFormModel implements ContactSupportPersist{
	subject: string;
	description: string;
	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();

	fromModel(item: ContactSupportPersist): ContactEmailFormModel {
		this.subject = item.subject;
		this.description = item.description;
		return this;
	}

	buildForm(): UntypedFormGroup {
		const context = this.createValidationContext();
		const formGroup = new UntypedFormBuilder().group({
			subject: [this.subject, context.getValidation('subject').validators],
			description: [this.description, context.getValidation('description').validators]
		});
		return formGroup;
	}

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'subject', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'subject')] });
		baseValidationArray.push({ key: 'description', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'description')] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}
