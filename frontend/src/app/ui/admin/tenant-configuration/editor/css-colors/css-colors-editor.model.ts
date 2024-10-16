import { UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { TenantConfigurationType } from "@app/core/common/enum/tenant-configuration-type";
import { CssColorsTenantConfiguration, CssColorsTenantConfigurationPersist, TenantConfiguration, TenantConfigurationPersist } from "@app/core/model/tenant-configuaration/tenant-configuration";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
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
	primaryColor2: string;
	primaryColor3: string;
	secondaryColor: string;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: CssColorsTenantConfiguration): CssColorsTenantConfigurationEditorModel {
		if (item) {
			this.primaryColor = item.primaryColor;
			this.primaryColor2 = item.primaryColor2;
			this.primaryColor3 = item.primaryColor3;
			this.secondaryColor = item.secondaryColor;
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
			context = CssColorsTenantConfigurationEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		const form: UntypedFormGroup = this.formBuilder.group({
			primaryColor: [{ value: this.primaryColor, disabled: disabled }, context.getValidation('primaryColor').validators],
			primaryColor2: [{ value: this.primaryColor2, disabled: disabled }, context.getValidation('primaryColor2').validators],
			primaryColor3: [{ value: this.primaryColor3, disabled: disabled }, context.getValidation('primaryColor3').validators],
			secondaryColor: [{ value: this.secondaryColor, disabled: disabled }, context.getValidation('secondaryColor').validators],
			primaryColorInput: [{ value: this.primaryColor, disabled: disabled}, context.getValidation('primaryColorInput').validators ],
			primaryColor2Input: [{ value: this.primaryColor2, disabled: disabled}, context.getValidation('primaryColor2Input').validators ],
			primaryColor3Input: [{ value: this.primaryColor3, disabled: disabled}, context.getValidation('primaryColor3Input').validators ],
			secondaryColorInput: [{ value: this.secondaryColor, disabled: disabled}, context.getValidation('secondaryColorInput').validators ],
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
		baseValidationArray.push({ key: 'primaryColor', validators: [validColorValidator(), Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}primaryColor`)] });
		baseValidationArray.push({ key: 'primaryColor2', validators: [validColorValidator(), Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}primaryColor2`)] });
		baseValidationArray.push({ key: 'primaryColor3', validators: [validColorValidator(), Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}primaryColor3`)] });
		baseValidationArray.push({ key: 'secondaryColor', validators: [validColorValidator(), Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}secondaryColor`)] });
		baseValidationArray.push({ key: 'primaryColorInput', validators: [validColorValidator()] });
		baseValidationArray.push({ key: 'primaryColor2Input', validators: [validColorValidator()] });
		baseValidationArray.push({ key: 'primaryColor3Input', validators: [validColorValidator()] });
		baseValidationArray.push({ key: 'secondaryColorInput', validators: [validColorValidator()] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}
