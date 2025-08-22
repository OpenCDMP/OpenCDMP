import { FormArray, FormControl, FormGroup, UntypedFormArray, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { PluginDataType } from "@app/core/common/enum/plugin-data-type";
import { PluginType } from "@app/core/common/enum/plugin-type";
import { PluginConfiguration, PluginConfigurationField, PluginConfigurationFieldPersist, PluginConfigurationPersist, PluginRepositoryConfiguration } from "@app/core/model/plugin-configuration/plugin-configuration";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";
import { Guid } from "@common/types/guid";

export interface PluginConfigurationForm extends FormGroup{
    pluginCode: FormControl<string>;
	pluginType: FormControl<PluginType>;
	fields: FormArray<PluginConfigurationFieldForm>
}
export class PluginConfigurationEditorModel implements PluginConfigurationPersist {
	pluginCode: string;
	pluginType: PluginType;
	fields: PluginConfigurationFieldEditorModel[] = [];

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: PluginConfiguration, configuration?: PluginRepositoryConfiguration[]): PluginConfigurationEditorModel {
		if (item) {
			this.pluginCode = item.pluginCode;
			this.pluginType = item.pluginType;
			if (item.fields) { item.fields.map(x => this.fields.push(new PluginConfigurationFieldEditorModel(this.validationErrorModel).fromModel(x, configuration?.find( x=> x.repositoryId === this.pluginCode && x.fields?.length > 0)))); }
		}
		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string
	}): UntypedFormGroup {
		let {context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = PluginConfigurationEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			pluginCode: [{ value: this.pluginCode, disabled: disabled }, context.getValidation('pluginCode').validators],
			pluginType: [{ value: this.pluginType, disabled: disabled }, context.getValidation('pluginType').validators],
			fields: this.formBuilder.array(
				(this.fields ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}fields[${index}].`,
						disabled: disabled
					})
				), context.getValidation('fields').validators
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
		baseValidationArray.push({ key: 'pluginCode', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}pluginCode`)] });
		baseValidationArray.push({ key: 'pluginType', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}pluginCode`)] });
		baseValidationArray.push({ key: 'fields', validators: [ BackendErrorValidator(validationErrorModel, `${rootPath}fields`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {
		const { formGroup, validationErrorModel, rootPath } = params;
		const context = PluginConfigurationEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		(formGroup.get('fields') as UntypedFormArray).controls?.forEach(
			(control, index) => PluginConfigurationFieldEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}fields[${index}].`,
				validationErrorModel: validationErrorModel
			}
			)
		);
		['pluginCode', 'pluginType'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});
	}

}

export interface PluginConfigurationFieldForm extends FormGroup {
    code: FormControl<string>,
	fileValue: FormControl<Guid>,
	textValue: FormControl<string>,
	type: FormControl<PluginDataType>
}
export class PluginConfigurationFieldEditorModel implements PluginConfigurationFieldPersist {
	code: string;
	fileValue: Guid;
	textValue: string;
	type: PluginDataType = PluginDataType.String;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: PluginConfigurationField, configuration: PluginRepositoryConfiguration): PluginConfigurationFieldEditorModel {
		if (item) {
			this.code = item.code;
			if (item.fileValue) this.fileValue = item.fileValue.id;
			this.textValue = item.textValue;
		}
		if (configuration) {
			this.type = configuration.fields?.find(x => x.code === this.code)?.type ?? PluginDataType.String;
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
			context = PluginConfigurationFieldEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		const form: UntypedFormGroup = this.formBuilder.group({
			code: [{ value: this.code, disabled: disabled }, context.getValidation('code').validators],
			fileValue: [{ value: this.fileValue, disabled: disabled }, context.getValidation('fileValue').validators],
			textValue: [{ value: this.textValue, disabled: disabled }, context.getValidation('textValue').validators],
			type: [{ value: this.type, disabled: disabled }, context.getValidation('type').validators],
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
		baseValidationArray.push({ key: 'code', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}code`)] });
		baseValidationArray.push({ key: 'fileValue', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}fileValue`)] });
		baseValidationArray.push({ key: 'textValue', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}textValue`)] });
		baseValidationArray.push({ key: 'type', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}type`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
			formGroup: UntypedFormGroup,
			validationErrorModel: ValidationErrorModel,
			rootPath: string
		}): void {
			const { formGroup, rootPath, validationErrorModel } = params;
			const context = PluginConfigurationFieldEditorModel.createValidationContext({
				rootPath,
				validationErrorModel
			});
	
			['code', 'fileValue', 'textValue', 'type'].forEach(keyField => {
				const control = formGroup?.get(keyField);
				control?.clearValidators();
				control?.addValidators(context.getValidation(keyField).validators);
			});
		}
}
