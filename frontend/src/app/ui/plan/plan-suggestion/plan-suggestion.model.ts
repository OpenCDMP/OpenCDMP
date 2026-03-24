import { UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";
import { Guid } from "@common/types/guid";
import { PreprocessingPlanModel } from "@app/core/model/plan/plan-import";
import { DescriptionImportRdaConfigEditorModel } from "../new/upload-dialogue/plan-common-model-config.editor.model";
import { PlanSuggestion } from "@app/core/model/plan-update-request/plan-update-request";

export class PlanSuggestionModel implements PlanSuggestion{
	planUpdateRequestId: Guid;
	blueprintId: Guid;
	descriptions: DescriptionImportRdaConfigEditorModel[] = [];

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { }

	fromModel(item: PreprocessingPlanModel, planUpdateRequestId: Guid): PlanSuggestionModel {
		
		this.planUpdateRequestId = planUpdateRequestId;
		if (item){
			this.blueprintId = item.blueprintId;
			if (item.preprocessingDescriptionModels?.length > 0) {
				item.preprocessingDescriptionModels.forEach(x => {
					this.descriptions.push(new DescriptionImportRdaConfigEditorModel(this.validationErrorModel).fromModel(x));
				})
			}
		}
		

		return this;
	}

    buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			planUpdateRequestId: [{ value: this.planUpdateRequestId, disabled: disabled }, context.getValidation('planUpdateRequestId').validators],
			blueprintId: [{ value: this.blueprintId, disabled: disabled }, context.getValidation('blueprintId').validators],
			descriptions: this.formBuilder.array(
				(this.descriptions ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `descriptions[${index}].`,
						disabled: disabled
					})
				), context.getValidation('descriptions').validators
			),
		});

		
	}

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		
		baseValidationArray.push({ key: 'planUpdateRequestId', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'planUpdateRequestId')] });
		baseValidationArray.push({ key: 'blueprintId', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'blueprintId')] });
		baseValidationArray.push({ key: 'descriptions', validators: [BackendErrorValidator(this.validationErrorModel, 'descriptions')] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}
	
}

