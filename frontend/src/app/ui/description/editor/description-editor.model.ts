import { FormArray, FormControl, FormGroup, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { DescriptionStatusEnum } from "@app/core/common/enum/description-status";
import { FieldType } from "@app/core/common/enum/field-type";
import { FieldValidationType } from "@app/core/common/enum/field-validation-type";
import { IsActive } from "@app/core/common/enum/is-active.enum";
import { DescriptionTemplate, BaseField, BaseFieldSet, BaseSection, FieldSystemData } from "@app/core/model/description-template/description-template";
import { Description, DescriptionExternalIdentifier, DescriptionExternalIdentifierPersist, PropertyDefinitionField, FieldPersist, DescriptionPersist, PropertyDefinition, PropertyDefinitionFieldSet, PropertyDefinitionFieldSetItem, PropertyDefinitionFieldSetItemPersist, PropertyDefinitionFieldSetPersist, PropertyDefinitionPersist, DescriptionReference, DescriptionReferencePersist } from "@app/core/model/description/description";
import { ReferencePersist } from "@app/core/model/reference/reference";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator, CustomValidators, RequiredWithVisibilityRulesValidator, UrlValidator, ValidateIfVisible } from '@common/forms/validation/custom-validator';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { Validation, ValidationContext } from '@common/forms/validation/validation-context';
import { Guid } from "@common/types/guid";
import { VisibilityRulesService } from "./description-form/visibility-rules/visibility-rules.service";
import { AppPermission } from "@app/core/common/enum/permission.enum";
import { PlanContactEditorModel, PlanContactForm, PlanUserForm } from "@app/ui/plan/plan-editor-blueprint/plan-editor.model";
import { SystemFieldType } from "@app/core/common/enum/system-field-type";
import { Plan } from "@app/core/model/plan/plan";

export class DescriptionEditorModel extends BaseEditorModel implements DescriptionPersist {
	label: string;
	planId: Guid;
	planDescriptionTemplateId: Guid;
	descriptionTemplateId: Guid;
	statusId: Guid;
	description: string;
	properties: PropertyDefinitionEditorModel = new PropertyDefinitionEditorModel(this.validationErrorModel);
	tags: string[] = [];

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
			this.statusId = item.status?.id;
			this.description = item.description;
			this.tags = item.isActive === IsActive.Active ? item.descriptionTags?.filter(x => x.isActive === IsActive.Active).map(x => x.tag?.label): item.descriptionTags?.map(x => x.tag?.label);
			
