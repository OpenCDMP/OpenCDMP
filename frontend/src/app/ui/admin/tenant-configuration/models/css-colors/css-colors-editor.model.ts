import { FormControl, FormGroup, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { TenantConfigurationType } from "@app/core/common/enum/tenant-configuration-type";
import { CssColorsTenantConfiguration, CssColorsTenantConfigurationPersist, TenantConfiguration, TenantConfigurationPersist } from "@app/core/model/tenant-configuaration/tenant-configuration";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator, JsonValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";
import { validColorValidator } from "ngx-colors";

export class TenantConfigurationEditorModel extends BaseEditorModel implements TenantConfigurationPersist {
	type: TenantConfigurationType;
	cssColors: CssColorsTenantConfigurationEditorModel = new CssColorsTenantConfigurationEditorModel(this.validationErrorModel);

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super(); this.type = TenantConfigurationType.CssColors; }

	public fromModel(item: TenantConfiguration): TenantConfigurationEditorModel {
		if (item) {
			super.fromModel(item);
			this.type = item.type;
			if (item.cssColors) this.cssColors = new CssColorsTenantConfigurationEditorModel(this.validationErrorModel).fromModel(item.cssColors);
		} else {
			this.type = TenantConfigurationType.CssColors;
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			type: [{ value: this.type, disabled: disabled }, context.getValidation('type').validators],
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators],
			cssColors: this.cssColors.buildForm({
				rootPath: `cssColors.`,
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

export class CssColorsTenantConfigurationEditorModel implements CssColorsTenantConfigurationPersist {
	primaryColor: string;
    cssOverride: string;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: CssColorsTenantConfiguration): CssColorsTenantConfigurationEditorModel {
		if (item) {
			this.primaryColor = item.primaryColor;
			this.cssOverride = item.cssOverride
		}
		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string
	}): FormGroup<CssColorForm> {
		let { context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = CssColorsTenantConfigurationEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		const form: UntypedFormGroup = this.formBuilder.group({
			primaryColor: [{ value: this.primaryColor, disabled: disabled }, context.getValidation('primaryColor').validators],
			primaryColorInput: [{ value: this.primaryColor, disabled: disabled}, context.getValidation('primaryColorInput').validators ],
            cssOverride: [{value: this.cssOverride, disabled: disabled},  context.getValidation('cssOverride').validators]
		},
		{ updateOn: "change" });

		return form;
	}


	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'primaryColor', validators: [validColorValidator(),BackendErrorValidator(validationErrorModel, `${rootPath}primaryColor`)] });
		baseValidationArray.push({ key: 'primaryColorInput', validators: [validColorValidator()] });
		baseValidationArray.push({ key: 'cssOverride', validators: [JsonValidator(), BackendErrorValidator(validationErrorModel, `${rootPath}cssOverride`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}

export interface CssColorForm {
    primaryColor: FormControl<string>;
    primaryColorInput: FormControl<string>;
    cssOverride: FormControl<string>;
}