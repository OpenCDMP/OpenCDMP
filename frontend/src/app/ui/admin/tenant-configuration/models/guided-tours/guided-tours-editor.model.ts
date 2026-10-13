import { FormControl, FormGroup, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { TenantConfigurationType } from "@app/core/common/enum/tenant-configuration-type";
import { GuidedToursTenantConfiguration, GuidedToursTenantConfigurationPersist, TenantConfiguration, TenantConfigurationPersist } from "@app/core/model/tenant-configuaration/tenant-configuration";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";

export class TenantConfigurationEditorModel extends BaseEditorModel implements TenantConfigurationPersist {
	type: TenantConfigurationType;
	guidedTours: GuidedToursTenantConfigurationEditorModel = new GuidedToursTenantConfigurationEditorModel(this.validationErrorModel);

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super(); this.type = TenantConfigurationType.GuidedTours; }

	public fromModel(item: TenantConfiguration): TenantConfigurationEditorModel {
		if (item) {
			super.fromModel(item);
			this.type = item.type;
			if (item.guidedTours) this.guidedTours = new GuidedToursTenantConfigurationEditorModel(this.validationErrorModel).fromModel(item.guidedTours);
		} else {
			this.type = TenantConfigurationType.GuidedTours;
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			type: [{ value: this.type, disabled: disabled }, context.getValidation('type').validators],
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators],
			guidedTours: this.guidedTours.buildForm({ rootPath: 'guidedTours.', disabled }),
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

export class GuidedToursTenantConfigurationEditorModel implements GuidedToursTenantConfigurationPersist {
	/** No stored configuration means the tours are on, so a fresh editor starts from "enabled". */
	enabled: boolean = true;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()) { }

	public fromModel(item: GuidedToursTenantConfiguration): GuidedToursTenantConfigurationEditorModel {
		if (item) {
			this.enabled = item.enabled ?? true;
		}
		return this;
	}

	buildForm(params?: { context?: ValidationContext; disabled?: boolean; rootPath?: string; }): FormGroup<GuidedToursForm> {
		const { disabled = false, rootPath } = params ?? {};
		const context = params?.context ?? GuidedToursTenantConfigurationEditorModel.createValidationContext({ validationErrorModel: this.validationErrorModel, rootPath });

		return this.formBuilder.group({
			enabled: [{ value: this.enabled, disabled: disabled }, context.getValidation('enabled').validators],
		}) as FormGroup<GuidedToursForm>;
	}

	static createValidationContext(params: { rootPath?: string; validationErrorModel: ValidationErrorModel; }): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'enabled', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}enabled`)] });
		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}

export interface GuidedToursForm {
	enabled: FormControl<boolean>;
}
