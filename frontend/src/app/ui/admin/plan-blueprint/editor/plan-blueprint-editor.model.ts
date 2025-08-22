import { FormArray, FormControl, FormGroup, UntypedFormArray, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { IsActive } from "@app/core/common/enum/is-active.enum";
import { PlanBlueprintFieldCategory } from "@app/core/common/enum/plan-blueprint-field-category";
import { PlanBlueprintExtraFieldDataType } from "@app/core/common/enum/plan-blueprint-field-type";
import { PlanBlueprintStatus } from "@app/core/common/enum/plan-blueprint-status";
import { PlanBlueprintSystemFieldType } from "@app/core/common/enum/plan-blueprint-system-field-type";
import { PlanBlueprintVersionStatus } from "@app/core/common/enum/plan-blueprint-version-status";
import { DescriptionTemplatesInSection, DescriptionTemplatesInSectionPersist, PlanBlueprint, PlanBlueprintDefinition, PlanBlueprintDefinitionPersist, PlanBlueprintDefinitionSection, PlanBlueprintDefinitionSectionPersist, PlanBlueprintPersist, ExtraFieldInSection, FieldInSection, FieldInSectionPersist, ReferenceTypeFieldInSection, SystemFieldInSection, UploadFieldInSection, UploadOptionPersist } from "@app/core/model/plan-blueprint/plan-blueprint";
import { PluginRepositoryConfiguration } from "@app/core/model/plugin-configuration/plugin-configuration";
import { PluginConfigurationEditorModel } from "@app/ui/plugin/plugin-editor.model";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator, PlanBlueprintSystemFieldRequiredValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";
import { Guid } from "@common/types/guid";
import { startWith, Subject, takeUntil } from "rxjs";

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

	public fromModel(item: PlanBlueprint, configuration?: PluginRepositoryConfiguration[]): PlanBlueprintEditorModel {
		if (item) {
			super.fromModel(item);
			this.label = item.label;
			this.description = item.description
			this.code = item.code;
			this.status = item.status;
			this.versionStatus = item.versionStatus;
			this.definition = new PlanBlueprintDefinitionEditorModel(this.validationErrorModel).fromModel(item.definition, configuration);
		}
		return this;
	}

	buildForm(destroyRef: Subject<any>, context: ValidationContext = null, disabled: boolean = false, isNewOrClone: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		const formGroup = this.formBuilder.group({
			id: [{ value: this.id, disabled }, context.getValidation('id').validators],
			label: [{ value: this.label, disabled }, context.getValidation('label').validators],
			description: [{ value: this.description, disabled }, context.getValidation('description').validators],
			code: [{ value: this.code, disabled: !isNewOrClone }, context.getValidation('code').validators],
			status: [{ value: this.status, disabled }, context.getValidation('status').validators],
			definition: this.definition.buildForm({
                destroyRef,
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
		baseValidationArray.push({ key: 'description', validators: [BackendErrorValidator(this.validationErrorModel, 'description')] });
		baseValidationArray.push({ key: 'code', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'code')] });
		baseValidationArray.push({ key: 'status', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'status')] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	createChildSection(destroyRef: Subject<any>,index: number): UntypedFormGroup {
		const section: PlanBlueprintDefinitionSectionEditorModel = new PlanBlueprintDefinitionSectionEditorModel(this.validationErrorModel);
		section.id = Guid.create();
		section.ordinal = index + 1;
		section.hasTemplates = false;
		section.prefillingSourcesEnabled = false;
		return section.buildForm({ destroyRef, rootPath: 'definition.sections[' + index + '].' });
	}

	createChildField(destroyRef, sectionIndex: number, index: number): UntypedFormGroup {
		const field: FieldInSectionEditorModel = new FieldInSectionEditorModel(this.validationErrorModel);
		field.id = Guid.create();
		field.ordinal = index + 1;
		field.multipleSelect = false;
		return field.buildForm({ destroyRef, rootPath: 'definition.sections[' + sectionIndex + '].fields[' + index + '].' });
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

    static createEmptyBlueprint(): PlanBlueprint {
        return {
            code: null,
            description: null,
            groupId: null,
            label: null,
            definition: {
                sections: []
            },
            status: null,
            version: null,
            versionStatus: null,
            id: null,
            isActive: IsActive.Active
        }
    }
}

export class PlanBlueprintDefinitionEditorModel implements PlanBlueprintDefinitionPersist {
	sections: PlanBlueprintDefinitionSectionEditorModel[] = [];
	pluginConfigurations: PluginConfigurationEditorModel[] = [];
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: PlanBlueprintDefinition, configuration?: PluginRepositoryConfiguration[]): PlanBlueprintDefinitionEditorModel {
		if (item) {
			if (item.sections) { item.sections.sort((a,b) => a.ordinal - b.ordinal).map(x => this.sections.push(new PlanBlueprintDefinitionSectionEditorModel(this.validationErrorModel).fromModel(x))); }
			if (item.pluginConfigurations) { item.pluginConfigurations.map(x => this.pluginConfigurations.push(new PluginConfigurationEditorModel(this.validationErrorModel).fromModel(x, configuration))); }
		}
		return this;
	}

	buildForm(params: {
        destroyRef: Subject<any>,
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string
	}): UntypedFormGroup {
		let { destroyRef, context = null, disabled = false, rootPath } = params ?? {}
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
                        destroyRef,
						rootPath: `${rootPath}sections[${index}].`,
						disabled: disabled
					})
				), context.getValidation('sections').validators
			),
			pluginConfigurations: this.formBuilder.array(
				(this.pluginConfigurations ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}pluginConfigurations[${index}].`,
						disabled: disabled
					})
				), context.getValidation('pluginConfigurations').validators
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
		baseValidationArray.push({ key: 'pluginConfigurations', validators: [ BackendErrorValidator(validationErrorModel, `${rootPath}pluginConfigurations`)] });

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
		formArray?.controls?.forEach(
			(control, index) => PluginConfigurationEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}pluginConfigurations[${index}].`,
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
	canEditDescriptionTemplates: boolean = true;
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
            this.canEditDescriptionTemplates = item.canEditDescriptionTemplates == null || item.canEditDescriptionTemplates 
			this.prefillingSourcesEnabled = item?.prefillingSourcesEnabled ?? false;
			if (item.fields) { item.fields.sort((a,b) => a.ordinal - b.ordinal).map(x => this.fields.push(new FieldInSectionEditorModel(this.validationErrorModel).fromModel(x))); }
			if (item.descriptionTemplates) { item.descriptionTemplates.map(x => this.descriptionTemplates.push(new DescriptionTemplatesInSectionEditorModel(this.validationErrorModel).fromModel(x))); }
			if (item.prefillingSources) this.prefillingSourcesIds = item.prefillingSources.map(x => x.id);
		}
		return this;
	}

	buildForm(params: {
        destroyRef: Subject<any>,
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string
	}): UntypedFormGroup {
		let { destroyRef, context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = PlanBlueprintDefinitionSectionEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		const formGroup = this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			label: [{ value: this.label, disabled: disabled }, context.getValidation('label').validators],
			ordinal: [{ value: this.ordinal, disabled: disabled }, context.getValidation('ordinal').validators],
			description: [{ value: this.description, disabled: disabled }, context.getValidation('description').validators],
			hasTemplates: [{ value: this.hasTemplates, disabled: disabled }, context.getValidation('hasTemplates').validators],
			canEditDescriptionTemplates: [{ value: this.canEditDescriptionTemplates, disabled: disabled }, context.getValidation('canEditDescriptionTemplates').validators],
			prefillingSourcesEnabled: [{ value: this.prefillingSourcesEnabled, disabled: disabled}, context.getValidation('prefillingSourcesEnabled').validators],
			fields: this.formBuilder.array(
				(this.fields ?? []).map(
					(item, index) => item.buildForm({
                        destroyRef,
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
        if(!disabled){
            formGroup.controls.hasTemplates.valueChanges.pipe(startWith(this.hasTemplates), takeUntil(destroyRef))
            .subscribe((val) => {
                if(val){
                    formGroup.controls.descriptionTemplates.enable();
                    formGroup.controls.canEditDescriptionTemplates.enable();
                    formGroup.controls.descriptionTemplates.updateValueAndValidity();
                }else{
                    formGroup.controls.descriptionTemplates.disable();
                    formGroup.controls.canEditDescriptionTemplates.disable();
                    formGroup.controls.descriptionTemplates.updateValueAndValidity();
                }
            });
            formGroup.controls.canEditDescriptionTemplates.valueChanges.pipe(startWith(this.canEditDescriptionTemplates), takeUntil(destroyRef))
            .subscribe((val) => {
                if(val){
                    formGroup.controls.descriptionTemplates.removeValidators(Validators.required);
                }else {
                    formGroup.controls.descriptionTemplates.addValidators(Validators.required);
                }
                formGroup.controls.descriptionTemplates.updateValueAndValidity();
            })
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
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}id`)] });
		baseValidationArray.push({ key: 'label', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}label`)] });
		baseValidationArray.push({ key: 'ordinal', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}ordinal`)] });
		baseValidationArray.push({ key: 'description', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}description`)] });
		baseValidationArray.push({ key: 'hasTemplates', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}hasTemplates`)] });
		baseValidationArray.push({ key: 'canEditDescriptionTemplates', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}canEditDescriptionTemplates`)] });
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

		['id', 'label', 'ordinal', 'description', 'hasTemplates', 'canEditDescriptionTemplates', 'prefillingSourcesIds', 'hash'].forEach(keyField => {
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

    static createEmptySection(): PlanBlueprintDefinitionSection {
        return {
            description: null,
            hasTemplates: null,
            canEditDescriptionTemplates: true,
            id: null,
            label: null,
            ordinal: null,
            prefillingSourcesEnabled: false,
            prefillingSources: null,
            fields: []
        }
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
	public types: UploadOptionEditorModel[] = [];
	public maxFileSizeInMB: number;

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
		} else if (this.category == PlanBlueprintFieldCategory.Upload) {
			const uploadField = item as UploadFieldInSection;
			this.maxFileSizeInMB = uploadField.maxFileSizeInMB;
			if (uploadField.types) { uploadField.types.map(x => this.types.push(new UploadOptionEditorModel(this.validationErrorModel).fromModel(x))); }
		}

		return this;
	}

	buildForm(params: {
        destroyRef: Subject<any>,
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string,
	}): UntypedFormGroup {
		let { destroyRef, context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = FieldInSectionEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		const formGroup = this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],

			category: [{ value: this.category, disabled: disabled }, context.getValidation('category').validators],
			label: [{ value: this.label, disabled: disabled }, context.getValidation('label').validators],
			placeholder: [{ value: this.placeholder, disabled: disabled }, context.getValidation('placeholder').validators],
			description: [{ value: this.description, disabled: disabled }, context.getValidation('description').validators],
			required: [{ value: this.required, disabled: disabled }, context.getValidation('required').validators],
			semantics: [{ value: this.semantics, disabled: disabled }, context.getValidation('semantics').validators],
			ordinal: [{ value: this.ordinal, disabled: disabled }, context.getValidation('ordinal').validators],
			dataType: [{ value: this.dataType, disabled: disabled }, context.getValidation('dataType').validators],
			systemFieldType: [{ value: this.systemFieldType, disabled: disabled }, context.getValidation('systemFieldType').validators],
			referenceTypeId: [{ value: this.referenceTypeId, disabled: disabled }, context.getValidation('referenceTypeId').validators],
			maxFileSizeInMB: [{ value: this.maxFileSizeInMB, disabled: disabled }, context.getValidation('maxFileSizeInMB').validators],
			types: this.formBuilder.array(
				(this.types ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}types[${index}].`,
						disabled: disabled
					})
				), context.getValidation('types').validators
			),
			multipleSelect: [{ value: this.multipleSelect ?? false, disabled: disabled }, context.getValidation('multipleSelect').validators],
		});
        if(!disabled){
            formGroup.get('systemFieldType').valueChanges.pipe(takeUntil(destroyRef)).subscribe((systemFieldType: PlanBlueprintSystemFieldType) => {
                const required = formGroup.get('required');
                if(systemFieldType != null && FieldInSectionEditorModel.alwaysRequiredSystemFieldTypes.includes(systemFieldType)){
                    required.setValue(true);
                } else {
                    if(required.disabled){
                        required.enable();
                    }
                }
            })
            formGroup.get('category').valueChanges.pipe(takeUntil(destroyRef), startWith(formGroup.controls.category.value))
            .subscribe((category: PlanBlueprintFieldCategory) => {
                ['systemFieldType', 'referenceTypeId', 'dataType', 'multipleSelect', 'maxFileSizeInMB', 'types'].forEach((key) => {
                    formGroup.get(key).disable();
                })
                formGroup.get('label').removeValidators(Validators.required);

                switch(category){
                    case PlanBlueprintFieldCategory.System:
                        formGroup.get('systemFieldType').enable();
                        break;
                    case PlanBlueprintFieldCategory.Extra:
                        formGroup.get('dataType').enable();
                        formGroup.get('label').addValidators(Validators.required);
                        break;
                    case PlanBlueprintFieldCategory.ReferenceType:
                        formGroup.get('multipleSelect').enable();
                        formGroup.get('referenceTypeId').enable();
                        break;
                    case PlanBlueprintFieldCategory.Upload:
                        formGroup.get('maxFileSizeInMB').enable();
                        formGroup.get('types').enable();
                        formGroup.get('label').addValidators(Validators.required);
                        break;
                }
                formGroup.get('label').updateValueAndValidity();
            })
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
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}id`)] });

		baseValidationArray.push({ key: 'category', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}category`)] });
		baseValidationArray.push({ key: 'label', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}label`)] });
		baseValidationArray.push({ key: 'placeholder', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}placeholder`)] });
		baseValidationArray.push({ key: 'description', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}description`)] });
		baseValidationArray.push({ key: 'semantics', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}semantics`)] });
		baseValidationArray.push({ key: 'required', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}required`)] });
		baseValidationArray.push({ key: 'ordinal', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}ordinal`)] });
		baseValidationArray.push({ key: 'dataType', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}dataType`)] });
		baseValidationArray.push({ key: 'systemFieldType', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}systemFieldType`)] });
		baseValidationArray.push({ key: 'referenceTypeId', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}referenceTypeId`)] });
		baseValidationArray.push({ key: 'multipleSelect', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}multipleSelect`)] });
		baseValidationArray.push({ key: 'types', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}types`)] });
		baseValidationArray.push({ key: 'maxFileSizeInMB', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}maxFileSizeInMB`)] });

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

		['id', 'category', 'dataType', 'systemFieldType', 'referenceTypeId', 'multipleSelect', 'label', 'placeholder', 'description', 'semantics', 'required', 'ordinal', 'maxFileSizeInMB', 'types'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
            control?.addValidators(context.getValidation(keyField).validators);
		});

		(formGroup.get('types') as FormArray).controls?.forEach(
			(control, index) => UploadOptionEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}types[${index}].`,
				validationErrorModel: validationErrorModel
			}
			)
		);
	}

}