            item.descriptionReferences = item.isActive === IsActive.Active ? item.descriptionReferences?.filter(x => x.isActive == IsActive.Active): item.descriptionReferences;
            this.properties = new PropertyDefinitionEditorModel(this.validationErrorModel).fromModel(item.properties, descriptionTemplate, item);
        }
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false, visibilityRulesService: VisibilityRulesService): FormGroup<DescriptionEditorForm> {
		if (context == null) { context = this.createValidationContext(); }

		const formGroup = this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			label: [{ value: this.label, disabled: disabled }, context.getValidation('label').validators],
			planId: [{ value: this.planId, disabled: disabled }, context.getValidation('planId').validators],
			planDescriptionTemplateId: [{ value: this.planDescriptionTemplateId, disabled: disabled }, context.getValidation('planDescriptionTemplateId').validators],
			descriptionTemplateId: [{ value: this.descriptionTemplateId, disabled: disabled }, context.getValidation('descriptionTemplateId').validators],
			statusId: [{ value: this.statusId, disabled: disabled }, context.getValidation('statusId').validators],
			description: [{ value: this.description, disabled: disabled }, context.getValidation('description').validators],
			tags: [{ value: this.tags, disabled: disabled }, context.getValidation('tags').validators],
			properties: this.buildProperties(visibilityRulesService, disabled),
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators]
		});
        if(disabled){
            formGroup.disable();
        }
        return formGroup;
	}

	buildProperties(visibilityRulesService: VisibilityRulesService, disabled: boolean = false) {
		return this.properties.buildForm({
			rootPath: `properties.`,
            disabled,
			visibilityRulesService: visibilityRulesService
		});
	}

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(this.validationErrorModel, 'id')] });
		baseValidationArray.push({ key: 'label', validators: [CustomValidators.required(), Validators.maxLength(250), BackendErrorValidator(this.validationErrorModel, 'label')] });
		baseValidationArray.push({ key: 'planId', validators: [CustomValidators.required(), BackendErrorValidator(this.validationErrorModel, 'planId')] });
		baseValidationArray.push({ key: 'planDescriptionTemplateId', validators: [CustomValidators.required(), BackendErrorValidator(this.validationErrorModel, 'planDescriptionTemplateId')] });
		baseValidationArray.push({ key: 'descriptionTemplateId', validators: [CustomValidators.required(), BackendErrorValidator(this.validationErrorModel, 'descriptionTemplateId')] });
		baseValidationArray.push({ key: 'statusId', validators: [BackendErrorValidator(this.validationErrorModel, 'statusId')] });
		baseValidationArray.push({ key: 'description', validators: [BackendErrorValidator(this.validationErrorModel, 'description')] });
		baseValidationArray.push({ key: 'tags', validators: [BackendErrorValidator(this.validationErrorModel, 'tags')] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static getFieldValueControlName(fieldType: FieldType, multipleSelect: boolean, systemFieldType?: SystemFieldType): string {
		switch (fieldType) {
			case FieldType.FREE_TEXT:
			case FieldType.TEXT_AREA:
			case FieldType.UPLOAD:
			case FieldType.RICH_TEXT_AREA:
			case FieldType.RADIO_BOX:
				return 'textValue';
			case FieldType.DATASET_IDENTIFIER:
			case FieldType.VALIDATION:
				return 'externalIdentifier';
			case FieldType.DATE_PICKER:
				return 'dateValue';
			case FieldType.CHECK_BOX:
			case FieldType.BOOLEAN_DECISION:
				return 'booleanValue';
			case FieldType.INTERNAL_ENTRIES_DESCRIPTIONS:
				if (multipleSelect) return 'textListValue';
				else return 'textValue';
			case FieldType.INTERNAL_ENTRIES_PLANS:
				if (multipleSelect) return 'textListValue';
				else return 'textValue';
			case FieldType.REFERENCE_TYPES:
				if (multipleSelect) return 'references';
				else return 'reference';
			case FieldType.SELECT:
				if (multipleSelect) return 'textListValue';
				else return 'textValue';
			case FieldType.TAGS:
				return 'tags';
            case FieldType.NUMBER:
                return 'numberValue';
            case FieldType.SYSTEM:
                switch(systemFieldType){
                    case SystemFieldType.PlanTitle: 
                    case SystemFieldType.PlanDescription: 
                    case SystemFieldType.PlanLanguage: 
                    case SystemFieldType.PlanDescriptionTitle: 
                    case SystemFieldType.PlanDescriptionDescription:
                        return 'textValue';
                    case SystemFieldType.PlanDescriptionTag:
                        return 'textListValue';
                    case SystemFieldType.PlanContact:
                        return 'contacts';
                    case SystemFieldType.PlanAccessRights:
                        return 'numberValue';
                }
		}
	}

    static isViewOnlyDescription(description: Description): boolean {
        return description?.status?.internalStatus == DescriptionStatusEnum.Finalized || description?.isActive === IsActive.Inactive || 
        !description.belongsToCurrentTenant || !description?.authorizationFlags?.some((x) => x === AppPermission.EditDescription) 
    }

    static baseFieldsAreInvalid(formGroup: FormGroup<DescriptionEditorForm>): boolean{
        if(!formGroup){ return false; }
        const baseControls = [formGroup.controls.label, formGroup.controls.planId, formGroup.controls.planDescriptionTemplateId, formGroup.controls.descriptionTemplateId]
        return baseControls.some((x) => !x.disabled && !x?.valid);
    }
}

export interface PropertiesFormGroup {
    fieldSets: FormGroup<{[key: string]: FieldSetsFormGroup}>
}

export class PropertyDefinitionEditorModel implements PropertyDefinitionPersist {
	fieldSets: Map<string, PropertyDefinitionFieldSetEditorModel> = new Map<string, PropertyDefinitionFieldSetEditorModel>;
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: PropertyDefinition, descriptionTemplate: DescriptionTemplate, description: Description): PropertyDefinitionEditorModel {
		this.fieldSets = this.calculateProperties(item, descriptionTemplate, description);
		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string,
		visibilityRulesService: VisibilityRulesService
	}): FormGroup<PropertiesFormGroup> {
		let { context = null, disabled = false, rootPath, visibilityRulesService } = params ?? {}
		if (context == null) {
			context = PropertyDefinitionEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}


		const formGroup = this.formBuilder.group({});
		const fieldSetsFormGroup = this.formBuilder.group({});
		if (this.fieldSets.size > 0) {
			this.fieldSets.forEach((value, key) => fieldSetsFormGroup.addControl(key.toString(), value.buildForm({
				rootPath: `${rootPath}fieldSets[${key}].`,
				visibilityRulesService,
                disabled
			})), context.getValidation('fieldSets'));
            if(disabled){
                fieldSetsFormGroup.disable();
            }
			formGroup.addControl('fieldSets', fieldSetsFormGroup);
		}
        if(disabled){
            formGroup.disable();
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

	private calculateProperties(item: PropertyDefinition, descriptionTemplate: DescriptionTemplate, description: Description): Map<string, PropertyDefinitionFieldSetEditorModel> {
		let result: Map<string, PropertyDefinitionFieldSetEditorModel> = new Map<string, PropertyDefinitionFieldSetEditorModel>();
		if (descriptionTemplate) (
			descriptionTemplate.definition.sections.forEach(definitionSection => {
                const sectionResult = this.calculateSectionProperties(definitionSection, item, description);
                if (sectionResult != null) {
                    result = new Map([...result, ...sectionResult]);
                }
			})
		)
		return result;
	}

	private calculateSectionProperties(definitionSection: BaseSection, item: PropertyDefinition, description: Description): Map<string, PropertyDefinitionFieldSetEditorModel> {
		if (definitionSection == null) return null;
		let result: Map<string, PropertyDefinitionFieldSetEditorModel> = new Map<string, PropertyDefinitionFieldSetEditorModel>();

		definitionSection?.fieldSets?.forEach(definitionFieldSet => {
			const fieldSetResult = this.calculateFieldSetProperties(definitionFieldSet, 0, item, description);
			if (fieldSetResult != null) {
				result.set(definitionFieldSet.id, fieldSetResult);
			}
		});

		if (definitionSection.sections != null && definitionSection.sections?.length > 0) {
			definitionSection.sections.forEach(nestedDefinitionSection => {
				const nestedSectionResult = this.calculateSectionProperties(nestedDefinitionSection, item, description);
				if (nestedSectionResult != null) {
					result = new Map([...result, ...nestedSectionResult]);
				}
			});
		}

		return result;
	}

	public calculateFieldSetProperties(
        definitionFieldSet: BaseFieldSet,
        ordinal: number,
        item: PropertyDefinition,
        description: Description
    ): PropertyDefinitionFieldSetEditorModel {
		if (definitionFieldSet == null) return null;

		// current saved values
		const fieldSetValue: PropertyDefinitionFieldSet = item?.fieldSets?.[definitionFieldSet.id] ?? {};

		// new item case, where we need to add controls for all the containing fields.
		if (fieldSetValue.items == null || fieldSetValue.items?.length == 0) {

			const fields = new Map<string, PropertyDefinitionField>();
			definitionFieldSet.fields.forEach(definitionField => {
				fields.set(definitionField.id, {
					textValue: definitionField.defaultValue ? definitionField.defaultValue.textValue : undefined,
					textListValue: definitionField.defaultValue && definitionField.defaultValue.textValue && definitionField.defaultValue.textValue.length > 0 ? [definitionField.defaultValue.textValue] : undefined,
					dateValue: definitionField.defaultValue ? definitionField.defaultValue.dateValue : undefined,
					booleanValue: definitionField.defaultValue ? definitionField.defaultValue.booleanValue : undefined,
					numberValue: definitionField.defaultValue ? definitionField.defaultValue.numberValue : undefined,
					externalIdentifier: undefined,
					references: undefined,
					tags: undefined
				});
			})
			fieldSetValue.items = [{
				fields: fields,
				ordinal: ordinal
			} as PropertyDefinitionFieldSetItem]
		}

		// preffiling item case we ned to ensure tha all fields of fieldset are contained
		for (let i = 0; i < definitionFieldSet.fields.length; i++) {
			const definitionField = definitionFieldSet.fields[i];
			for (let j = 0; j < fieldSetValue.items.length; j++) {
				const fieldSetValueItem = fieldSetValue.items[j];
				const descriptionField = fieldSetValueItem?.fields?.[definitionField.id];
				if (!descriptionField) {
					fieldSetValueItem.fields[definitionField.id] = {
						textValue: definitionField.defaultValue ? definitionField.defaultValue.textValue : undefined,
						textListValue:  definitionField.defaultValue && definitionField.defaultValue.textValue && definitionField.defaultValue.textValue.length > 0 ? [definitionField.defaultValue.textValue] : undefined,
						dateValue: definitionField.defaultValue ? definitionField.defaultValue.dateValue : undefined,
						booleanValue: definitionField.defaultValue ? definitionField.defaultValue.booleanValue : undefined,
						numberValue: definitionField.defaultValue ? definitionField.defaultValue.numberValue : undefined,
						externalIdentifier: undefined,
						references: undefined,
						tags: undefined
					};
				}
			}

		}

		return new PropertyDefinitionFieldSetEditorModel(this.validationErrorModel).fromModel({
            item: fieldSetValue, definitionFieldSet, description
        });
	}

}

export interface FieldSetsFormGroup extends FormGroup<{
    items: FormArray<FieldSetItemsFormGroup>;
    comment: FormControl<string>;
}>{}

export class PropertyDefinitionFieldSetEditorModel implements PropertyDefinitionFieldSetPersist {
	items?: PropertyDefinitionFieldSetItemEditorModel[] = [];
	comment?: string;
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	fieldSetDefinition: BaseFieldSet;

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(params: {
        item: PropertyDefinitionFieldSet
        definitionFieldSet: BaseFieldSet,
        description?: Description,
        plan?: Plan
    }): PropertyDefinitionFieldSetEditorModel {
        const {item, definitionFieldSet, description = null, plan = null} = params;
		this.fieldSetDefinition = definitionFieldSet;
		if (item) {
			this.comment = item.comment;
			if (item.items) { item.items.sort(x=> x.ordinal).map(x => this.items.push(
                new PropertyDefinitionFieldSetItemEditorModel(this.validationErrorModel)
                .fromModel({item: x, definitionFieldSet, description, plan})
            )); }
		}
		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string,
		visibilityRulesService: VisibilityRulesService,
	}): UntypedFormGroup {
		let { context = null, disabled = false, rootPath, visibilityRulesService } = params ?? {}
		if (context == null) {
			context = PropertyDefinitionFieldSetEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath,
                visibilityRulesService,
				fieldSetDefinition: this.fieldSetDefinition
			});
		}

		return this.formBuilder.group({
			items: this.formBuilder.array(
				(this.items ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}items[${index}].`,
						visibilityRulesService
					})
				), context.getValidation('items').validators
			),
			comment: [{ value: this.comment, disabled: disabled }, context.getValidation('comment').validators],
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel,
        visibilityRulesService: VisibilityRulesService,
		fieldSetDefinition: BaseFieldSet
	}): ValidationContext {
		const { rootPath = '', validationErrorModel, visibilityRulesService } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		const validators = [];
		validators.push(BackendErrorValidator(validationErrorModel, `${rootPath}items`));
        
		if (params.fieldSetDefinition?.hasMultiplicity) {
			if (params.fieldSetDefinition?.multiplicity?.min >= 0 && params.fieldSetDefinition?.multiplicity?.max >= 0) {
				validators.push(ValidateIfVisible(visibilityRulesService, params.fieldSetDefinition.id, Validators.minLength(params.fieldSetDefinition.multiplicity.min), 'minlength'));
				validators.push(Validators.maxLength(params.fieldSetDefinition.multiplicity.max));

			} else if (params.fieldSetDefinition?.multiplicity?.min >= 0) {
				validators.push(ValidateIfVisible(visibilityRulesService, params.fieldSetDefinition.id, Validators.minLength(params.fieldSetDefinition.multiplicity.min), 'minlength'));
			} else if (params.fieldSetDefinition?.multiplicity?.max >= 0) {
				validators.push(ValidateIfVisible(visibilityRulesService, params.fieldSetDefinition.id, Validators.maxLength(params.fieldSetDefinition.multiplicity.max), 'maxlength'));
			}
		}

		baseValidationArray.push({ key: 'items', validators: validators });
		baseValidationArray.push({ key: 'comment', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}comment`)] });
		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formArray: FormArray<FieldSetItemsFormGroup>,
		validationErrorModel: ValidationErrorModel,
		rootPath: string,
		fieldSetDefinition: BaseFieldSet,
		visibilityRulesService: VisibilityRulesService,
	}): void {
		const { validationErrorModel, rootPath, formArray, fieldSetDefinition, visibilityRulesService } = params;
		formArray?.controls?.forEach(
			(control, index) => PropertyDefinitionFieldSetItemEditorModel.reapplyValidators({
				formGroup: control,
				rootPath: `${rootPath}items[${index}].`,
				validationErrorModel: validationErrorModel,
				fieldSetDefinition: fieldSetDefinition,
				visibilityRulesService
			})
		);
	}

}

