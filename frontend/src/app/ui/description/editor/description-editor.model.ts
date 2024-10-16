import { FormControl, UntypedFormArray, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { DescriptionStatusEnum } from "@app/core/common/enum/description-status";
import { DescriptionTemplateFieldType } from "@app/core/common/enum/description-template-field-type";
import { DescriptionTemplateFieldValidationType } from "@app/core/common/enum/description-template-field-validation-type";
import { IsActive } from "@app/core/common/enum/is-active.enum";
import { DescriptionTemplate, DescriptionTemplateField, DescriptionTemplateFieldSet, DescriptionTemplateSection } from "@app/core/model/description-template/description-template";
import { Description, DescriptionExternalIdentifier, DescriptionExternalIdentifierPersist, DescriptionField, DescriptionFieldPersist, DescriptionPersist, DescriptionPropertyDefinition, DescriptionPropertyDefinitionFieldSet, DescriptionPropertyDefinitionFieldSetItem, DescriptionPropertyDefinitionFieldSetItemPersist, DescriptionPropertyDefinitionFieldSetPersist, DescriptionPropertyDefinitionPersist, DescriptionReference, DescriptionReferencePersist } from "@app/core/model/description/description";
import { ReferencePersist } from "@app/core/model/reference/reference";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator, CustomValidators, RequiredWithVisibilityRulesValidator, UrlValidator } from '@common/forms/validation/custom-validator';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { Validation, ValidationContext } from '@common/forms/validation/validation-context';
import { Guid } from "@common/types/guid";
import { VisibilityRulesService } from "./description-form/visibility-rules/visibility-rules.service";

