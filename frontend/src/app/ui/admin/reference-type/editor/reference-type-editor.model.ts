import { FormArray, FormControl, FormGroup, UntypedFormArray, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { ReferenceFieldDataType } from "@app/core/common/enum/reference-field-data-type";
import { ReferenceType, ReferenceTypeDefinition, ReferenceTypeDefinitionPersist, ReferenceTypeField, ReferenceTypeFieldPersist, ReferenceTypePersist } from "@app/core/model/reference-type/reference-type";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";
import { ExternalFetcherBaseSourceConfigurationEditorModel, QueryCaseConfigEditorModel, QueryConfigEditorModel, ResultFieldsMappingConfigurationEditorModel, StaticOptionEditorModel } from "@app/ui/external-fetcher/external-fetcher-source-editor.model";

export class ReferenceTypeEditorModel extends BaseEditorModel implements ReferenceTypePersist {
	name: string;
	code: string;
	definition: ReferenceTypeDefinitionEditorModel = new ReferenceTypeDefinitionEditorModel();

	permissions: string[];

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super(); }

	public fromModel(item: ReferenceType): ReferenceTypeEditorModel {
		if (item) {
			super.fromModel(item);
			this.id = item.id;
			this.name = item.name;
			this.code = item.code;
			this.definition = new ReferenceTypeDefinitionEditorModel(this.validationErrorModel).fromModel(item.definition);
			this.hash = item.hash;
			if (item.createdAt) { this.createdAt = item.createdAt; }
			if (item.updatedAt) { this.updatedAt = item.updatedAt; }
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			name: [{ value: this.name, disabled: disabled }, context.getValidation('name').validators],
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
		baseValidationArray.push({ key: 'name', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'name')] });
		baseValidationArray.push({ key: 'code', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'code')] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	createChildField(index: number): UntypedFormGroup {
		const field: ReferenceTypeFieldEditorModel = new ReferenceTypeFieldEditorModel(this.validationErrorModel);
		return field.buildForm({ rootPath: 'definition.fields[' + index + '].' });
	}

	createChildSource(index: number): UntypedFormGroup {
		const source: ExternalFetcherBaseSourceConfigurationEditorModel = new ExternalFetcherBaseSourceConfigurationEditorModel(this.validationErrorModel);
		source.ordinal = index + 1;
        return source.buildForm({ rootPath: 'definition.sources[' + index + '].' });
	}

	createFieldsMapping(sourceIndex: number, index: number): UntypedFormGroup {
		const fieldMapping: ResultFieldsMappingConfigurationEditorModel = new ResultFieldsMappingConfigurationEditorModel(this.validationErrorModel);
		return fieldMapping.buildForm({ rootPath: 'definition.sources[' + sourceIndex + '].results.fieldsMapping[' + index + '].'});
	}

	static reApplyDefinitionFieldsValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
	}): void {

		const { formGroup, validationErrorModel } = params;
		const control = formGroup?.get('definition');
		ReferenceTypeDefinitionEditorModel.reapplyFieldsValidators({
			formArray: control.get('fields') as UntypedFormArray,
			rootPath: `definition.`,
			validationErrorModel: validationErrorModel
		});
	}

	static reApplyDefinitionSourcesValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
	}): void {

		const { formGroup, validationErrorModel } = params;
		const control = formGroup?.get('definition');
		ReferenceTypeDefinitionEditorModel.reapplySourcesValidators({
			formArray: control.get('sources') as UntypedFormArray,
			rootPath: `definition.`,
			validationErrorModel: validationErrorModel
		});
	}
}

export class ReferenceTypeDefinitionEditorModel implements ReferenceTypeDefinitionPersist {
	fields: ReferenceTypeFieldEditorModel[] = [];
	sources: ExternalFetcherBaseSourceConfigurationEditorModel[] = [];

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: ReferenceTypeDefinition): ReferenceTypeDefinitionEditorModel {
		if (item) {
			if (item.fields) { item.fields.map(x => this.fields.push(new ReferenceTypeFieldEditorModel(this.validationErrorModel).fromModel(x))); }
			if (item.sources) { item.sources.map(x => this.sources.push(new ExternalFetcherBaseSourceConfigurationEditorModel(this.validationErrorModel).fromModel(x))); }
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
			context = ReferenceTypeDefinitionEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			fields: this.formBuilder.array(
				(this.fields ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}fields[${index}].`,
						disabled: disabled
					})
				), context.getValidation('fields').validators
			),
			sources: this.formBuilder.array(
				(this.sources ?? []).map(
					(item, index) =>item.buildForm({
						rootPath: `${rootPath}sources[${index}].`,
						disabled: disabled
					})
				), context.getValidation('sources').validators
			)
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'fields', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}fields`)] });
		baseValidationArray.push({ key: 'sources', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}sources`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyFieldsValidators(params: {
		formArray: UntypedFormArray,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {
		const { validationErrorModel, rootPath, formArray } = params;
		formArray?.controls?.forEach(
			(control, index) => ReferenceTypeFieldEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}fields[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);
	}

	static reapplySourcesValidators(params: {
		formArray: UntypedFormArray,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {
		const { validationErrorModel, rootPath, formArray } = params;
		formArray?.controls?.forEach(
			(control, index) => ExternalFetcherBaseSourceConfigurationEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}sources[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);
	}

}

export class ReferenceTypeFieldEditorModel implements ReferenceTypeFieldPersist {
	code: string;
	label: string;
	description: string;
	dataType: ReferenceFieldDataType;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: ReferenceTypeField): ReferenceTypeFieldEditorModel {
		if (item) {
			this.code = item.code;
			this.label = item.label;
			this.description = item.description;
			this.dataType = item.dataType;
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
			context = ReferenceTypeFieldEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			code: [{ value: this.code, disabled: disabled }, context.getValidation('code').validators],
			label: [{ value: this.label, disabled: disabled }, context.getValidation('label').validators],
			description: [{ value: this.description, disabled: disabled }, context.getValidation('description').validators],
			dataType: [{ value: this.dataType, disabled: disabled }, context.getValidation('dataType').validators],
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
		baseValidationArray.push({ key: 'label', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}label`)] });
		baseValidationArray.push({ key: 'description', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}description`)] });
		baseValidationArray.push({ key: 'dataType', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}dataType`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = ReferenceTypeFieldEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['code', 'label', 'description', 'dataType'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}

export interface ReferenceTypeFieldFormGroup{
    code: FormControl<string>;
	label: FormControl<string>;
	description: FormControl<string>;
	dataType: FormControl<ReferenceFieldDataType>;
}