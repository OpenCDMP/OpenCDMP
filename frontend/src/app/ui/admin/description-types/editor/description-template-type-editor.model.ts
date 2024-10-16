import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { DescriptionTemplateTypeStatus } from '@app/core/common/enum/description-template-type-status';
import { DescriptionTemplateType, DescriptionTemplateTypePersist } from '@app/core/model/description-template-type/description-template-type';
import { BaseEditorModel } from '@common/base/base-form-editor-model';
import { BackendErrorValidator } from '@common/forms/validation/custom-validator';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { Validation, ValidationContext } from '@common/forms/validation/validation-context';

export class DescriptionTemplateTypeEditorModel extends BaseEditorModel implements DescriptionTemplateTypePersist {
	name: string;
	code: string;
	status: DescriptionTemplateTypeStatus = DescriptionTemplateTypeStatus.Draft;
	permissions: string[];

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super(); }

	public fromModel(item: DescriptionTemplateType): DescriptionTemplateTypeEditorModel {
		if (item) {
			super.fromModel(item);
			this.id = item.id;
			this.name = item.name;
			this.code = item.code;
			this.status = item.status;
			this.isActive = item.isActive;
			this.hash = item.hash;
			if (item.createdAt) { this.createdAt = item.createdAt; }
			if (item.updatedAt) { this.updatedAt = item.updatedAt; }
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			name: [{ value: this.name, disabled: disabled }, context.getValidation('name').validators],
			code: [{ value: this.code, disabled: disabled }, context.getValidation('code').validators],
			status: [{ value: this.status, disabled: disabled }, context.getValidation('status').validators],
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators]
		});
	}

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(this.validationErrorModel, 'id')] });
		baseValidationArray.push({ key: 'name', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'name')] });
		baseValidationArray.push({ key: 'code', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'code')] });
		baseValidationArray.push({ key: 'status', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'status')] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}
