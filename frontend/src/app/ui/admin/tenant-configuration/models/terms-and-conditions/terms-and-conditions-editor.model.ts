import { FormControl, FormGroup, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { TenantConfigurationType } from "@app/core/common/enum/tenant-configuration-type";
import { TenantConfiguration, TenantConfigurationPersist, TermsAndConditionsTenantConfiguration, TermsAndConditionsTenantConfigurationPersist } from "@app/core/model/tenant-configuaration/tenant-configuration";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";

export class TenantConfigurationEditorModel extends BaseEditorModel implements TenantConfigurationPersist {
	type: TenantConfigurationType;
	termsAndConditions: TermsAndConditionsTenantConfigurationEditorModel = new TermsAndConditionsTenantConfigurationEditorModel(this.validationErrorModel);

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super(); this.type = TenantConfigurationType.TermsAndConditions; }

	public fromModel(item: TenantConfiguration): TenantConfigurationEditorModel {
		if (item) {
			super.fromModel(item);
			this.type = item.type;
			if (item.termsAndConditions) this.termsAndConditions = new TermsAndConditionsTenantConfigurationEditorModel(this.validationErrorModel).fromModel(item.termsAndConditions);
		} else {
			this.type = TenantConfigurationType.TermsAndConditions;
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			type: [{ value: this.type, disabled: disabled }, context.getValidation('type').validators],
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators],
			termsAndConditions: this.termsAndConditions.buildForm({ rootPath: 'termsAndConditions.' }),
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

export class TermsAndConditionsTenantConfigurationEditorModel implements TermsAndConditionsTenantConfigurationPersist {
	content: string = '';
	version: number = 1;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()) { }

	public fromModel(item: TermsAndConditionsTenantConfiguration): TermsAndConditionsTenantConfigurationEditorModel {
		if (item) {
			this.content = item.content;
			this.version = item.version ?? 1;
		}
		return this;
	}

	buildForm(params?: { context?: ValidationContext; disabled?: boolean; rootPath?: string; }): FormGroup<TermsAndConditionsForm> {
		let { context = null, disabled = false, rootPath } = params ?? {};
		if (context == null) {
			context = TermsAndConditionsTenantConfigurationEditorModel.createValidationContext({ validationErrorModel: this.validationErrorModel, rootPath });
		}

		return this.formBuilder.group({
			content: [{ value: this.content, disabled: disabled }, context.getValidation('content').validators],
			version: [{ value: this.version, disabled: disabled }, context.getValidation('version').validators],
		}) as FormGroup<TermsAndConditionsForm>;
	}

	static createValidationContext(params: { rootPath?: string; validationErrorModel: ValidationErrorModel; }): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'content', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}content`)] });
		baseValidationArray.push({ key: 'version', validators: [Validators.required, Validators.min(1), BackendErrorValidator(validationErrorModel, `${rootPath}version`)] });
		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}

export interface TermsAndConditionsForm {
	content: FormControl<string>;
	version: FormControl<number>;
}
