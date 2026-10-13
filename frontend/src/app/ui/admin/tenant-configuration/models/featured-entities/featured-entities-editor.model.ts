import { UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { TenantConfigurationType } from "@app/core/common/enum/tenant-configuration-type";
import { DescriptionTemplate } from "@app/core/model/description-template/description-template";
import { PlanBlueprint } from "@app/core/model/plan-blueprint/plan-blueprint";
import { DescriptionTemplatePersist, FeaturedEntities, FeaturedEntitiesPersist, PlanBlueprintPersist, TenantConfiguration, TenantConfigurationPersist } from "@app/core/model/tenant-configuaration/tenant-configuration";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";
import { Guid } from "@common/types/guid";

export class TenantConfigurationEditorModel extends BaseEditorModel implements TenantConfigurationPersist {
	type: TenantConfigurationType;
	featuredEntities: FeaturedEntitiesTenantConfigurationEditorModel = new FeaturedEntitiesTenantConfigurationEditorModel(this.validationErrorModel);

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super(); this.type = TenantConfigurationType.FeaturedEntities; }

	public fromModel(item: TenantConfiguration): TenantConfigurationEditorModel {
		if (item) {
			super.fromModel(item);
			this.type = item.type;
			if (item.featuredEntities) this.featuredEntities = new FeaturedEntitiesTenantConfigurationEditorModel(this.validationErrorModel).fromModel(item.featuredEntities);
		} else {
			this.type = TenantConfigurationType.FeaturedEntities;
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			type: [{ value: this.type, disabled: disabled }, context.getValidation('type').validators],
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators],
			featuredEntities: this.featuredEntities.buildForm({
				rootPath: `featuredEntities.`,
			}),
		});
	}

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(this.validationErrorModel, 'id')] });
		baseValidationArray.push({ key: 'type', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'type')] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	createPlanBlueprint(index: number, groupId: Guid): UntypedFormGroup {
		const planBlueprint: FeaturedPlanBlueprintEditorModel = new FeaturedPlanBlueprintEditorModel(this.validationErrorModel);
		planBlueprint.groupId = groupId;
		planBlueprint.ordinal = index;
		return planBlueprint.buildForm({ rootPath: 'featuredEntities.planBlueprints[' + index + '].' });
	}

	createDescriptionTemplate(index: number, groupId: Guid): UntypedFormGroup {
		const descriptionTemplate: FeaturedDescriptionTemplateEditorModel = new FeaturedDescriptionTemplateEditorModel(this.validationErrorModel);
		descriptionTemplate.groupId = groupId;
		descriptionTemplate.ordinal = index;
		return descriptionTemplate.buildForm({ rootPath: 'featuredEntities.descriptionTemplate[' + index + '].' });
	}
}

export class FeaturedEntitiesTenantConfigurationEditorModel implements FeaturedEntitiesPersist {
	planBlueprints: FeaturedPlanBlueprintEditorModel[] = [];
	descriptionTemplates: FeaturedDescriptionTemplateEditorModel[] = [];
	
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: FeaturedEntities): FeaturedEntitiesTenantConfigurationEditorModel {
		if (item) {
			if (item.planBlueprints) { item.planBlueprints.map(x => this.planBlueprints.push(new FeaturedPlanBlueprintEditorModel(this.validationErrorModel).fromModel(x))); }
			if (item.descriptionTemplates) { item.descriptionTemplates.map(x => this.descriptionTemplates.push(new FeaturedDescriptionTemplateEditorModel(this.validationErrorModel).fromModel(x))); }
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
			context = FeaturedEntitiesTenantConfigurationEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			planBlueprints: this.formBuilder.array(
				(this.planBlueprints ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}planBlueprints[${index}].`,
						disabled: disabled
					})
				), context.getValidation('planBlueprints').validators
			),
			descriptionTemplates: this.formBuilder.array(
				(this.descriptionTemplates ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}descriptionTemplates[${index}].`,
						disabled: disabled
					})
				), context.getValidation('descriptionTemplates').validators
			),
		});
	}


	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'planBlueprints', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}planBlueprints`)] });
		baseValidationArray.push({ key: 'descriptionTemplates', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}descriptionTemplates`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}

export class FeaturedPlanBlueprintEditorModel implements PlanBlueprintPersist {
	groupId: Guid;
	ordinal: number;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: PlanBlueprint): FeaturedPlanBlueprintEditorModel {
		if (item) {
			this.groupId = item.groupId;
			this.ordinal = item.ordinal;
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
			context = FeaturedPlanBlueprintEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			groupId: [{ value: this.groupId, disabled: disabled }, context.getValidation('groupId').validators],
			ordinal: [{ value: this.ordinal, disabled: disabled }, context.getValidation('ordinal').validators],
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();

		baseValidationArray.push({ key: 'groupId', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}groupId`)] });
		baseValidationArray.push({ key: 'ordinal', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}ordinal`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = FeaturedPlanBlueprintEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['groupId', 'ordinal'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}

export class FeaturedDescriptionTemplateEditorModel implements DescriptionTemplatePersist {
	groupId: Guid;
	ordinal: number;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: DescriptionTemplate): FeaturedDescriptionTemplateEditorModel {
		if (item) {
			this.groupId = item.groupId;
			this.ordinal = item.ordinal;
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
			context = FeaturedDescriptionTemplateEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			groupId: [{ value: this.groupId, disabled: disabled }, context.getValidation('groupId').validators],
			ordinal: [{ value: this.ordinal, disabled: disabled }, context.getValidation('ordinal').validators],
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();

		baseValidationArray.push({ key: 'groupId', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}groupId`)] });
		baseValidationArray.push({ key: 'ordinal', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}ordinal`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = FeaturedDescriptionTemplateEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['groupId', 'ordinal'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}