export interface FieldSetItemsFormGroup extends FormGroup <{
    ordinal: FormControl<number>;
    fields: FormGroup<{ [key: string]: FieldFormGroup }>
}>{}

export class PropertyDefinitionFieldSetItemEditorModel implements PropertyDefinitionFieldSetItemPersist {
	fields: Map<string, FieldEditorModel> = new Map<string, FieldEditorModel>;
	ordinal?: number;
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(params: {
        item: PropertyDefinitionFieldSetItem,
        definitionFieldSet: BaseFieldSet,
        description?: Description,
        plan?: Plan
    }): PropertyDefinitionFieldSetItemEditorModel {
        const {item, definitionFieldSet, description = null, plan = null} = params;
		if (item) {
			this.ordinal = item.ordinal;
			if (item.fields) {
                const iterable = item.fields instanceof Map ? Object.fromEntries(item.fields) : item.fields;
                Object.keys(iterable)?.forEach((key) => {
					if ( definitionFieldSet?.fields?.find(x => x.id == key) != null){
                        const fieldDefinition = definitionFieldSet?.fields?.find(x => x.id == key);

						this.fields.set(key, new FieldEditorModel(
                            this.validationErrorModel
                        ).fromModel({
                            item: item.fields[key],
                            ordinal: item.ordinal,
                            templateField: definitionFieldSet?.fields?.find(x => x.id == key),
                            description,
                            plan
                        }))
					}
                });
				// if (item.fields instanceof Map){ 
                //     console.log(Object.fromEntries(item.fields))
				// 	new Map(item.fields)?.forEach((value, key) => this.fields.set(key, new DescriptionFieldEditorModel(this.validationErrorModel).fromModel(value, item.ordinal, definitionFieldSet?.fields?.find(x => x.id == key), descriptionReferences)));
                // }
				// else
				// 	Object.keys(item.fields)?.forEach((key) => {
				// 	if ( definitionFieldSet?.fields?.find(x => x.id == key) != null){
				// 		this.fields.set(key, new DescriptionFieldEditorModel(this.validationErrorModel).fromModel(item.fields[key], item.ordinal, definitionFieldSet?.fields?.find(x => x.id == key), descriptionReferences))
				// 	}
				// });
			}
		}
		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string,
		visibilityRulesService: VisibilityRulesService
	}): FieldSetItemsFormGroup {
		let { context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = PropertyDefinitionFieldSetItemEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		const formGroup = this.formBuilder.group({}) as FieldSetItemsFormGroup;
		formGroup.addControl('ordinal', new FormControl({ value: this.ordinal, disabled: disabled }, context.getValidation('ordinal').validators));


		const fieldsFormGroup = this.formBuilder.group({});
		this.fields.forEach((value, key) => fieldsFormGroup.addControl(key.toString(), value.buildForm({
			rootPath: `${rootPath}fields[${key}].`,
            disabled,
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
		formGroup: FieldSetItemsFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string,
		fieldSetDefinition: BaseFieldSet,
		visibilityRulesService: VisibilityRulesService
	}): void {

		const { formGroup, rootPath, validationErrorModel, fieldSetDefinition } = params;
		const context = PropertyDefinitionFieldSetItemEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		const fields = formGroup.controls.fields;
		const keys = Object.keys(fields.value as object);
		keys.forEach((key) => {
			const control = fields?.get(key) as FieldFormGroup;
			FieldEditorModel.reapplyValidators({
				formGroup: control,
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

export interface FieldFormGroup extends FormGroup<{
    [key: string]: FormControl<string | string[] | boolean | Date | number | ReferencePersist[] | ReferencePersist> | DescriptionExternalIdentifierForm | FormArray<PlanUserForm | PlanContactForm>}
>{}
export class FieldEditorModel implements FieldPersist {
	textValue: string;
	textListValue: string[];
	dateValue: Date;
	booleanValue: boolean;
	numberValue: number;
	references: ReferencePersist[] = [];
	reference: ReferencePersist;
	tags: string[] = [];
	externalIdentifier?: DescriptionExternalIdentifierEditorModel = new DescriptionExternalIdentifierEditorModel(this.validationErrorModel);

	fieldDefinition: BaseField;

    contacts: PlanContactEditorModel[];

    systemFieldType?: SystemFieldType;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(params: {
        item: PropertyDefinitionField,
        ordinal: number,
        templateField: BaseField,
        description?: Description,
        plan?: Plan
    }): FieldEditorModel {
        const {item, ordinal, templateField, description = null, plan = null} = params;
		this.fieldDefinition = templateField;
		if (item) {
			this.textValue = item.textValue;
			this.textListValue = item.textListValue;
			this.dateValue = item.dateValue;
			this.booleanValue = item.booleanValue;
			this.numberValue = item.numberValue;
			this.tags = item.tags?.map(x => x.label);
            this.systemFieldType = (templateField?.data as FieldSystemData)?.systemFieldType ?? null;
            
            const entityReferences = (description ? 
                description.descriptionReferences?.filter(x => x.data?.fieldId == templateField?.id && x.data.ordinal == ordinal) :
                plan?.planReferences?.filter(x => x.data?.blueprintFieldId?.toString() == templateField?.id && x.data.ordinal == ordinal)
            ) ?? [];
			const references = entityReferences.map(x => {
				return {
					id: x.reference.id,
					label: x.reference.label,
					reference: x.reference.reference,
					source: x.reference.source,
					typeId: x.reference.type.id,
					description: x.reference.description,
					definition: x.reference.definition,
					abbreviation: x.reference.abbreviation,
					sourceType: x.reference.sourceType
				}
			});
			if (templateField?.data?.multipleSelect == true) {
				this.references = references;
			} else {
				if (references?.length == 1) this.reference = references[0];
				if (references?.length > 1) {
					console.error("multiple references on single reference field: " + references.map((x) => x.id));
					this.reference = references[0];
				}
			}


			this.externalIdentifier = new DescriptionExternalIdentifierEditorModel(this.validationErrorModel).fromModel(item.externalIdentifier);
            if(plan){
                this.contacts = plan.properties?.contacts?.map(x => new PlanContactEditorModel().fromModel(x));
            }
		}
		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string,
		visibilityRulesService: VisibilityRulesService,
		visibilityRulesKey: string
	}): FieldFormGroup {
		let { context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = FieldEditorModel.createValidationContext({
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
		const formGroup = this.formBuilder.group({}) as FieldFormGroup;
		switch (fieldType) {
			case FieldType.FREE_TEXT:
			case FieldType.TEXT_AREA:
			case FieldType.UPLOAD:
			case FieldType.RICH_TEXT_AREA:
			case FieldType.RADIO_BOX:
				formGroup.addControl(fieldValueControlName, new FormControl({ value: this.textValue, disabled: disabled }, context.getValidation(fieldValueControlName).validators));
                break;
			case FieldType.DATASET_IDENTIFIER:
			case FieldType.VALIDATION:
				formGroup.addControl(fieldValueControlName, this.externalIdentifier.buildForm({
					rootPath: `${rootPath}externalIdentifier.`,
					fieldDefinition: this.fieldDefinition,
					visibilityRulesService: params.visibilityRulesService,
					visibilityRulesKey: params.visibilityRulesKey
				}));
                break;
			case FieldType.DATE_PICKER:
				formGroup.addControl(fieldValueControlName, new FormControl({ value: this.dateValue, disabled: disabled }, context.getValidation(fieldValueControlName).validators));
                break;
			case FieldType.CHECK_BOX:
			case FieldType.BOOLEAN_DECISION:
				formGroup.addControl(fieldValueControlName, new FormControl({ value: this.booleanValue, disabled: disabled }, context.getValidation(fieldValueControlName).validators));
                break;
			case FieldType.INTERNAL_ENTRIES_DESCRIPTIONS:
				if (multipleSelect) formGroup.addControl(fieldValueControlName, new FormControl({ value: this.textListValue, disabled: disabled }, context.getValidation(fieldValueControlName).validators));
				else formGroup.addControl(fieldValueControlName, new FormControl({ value: this.textValue, disabled: disabled }, context.getValidation(fieldValueControlName).validators));
                break;
			case FieldType.INTERNAL_ENTRIES_PLANS:
				if (multipleSelect) formGroup.addControl(fieldValueControlName, new FormControl({ value: this.textListValue, disabled: disabled }, context.getValidation(fieldValueControlName).validators));
				else formGroup.addControl(fieldValueControlName, new FormControl({ value: this.textValue, disabled: disabled }, context.getValidation(fieldValueControlName).validators));
                break;
			case FieldType.REFERENCE_TYPES:
				if (multipleSelect) formGroup.addControl(fieldValueControlName, new FormControl({ value: this.references, disabled: disabled }, context.getValidation(fieldValueControlName).validators));
				else formGroup.addControl(fieldValueControlName, new FormControl({ value: this.reference, disabled: disabled }, context.getValidation(fieldValueControlName).validators));
                break;
			case FieldType.SELECT:
				if (multipleSelect) formGroup.addControl(fieldValueControlName, new FormControl({ value: this.textListValue, disabled: disabled }, context.getValidation(fieldValueControlName).validators));
				else formGroup.addControl(fieldValueControlName, new FormControl({ value: this.textValue, disabled: disabled }, context.getValidation(fieldValueControlName).validators));
                break;
			case FieldType.TAGS:
				formGroup.addControl(fieldValueControlName, new FormControl({ value: this.tags, disabled: disabled }, context.getValidation(fieldValueControlName).validators));
                break;
            case FieldType.NUMBER:
				formGroup.addControl(fieldValueControlName, new FormControl({ value: this.numberValue, disabled: disabled }, context.getValidation(fieldValueControlName).validators));
                break;
            case FieldType.SYSTEM:
                break;
            
		}
		return formGroup;
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel,
		fieldDefinition: BaseField,
		visibilityRulesService: VisibilityRulesService,
		visibilityRulesKey: string
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();

		const fieldValueControlName = DescriptionEditorModel.getFieldValueControlName(params.fieldDefinition.data.fieldType, params.fieldDefinition.data.multipleSelect);
		const validators = [];
		validators.push(BackendErrorValidator(validationErrorModel, `${rootPath}${fieldValueControlName}`));

        if(params.fieldDefinition.data?.fieldType != FieldType.SYSTEM){
            params.fieldDefinition.validations?.forEach(validation => {
                switch (validation) {
                    case FieldValidationType.Required:
                        validators.push(CustomValidators.RequiredWithVisibilityRulesValidator(params.visibilityRulesService, params.visibilityRulesKey));
                        break;
                    case FieldValidationType.Url:
                        validators.push(UrlValidator());
                        break;
                    case FieldValidationType.Email:
                        validators.push(Validators.email);
                }
            });
        }
		baseValidationArray.push({ key: fieldValueControlName, validators: validators });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: FieldFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string,
		fieldDefinition: BaseField,
		visibilityRulesService: VisibilityRulesService,
		visibilityRulesKey: string
	}): void {

		const { formGroup, rootPath, validationErrorModel, fieldDefinition } = params;
		const context = FieldEditorModel.createValidationContext({
			rootPath,
			validationErrorModel,
			fieldDefinition: fieldDefinition,
			visibilityRulesService: params.visibilityRulesService,
			visibilityRulesKey: params.visibilityRulesKey

		});

		['textValue', 'textListValue', 'dateValue', 'booleanValue', 'numberValue'].forEach(keyField => {
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

export interface DescriptionExternalIdentifierForm extends FormGroup <{
    identifier: FormControl<string>;
    type: FormControl<string>;
}>{}
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
		fieldDefinition: BaseField,
		visibilityRulesService: VisibilityRulesService,
		visibilityRulesKey: string
	}): DescriptionExternalIdentifierForm {
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
		}) as DescriptionExternalIdentifierForm; 
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel,
		fieldDefinition: BaseField,
		visibilityRulesService: VisibilityRulesService,
		visibilityRulesKey: string
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();

		let validators = [];
		validators.push(BackendErrorValidator(validationErrorModel, `${rootPath}identifier`));
		if (params.fieldDefinition?.validations?.includes(FieldValidationType.Required)) {
			validators.push(RequiredWithVisibilityRulesValidator(params.visibilityRulesService, params.visibilityRulesKey));
		}
		baseValidationArray.push({ key: 'identifier', validators: validators });

		validators = [];
		validators.push(BackendErrorValidator(validationErrorModel, `${rootPath}type`));
		if (params.fieldDefinition?.validations?.includes(FieldValidationType.Required)) {
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
		fieldDefinition: BaseField,
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
		fieldDefinition: BaseField,
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

// export class DescriptionFieldIndicator {
// 	pageId: string;
// 	sectionIds: string[];
// 	fieldSetId: string;
// 	fieldId: string;
// 	type: string;

// 	constructor(pageId: string, sectionIds: string[], fieldSetId: string, fieldId: string, type: DescriptionTemplateFieldType, multipleSelect: boolean = false) {
// 		this.pageId = pageId;
// 		this.sectionIds = sectionIds;
// 		this.fieldSetId = fieldSetId;
// 		this.fieldId = fieldId;
// 		this.type = DescriptionEditorModel.getFieldValueControlName(type, multipleSelect);
// 	}
// }

export interface DescriptionEditorForm {
	id: FormControl<Guid>;
	hash: FormControl<string>;
	label: FormControl<string>;
	planId: FormControl<Guid>;
	planDescriptionTemplateId: FormControl<Guid>;
	descriptionTemplateId: FormControl<Guid>;
	statusId: FormControl<Guid>;
	description: FormControl<string>;
	properties: FormGroup<PropertiesFormGroup>;// | FormGroup<any>;
	tags: FormControl<string[]>;
}

export interface PlanDescriptionTemplateForm {

}

export interface DescriptionTemplateForm {

}