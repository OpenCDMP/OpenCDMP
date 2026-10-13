import { UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { TenantConfigurationType } from "@app/core/common/enum/tenant-configuration-type";
import { DefaultPlanBlueprintConfiguration, DefaultPlanBlueprintConfigurationPersist, TenantConfiguration, TenantConfigurationPersist } from "@app/core/model/tenant-configuaration/tenant-configuration";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";
import { Guid } from "@common/types/guid";

export class TenantConfigurationEditorModel extends BaseEditorModel implements TenantConfigurationPersist {
	type: TenantConfigurationType;
	defaultPlanBlueprint: DefaultPlanBlueprintTenantConfigurationEditorModel = new DefaultPlanBlueprintTenantConfigurationEditorModel(this.validationErrorModel);

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super(); this.type = TenantConfigurationType.DefaultPlanBlueprint; }

	public fromModel(item: TenantConfiguration): TenantConfigurationEditorModel {
		if (item) {
			super.fromModel(item);
			this.type = item.type;
			if (item.defaultPlanBlueprint) this.defaultPlanBlueprint = new DefaultPlanBlueprintTenantConfigurationEditorModel(this.validationErrorModel).fromModel(item.defaultPlanBlueprint);
		} else {
			this.type = TenantConfigurationType.DefaultPlanBlueprint;
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			type: [{ value: this.type, disabled: disabled }, context.getValidation('type').validators],
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators],
			defaultPlanBlueprint: this.defaultPlanBlueprint.buildForm({
				rootPath: `defaultPlanBlueprint.`,
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
}

export class DefaultPlanBlueprintTenantConfigurationEditorModel implements DefaultPlanBlueprintConfigurationPersist {
	groupId: Guid;
	
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: DefaultPlanBlueprintConfiguration): DefaultPlanBlueprintTenantConfigurationEditorModel {
		if (item) {
			this.groupId = item.groupId
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
			context = DefaultPlanBlueprintTenantConfigurationEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			groupId: [{ value: this.groupId, disabled: disabled }, context.getValidation('groupId').validators]
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

		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}

