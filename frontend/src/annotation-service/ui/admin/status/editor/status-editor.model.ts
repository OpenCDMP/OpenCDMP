import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { InternalStatus } from '@annotation-service/core/enum/internal-status.enum';
import { Status, StatusPersist } from '@annotation-service/core/model/status.model';
import { BaseEditorModel } from '@common/base/base-form-editor-model';
import { BackendErrorValidator } from '@common/forms/validation/custom-validator';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { Validation, ValidationContext } from '@common/forms/validation/validation-context';

export class StatusEditorModel extends BaseEditorModel implements StatusPersist {
	label: string;
	internalStatus: InternalStatus;
	permissions: string[];

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super(); }

	public fromModel(item: Status): StatusEditorModel {
		if (item) {
			super.fromModel(item);
			this.label = item.label;
			this.internalStatus = item.internalStatus;
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			label: [{ value: this.label, disabled: disabled }, context.getValidation('label').validators],
			internalStatus: [{ value: this.internalStatus, disabled: disabled }, context.getValidation('internalStatus').validators],
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators]
		});
	}

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(this.validationErrorModel, 'id')] });
		baseValidationArray.push({ key: 'label', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'label')] });
		baseValidationArray.push({ key: 'internalStatus', validators: [BackendErrorValidator(this.validationErrorModel, 'internalStatus')] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}
