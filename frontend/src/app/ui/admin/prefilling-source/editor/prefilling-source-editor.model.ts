import { UntypedFormArray, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { PrefillingSource, PrefillingSourceDefinition, PrefillingSourceDefinitionField, PrefillingSourceDefinitionFieldPersist, PrefillingSourceDefinitionFixedValueField, PrefillingSourceDefinitionFixedValueFieldPersist, PrefillingSourceDefinitionPersist, PrefillingSourcePersist } from "@app/core/model/prefilling-source/prefilling-source";
import { ExternalFetcherBaseSourceConfigurationEditorModel, QueryCaseConfigEditorModel, QueryConfigEditorModel } from "@app/ui/external-fetcher/external-fetcher-source-editor.model";

import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";

export class PrefillingSourceEditorModel extends BaseEditorModel implements PrefillingSourcePersist {
	label: string;
	code: string;
	definition: PrefillingSourceDefinitionEditorModel = new PrefillingSourceDefinitionEditorModel(this.validationErrorModel);
	permissions: string[];

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super(); }

	public fromModel(item: PrefillingSource): PrefillingSourceEditorModel {
		if (item) {
			super.fromModel(item);
			this.label = item.label;
			this.code = item.code;
			if (item.definition) this.definition = new PrefillingSourceDefinitionEditorModel(this.validationErrorModel).fromModel(item.definition);
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			label: [{ value: this.label, disabled: disabled }, context.getValidation('label').validators],
			code: [{ value: this.code, disabled: disabled }, context.getValidation('code').validators],
			definition: this.definition.buildForm({
				rootPath: `definition.`,
				disabled: disabled
			}),
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators]
		});
	}

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(this.validationErrorModel, 'id')] });
		baseValidationArray.push({ key: 'label', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'label')] });
		baseValidationArray.push({ key: 'code', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'code')] });
		baseValidationArray.push({ key: 'definition', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'definition')] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reApplyDefinitionValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
	}): void {

		const { formGroup, validationErrorModel } = params;

		PrefillingSourceDefinitionEditorModel.reapplyValidators({
			formGroup: formGroup?.get('definition') as UntypedFormGroup,
			rootPath: `definition.`,
			validationErrorModel: validationErrorModel
		});
	}

	createChildField(index: number): UntypedFormGroup {
		const field: PrefillingSourceDefinitionFieldEditorModel = new PrefillingSourceDefinitionFieldEditorModel(this.validationErrorModel);
		return field.buildForm({ rootPath: 'definition.fields[' + index + '].' });
	}

	createChildFixedValueField(index: number): UntypedFormGroup {
		const field: PrefillingSourceDefinitionFixedValueFieldEditorModel = new PrefillingSourceDefinitionFixedValueFieldEditorModel(this.validationErrorModel);
		return field.buildForm({ rootPath: 'definition.fixedValueFields[' + index + '].' });
	}
}

