import { UntypedFormArray, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { ReferenceFieldDataType } from "@app/core/common/enum/reference-field-data-type";
import { ReferenceSourceType } from "@app/core/common/enum/reference-source-type";
import { Definition, DefinitionPersist, Field, FieldPersist, Reference, ReferencePersist } from "@app/core/model/reference/reference";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";
import { Guid } from "@common/types/guid";

export class ReferenceEditorModel extends BaseEditorModel implements ReferencePersist {
	label: string;
	typeId: Guid;
	description: string;
	definition: DefinitionEditorModel = new DefinitionEditorModel();
	reference: string;
	abbreviation: string;
	source: string;
	sourceType: ReferenceSourceType;

	permissions: string[];

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super(); }

	public fromModel(item: Reference): ReferenceEditorModel {
		if (item) {
			super.fromModel(item);
			this.label = item.label;
			this.typeId = item.type?.id;
			this.description = item.description;
			if (item.definition) this.definition = new DefinitionEditorModel(this.validationErrorModel).fromModel(item.definition);
			this.reference = item.reference;
			this.abbreviation = item.abbreviation;
			this.source = item.source;
			this.sourceType = item.sourceType;
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			label: [{ value: this.label, disabled: disabled }, context.getValidation('label').validators],
			typeId: [{ value: this.typeId, disabled: disabled }, context.getValidation('typeId').validators],
			description: [{ value: this.description, disabled: disabled }, context.getValidation('description').validators],
			definition: this.definition.buildForm({
				rootPath: `definition.`,
				disabled: disabled
			}),
			reference: [{ value: this.reference, disabled: disabled }, context.getValidation('reference').validators],
			abbreviation: [{ value: this.abbreviation, disabled: disabled }, context.getValidation('abbreviation').validators],
			source: [{ value: this.source, disabled: disabled }, context.getValidation('source').validators],
			sourceType: [{ value: this.sourceType, disabled: disabled }, context.getValidation('sourceType').validators],

			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators]
		});
	}

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(this.validationErrorModel, 'id')] });
		baseValidationArray.push({ key: 'label', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'label')] });
		baseValidationArray.push({ key: 'typeId', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'typeId')] });
		baseValidationArray.push({ key: 'description', validators: [BackendErrorValidator(this.validationErrorModel, 'description')] });
		baseValidationArray.push({ key: 'reference', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'reference')] });
		baseValidationArray.push({ key: 'abbreviation', validators: [BackendErrorValidator(this.validationErrorModel, 'abbreviation')] });
		baseValidationArray.push({ key: 'source', validators: [BackendErrorValidator(this.validationErrorModel, 'source')] });
		baseValidationArray.push({ key: 'sourceType', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'sourceType')] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	createChildField(index: number): UntypedFormGroup {
		const field: FieldEditorModel = new FieldEditorModel(this.validationErrorModel);
		return field.buildForm({ rootPath: 'definition.fields[' + index + '].' });
	}

	static reApplyDefinitionFieldsValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
	}): void {

		const { formGroup, validationErrorModel } = params;
		const control = formGroup?.get('definition');
		DefinitionEditorModel.reapplyFieldsValidators({
			formArray: control.get('fields') as UntypedFormArray,
			rootPath: `definition.`,
			validationErrorModel: validationErrorModel
		});
	}
}

export class DefinitionEditorModel implements DefinitionPersist {
	fields: FieldEditorModel[] = [];

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: Definition): DefinitionEditorModel {
		if (item) {
			if (item.fields) { item.fields.map(x => this.fields.push(new FieldEditorModel(this.validationErrorModel).fromModel(x))); }
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
			context = DefinitionEditorModel.createValidationContext({
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
			(control, index) => FieldEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}fields[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);
	}
}

export class FieldEditorModel implements FieldPersist {
	code: string;
	dataType: ReferenceFieldDataType;
	value: string;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: Field): FieldEditorModel {
		if (item) {
			this.code = item.code;
			this.dataType = item.dataType;
			this.value = item.value;
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
			context = FieldEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			code: [{ value: this.code, disabled: disabled }, context.getValidation('code').validators],
			dataType: [{ value: this.dataType, disabled: disabled }, context.getValidation('dataType').validators],
			value: [{ value: this.value, disabled: disabled }, context.getValidation('value').validators],
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
		baseValidationArray.push({ key: 'dataType', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}dataType`)] });
		baseValidationArray.push({ key: 'value', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}value`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = FieldEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['code', 'dataType', 'value'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}

