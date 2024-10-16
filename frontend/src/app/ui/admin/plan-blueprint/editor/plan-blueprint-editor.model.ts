import { FormArray, UntypedFormArray, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { PlanBlueprintFieldCategory } from "@app/core/common/enum/plan-blueprint-field-category";
import { PlanBlueprintExtraFieldDataType } from "@app/core/common/enum/plan-blueprint-field-type";
import { PlanBlueprintStatus } from "@app/core/common/enum/plan-blueprint-status";
import { PlanBlueprintSystemFieldType } from "@app/core/common/enum/plan-blueprint-system-field-type";
import { PlanBlueprintVersionStatus } from "@app/core/common/enum/plan-blueprint-version-status";
import { DescriptionTemplatesInSection, DescriptionTemplatesInSectionPersist, PlanBlueprint, PlanBlueprintDefinition, PlanBlueprintDefinitionPersist, PlanBlueprintDefinitionSection, PlanBlueprintDefinitionSectionPersist, PlanBlueprintPersist, ExtraFieldInSection, FieldInSection, FieldInSectionPersist, ReferenceTypeFieldInSection, SystemFieldInSection } from "@app/core/model/plan-blueprint/plan-blueprint";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator, PlanBlueprintSystemFieldRequiredValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";
import { Guid } from "@common/types/guid";

export class PlanBlueprintEditorModel extends BaseEditorModel implements PlanBlueprintPersist {
	label: string;
	code: string;
	definition: PlanBlueprintDefinitionEditorModel = new PlanBlueprintDefinitionEditorModel();
	status: PlanBlueprintStatus = PlanBlueprintStatus.Draft;
	versionStatus: PlanBlueprintVersionStatus = PlanBlueprintVersionStatus.Current;
	description: string;
	permissions: string[];

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();
    

	constructor() { super(); }

	public fromModel(item: PlanBlueprint): PlanBlueprintEditorModel {
		if (item) {
			super.fromModel(item);
			this.label = item.label;
			this.code = item.code;
			this.status = item.status;
			this.versionStatus = item.versionStatus;
			this.definition = new PlanBlueprintDefinitionEditorModel(this.validationErrorModel).fromModel(item.definition);
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false, isNewOrClone: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		const formGroup = this.formBuilder.group({
			id: [{ value: this.id, disabled }, context.getValidation('id').validators],
			label: [{ value: this.label, disabled }, context.getValidation('label').validators],
			code: [{ value: this.code, disabled: !isNewOrClone }, context.getValidation('code').validators],
			status: [{ value: this.status, disabled }, context.getValidation('status').validators],
			definition: this.definition.buildForm({
				rootPath: `definition.`,
				disabled
			}),
			hash: [{ value: this.hash, disabled }, context.getValidation('hash').validators]
		});
        return formGroup;
	}

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(this.validationErrorModel, 'id')] });
		baseValidationArray.push({ key: 'label', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'label')] });
		baseValidationArray.push({ key: 'code', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'code')] });
		baseValidationArray.push({ key: 'status', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'status')] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	createChildSection(index: number): UntypedFormGroup {
		const section: PlanBlueprintDefinitionSectionEditorModel = new PlanBlueprintDefinitionSectionEditorModel(this.validationErrorModel);
		section.id = Guid.create();
		section.ordinal = index + 1;
		section.hasTemplates = false;
		section.prefillingSourcesEnabled = false;
		return section.buildForm({ rootPath: 'definition.sections[' + index + '].' });
	}

	createChildField(sectionIndex: number, index: number): UntypedFormGroup {
		const field: FieldInSectionEditorModel = new FieldInSectionEditorModel(this.validationErrorModel);
		field.id = Guid.create();
		field.ordinal = index + 1;
		field.multipleSelect = false;
		return field.buildForm({ rootPath: 'definition.sections[' + sectionIndex + '].fields[' + index + '].' });
	}

	createChildDescriptionTemplate(sectionIndex: number, index: number, item?: any): UntypedFormGroup {
		const descriptionTemplate: DescriptionTemplatesInSectionEditorModel = new DescriptionTemplatesInSectionEditorModel(this.validationErrorModel);
		return descriptionTemplate.buildForm({ rootPath: 'definition.sections[' + sectionIndex + '].descriptionTemplates[' + index + '].' });
	}

	static reApplySectionValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
	}): void {

		const { formGroup, validationErrorModel } = params;
		const control = formGroup?.get('definition');
		PlanBlueprintDefinitionEditorModel.reapplySectionsValidators({
			formArray: control.get('sections') as UntypedFormArray,
			rootPath: `definition.`,
			validationErrorModel: validationErrorModel
		});
		formGroup.updateValueAndValidity();
	}
}

