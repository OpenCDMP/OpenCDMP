import { UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { Tenant, TenantPersist } from "@app/core/model/tenant/tenant";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";

export class TenantEditorModel extends BaseEditorModel implements TenantPersist {
	name: string;
	code: string;
	description: string;
	permissions: string[];

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super(); }

	public fromModel(item: Tenant): TenantEditorModel {
		if (item) {
			super.fromModel(item);
			this.name = item.name;
			this.code = item.code;
			this.description = item.description;
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			name: [{ value: this.name, disabled: disabled }, context.getValidation('name').validators],
			code: [{ value: this.code, disabled: disabled }, context.getValidation('code').validators],
			description: [{ value: this.description, disabled: disabled }, context.getValidation('description').validators],
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators]
		});
	}

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(this.validationErrorModel, 'id')] });
		baseValidationArray.push({ key: 'name', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'name')] });
		baseValidationArray.push({ key: 'code', validators: [Validators.required, Validators.pattern('^[a-z0-9\_\]+$'), BackendErrorValidator(this.validationErrorModel, 'code')] });
		baseValidationArray.push({ key: 'description', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'description')] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}
