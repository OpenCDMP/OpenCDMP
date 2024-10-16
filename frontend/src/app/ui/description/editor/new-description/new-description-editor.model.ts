import { UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { DescriptionPrefillingRequest, DescriptionPrefillingRequestData } from "@app/core/model/description-prefilling-request/description-prefilling-request";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";
import { Guid } from "@common/types/guid";

export class DescriptionPrefillingRequestEditorModel implements DescriptionPrefillingRequest {
	prefillingSourceId: Guid;
	descriptionTemplateId: Guid;
	data: DescriptionPrefillingRequestData;
	project: string[];

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

    buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			prefillingSourceId: [{ value: this.prefillingSourceId, disabled: disabled }, context.getValidation('prefillingSourceId').validators],
			descriptionTemplateId: [{ value: this.descriptionTemplateId, disabled: disabled }, context.getValidation('descriptionTemplateId').validators],
			data: [{ value: this.data, disabled: disabled }, context.getValidation('data').validators],
		});
	}

	createValidationContext(): ValidationContext {

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'prefillingSourceId', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, `prefillingSourceId`)] });
		baseValidationArray.push({ key: 'descriptionTemplateId', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, `descriptionTemplateId`)] });
        baseValidationArray.push({ key: 'data', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, `data`)] });

        baseContext.validation = baseValidationArray;
		return baseContext;
	}
}