export class PlanBlueprintDefinitionEditorModel implements PlanBlueprintDefinitionPersist {
	sections: PlanBlueprintDefinitionSectionEditorModel[] = [];
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: PlanBlueprintDefinition): PlanBlueprintDefinitionEditorModel {
		if (item) {
			if (item.sections) { item.sections.sort((a,b) => a.ordinal - b.ordinal).map(x => this.sections.push(new PlanBlueprintDefinitionSectionEditorModel(this.validationErrorModel).fromModel(x))); }
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
			context = PlanBlueprintDefinitionEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			sections: this.formBuilder.array(
				(this.sections ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}sections[${index}].`,
						disabled: disabled
					})
				), context.getValidation('sections').validators
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
		baseValidationArray.push({ key: 'sections', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}sections`), PlanBlueprintSystemFieldRequiredValidator()] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplySectionsValidators(params: {
		formArray: UntypedFormArray,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {
		const { validationErrorModel, rootPath, formArray } = params;
		formArray?.controls?.forEach(
			(control, index) => PlanBlueprintDefinitionSectionEditorModel.reapplySectionValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}sections[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);
	}

}

export class PlanBlueprintDefinitionSectionEditorModel implements PlanBlueprintDefinitionSectionPersist {
	id: Guid;
	label: string;
	description: string;
	ordinal: number;
	fields: FieldInSectionEditorModel[] = [];
	hasTemplates: boolean = false;
	descriptionTemplates?: DescriptionTemplatesInSectionEditorModel[] = [];
	prefillingSourcesEnabled: boolean = false;
	prefillingSourcesIds: Guid[]= [];

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: PlanBlueprintDefinitionSection): PlanBlueprintDefinitionSectionEditorModel {
		if (item) {
			this.id = item.id;
			this.label = item.label;
			this.description = item.description;
			this.ordinal = item.ordinal;
			this.hasTemplates = item.hasTemplates;
			this.prefillingSourcesEnabled = item?.prefillingSourcesEnabled ?? false;
			if (item.fields) { item.fields.sort((a,b) => a.ordinal - b.ordinal).map(x => this.fields.push(new FieldInSectionEditorModel(this.validationErrorModel).fromModel(x))); }
			if (item.descriptionTemplates) { item.descriptionTemplates.map(x => this.descriptionTemplates.push(new DescriptionTemplatesInSectionEditorModel(this.validationErrorModel).fromModel(x))); }
			if (item.prefillingSources) this.prefillingSourcesIds = item.prefillingSources.map(x => x.id);
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
			context = PlanBlueprintDefinitionSectionEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			label: [{ value: this.label, disabled: disabled }, context.getValidation('label').validators],
			ordinal: [{ value: this.ordinal, disabled: disabled }, context.getValidation('ordinal').validators],
			description: [{ value: this.description, disabled: disabled }, context.getValidation('description').validators],
			hasTemplates: [{ value: this.hasTemplates, disabled: disabled }, context.getValidation('hasTemplates').validators],
			prefillingSourcesEnabled: [{ value: this.prefillingSourcesEnabled, disabled: disabled}, context.getValidation('prefillingSourcesEnabled').validators],
			fields: this.formBuilder.array(
				(this.fields ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}fields[${index}].`,
						disabled: disabled
					})
				), context.getValidation('fields').validators
			),
			descriptionTemplates: this.formBuilder.array(
				(this.descriptionTemplates ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}descriptionTemplates[${index}].`,
						disabled: disabled
					})
				), context.getValidation('descriptionTemplates').validators
			),
			prefillingSourcesIds: [{ value: this.prefillingSourcesIds, disabled: disabled }, context.getValidation('prefillingSourcesIds').validators],
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
		baseValidationArray.push({ key: 'label', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}label`)] });
		baseValidationArray.push({ key: 'ordinal', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}ordinal`)] });
		baseValidationArray.push({ key: 'description', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}description`)] });
		baseValidationArray.push({ key: 'hasTemplates', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}hasTemplates`)] });
		baseValidationArray.push({ key: 'fields', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}fields`)] });
		baseValidationArray.push({ key: 'descriptionTemplates', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}descriptionTemplates`)] });
		baseValidationArray.push({ key: 'prefillingSourcesIds', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}prefillingSourcesIds`)] });
		baseValidationArray.push({ key: 'prefillingSourcesEnabled', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}prefillingSourcesEnabled`)] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplySectionValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {
		const { formGroup, rootPath, validationErrorModel } = params;
		const context = PlanBlueprintDefinitionSectionEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['id', 'label', 'ordinal', 'description', 'hasTemplates', 'prefillingSourcesIds', 'hash'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});

		(formGroup.get('fields') as FormArray).controls?.forEach(
			(control, index) => FieldInSectionEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}fields[${index}].`,
				validationErrorModel: validationErrorModel
			}
			)
		);

		(formGroup.get('descriptionTemplates') as FormArray).controls?.forEach(
			(control, index) => DescriptionTemplatesInSectionEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}descriptionTemplates[${index}].`,
				validationErrorModel: validationErrorModel
			}
			)
		);

	}

}

