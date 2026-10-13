import { UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { TenantConfigurationType } from "@app/core/common/enum/tenant-configuration-type";
import { DefaultUserLocaleTenantConfiguration, DefaultUserLocaleTenantConfigurationPersist, TenantConfiguration, TenantConfigurationPersist } from "@app/core/model/tenant-configuaration/tenant-configuration";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";

export class TenantConfigurationEditorModel extends BaseEditorModel implements TenantConfigurationPersist {
	type: TenantConfigurationType;
	defaultUserLocale: DefaultUserLocaleTenantConfigurationEditorModel = new DefaultUserLocaleTenantConfigurationEditorModel(this.validationErrorModel);

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super(); this.type = TenantConfigurationType.DefaultUserLocale; }

	public fromModel(item: TenantConfiguration): TenantConfigurationEditorModel {
		if (item) {
			super.fromModel(item);
			this.type = item.type;
			if (item.defaultUserLocale) this.defaultUserLocale = new DefaultUserLocaleTenantConfigurationEditorModel(this.validationErrorModel).fromModel(item.defaultUserLocale);
		} else {
			this.type = TenantConfigurationType.DefaultUserLocale;
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			type: [{ value: this.type, disabled: disabled }, context.getValidation('type').validators],
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators],
			defaultUserLocale: this.defaultUserLocale.buildForm({
				rootPath: `defaultUserLocale.`,
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

export class DefaultUserLocaleTenantConfigurationEditorModel implements DefaultUserLocaleTenantConfigurationPersist {
	timezone: string;
	language: string;
	culture: string;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: DefaultUserLocaleTenantConfiguration): DefaultUserLocaleTenantConfigurationEditorModel {
		if (item) {
			this.timezone = item.timezone;
			this.language = item.language;
			this.culture = item.culture;
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
			context = DefaultUserLocaleTenantConfigurationEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		const form: UntypedFormGroup = this.formBuilder.group({
			timezone: [{ value: this.timezone, disabled: disabled }, context.getValidation('timezone').validators],
			language: [{ value: this.language, disabled: disabled }, context.getValidation('language').validators],
			culture: [{ value: this.culture, disabled: disabled }, context.getValidation('culture').validators],
		});

		return form;
	}


	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'timezone', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}timezone`)] });
		baseValidationArray.push({ key: 'language', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}language`)] });
		baseValidationArray.push({ key: 'culture', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}culture`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}