export class PrefillingSourceDefinitionEditorModel implements PrefillingSourceDefinitionPersist {
	fields: PrefillingSourceDefinitionFieldEditorModel[] = [];
	fixedValueFields: PrefillingSourceDefinitionFixedValueFieldEditorModel[] = [];
	searchConfiguration: ExternalFetcherBaseSourceConfigurationEditorModel = new ExternalFetcherBaseSourceConfigurationEditorModel(this.validationErrorModel);
	getConfiguration: ExternalFetcherBaseSourceConfigurationEditorModel;
	getEnabled = false;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: PrefillingSourceDefinition): PrefillingSourceDefinitionEditorModel {
		if (item) {
			if (item.fields) { item.fields.map(x => this.fields.push(new PrefillingSourceDefinitionFieldEditorModel(this.validationErrorModel).fromModel(x))); }
			if (item.fixedValueFields) { item.fixedValueFields.map(x => this.fixedValueFields.push(new PrefillingSourceDefinitionFixedValueFieldEditorModel(this.validationErrorModel).fromModel(x))); }
			if (item.searchConfiguration) this.searchConfiguration = new ExternalFetcherBaseSourceConfigurationEditorModel(this.validationErrorModel).fromModel(item.searchConfiguration);
			if (item.getConfiguration) {
				this.getConfiguration = new ExternalFetcherBaseSourceConfigurationEditorModel(this.validationErrorModel).fromModel(item.getConfiguration);
				this.getEnabled = true;
			}
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
			context = PrefillingSourceDefinitionEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		const form: UntypedFormGroup = this.formBuilder.group({
			fields: this.formBuilder.array(
				(this.fields ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}fields[${index}].`,
						disabled: disabled
					})
				), context.getValidation('fields')
			),
			fixedValueFields: this.formBuilder.array(
				(this.fixedValueFields ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}fixedValueFields[${index}].`,
						disabled: disabled
					})
				), context.getValidation('fixedValueFields')
			),
			searchConfiguration: this.searchConfiguration.buildForm({
				rootPath: `${rootPath}searchConfiguration.`,
				disabled: disabled
			}),
			getEnabled: [{ value: this.getEnabled, disabled: disabled }, context.getValidation('getEnabled').validators],
		});

		if (this.getEnabled == true) {
			form.addControl('getConfiguration',  this.getConfiguration.buildForm({
				rootPath: `${rootPath}getConfiguration.`,
				disabled: disabled
			}));
		}
		return form;
	}

	buildGetConfiguration(
		form: UntypedFormGroup,
		rootPath?: string
	){
		form.addControl('getConfiguration', new ExternalFetcherBaseSourceConfigurationEditorModel(this.validationErrorModel)
			.buildForm({
			rootPath: `${rootPath}getConfiguration.`
		}));

	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'fields', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}fields`)] });
		baseValidationArray.push({ key: 'fixedValueFields', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}fixedValueFields`)] });
		baseValidationArray.push({ key: 'searchConfiguration', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}searchConfiguration`)] });
		baseValidationArray.push({ key: 'getConfiguration', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}getConfiguration`)] });
		baseValidationArray.push({ key: 'getEnabled', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}getConfiguration`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {
		const { validationErrorModel, rootPath, formGroup } = params;
		(formGroup.get('fields') as UntypedFormArray).controls?.forEach(
			(control, index) => PrefillingSourceDefinitionFieldEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}fields[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);

		(formGroup.get('fixedValueFields') as UntypedFormArray).controls?.forEach(
			(control, index) => PrefillingSourceDefinitionFixedValueFieldEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}fixedValueFields[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);

		ExternalFetcherBaseSourceConfigurationEditorModel.reapplyValidators({
			formGroup: formGroup.get('searchConfiguration') as UntypedFormGroup,
			rootPath: `${rootPath}searchConfiguration.`,
			validationErrorModel: validationErrorModel
		});

		if(formGroup.get('getEnabled').value == true){
			ExternalFetcherBaseSourceConfigurationEditorModel.reapplyValidators({
				formGroup: formGroup.get('getConfiguration') as UntypedFormGroup,
				rootPath: `${rootPath}getConfiguration.`,
				validationErrorModel: validationErrorModel
			});
		}
	}
}

export class PrefillingSourceDefinitionFieldEditorModel implements PrefillingSourceDefinitionFieldPersist {
	code: string;
	systemFieldTarget: string;
	semanticTarget: string;
	trimRegex: string;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: PrefillingSourceDefinitionField): PrefillingSourceDefinitionFieldEditorModel {
		if (item) {
			this.code = item.code;
			this.systemFieldTarget = item.systemFieldTarget;
			this.semanticTarget = item.semanticTarget;
			this.trimRegex = item.trimRegex;
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
			context = PrefillingSourceDefinitionFieldEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			code: [{ value: this.code, disabled: disabled }, context.getValidation('code').validators],
			systemFieldTarget: [{ value: this.systemFieldTarget, disabled: disabled }, context.getValidation('systemFieldTarget').validators],
			semanticTarget: [{ value: this.semanticTarget, disabled: disabled }, context.getValidation('semanticTarget').validators],
			trimRegex: [{ value: this.trimRegex, disabled: disabled }, context.getValidation('trimRegex').validators],
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'code', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}code`)] });
		baseValidationArray.push({ key: 'systemFieldTarget', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}systemFieldTarget`)] });
		baseValidationArray.push({ key: 'semanticTarget', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}semanticTarget`)] });
		baseValidationArray.push({ key: 'trimRegex', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}trimRegex`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = PrefillingSourceDefinitionFieldEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['code', 'systemFieldTarget', 'semanticTarget', 'trimRegex'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}

export class PrefillingSourceDefinitionFixedValueFieldEditorModel implements PrefillingSourceDefinitionFixedValueFieldPersist {
	systemFieldTarget: string;
	semanticTarget: string;
	trimRegex: string;
	fixedValue: string;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: PrefillingSourceDefinitionFixedValueField): PrefillingSourceDefinitionFixedValueFieldEditorModel {
		if (item) {
			this.systemFieldTarget = item.systemFieldTarget;
			this.semanticTarget = item.semanticTarget;
			this.trimRegex = item.trimRegex;
			this.fixedValue = item.fixedValue;
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
			context = PrefillingSourceDefinitionFixedValueFieldEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			systemFieldTarget: [{ value: this.systemFieldTarget, disabled: disabled }, context.getValidation('systemFieldTarget').validators],
			semanticTarget: [{ value: this.semanticTarget, disabled: disabled }, context.getValidation('semanticTarget').validators],
			trimRegex: [{ value: this.trimRegex, disabled: disabled }, context.getValidation('trimRegex').validators],
			fixedValue: [{ value: this.fixedValue, disabled: disabled }, context.getValidation('fixedValue').validators],
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'systemFieldTarget', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}systemFieldTarget`)] });
		baseValidationArray.push({ key: 'semanticTarget', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}semanticTarget`)] });
		baseValidationArray.push({ key: 'trimRegex', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}trimRegex`)] });
		baseValidationArray.push({ key: 'fixedValue', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}fixedValue`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = PrefillingSourceDefinitionFixedValueFieldEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['systemFieldTarget', 'semanticTarget', 'trimRegex', 'fixedValue'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}