export class UploadOptionEditorModel implements UploadOptionPersist {
	label: string;
	value: string;
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: UploadOptionPersist): UploadOptionEditorModel {
		if (item) {
			this.label = item.label;
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
			context = UploadOptionEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			label: [{ value: this.label, disabled: disabled }, context.getValidation('label').validators],
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
		baseValidationArray.push({ key: 'label', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}label`)] });
		baseValidationArray.push({ key: 'value', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}value`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = UploadOptionEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['label', 'value'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}

export class DescriptionTemplatesInSectionEditorModel implements DescriptionTemplatesInSectionPersist {
	descriptionTemplateGroupId: Guid;
	minMultiplicity: number;
	maxMultiplicity: number;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: DescriptionTemplatesInSection): DescriptionTemplatesInSectionEditorModel {
		if (item) {
			this.descriptionTemplateGroupId = item.descriptionTemplate?.groupId;
			this.minMultiplicity = item.minMultiplicity;
			this.maxMultiplicity = item.maxMultiplicity;
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
			context = DescriptionTemplatesInSectionEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			descriptionTemplateGroupId: [{ value: this.descriptionTemplateGroupId, disabled: disabled }, context.getValidation('descriptionTemplateGroupId').validators],
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

		['descriptionTemplateGroupId', 'minMultiplicity', 'maxMultiplicity'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}

export interface PlanBlueprintForm {
    id: FormControl<Guid>;
    hash: FormControl<string>;
    label: FormControl<string>;
	code: FormControl<string>;
	definition: FormGroup<PlanBlueprintDefinitionForm>;
	status: FormControl<PlanBlueprintStatus>;
	versionStatus: FormControl<PlanBlueprintVersionStatus>;
	description: FormControl<string>;
	permissions: FormControl<string[]>;
}

export interface PlanBlueprintDefinitionForm {
    sections: FormArray<FormGroup<PlanBlueprintSectionForm>>;
    pluginConfigurations: FormArray
}

export interface PlanBlueprintSectionForm {
	id: FormControl<Guid>;
	label: FormControl<string>;
	description: FormControl<string>;
	ordinal: FormControl<number>;
	fields: FormArray<FormGroup<PlanBlueprintSectionFieldForm>>;
	hasTemplates: FormControl<boolean>;
    canEditDescriptionTemplates: FormControl<boolean>;
	descriptionTemplates?: FormArray<FormGroup<PlanBlueprintSectionDescriptionTemplatesForm>>;
	prefillingSourcesEnabled: FormControl<boolean>;
	prefillingSourcesIds: FormControl<Guid[]>
}

export interface PlanBlueprintSectionFieldForm {
	id: FormControl<Guid>;
	category: FormControl<PlanBlueprintFieldCategory>;
	label: FormControl<string>;
	placeholder: FormControl<string>;
	description: FormControl<string>;
	semantics: FormControl<string[]>;
	required: FormControl<boolean>;
	ordinal: FormControl<number>;
	dataType: FormControl<PlanBlueprintExtraFieldDataType>;
	systemFieldType: FormControl<PlanBlueprintSystemFieldType>;
	referenceTypeId: FormControl<Guid>;
	multipleSelect: FormControl<boolean>;
    maxFileSizeInMB: FormControl<number>;
    types: FormArray;
}

export interface PlanBlueprintSectionDescriptionTemplatesForm {
	descriptionTemplateGroupId: FormControl<Guid>;
	label: FormControl<string>;
	minMultiplicity: FormControl<number>;
	maxMultiplicity: FormControl<number>;
}