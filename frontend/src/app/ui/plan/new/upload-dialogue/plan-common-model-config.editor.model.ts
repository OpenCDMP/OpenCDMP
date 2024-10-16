import { UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";
import { Guid } from "@common/types/guid";
import { PlanCommonModelConfig, PreprocessingPlanModel } from "@app/core/model/plan/plan-import";
import { DescriptionCommonModelConfig, PreprocessingDescriptionModel } from "@app/core/model/description/description-import";

export class PlanImportRdaConfigEditorModel implements PlanCommonModelConfig{
	fileId: Guid;
	label: string;
	blueprintId: Guid;
	repositoryId: string = 'rda-file-transformer';
	descriptions: DescriptionImportRdaConfigEditorModel[] = [];

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { }

	fromModel(item: PreprocessingPlanModel, fileId: Guid): PlanImportRdaConfigEditorModel {
		this.fileId = fileId;
		if (item){
			this.label = item.label + '.json';
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
			fileId: [{ value: this.fileId, disabled: disabled }, context.getValidation('fileId').validators],
			label: [{ value: this.label, disabled: disabled }, context.getValidation('label').validators],
			blueprintId: [{ value: this.blueprintId, disabled: disabled }, context.getValidation('blueprintId').validators],
			repositoryId: [{ value: this.repositoryId, disabled: disabled }, context.getValidation('repositoryId').validators],
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
		
		baseValidationArray.push({ key: 'fileId', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'fileId')] });
		baseValidationArray.push({ key: 'label', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'label')] });
		baseValidationArray.push({ key: 'blueprintId', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'blueprintId')] });
		baseValidationArray.push({ key: 'repositoryId', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'repositoryId')] });
		baseValidationArray.push({ key: 'descriptions', validators: [BackendErrorValidator(this.validationErrorModel, 'descriptions')] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}
	
}

export class DescriptionImportRdaConfigEditorModel implements DescriptionCommonModelConfig {
	id: string;
	sectionId: Guid;
    templateId: Guid;
	label: string;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: PreprocessingDescriptionModel): DescriptionImportRdaConfigEditorModel {
		if (item) {
			this.id = item.id;
			this.label = item.label;
		}

		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string
	}): UntypedFormGroup {
		let { context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = DescriptionImportRdaConfigEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			sectionId: [{ value: this.sectionId, disabled: disabled }, context.getValidation('sectionId').validators],
			templateId: [{ value: this.templateId, disabled: disabled }, context.getValidation('templateId').validators],
			label: [{ value: this.label, disabled: disabled }, context.getValidation('label').validators],
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'id', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}id`)] });
		baseValidationArray.push({ key: 'sectionId', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}descriptionId`)] });
		baseValidationArray.push({ key: 'templateId', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}blueprintSectionId`)] });
		baseValidationArray.push({ key: 'label', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}