export class FieldInSectionEditorModel implements FieldInSectionPersist {
	public id: Guid;
	public category: PlanBlueprintFieldCategory;
	public label: string;
	public placeholder: string;
	public description: string;
	public semantics: string[];
	public required: boolean = false;
	public ordinal: number;
	public dataType: PlanBlueprintExtraFieldDataType;
	public systemFieldType: PlanBlueprintSystemFieldType;
	public referenceTypeId: Guid;
	public multipleSelect: boolean;

	static get alwaysRequiredSystemFieldTypes(): PlanBlueprintSystemFieldType[] {
		return [PlanBlueprintSystemFieldType.Title, PlanBlueprintSystemFieldType.Description, PlanBlueprintSystemFieldType.Language, PlanBlueprintSystemFieldType.AccessRights];
	}

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: FieldInSection): FieldInSectionEditorModel {
		this.id = item.id;
		this.category = item.category;
		this.label = item.label;
		this.placeholder = item.placeholder;
		this.description = item.description;
		this.semantics = item.semantics;
		this.required = item.required;
		this.ordinal = item.ordinal;

		if (this.category == PlanBlueprintFieldCategory.System) {
			this.systemFieldType = (item as SystemFieldInSection).systemFieldType;
			if (FieldInSectionEditorModel.alwaysRequiredSystemFieldTypes.includes(this.systemFieldType)) {
				this.required = true;
			}
		} else if (this.category == PlanBlueprintFieldCategory.Extra) {
			this.dataType = (item as ExtraFieldInSection).dataType;
		} else if (this.category == PlanBlueprintFieldCategory.ReferenceType) {
			this.referenceTypeId = (item as ReferenceTypeFieldInSection).referenceType?.id;
			this.multipleSelect= (item as ReferenceTypeFieldInSection).multipleSelect;
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
			context = FieldInSectionEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		const formGroup = this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],

			category: [{ value: this.category, disabled: disabled }, context.getValidation('category').validators],
			label: [{ value: this.label, disabled: disabled }, this.category === PlanBlueprintFieldCategory.System ? context.getValidation('label-system').validators : ( this.category === PlanBlueprintFieldCategory.Extra ? context.getValidation('label-extra').validators : context.getValidation('label-external-reference').validators)],
			placeholder: [{ value: this.placeholder, disabled: disabled }, context.getValidation('placeholder').validators],
			description: [{ value: this.description, disabled: disabled }, context.getValidation('description').validators],
			required: [{ value: this.required, disabled: disabled }, context.getValidation('required').validators],
			semantics: [{ value: this.semantics, disabled: disabled }, context.getValidation('semantics').validators],
			ordinal: [{ value: this.ordinal, disabled: disabled }, context.getValidation('ordinal').validators],
			dataType: [{ value: this.dataType, disabled: disabled }, context.getValidation('dataType').validators],
			systemFieldType: [{ value: this.systemFieldType, disabled: disabled }, context.getValidation('systemFieldType').validators],
			referenceTypeId: [{ value: this.referenceTypeId, disabled: disabled }, context.getValidation('referenceTypeId').validators],
			multipleSelect: [{ value: this.multipleSelect, disabled: disabled }, context.getValidation('multipleSelect').validators],
		});

        formGroup.get('systemFieldType').valueChanges.subscribe((systemFieldType: PlanBlueprintSystemFieldType) => {
            const required = formGroup.get('required');
            if(systemFieldType != null && FieldInSectionEditorModel.alwaysRequiredSystemFieldTypes.includes(systemFieldType)){
                required.setValue(true);
            } else {
                if(required.disabled){
                    required.enable();
                }
            }
        })
        return formGroup;
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}id`)] });

		baseValidationArray.push({ key: 'category', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}category`)] });
		baseValidationArray.push({ key: 'label-system', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}label`)] });
		baseValidationArray.push({ key: 'label-extra', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}label`)] });
		baseValidationArray.push({ key: 'label-external-reference', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}label`)] });
		baseValidationArray.push({ key: 'placeholder', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}placeholder`)] });
		baseValidationArray.push({ key: 'description', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}description`)] });
		baseValidationArray.push({ key: 'semantics', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}semantics`)] });
		baseValidationArray.push({ key: 'required', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}required`)] });
		baseValidationArray.push({ key: 'ordinal', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}ordinal`)] });
		baseValidationArray.push({ key: 'dataType', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}dataType`)] });
		baseValidationArray.push({ key: 'systemFieldType', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}systemFieldType`)] });
		baseValidationArray.push({ key: 'referenceTypeId', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}referenceTypeId`)] });
		baseValidationArray.push({ key: 'multipleSelect', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}multipleSelect`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = FieldInSectionEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['id', 'category', 'dataType', 'systemFieldType', 'referenceTypeId', 'multipleSelect', 'label', 'placeholder', 'description', 'semantics', 'required', 'ordinal'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			if (keyField == 'label') {
				control?.addValidators(context.has('label-system') ? context.getValidation('label-system').validators : (context.has('label-extra') ? context.getValidation('label-extra').validators : context.getValidation('label-external-reference').validators));
			} else if (keyField == 'referenceTypeId') {
				if (formGroup.get('category').value === PlanBlueprintFieldCategory.ReferenceType) control?.addValidators([Validators.required, ...context.getValidation('referenceTypeId').validators]);
				else control?.addValidators([...context.getValidation('referenceTypeId').validators]);
			} else if (keyField == 'multipleSelect') {
				if (formGroup.get('category').value === PlanBlueprintFieldCategory.ReferenceType) control?.addValidators([Validators.required, ...context.getValidation('multipleSelect').validators]);
				else control?.addValidators([...context.getValidation('multipleSelect').validators]);
			}else if (keyField == 'systemFieldType') {
				if (formGroup.get('category').value === PlanBlueprintFieldCategory.System) control?.addValidators([Validators.required, ...context.getValidation('systemFieldType').validators]);
				else control?.addValidators([...context.getValidation('systemFieldType').validators]);
			} else if (keyField == 'dataType') {
				if (formGroup.get('category').value === PlanBlueprintFieldCategory.Extra) control?.addValidators([Validators.required, ...context.getValidation('dataType').validators]);
				else control?.addValidators([...context.getValidation('dataType').validators]);
			} else {
				control?.addValidators(context.getValidation(keyField).validators);
			}
		})
	}
}