export class DescriptionEditorModel extends BaseEditorModel implements DescriptionPersist {
	label: string;
	planId: Guid;
	planDescriptionTemplateId: Guid;
	descriptionTemplateId: Guid;
	status: DescriptionStatusEnum;
	description: string;
	properties: DescriptionPropertyDefinitionEditorModel = new DescriptionPropertyDefinitionEditorModel(this.validationErrorModel);
	tags: string[] = [];
	permissions: string[];

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super(); }

	public fromModel(item: Description, descriptionTemplate?: DescriptionTemplate): DescriptionEditorModel {
		if (item) {
			super.fromModel(item);
			this.label = item.label;
			this.planId = item.plan?.id;
			this.planDescriptionTemplateId = item.planDescriptionTemplate?.id;
			this.descriptionTemplateId = item.descriptionTemplate?.id;
			this.status = item.status ?? DescriptionStatusEnum.Draft;
			this.description = item.description;
			this.tags = item.descriptionTags?.filter(x => x.isActive === IsActive.Active).map(x => x.tag?.label);
			this.properties = new DescriptionPropertyDefinitionEditorModel(this.validationErrorModel).fromModel(item.properties, descriptionTemplate, item.descriptionReferences);
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false, visibilityRulesService: VisibilityRulesService): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			label: [{ value: this.label, disabled: disabled }, context.getValidation('label').validators],
			planId: [{ value: this.planId, disabled: disabled }, context.getValidation('planId').validators],
			planDescriptionTemplateId: [{ value: this.planDescriptionTemplateId, disabled: disabled }, context.getValidation('planDescriptionTemplateId').validators],
			descriptionTemplateId: [{ value: this.descriptionTemplateId, disabled: disabled }, context.getValidation('descriptionTemplateId').validators],
			status: [{ value: this.status, disabled: disabled }, context.getValidation('status').validators],
			description: [{ value: this.description, disabled: disabled }, context.getValidation('description').validators],
			tags: [{ value: this.tags, disabled: disabled }, context.getValidation('tags').validators],
			properties: this.buildProperties(visibilityRulesService),
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators]
		});
	}

	buildProperties(visibilityRulesService: VisibilityRulesService) {
		return this.properties.buildForm({
			rootPath: `properties.`,
			visibilityRulesService: visibilityRulesService
		});
	}

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(this.validationErrorModel, 'id')] });
		baseValidationArray.push({ key: 'label', validators: [CustomValidators.required(), BackendErrorValidator(this.validationErrorModel, 'label')] });
		baseValidationArray.push({ key: 'planId', validators: [CustomValidators.required(), BackendErrorValidator(this.validationErrorModel, 'planId')] });
		baseValidationArray.push({ key: 'planDescriptionTemplateId', validators: [CustomValidators.required(), BackendErrorValidator(this.validationErrorModel, 'planDescriptionTemplateId')] });
		baseValidationArray.push({ key: 'descriptionTemplateId', validators: [CustomValidators.required(), BackendErrorValidator(this.validationErrorModel, 'descriptionTemplateId')] });
		baseValidationArray.push({ key: 'status', validators: [CustomValidators.required(), BackendErrorValidator(this.validationErrorModel, 'status')] });
		baseValidationArray.push({ key: 'description', validators: [BackendErrorValidator(this.validationErrorModel, 'description')] });
		baseValidationArray.push({ key: 'tags', validators: [BackendErrorValidator(this.validationErrorModel, 'tags')] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static getFieldValueControlName(fieldType: DescriptionTemplateFieldType, multipleSelect: boolean): string {
		switch (fieldType) {
			case DescriptionTemplateFieldType.FREE_TEXT:
			case DescriptionTemplateFieldType.TEXT_AREA:
			case DescriptionTemplateFieldType.UPLOAD:
			case DescriptionTemplateFieldType.RICH_TEXT_AREA:
			case DescriptionTemplateFieldType.RADIO_BOX:
				return 'textValue';
			case DescriptionTemplateFieldType.DATASET_IDENTIFIER:
			case DescriptionTemplateFieldType.VALIDATION:
				return 'externalIdentifier';
			case DescriptionTemplateFieldType.DATE_PICKER:
				return 'dateValue';
			case DescriptionTemplateFieldType.CHECK_BOX:
			case DescriptionTemplateFieldType.BOOLEAN_DECISION:
				return 'booleanValue';
			case DescriptionTemplateFieldType.INTERNAL_ENTRIES_DESCRIPTIONS:
				if (multipleSelect) return 'textListValue';
				else return 'textValue';
			case DescriptionTemplateFieldType.INTERNAL_ENTRIES_PLANS:
				if (multipleSelect) return 'textListValue';
				else return 'textValue';
			case DescriptionTemplateFieldType.REFERENCE_TYPES:
				if (multipleSelect) return 'references';
				else return 'reference';
			case DescriptionTemplateFieldType.SELECT:
				if (multipleSelect) return 'textListValue';
				else return 'textValue';
			case DescriptionTemplateFieldType.TAGS:
				return 'tags';
		}
	}
}

export class DescriptionPropertyDefinitionEditorModel implements DescriptionPropertyDefinitionPersist {
	fieldSets: Map<string, DescriptionPropertyDefinitionFieldSetEditorModel> = new Map<string, DescriptionPropertyDefinitionFieldSetEditorModel>;
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: DescriptionPropertyDefinition, descriptionTemplate: DescriptionTemplate, descriptionReferences: DescriptionReference[]): DescriptionPropertyDefinitionEditorModel {
		this.fieldSets = this.calculateProperties(item, descriptionTemplate, descriptionReferences);
		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string,
		visibilityRulesService: VisibilityRulesService
	}): UntypedFormGroup {
		let { context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = DescriptionPropertyDefinitionEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}


		const formGroup = this.formBuilder.group({});
		const fieldSetsFormGroup = this.formBuilder.group({});
		if (this.fieldSets.size > 0) {
			this.fieldSets.forEach((value, key) => fieldSetsFormGroup.addControl(key.toString(), value.buildForm({
				rootPath: `${rootPath}fieldSets[${key}].`,
				visibilityRulesService: params.visibilityRulesService
			})), context.getValidation('fieldSets'));
			formGroup.addControl('fieldSets', fieldSetsFormGroup);
		}
		return formGroup;
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'fieldSets', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}fieldSets`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	private calculateProperties(item: DescriptionPropertyDefinition, descriptionTemplate: DescriptionTemplate, descriptionReferences: DescriptionReference[]): Map<string, DescriptionPropertyDefinitionFieldSetEditorModel> {
		let result: Map<string, DescriptionPropertyDefinitionFieldSetEditorModel> = new Map<string, DescriptionPropertyDefinitionFieldSetEditorModel>();
		if (descriptionTemplate) (
			descriptionTemplate.definition.pages.forEach(definitionPage => {

				definitionPage.sections.forEach(definitionSection => {

					const sectionResult = this.calculateSectionProperties(definitionSection, item, descriptionReferences);
					if (sectionResult != null) {
						result = new Map([...result, ...sectionResult]);
					}
				})
			})
		)
		return result;
	}

	private calculateSectionProperties(definitionSection: DescriptionTemplateSection, item: DescriptionPropertyDefinition, descriptionReferences: DescriptionReference[]): Map<string, DescriptionPropertyDefinitionFieldSetEditorModel> {
		if (definitionSection == null) return null;
		let result: Map<string, DescriptionPropertyDefinitionFieldSetEditorModel> = new Map<string, DescriptionPropertyDefinitionFieldSetEditorModel>();

		definitionSection?.fieldSets?.forEach(definitionFieldSet => {
			const fieldSetResult = this.calculateFieldSetProperties(definitionFieldSet, 0, item, descriptionReferences);
			if (fieldSetResult != null) {
				result.set(definitionFieldSet.id, fieldSetResult);
			}
		});

		if (definitionSection.sections != null && definitionSection.sections?.length > 0) {
			definitionSection.sections.forEach(nestedDefinitionSection => {
				const nestedSectionResult = this.calculateSectionProperties(nestedDefinitionSection, item, descriptionReferences);
				if (nestedSectionResult != null) {
					result = new Map([...result, ...nestedSectionResult]);
				}
			});
		}

		return result;
	}

	public calculateFieldSetProperties(definitionFieldSet: DescriptionTemplateFieldSet, ordinal: number, item: DescriptionPropertyDefinition, descriptionReferences: DescriptionReference[]): DescriptionPropertyDefinitionFieldSetEditorModel {
		if (definitionFieldSet == null) return null;

		// current saved values
		const fieldSetValue: DescriptionPropertyDefinitionFieldSet = item?.fieldSets[definitionFieldSet.id] ?? {};

		// new item case, where we need to add controls for all the containing fields.
		if (fieldSetValue.items == null || fieldSetValue.items?.length == 0) {

			const fields = new Map<string, DescriptionField>();
			definitionFieldSet.fields.forEach(definitionField => {
				fields.set(definitionField.id, {
					textValue: definitionField.defaultValue ? definitionField.defaultValue.textValue : undefined,
					textListValue: definitionField.defaultValue && definitionField.defaultValue.textValue && definitionField.defaultValue.textValue.length > 0 ? [definitionField.defaultValue.textValue] : undefined,
					dateValue: definitionField.defaultValue ? definitionField.defaultValue.dateValue : undefined,
					booleanValue: definitionField.defaultValue ? definitionField.defaultValue.booleanValue : undefined,
					externalIdentifier: undefined,
					references: undefined,
					tags: undefined
				});
			})
			fieldSetValue.items = [{
				fields: fields,
				ordinal: ordinal
			} as DescriptionPropertyDefinitionFieldSetItem]
		}

		// preffiling item case we ned to ensure tha all fields of fieldset are contained
		for (let i = 0; i < definitionFieldSet.fields.length; i++) {
			const definitionField = definitionFieldSet.fields[i];
			for (let j = 0; j < fieldSetValue.items.length; j++) {
				const fieldSetValueItem = fieldSetValue.items[j];
				const descriptionField = fieldSetValueItem.fields[definitionField.id];
				if (!descriptionField) {
					fieldSetValueItem.fields[definitionField.id] = {
						textValue: undefined,
						textListValue: undefined,
						dateValue: undefined,
						booleanValue: undefined,
						externalIdentifier: undefined,
						references: undefined,
						tags: undefined
					};
				}
			}

		}

		return new DescriptionPropertyDefinitionFieldSetEditorModel(this.validationErrorModel).fromModel(fieldSetValue, descriptionReferences, definitionFieldSet);
	}

}

export class DescriptionPropertyDefinitionFieldSetEditorModel implements DescriptionPropertyDefinitionFieldSetPersist {
	items?: DescriptionPropertyDefinitionFieldSetItemEditorModel[] = [];
	comment?: string;
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	fieldSetDefinition: DescriptionTemplateFieldSet;

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: DescriptionPropertyDefinitionFieldSet, descriptionReferences: DescriptionReference[], definitionFieldSet: DescriptionTemplateFieldSet): DescriptionPropertyDefinitionFieldSetEditorModel {
		this.fieldSetDefinition = definitionFieldSet;
		if (item) {
			this.comment = item.comment;
			if (item.items) { item.items.sort(x=> x.ordinal).map(x => this.items.push(new DescriptionPropertyDefinitionFieldSetItemEditorModel(this.validationErrorModel).fromModel(x, descriptionReferences, definitionFieldSet))); }
		}
		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string,
		visibilityRulesService: VisibilityRulesService
	}): UntypedFormGroup {
		let { context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = DescriptionPropertyDefinitionFieldSetEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath,
				fieldSetDefinition: this.fieldSetDefinition
			});
		}

		return this.formBuilder.group({
			items: this.formBuilder.array(
				(this.items ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}items[${index}].`,
						visibilityRulesService: params.visibilityRulesService
					})
				), context.getValidation('items').validators
			),
			comment: [{ value: this.comment, disabled: disabled }, context.getValidation('comment').validators],
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel,
		fieldSetDefinition: DescriptionTemplateFieldSet
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		const validators = [];
		validators.push(BackendErrorValidator(validationErrorModel, `${rootPath}items`));

		if (params.fieldSetDefinition?.hasMultiplicity) {
			if (params.fieldSetDefinition?.multiplicity?.min >= 0 && params.fieldSetDefinition?.multiplicity?.max >= 0) {
				validators.push(Validators.minLength(params.fieldSetDefinition.multiplicity.min));
				validators.push(Validators.maxLength(params.fieldSetDefinition.multiplicity.max));

			} else if (params.fieldSetDefinition?.multiplicity?.min >= 0) {
				validators.push(Validators.minLength(params.fieldSetDefinition.multiplicity.min));
			} else if (params.fieldSetDefinition?.multiplicity?.max >= 0) {
				validators.push(Validators.maxLength(params.fieldSetDefinition.multiplicity.max));
			}
		}

		baseValidationArray.push({ key: 'items', validators: validators });
		baseValidationArray.push({ key: 'comment', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}comment`)] });
		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formArray: UntypedFormArray,
		validationErrorModel: ValidationErrorModel,
		rootPath: string,
		fieldSetDefinition: DescriptionTemplateFieldSet,
		visibilityRulesService: VisibilityRulesService
	}): void {
		const { validationErrorModel, rootPath, formArray, fieldSetDefinition } = params;
		formArray?.controls?.forEach(
			(control, index) => DescriptionPropertyDefinitionFieldSetItemEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}items[${index}].`,
				validationErrorModel: validationErrorModel,
				fieldSetDefinition: fieldSetDefinition,
				visibilityRulesService: params.visibilityRulesService
			})
		);
	}

}

