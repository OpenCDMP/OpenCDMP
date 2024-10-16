import { UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { Language, LanguagePersist } from "@app/core/model/language/language";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";

export class LanguageEditorModel extends BaseEditorModel implements LanguagePersist {
	code: string;
	payload: string;
	ordinal: number;

	permissions: string[];

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super(); }

	public fromModel(item: Language): LanguageEditorModel {
		if (item) {
			super.fromModel(item);
			this.code = item.code;
			this.payload = item.payload;
			this.ordinal = item.ordinal;
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			code: [{ value: this.code, disabled: disabled }, context.getValidation('code').validators],
			payload: [{ value: this.payload, disabled: disabled }, context.getValidation('payload').validators],
			ordinal: [{ value: this.ordinal, disabled: disabled }, context.getValidation('ordinal').validators],				
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators]
		});
	}

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(this.validationErrorModel, 'id')] });
		baseValidationArray.push({ key: 'code', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'code')] });
		baseValidationArray.push({ key: 'payload', validators: [BackendErrorValidator(this.validationErrorModel, 'payload')] });
		baseValidationArray.push({ key: 'ordinal', validators: [Validators.required, Validators.pattern("^[0-9]*$"), BackendErrorValidator(this.validationErrorModel, 'ordinal')] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}