export class DescriptionTemplatesInSectionEditorModel implements DescriptionTemplatesInSectionPersist {
	descriptionTemplateGroupId: Guid;
	label: string;
	minMultiplicity: number;
	maxMultiplicity: number;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: DescriptionTemplatesInSection): DescriptionTemplatesInSectionEditorModel {
		this.descriptionTemplateGroupId = item.descriptionTemplateGroupId;
		this.label = item.label;
		this.minMultiplicity = item.minMultiplicity;
		this.maxMultiplicity = item.maxMultiplicity;
		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string
	}): UntypedFormGroup {
		let { context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = DescriptionTemplatesInSectionEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			descriptionTemplateGroupId: [{ value: this.descriptionTemplateGroupId, disabled: disabled }, context.getValidation('descriptionTemplateGroupId').validators],
			label: [{ value: this.label, disabled: disabled }, context.getValidation('label').validators],
			minMultiplicity: [{ value: this.minMultiplicity, disabled: disabled }, context.getValidation('minMultiplicity').validators],
			maxMultiplicity: [{ value: this.maxMultiplicity, disabled: disabled }, context.getValidation('maxMultiplicity').validators],
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'descriptionTemplateGroupId', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}descriptionTemplateGroupId`)] });
		baseValidationArray.push({ key: 'label', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}label`)] });
		baseValidationArray.push({ key: 'minMultiplicity', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}minMultiplicity`)] });
		baseValidationArray.push({ key: 'maxMultiplicity', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}maxMultiplicity`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = DescriptionTemplatesInSectionEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['descriptionTemplateGroupId', 'label', 'minMultiplicity', 'maxMultiplicity'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}