export class DescriptionPropertyDefinitionFieldSetItemEditorModel implements DescriptionPropertyDefinitionFieldSetItemPersist {
	fields: Map<string, DescriptionFieldEditorModel> = new Map<string, DescriptionFieldEditorModel>;
	ordinal?: number;
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: DescriptionPropertyDefinitionFieldSetItem, descriptionReferences: DescriptionReference[], definitionFieldSet: DescriptionTemplateFieldSet): DescriptionPropertyDefinitionFieldSetItemEditorModel {
		if (item) {
			this.ordinal = item.ordinal;
			if (item.fields) {
				//TODO: don't like it. Find a common way to parse it either its Map or json.
				if (item.fields instanceof Map)
					new Map(item.fields)?.forEach((value, key) => this.fields.set(key, new DescriptionFieldEditorModel(this.validationErrorModel).fromModel(value, item.ordinal, definitionFieldSet?.fields?.find(x => x.id == key), descriptionReferences)));
				else
					Object.keys(item.fields)?.forEach((key) => {
					if ( definitionFieldSet?.fields?.find(x => x.id == key) != null){
						this.fields.set(key, new DescriptionFieldEditorModel(this.validationErrorModel).fromModel(item.fields[key], item.ordinal, definitionFieldSet?.fields?.find(x => x.id == key), descriptionReferences))
					}
				});
			}
		}
		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string,
		visibilityRulesService: VisibilityRulesService
	}): UntypedFormGroup {
		let { context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = DescriptionPropertyDefinitionFieldSetItemEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		const formGroup = this.formBuilder.group({});
		formGroup.addControl('ordinal', new FormControl({ value: this.ordinal, disabled: disabled }, context.getValidation('ordinal').validators));


		const fieldsFormGroup = this.formBuilder.group({});
		this.fields.forEach((value, key) => fieldsFormGroup.addControl(key.toString(), value.buildForm({
			rootPath: `${rootPath}fields[${key}].`,
			visibilityRulesService: params.visibilityRulesService,
			visibilityRulesKey: key + '_' + formGroup.get('ordinal').value
		})), context.getValidation('fields')
		)
		formGroup.addControl('fields', fieldsFormGroup);

		return formGroup;
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'fields', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}fields`)] });
		baseValidationArray.push({ key: 'ordinal', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}ordinal`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string,
		fieldSetDefinition: DescriptionTemplateFieldSet,
		visibilityRulesService: VisibilityRulesService
	}): void {

		const { formGroup, rootPath, validationErrorModel, fieldSetDefinition } = params;
		const context = DescriptionPropertyDefinitionFieldSetItemEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		const fields = formGroup?.get('fields') as UntypedFormGroup;
		const keys = Object.keys(fields.value as Object);
		keys.forEach((key) => {
			const control = fields?.get(key);
			DescriptionFieldEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}fields[${key}].`,
				validationErrorModel: validationErrorModel,
				fieldDefinition: fieldSetDefinition.fields.find(x => x.id == key),
				visibilityRulesService: params.visibilityRulesService,
				visibilityRulesKey: key + '_' + formGroup.get('ordinal').value
			})
		});

		['ordinal'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});
	}

}

export class DescriptionFieldEditorModel implements DescriptionFieldPersist {
	textValue: string;
	textListValue: string[];
	dateValue: Date;
	booleanValue: boolean;
	references: ReferencePersist[] = [];
	reference: ReferencePersist;
	tags: string[] = [];
	externalIdentifier?: DescriptionExternalIdentifierEditorModel = new DescriptionExternalIdentifierEditorModel(this.validationErrorModel);

	fieldDefinition: DescriptionTemplateField;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: DescriptionField, ordinal: number, descriptionTemplateField: DescriptionTemplateField, descriptionReferences: DescriptionReference[]): DescriptionFieldEditorModel {
		this.fieldDefinition = descriptionTemplateField;
		if (item) {
			this.textValue = item.textValue;
			this.textListValue = item.textListValue;
			this.dateValue = item.dateValue;
			this.booleanValue = item.booleanValue;
			this.tags = item.tags?.map(x => x.label);
			const references = descriptionReferences?.filter(x => x.data?.fieldId == descriptionTemplateField?.id && x.data.ordinal == ordinal && x.isActive == IsActive.Active).map(x => {
				return {
					id: x.reference.id,
					label: x.reference.label,
					reference: x.reference.reference,
					source: x.reference.source,
					typeId: x.reference.type.id,
					description: x.reference.source,
					definition: x.reference.definition,
					abbreviation: x.reference.abbreviation,
					sourceType: x.reference.sourceType
				}
			});
			if (descriptionTemplateField?.data?.multipleSelect == true) {
				this.references = references;
			} else {
				if (references?.length == 1) this.reference = references[0];
				if (references?.length > 1) {
					console.error("multiple references on single reference field: " + references);
					this.reference = references[0];
				}
			}


			this.externalIdentifier = new DescriptionExternalIdentifierEditorModel(this.validationErrorModel).fromModel(item.externalIdentifier);
		}
		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string,
		visibilityRulesService: VisibilityRulesService,
		visibilityRulesKey: string
	}): UntypedFormGroup {
		let { context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = DescriptionFieldEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath,
				fieldDefinition: this.fieldDefinition,
				visibilityRulesService: params.visibilityRulesService,
				visibilityRulesKey: params.visibilityRulesKey
			});
		}

		const fieldType = this.fieldDefinition.data.fieldType;
		const multipleSelect = this.fieldDefinition.data.multipleSelect;
		const fieldValueControlName = DescriptionEditorModel.getFieldValueControlName(fieldType, multipleSelect);
		const formGroup = this.formBuilder.group({});
		switch (fieldType) {
			case DescriptionTemplateFieldType.FREE_TEXT:
			case DescriptionTemplateFieldType.TEXT_AREA:
			case DescriptionTemplateFieldType.UPLOAD:
			case DescriptionTemplateFieldType.RICH_TEXT_AREA:
			case DescriptionTemplateFieldType.RADIO_BOX:
				formGroup.addControl(fieldValueControlName, new FormControl({ value: this.textValue, disabled: disabled }, context.getValidation(fieldValueControlName).validators));
			case DescriptionTemplateFieldType.DATASET_IDENTIFIER:
			case DescriptionTemplateFieldType.VALIDATION:
				formGroup.addControl(fieldValueControlName, this.externalIdentifier.buildForm({
					rootPath: `${rootPath}externalIdentifier.`,
					fieldDefinition: this.fieldDefinition,
					visibilityRulesService: params.visibilityRulesService,
					visibilityRulesKey: params.visibilityRulesKey
				}));
			case DescriptionTemplateFieldType.DATE_PICKER:
				formGroup.addControl(fieldValueControlName, new FormControl({ value: this.dateValue, disabled: disabled }, context.getValidation(fieldValueControlName).validators));
			case DescriptionTemplateFieldType.CHECK_BOX:
			case DescriptionTemplateFieldType.BOOLEAN_DECISION:
				formGroup.addControl(fieldValueControlName, new FormControl({ value: this.booleanValue, disabled: disabled }, context.getValidation(fieldValueControlName).validators));
			case DescriptionTemplateFieldType.INTERNAL_ENTRIES_DESCRIPTIONS:
				if (multipleSelect) formGroup.addControl(fieldValueControlName, new FormControl({ value: this.textListValue, disabled: disabled }, context.getValidation(fieldValueControlName).validators));
				else formGroup.addControl(fieldValueControlName, new FormControl({ value: this.textValue, disabled: disabled }, context.getValidation(fieldValueControlName).validators));
			case DescriptionTemplateFieldType.INTERNAL_ENTRIES_PLANS:
				if (multipleSelect) formGroup.addControl(fieldValueControlName, new FormControl({ value: this.textListValue, disabled: disabled }, context.getValidation(fieldValueControlName).validators));
				else formGroup.addControl(fieldValueControlName, new FormControl({ value: this.textValue, disabled: disabled }, context.getValidation(fieldValueControlName).validators));
			case DescriptionTemplateFieldType.REFERENCE_TYPES:
				if (multipleSelect) formGroup.addControl(fieldValueControlName, new FormControl({ value: this.references, disabled: disabled }, context.getValidation(fieldValueControlName).validators));
				else formGroup.addControl(fieldValueControlName, new FormControl({ value: this.reference, disabled: disabled }, context.getValidation(fieldValueControlName).validators));
			case DescriptionTemplateFieldType.SELECT:
				if (multipleSelect) formGroup.addControl(fieldValueControlName, new FormControl({ value: this.textListValue, disabled: disabled }, context.getValidation(fieldValueControlName).validators));
				else formGroup.addControl(fieldValueControlName, new FormControl({ value: this.textValue, disabled: disabled }, context.getValidation(fieldValueControlName).validators));
			case DescriptionTemplateFieldType.TAGS:
				formGroup.addControl(fieldValueControlName, new FormControl({ value: this.tags, disabled: disabled }, context.getValidation(fieldValueControlName).validators));
		}

		return formGroup;
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel,
		fieldDefinition: DescriptionTemplateField,
		visibilityRulesService: VisibilityRulesService,
		visibilityRulesKey: string
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();

		const fieldValueControlName = DescriptionEditorModel.getFieldValueControlName(params.fieldDefinition.data.fieldType, params.fieldDefinition.data.multipleSelect);
		const validators = [];
		validators.push(BackendErrorValidator(validationErrorModel, `${rootPath}${fieldValueControlName}`));

		params.fieldDefinition.validations.forEach(validation => {
			switch (validation) {
				case DescriptionTemplateFieldValidationType.Required:
					validators.push(CustomValidators.RequiredWithVisibilityRulesValidator(params.visibilityRulesService, params.visibilityRulesKey));
					break;
				case DescriptionTemplateFieldValidationType.Url:
					validators.push(UrlValidator());
					break;
			}
		});
		baseValidationArray.push({ key: fieldValueControlName, validators: validators });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string,
		fieldDefinition: DescriptionTemplateField,
		visibilityRulesService: VisibilityRulesService,
		visibilityRulesKey: string
	}): void {

		const { formGroup, rootPath, validationErrorModel, fieldDefinition } = params;
		const context = DescriptionFieldEditorModel.createValidationContext({
			rootPath,
			validationErrorModel,
			fieldDefinition: fieldDefinition,
			visibilityRulesService: params.visibilityRulesService,
			visibilityRulesKey: params.visibilityRulesKey

		});

		['textValue', 'textListValue', 'dateValue', 'booleanValue'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})

		DescriptionExternalIdentifierEditorModel.reapplyValidators({
			formGroup: formGroup?.get('externalIdentifier') as UntypedFormGroup,
			rootPath: `${rootPath}externalIdentifier.`,
			validationErrorModel: validationErrorModel,
			fieldDefinition: params.fieldDefinition,
			visibilityRulesService: params.visibilityRulesService,
			visibilityRulesKey: params.visibilityRulesKey
		});
	}
}

export class DescriptionExternalIdentifierEditorModel implements DescriptionExternalIdentifierPersist {
	identifier: string;
	type: string;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: DescriptionExternalIdentifier): DescriptionExternalIdentifierEditorModel {
		if (item) {
			this.identifier = item.identifier;
			this.type = item.type;
		}
		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string,
		fieldDefinition: DescriptionTemplateField,
		visibilityRulesService: VisibilityRulesService,
		visibilityRulesKey: string
	}): UntypedFormGroup {
		let { context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = DescriptionExternalIdentifierEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath,
				fieldDefinition: params.fieldDefinition,
				visibilityRulesService: params.visibilityRulesService,
				visibilityRulesKey: params.visibilityRulesKey
			});
		}

		return this.formBuilder.group({
			identifier: [{ value: this.identifier, disabled: disabled }, context.getValidation('identifier').validators],
			type: [{ value: this.type, disabled: disabled }, context.getValidation('type').validators],
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel,
		fieldDefinition: DescriptionTemplateField,
		visibilityRulesService: VisibilityRulesService,
		visibilityRulesKey: string
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();

		let validators = [];
		validators.push(BackendErrorValidator(validationErrorModel, `${rootPath}identifier`));
		if (params.fieldDefinition?.validations?.includes(DescriptionTemplateFieldValidationType.Required)) {
			validators.push(RequiredWithVisibilityRulesValidator(params.visibilityRulesService, params.visibilityRulesKey));
		}
		baseValidationArray.push({ key: 'identifier', validators: validators });

		validators = [];
		validators.push(BackendErrorValidator(validationErrorModel, `${rootPath}type`));
		if (params.fieldDefinition?.validations?.includes(DescriptionTemplateFieldValidationType.Required)) {
			validators.push(RequiredWithVisibilityRulesValidator(params.visibilityRulesService, params.visibilityRulesKey));
		}
		baseValidationArray.push({ key: 'type', validators: validators });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string,
		fieldDefinition: DescriptionTemplateField,
		visibilityRulesService: VisibilityRulesService,
		visibilityRulesKey: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = DescriptionExternalIdentifierEditorModel.createValidationContext({
			rootPath,
			validationErrorModel,
			fieldDefinition: params.fieldDefinition,
			visibilityRulesService: params.visibilityRulesService,
			visibilityRulesKey: params.visibilityRulesKey
		});

		['identifier', 'type'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});
	}
}

export class DescriptionReferenceEditorModel implements DescriptionReferencePersist {
	id: Guid;
	reference?: ReferencePersist;
	hash: string;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: DescriptionReference): DescriptionReferenceEditorModel {
		this.id = item.id;
		//TODO: refactor reference type
		//this.reference = item.reference;
		this.hash = item.hash;

		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string
	}): UntypedFormGroup {
		let { context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = DescriptionReferenceEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			reference: [{ value: this.reference, disabled: disabled }, context.getValidation('reference').validators],
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators]
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}id`)] });
		baseValidationArray.push({ key: 'reference', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}reference`)] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string,
		fieldDefinition: DescriptionTemplateField,
		visibilityRulesService: VisibilityRulesService,
		visibilityRulesKey: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = DescriptionExternalIdentifierEditorModel.createValidationContext({
			rootPath,
			validationErrorModel,
			fieldDefinition: params.fieldDefinition,
			visibilityRulesService: params.visibilityRulesService,
			visibilityRulesKey: params.visibilityRulesKey
		});

		['id', 'reference'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});
	}
}

export class DescriptionFieldIndicator {
	pageId: string;
	sectionIds: string[];
	fieldSetId: string;
	fieldId: string;
	type: string;

	constructor(pageId: string, sectionIds: string[], fieldSetId: string, fieldId: string, type: DescriptionTemplateFieldType, multipleSelect: boolean = false) {
		this.pageId = pageId;
		this.sectionIds = sectionIds;
		this.fieldSetId = fieldSetId;
		this.fieldId = fieldId;
		this.type = DescriptionEditorModel.getFieldValueControlName(type, multipleSelect);
	}
}
