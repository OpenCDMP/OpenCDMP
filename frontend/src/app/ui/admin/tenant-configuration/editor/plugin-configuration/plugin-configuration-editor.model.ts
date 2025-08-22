import { UntypedFormArray, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { TenantConfigurationType } from "@app/core/common/enum/tenant-configuration-type";
import { PluginConfiguration, PluginRepositoryConfiguration } from "@app/core/model/plugin-configuration/plugin-configuration";
import { PluginTenantConfiguration, PluginTenantConfigurationPersist, TenantConfiguration, TenantConfigurationPersist } from "@app/core/model/tenant-configuaration/tenant-configuration";
import { PluginConfigurationEditorModel } from "@app/ui/plugin/plugin-editor.model";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";

export class TenantConfigurationEditorModel extends BaseEditorModel implements TenantConfigurationPersist {
	type: TenantConfigurationType;
	pluginConfiguration: PluginTenantConfigurationEditorModel = new PluginTenantConfigurationEditorModel(this.validationErrorModel);

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super(); this.type = TenantConfigurationType.PluginConfiguration; }

	public fromModel(item: TenantConfiguration, configuration?: PluginRepositoryConfiguration[], availablePlugins?: PluginConfiguration[]): TenantConfigurationEditorModel {
		if (item) {
			super.fromModel(item);
			this.type = item.type;
			this.pluginConfiguration = new PluginTenantConfigurationEditorModel(this.validationErrorModel).fromModel(item.pluginConfiguration, configuration, availablePlugins);
		} else {
			this.type = TenantConfigurationType.PluginConfiguration;
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			type: [{ value: this.type, disabled: disabled }, context.getValidation('type').validators],
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators],
			pluginConfiguration: this.pluginConfiguration.buildForm({
				rootPath: `pluginConfiguration.`,
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

export class PluginTenantConfigurationEditorModel implements PluginTenantConfigurationPersist {
	pluginConfigurations: PluginConfigurationEditorModel[] = [];

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: PluginTenantConfiguration, configuration?: PluginRepositoryConfiguration[], defaultPluginConfig?: PluginConfiguration[]): PluginTenantConfigurationEditorModel {
		const pluginConfigurations = item.pluginConfigurations ?? defaultPluginConfig;
        if (pluginConfigurations) {
			pluginConfigurations.map(x => this.pluginConfigurations.push(new PluginConfigurationEditorModel(this.validationErrorModel).fromModel(x, configuration)));
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
			context = PluginTenantConfigurationEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			pluginConfigurations: this.formBuilder.array(
				(this.pluginConfigurations ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}pluginConfigurations[${index}].`,
						disabled: disabled
					})
				), context.getValidation('pluginConfigurations').validators
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
		baseValidationArray.push({ key: 'pluginConfigurations', validators: [ BackendErrorValidator(validationErrorModel, `${rootPath}pluginConfigurations`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplySectionsValidators(params: {
		formArray: UntypedFormArray,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {
		const { validationErrorModel, rootPath, formArray } = params;
		formArray?.controls?.forEach(
			(control, index) => PluginConfigurationEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}pluginConfigurations[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);
	}

}
