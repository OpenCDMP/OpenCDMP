import { UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { TenantConfigurationType } from "@app/core/common/enum/tenant-configuration-type";
import { TenantConfiguration, TenantConfigurationPersist, ViewPreference, ViewPreferencePersist, ViewPreferencesConfiguration, ViewPreferencesConfigurationPersist } from "@app/core/model/tenant-configuaration/tenant-configuration";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";
import { Guid } from "@common/types/guid";

export class TenantConfigurationEditorModel extends BaseEditorModel implements TenantConfigurationPersist {
	type: TenantConfigurationType;
	viewPreferences: ViewPreferencesConfigurationEditorModel = new ViewPreferencesConfigurationEditorModel(this.validationErrorModel);

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super(); this.type = TenantConfigurationType.ViewPreferences; }

	public fromModel(item: TenantConfiguration): TenantConfigurationEditorModel {
		if (item) {
			super.fromModel(item);
			this.type = item.type;
			if (item.viewPreferences) this.viewPreferences = new ViewPreferencesConfigurationEditorModel(this.validationErrorModel).fromModel(item.viewPreferences);
		} else {
			this.type = TenantConfigurationType.ViewPreferences;
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			type: [{ value: this.type, disabled: disabled }, context.getValidation('type').validators],
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators],
			viewPreferences: this.viewPreferences.buildForm({
				rootPath: `viewPreferences.`,
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

export class ViewPreferencesConfigurationEditorModel implements ViewPreferencesConfigurationPersist {
	planPreferences: ViewPreferenceEditorModel[] = [];
	descriptionPreferences: ViewPreferenceEditorModel[] = [];
	
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: ViewPreferencesConfiguration): ViewPreferencesConfigurationEditorModel {
		if (item) {
			if (item.planPreferences) { item.planPreferences.map(x => this.planPreferences.push(new ViewPreferenceEditorModel(this.validationErrorModel).fromModel(x))); }
			if (item.descriptionPreferences) { item.descriptionPreferences.map(x => this.descriptionPreferences.push(new ViewPreferenceEditorModel(this.validationErrorModel).fromModel(x))); }
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
			context = ViewPreferencesConfigurationEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			planPreferences: this.formBuilder.array(
				(this.planPreferences ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}planPreferences[${index}].`,
						disabled: disabled
					})
				), context.getValidation('planPreferences').validators
			),
			descriptionPreferences: this.formBuilder.array(
				(this.descriptionPreferences ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}descriptionPreferences[${index}].`,
						disabled: disabled
					})
				), context.getValidation('descriptionPreferences').validators
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
		baseValidationArray.push({ key: 'planPreferences', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}planPreferences`)] });
		baseValidationArray.push({ key: 'descriptionPreferences', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}descriptionPreferences`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}

export class ViewPreferenceEditorModel implements ViewPreferencePersist {
	referenceTypeId: Guid;
	ordinal: number;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: ViewPreference): ViewPreferenceEditorModel {
		if (item) {
			this.referenceTypeId = item.referenceType?.id;
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
			context = ViewPreferenceEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			referenceTypeId: [{ value: this.referenceTypeId, disabled: disabled }, context.getValidation('referenceTypeId').validators],
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

		baseValidationArray.push({ key: 'referenceTypeId', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}referenceTypeId`)] });
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
		const context = ViewPreferenceEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['referenceTypeId', 'ordinal'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}
