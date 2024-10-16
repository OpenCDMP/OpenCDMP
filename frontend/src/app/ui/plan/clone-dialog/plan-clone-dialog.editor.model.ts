import { UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { ClonePlanPersist, Plan } from "@app/core/model/plan/plan";
import { BackendErrorValidator } from '@common/forms/validation/custom-validator';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { Validation, ValidationContext } from '@common/forms/validation/validation-context';
import { Guid } from "@common/types/guid";

export class PlanCloneDialogEditorModel implements ClonePlanPersist {
	id: Guid;
	label: string;
	description: String;
	descriptions: Guid[] = [];

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { }

	public fromModel(item: Plan): PlanCloneDialogEditorModel {
		if (item) {
			this.id = item.id;
			this.label = item.label + " New";
			this.description = item.description;
			this.descriptions = item.descriptions?.map(d=> d.id);
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			label: [{ value: this.label, disabled: disabled }, context.getValidation('label').validators],
			description: [{ value: this.description, disabled: disabled }, context.getValidation('description').validators],
			descriptions: [{ value: this.descriptions, disabled: disabled }, context.getValidation('descriptions').validators],
		});
	}

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(this.validationErrorModel, 'id')] });
		baseValidationArray.push({ key: 'label', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'label')] });
		baseValidationArray.push({ key: 'description', validators: [BackendErrorValidator(this.validationErrorModel, 'description')] });
		baseValidationArray.push({ key: 'descriptions', validators: [BackendErrorValidator(this.validationErrorModel, 'descriptions')] });
		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}
