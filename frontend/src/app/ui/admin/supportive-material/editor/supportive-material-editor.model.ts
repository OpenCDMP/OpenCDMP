import { UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { SupportiveMaterialFieldType } from "@app/core/common/enum/supportive-material-field-type";
import { SupportiveMaterial, SupportiveMaterialPersist } from "@app/core/model/supportive-material/supportive-material";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";

export class SupportiveMaterialEditorModel extends BaseEditorModel implements SupportiveMaterialPersist {
	type: SupportiveMaterialFieldType;
	languageCode: string;
	payload: string;

	permissions: string[];

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super(); }

	public fromModel(item: SupportiveMaterial): SupportiveMaterialEditorModel {
		if (item) {
			super.fromModel(item);
			this.type = item.type;
			this.languageCode = item.languageCode;
			this.payload = item.payload;
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			type: [{ value: this.type, disabled: disabled }, context.getValidation('type').validators],
			languageCode: [{ value: this.languageCode, disabled: disabled }, context.getValidation('languageCode').validators],
			payload: [{ value: this.payload, disabled: disabled }, context.getValidation('payload').validators],		
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators]
		});
	}

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(this.validationErrorModel, 'id')] });
		baseValidationArray.push({ key: 'type', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'type')] });
		baseValidationArray.push({ key: 'languageCode', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'languageCode')] });
		baseValidationArray.push({ key: 'payload', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'payload')] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}
