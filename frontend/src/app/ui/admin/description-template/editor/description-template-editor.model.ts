import { AbstractControl, FormArray, FormControl, FormGroup, UntypedFormArray, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { FieldDataExternalDatasetType } from "@app/core/common/enum/field-data-external-dataset-type";
import { FieldType } from "@app/core/common/enum/field-type";
import { FieldValidationType } from "@app/core/common/enum/field-validation-type";
import { DescriptionTemplateStatus } from "@app/core/common/enum/description-template-status";
import { IsActive } from "@app/core/common/enum/is-active.enum";
import { UserDescriptionTemplateRole } from "@app/core/common/enum/user-description-template-role";
import { DescriptionTemplate, FieldDefaultValue, DescriptionTemplateDefinition, BaseField, BaseFieldSet, FieldLabelData, Multiplicity, FieldReferenceTypeData, FieldRule, BaseSection, UserDescriptionTemplate, FieldSystemData } from "@app/core/model/description-template/description-template";
import { DefaultValuePersist, DescriptionTemplateDefinitionPersist, FieldExternalDatasetDataPersist, BaseFieldPersist, FieldSetPersist, FieldLabelAndMultiplicityDataPersist, FieldLabelDataPersist, MultiplicityPersist, DescriptionTemplatePersist, FieldRadioBoxDataPersist, FieldRadioBoxOptionPersist, FieldReferenceTypePersist, RulePersist, SectionPersist, FieldSelectDataPersist, FieldSelectOptionPersist, SemanticValuePersist, FieldUploadDataPersist, FieldUploadOptionPersist, UserDescriptionTemplatePersist, FieldNumberDataPersist, FieldSystemDataPersist } from "@app/core/model/description-template/description-template-persist";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator, DescriptionTemplateSystemFieldRequiredValidator, MultiplicityMinMaxValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";
import { Guid } from "@common/types/guid";
import { EditorCustomValidators } from "./custom-validators/editor-custom-validators";
import { PluginRepositoryConfiguration } from "@app/core/model/plugin-configuration/plugin-configuration";
import { PluginConfigurationEditorModel } from "@app/ui/plugin/plugin-editor.model";
import { PluginType } from "@app/core/common/enum/plugin-type";
import { IconConfigurationEditorModel } from "@app/ui/iconconfiguration/icon-configuration-editor.model";
import { SystemFieldType } from "@app/core/common/enum/system-field-type";
import { DescriptionEditorForm } from "@app/ui/description/editor/description-editor.model";
import { PlanEditorForm } from "@app/ui/plan/plan-editor-blueprint/plan-editor.model";

export interface DescriptionTemplateForm {
    id: FormControl<Guid>;
	hash: FormControl<string>;
    label: FormControl<string>;
	code: FormControl<string>;
	description: FormControl<string>;
	language: FormControl<string>;
	type: FormControl<Guid>;
	status: FormControl<DescriptionTemplateStatus>;
	definition: FormGroup<DescriptionTemplateDefinitionForm>;
	users: FormArray<FormGroup<UserDescriptionTemplateForm>>
	permissions: FormControl<string[]>;
}

export class DescriptionTemplateEditorModel extends BaseEditorModel implements DescriptionTemplatePersist {
	label: string;
	code: string;
	description: string;
	language: string;
	type: Guid;
	status: DescriptionTemplateStatus = DescriptionTemplateStatus.Draft;
	definition: DescriptionTemplateDefinitionEditorModel = new DescriptionTemplateDefinitionEditorModel(this.validationErrorModel);
	users: UserDescriptionTemplateEditorModel[] = [];
	permissions: string[];

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super(); }

	public fromModel(item: DescriptionTemplate, configuration?: PluginRepositoryConfiguration[]): DescriptionTemplateEditorModel {
		if (item) {
			super.fromModel(item);
			this.label = item.label;
			this.code = item.code;
			this.description = item.description;
			this.language = item.language;
			this.type = item.type?.id;
			this.status = item.status;
			this.definition = new DescriptionTemplateDefinitionEditorModel(this.validationErrorModel).fromModel(item.definition, configuration);
			if (item.users) { item.users.filter(x => x.isActive === IsActive.Active).map(x => this.users.push(new UserDescriptionTemplateEditorModel(this.validationErrorModel).fromModel(x))); }
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): FormGroup<DescriptionTemplateForm> {
		if (context == null) { context = this.createValidationContext(); }

		const formGroup = this.formBuilder.group({
			id: [{ value: this.id, disabled }, context.getValidation('id').validators],
			label: [{ value: this.label, disabled }, context.getValidation('label').validators],
			code: [{ value: this.code, disabled: !!this.id }, context.getValidation('code').validators],
			description: [{ value: this.description, disabled }, context.getValidation('description').validators],
			language: [{ value: this.language, disabled }, context.getValidation('language').validators],
			type: [{ value: this.type, disabled }, context.getValidation('type').validators],
			status: [{ value: this.status, disabled }, context.getValidation('status').validators],
			definition: this.definition.buildForm({
				rootPath: `definition.`,
				disabled
			}),
			users: this.formBuilder.array(
				(this.users ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `users[${index}].`
					})
				), context.getValidation('users').validators
			),
			hash: [{ value: this.hash, disabled }, context.getValidation('hash').validators]
		});
        if(disabled){
            formGroup.disable();
        }
        return formGroup;
	}

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(this.validationErrorModel, 'id')] });
		baseValidationArray.push({ key: 'label', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'label')] });
		baseValidationArray.push({ key: 'code', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'code')] });
		baseValidationArray.push({ key: 'description', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'description')] });
		baseValidationArray.push({ key: 'language', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'language')] });
		baseValidationArray.push({ key: 'type', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'type')] });
		baseValidationArray.push({ key: 'status', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'status')] });
		baseValidationArray.push({ key: 'users', validators: [BackendErrorValidator(this.validationErrorModel, 'users')] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}


	static reApplyDefinitionValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
	}): void {

		const { formGroup, validationErrorModel } = params;
		const control = formGroup?.get('definition');
		DescriptionTemplateDefinitionEditorModel.reapplySectionsValidators({
			formArray: control.get('sections') as UntypedFormArray,
			rootPath: `definition.`,
			validationErrorModel: validationErrorModel
		});

		DescriptionTemplateDefinitionEditorModel.reapplyPluginValidators({
			formArray: control.get('pluginConfigurations') as UntypedFormArray,
			rootPath: `definition.`,
			validationErrorModel: validationErrorModel
		});

		(formGroup.get('users') as FormArray).controls?.forEach(
			(control, index) => UserDescriptionTemplateEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `users[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);
	}

}

export interface UserDescriptionTemplateForm {
    userId: FormControl<Guid>;
    role: FormControl<UserDescriptionTemplateRole>;
}
export class UserDescriptionTemplateEditorModel implements UserDescriptionTemplatePersist {
	userId?: Guid;
	role?: UserDescriptionTemplateRole;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: UserDescriptionTemplate): UserDescriptionTemplateEditorModel {
		if (item) {
			this.userId = item.user?.id;
			this.role = item.role;
		}
		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string
	}): FormGroup<UserDescriptionTemplateForm> {
		let { context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = UserDescriptionTemplateEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			userId: [{ value: this.userId, disabled: disabled }, context.getValidation('userId').validators],
			role: [{ value: this.role, disabled: disabled }, context.getValidation('role').validators],
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'userId', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}userId`)] });
		baseValidationArray.push({ key: 'role', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}role`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = UserDescriptionTemplateEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['userId', 'role'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}

}


export interface DescriptionTemplateDefinitionForm {
    sections: FormArray<FormGroup<TemplateSectionForm>>;
	pluginConfigurations?: FormArray<FormGroup<PluginConfigurationForm>>;
	iconConfiguration?: FormGroup<IconConfigurationForm>
}

export interface PluginConfigurationForm{
    pluginCode: FormControl<string>;
	pluginType: FormControl<PluginType>;
    fields: FormArray<FormGroup<PluginConfigurationFieldForm>>;
}

export interface IconConfigurationForm{
    matIconName: FormControl<string>;
	storageFileId: FormControl<Guid>;
}

export interface PluginConfigurationFieldForm{
    code: FormControl<string>;
	fileValue: FormControl<Guid>;
    textValue: FormControl<string>;
}

export class DescriptionTemplateDefinitionEditorModel implements DescriptionTemplateDefinitionPersist {
	sections: TemplateSectionEditorModel[] = [];
	pluginConfigurations: PluginConfigurationEditorModel[] = [];
	iconConfiguration: IconConfigurationEditorModel = new IconConfigurationEditorModel(this.validationErrorModel);

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: DescriptionTemplateDefinition, configuration?: PluginRepositoryConfiguration[]): DescriptionTemplateDefinitionEditorModel {
		if (item) {
			if (item.sections) { item.sections.map(x => this.sections.push(new TemplateSectionEditorModel(this.validationErrorModel).fromModel(x))); }
			if (item.pluginConfigurations) { item.pluginConfigurations.map(x => this.pluginConfigurations.push(new PluginConfigurationEditorModel(this.validationErrorModel).fromModel(x, configuration))); }
			if (item.iconConfiguration) this.iconConfiguration = new IconConfigurationEditorModel(this.validationErrorModel).fromModel(item.iconConfiguration);
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
			context = DescriptionTemplateDefinitionEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			sections: this.formBuilder.array(
				(this.sections ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}sections[${index}].`,
						disabled
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
			iconConfiguration: this.iconConfiguration.buildForm({
				rootPath: `${rootPath}iconConfiguration`,
				disabled: disabled
			}),		
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'sections', validators: [Validators.required, DescriptionTemplateSystemFieldRequiredValidator(), BackendErrorValidator(validationErrorModel, `${rootPath}sections`)] });
		baseValidationArray.push({ key: 'pluginConfigurations', validators: [ BackendErrorValidator(validationErrorModel, `${rootPath}pluginConfigurations`)] });
		baseValidationArray.push({ key: 'iconConfiguration', validators: [ BackendErrorValidator(validationErrorModel, `${rootPath}iconConfiguration`)] });

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
			(control, index) => TemplateSectionEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}sections[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);
	}

	static reapplyPluginValidators(params: {
		formArray: UntypedFormArray,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {
		const { validationErrorModel, rootPath, formArray } = params;
		
		formArray?.controls?.forEach(
			(control, index) => PluginConfigurationEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}pluginConfigurations[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);
	}

}

// export interface DescriptionTemplatePageForm{
//     id: FormControl<string>;
//     ordinal: FormControl<number>;
//     title: FormControl<string>;
//     sections: FormArray<FormGroup<DescriptionTemplateSectionForm>>;
// }

// export class DescriptionTemplatePageEditorModel implements DescriptionTemplatePagePersist {
// 	id: string;
// 	ordinal: number;
// 	title: string;
// 	sections: DescriptionTemplateSectionEditorModel[] = [];

// 	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

// 	constructor(
// 		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
// 	) { }

// 	public fromModel(item: DescriptionTemplatePage): DescriptionTemplatePageEditorModel {
// 		if (item) {
// 			this.id = item.id;
// 			this.ordinal = item.ordinal;
// 			this.title = item.title;
// 			if (item.sections) { item.sections.map(x => this.sections.push(new DescriptionTemplateSectionEditorModel(this.validationErrorModel).fromModel(x))); }
// 		}
// 		return this;
// 	}

// 	buildForm(params?: {
// 		context?: ValidationContext,
// 		disabled?: boolean,
// 		rootPath?: string
// 	}): UntypedFormGroup {
// 		let { context = null, disabled = false, rootPath } = params ?? {}
// 		if (context == null) {
// 			context = DescriptionTemplatePageEditorModel.createValidationContext({
// 				validationErrorModel: this.validationErrorModel,
// 				rootPath
// 			});
// 		}

// 		return this.formBuilder.group({
// 			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
// 			ordinal: [{ value: this.ordinal, disabled: disabled }, context.getValidation('ordinal').validators],
// 			title: [{ value: this.title, disabled: disabled }, context.getValidation('title').validators],
// 			sections: this.formBuilder.array(
// 				(this.sections ?? []).map(
// 					(item, index) => item.buildForm({
// 						rootPath: `${rootPath}sections[${index}].`,
// 						disabled
// 					})
// 				), context.getValidation('sections').validators
// 			),
// 		});
// 	}

// 	static createValidationContext(params: {
// 		rootPath?: string,
// 		validationErrorModel: ValidationErrorModel
// 	}): ValidationContext {
// 		const { rootPath = '', validationErrorModel } = params;

// 		const baseContext: ValidationContext = new ValidationContext();
// 		const baseValidationArray: Validation[] = new Array<Validation>();
// 		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}id`)] });
// 		baseValidationArray.push({ key: 'ordinal', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}ordinal`)] });
// 		baseValidationArray.push({ key: 'title', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}title`)] });
// 		baseValidationArray.push({ key: 'sections', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}sections`)] });

// 		baseContext.validation = baseValidationArray;
// 		return baseContext;
// 	}

// 	static reapplySectionsValidators(params: {
// 		formArray: UntypedFormArray,
// 		validationErrorModel: ValidationErrorModel,
// 		rootPath: string
// 	}): void {
// 		const { validationErrorModel, rootPath, formArray } = params;

// 	}

// 	static reapplyValidators(params: {
// 		formGroup: UntypedFormGroup,
// 		validationErrorModel: ValidationErrorModel,
// 		rootPath: string
// 	}): void {

// 		const { formGroup, rootPath, validationErrorModel } = params;
// 		const context = DescriptionTemplatePageEditorModel.createValidationContext({
// 			rootPath,
// 			validationErrorModel
// 		});

// 		(formGroup.get('sections') as FormArray)?.controls?.forEach(
// 			(control, index) => DescriptionTemplateSectionEditorModel.reapplyValidators({
// 				formGroup: control as UntypedFormGroup,
// 				rootPath: `${rootPath}sections[${index}].`,
// 				validationErrorModel: validationErrorModel
// 			})
// 		);

// 		['id', 'ordinal', 'title'].forEach(keyField => {
// 			const control = formGroup?.get(keyField);
// 			control?.clearValidators();
// 			control?.addValidators(context.getValidation(keyField).validators);
// 		})
// 	}

// }

export interface TemplateSectionForm {
    id: FormControl<string>;
	ordinal: FormControl<number>;
	title: FormControl<string>;
	description: FormControl<string>;
	sections: FormArray<FormGroup<TemplateSectionForm>>;
	fieldSets: FormArray<FormGroup<TemplateFieldSetForm>>;
}
export class TemplateSectionEditorModel implements SectionPersist {
	id: string;
	ordinal: number;
	title: string;
	description: string;
	sections: TemplateSectionEditorModel[] = [];
	fieldSets: TemplateFieldSetEditorModel[] = [];
	/** The object keys the section describes. */
	semantics: string[] = [];

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: BaseSection | TemplateSectionEditorModel): TemplateSectionEditorModel {
		if (item) {
			this.id = item.id;
			this.ordinal = item.ordinal;
			this.title = item.title;
			this.description = item.description;
			this.semantics = [...(item.semantics ?? [])];
			if (item.sections) { item.sections.map(x => this.sections.push(new TemplateSectionEditorModel(this.validationErrorModel).fromModel(x))); }
			if (item.fieldSets) { item.fieldSets.map(x => this.fieldSets.push(new TemplateFieldSetEditorModel(this.validationErrorModel).fromModel(x))); }
		}
		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string,
        addSectionValidator?: boolean
	}): UntypedFormGroup {
		let { context = null, disabled = false, rootPath, addSectionValidator = true } = params ?? {}
		if (context == null) {
			context = TemplateSectionEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		const formGroup= this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			ordinal: [{ value: this.ordinal, disabled: disabled }, context.getValidation('ordinal').validators],
			title: [{ value: this.title, disabled: disabled }, context.getValidation('title').validators],
			description: [{ value: this.description, disabled: disabled }, context.getValidation('description').validators],
			semantics: [{ value: this.semantics ?? [], disabled: disabled }, context.getValidation('semantics').validators],
			sections: this.formBuilder.array(
				(this.sections ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}sections[${index}].`,
						disabled
					})
				), context.getValidation('sections').validators
			),
			fieldSets: this.formBuilder.array(
				(this.fieldSets ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}fieldSets[${index}].`,
						disabled
					})
				), context.getValidation('fieldSets').validators
			)
		});
        if(addSectionValidator){
            formGroup.setValidators(EditorCustomValidators.sectionHasAtLeastOneChildOf('fieldSets','sections'));
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
		baseValidationArray.push({ key: 'ordinal', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}ordinal`)] });
		baseValidationArray.push({ key: 'title', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}title`)] });
		baseValidationArray.push({ key: 'description', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}description`)] });
		baseValidationArray.push({ key: 'sections', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}sections`)] });
		baseValidationArray.push({ key: 'semantics', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}semantics`)] });
		baseValidationArray.push({ key: 'fieldSets', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}fieldSets`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = TemplateSectionEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['id', 'ordinal', 'page', 'title', 'description', 'semantics'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});

		(formGroup.get('sections') as FormArray).controls?.forEach(
			(control, index) => TemplateSectionEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}sections[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);

		(formGroup.get('fieldSets') as FormArray).controls?.forEach(
			(control, index) => TemplateFieldSetEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}fieldSets[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);
	}

}

export interface TemplateFieldSetForm {
    id: FormControl<string>;
	ordinal: FormControl<number>;
	title: FormControl<string>;
	description: FormControl<string>;
	extendedDescription: FormControl<string>;
	additionalInformation: FormControl<string>;
	multiplicity: FormGroup<MultiplicityForm>;
	hasMultiplicity: FormControl<boolean>;
	hasCommentField: FormControl<boolean>;
	fields: FormArray<FormGroup<TemplateFieldForm>>;
}
export class TemplateFieldSetEditorModel implements FieldSetPersist {
	id: string;
	ordinal: number;
	title: string;
	description: string;
	extendedDescription: string;
	additionalInformation: string;
	multiplicity: MultiplicityEditorModel = new MultiplicityEditorModel(this.validationErrorModel);
	hasMultiplicity: boolean = false;
	hasCommentField: boolean = false;
	fields: TemplateFieldEditorModel[] = [];
	/** The object keys the question describes. */
	semantics: string[] = [];

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: BaseFieldSet | TemplateFieldSetEditorModel): TemplateFieldSetEditorModel {
		if (item) {
			this.id = item.id;
			this.ordinal = item.ordinal;
			this.title = item.title;
			this.description = item.description;
			this.extendedDescription = item.extendedDescription;
			this.additionalInformation = item.additionalInformation;
			this.hasCommentField = item.hasCommentField;
			this.hasMultiplicity = item.hasMultiplicity;
			this.semantics = [...(item.semantics ?? [])];

			this.multiplicity = new MultiplicityEditorModel(this.validationErrorModel).fromModel(item.multiplicity);
			if (item.fields) { item.fields.map(x => this.fields.push(new TemplateFieldEditorModel(this.validationErrorModel).fromModel(x))); }
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
			context = TemplateFieldSetEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}
        const hasSystemFields = this.fields?.some((field) => field.data?.fieldType == FieldType.SYSTEM);

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			ordinal: [{ value: this.ordinal, disabled: disabled }, context.getValidation('ordinal').validators],
			title: [{ value: this.title, disabled: disabled }, context.getValidation('title').validators],
			description: [{ value: this.description, disabled: disabled }, context.getValidation('description').validators],
			extendedDescription: [{ value: this.extendedDescription, disabled: disabled }, context.getValidation('extendedDescription').validators],
			additionalInformation: [{ value: this.additionalInformation, disabled: disabled }, context.getValidation('additionalInformation').validators],
			hasCommentField: [{ value: this.hasCommentField, disabled: disabled }, context.getValidation('hasCommentField').validators],
			hasMultiplicity: [{ value: this.hasMultiplicity, disabled: disabled || hasSystemFields }, context.getValidation('hasMultiplicity').validators],
			semantics: [{ value: this.semantics ?? [], disabled: disabled }, context.getValidation('semantics').validators],
			multiplicity: this.multiplicity.buildForm({
				rootPath: `${rootPath}multiplicity.`,
				disabled
			}),
			fields: this.formBuilder.array(
				(this.fields ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}fields[${index}].`,
						disabled
					})
				), context.getValidation('fields').validators
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
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}id`)] });
		baseValidationArray.push({ key: 'ordinal', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}ordinal`)] });
		baseValidationArray.push({ key: 'title', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}title`)] });
		baseValidationArray.push({ key: 'description', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}description`)] });
		baseValidationArray.push({ key: 'extendedDescription', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}extendedDescription`)] });
		baseValidationArray.push({ key: 'additionalInformation', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}additionalInformation`)] });
		baseValidationArray.push({ key: 'hasCommentField', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}hasCommentField`)] });
		baseValidationArray.push({ key: 'hasMultiplicity', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}hasMultiplicity`)] });
		baseValidationArray.push({ key: 'fields', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}fields`)] });
		baseValidationArray.push({ key: 'semantics', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}semantics`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = TemplateFieldSetEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['id', 'ordinal', 'title', 'description', 'extendedDescription', 'additionalInformation', 'hasCommentField', 'hasMultiplicity', 'semantics'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});

		MultiplicityEditorModel.reapplyValidators({
			formGroup: formGroup?.get('multiplicity') as UntypedFormGroup,
			rootPath: `${rootPath}multiplicity.`,
			validationErrorModel: validationErrorModel
		});

		(formGroup.get('fields') as FormArray).controls?.forEach(
			(control, index) => TemplateFieldEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}fields[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);
	}
}

export interface MultiplicityForm {
    min: FormControl<number>;
	max: FormControl<number>;
	placeholder: FormControl<string>;
	tableView: FormControl<boolean>;
	transposedTableView: FormControl<boolean>;
}
export class MultiplicityEditorModel implements MultiplicityPersist {
	min: number;
	max: number;
	placeholder: string;
	tableView: boolean;
	transposedTableView: boolean;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: Multiplicity): MultiplicityEditorModel {
		if (item) {
			this.min = item.min;
			this.max = item.max;
			this.placeholder = item.placeholder;
			this.tableView = item.tableView;
			this.transposedTableView = item.transposedTableView;
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
			context = MultiplicityEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			min: [{ value: this.min, disabled: disabled }, context.getValidation('min').validators],
			max: [{ value: this.max, disabled: disabled }, context.getValidation('max').validators],
			placeholder: [{ value: this.placeholder, disabled: disabled }, context.getValidation('placeholder').validators],
			tableView: [{ value: this.tableView, disabled: disabled }, context.getValidation('tableView').validators],
			transposedTableView: [{ value: this.transposedTableView, disabled: disabled }, context.getValidation('transposedTableView').validators],
		}, { validators: MultiplicityMinMaxValidator() });
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'min', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}min`)] });
		baseValidationArray.push({ key: 'max', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}max`)] });
		baseValidationArray.push({ key: 'placeholder', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}placeholder`)] });
		baseValidationArray.push({ key: 'tableView', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}tableView`)] });
		baseValidationArray.push({ key: 'transposedTableView', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}transposedTableView`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = MultiplicityEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['min', 'max', 'placeholder', 'tableView', 'transposedTableView'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}

//
//
// Field Editor Model
//
//

export interface TemplateFieldForm {
    id: FormControl<string>;
	ordinal: FormControl<number>;
	semantics: FormControl<string[]>;
	defaultValue: FormGroup<DefaultValueForm>;
	visibilityRules: FormArray<FormGroup<RuleForm>>;
	validations: FormControl<FieldValidationType[]>;
	includeInExport: FormControl<boolean>;
	data: FormGroup<DescriptionTemplateLabelDataForm>;
} 
export class TemplateFieldEditorModel implements BaseFieldPersist {
	id: string;
	ordinal: number;
	semantics: string[];
	defaultValue: DefaultValueEditorModel = new DefaultValueEditorModel(this.validationErrorModel);
	visibilityRules: RuleEditorModel[] = [];
	validations: FieldValidationType[] = [];
	includeInExport: boolean = true;
	data: LabelDataEditorModel = new LabelDataEditorModel(this.validationErrorModel);

    fieldType: FieldType;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) {
	}

	fromModel(item: BaseField | TemplateFieldEditorModel): TemplateFieldEditorModel {
		if (item) {
			this.id = item.id;
			this.ordinal = item.ordinal;
			this.semantics = item.semantics;
			this.defaultValue = new DefaultValueEditorModel(this.validationErrorModel).fromModel(item.defaultValue);
			this.validations = item.validations ?? [];
			this.includeInExport = item.includeInExport;

			this.data = this.getFieldEditorModel(item.data.fieldType).fromModel(item.data);
            this.fieldType = item.data?.fieldType;
			if (item.visibilityRules) { item.visibilityRules.map(x => this.visibilityRules.push(new RuleEditorModel(this.validationErrorModel).fromModel(x))); }
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
			context = TemplateFieldEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			ordinal: [{ value: this.ordinal, disabled: disabled }, context.getValidation('ordinal').validators],
			semantics: [{ value: this.semantics, disabled: disabled }, context.getValidation('semantics').validators],
			validations: [{ value: this.validations, disabled: disabled }, context.getValidation('validations').validators],
			includeInExport: [{ value: this.includeInExport, disabled: disabled }, context.getValidation('includeInExport').validators],
			data: this.data?.buildForm({
				rootPath: `${rootPath}data.`,
				disabled
			}),
			defaultValue: this.defaultValue.buildForm({
				rootPath: `${rootPath}defaultValue.`,
				disabled: disabled || this.fieldType == FieldType.SYSTEM
			}),
			visibilityRules: this.formBuilder.array(
				(this.visibilityRules ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}visibilityRules[${index}].`,
						disabled
					})
				), context.getValidation('visibilityRules').validators
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
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}id`)] });
		baseValidationArray.push({ key: 'ordinal', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}ordinal`)] });
		baseValidationArray.push({ key: 'semantics', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}semantics`)] });
		baseValidationArray.push({ key: 'defaultValue', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}defaultValue`)] });
		baseValidationArray.push({ key: 'validations', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}validations`)] });
		baseValidationArray.push({ key: 'includeInExport', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}includeInExport`)] });
		baseValidationArray.push({ key: 'visibilityRules', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}visibilityRules`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	private getFieldEditorModel(fieldType: FieldType): LabelDataEditorModel {
		switch (fieldType) {
			case FieldType.REFERENCE_TYPES:
				return new FieldReferenceTypeDataEditorModel(this.validationErrorModel);
			case FieldType.RADIO_BOX:
				return new FieldRadioBoxDataEditorModel(this.validationErrorModel);
			case FieldType.SELECT:
				return new FieldSelectDataEditorModel(this.validationErrorModel);
			case FieldType.BOOLEAN_DECISION:
			case FieldType.CHECK_BOX:
			case FieldType.FREE_TEXT:
			case FieldType.TEXT_AREA:
			case FieldType.RICH_TEXT_AREA:
			case FieldType.DATE_PICKER:
			case FieldType.TAGS:
			case FieldType.DATASET_IDENTIFIER:
			case FieldType.VALIDATION:
				return new LabelDataEditorModel(this.validationErrorModel);
			case FieldType.INTERNAL_ENTRIES_PLANS:
			case FieldType.INTERNAL_ENTRIES_DESCRIPTIONS:
				return new FieldLabelAndMultiplicityDataEditorModel(this.validationErrorModel);
			case FieldType.UPLOAD:
				return new FieldUploadDataEditorModel(this.validationErrorModel);
            case FieldType.NUMBER: 
                return new FieldNumberDataEditorModel(this.validationErrorModel);
            case FieldType.SYSTEM:
                return new FieldSystemDataEditorModel(this.validationErrorModel);
            default: 
                return new LabelDataEditorModel(this.validationErrorModel);
		}
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = TemplateFieldEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['id', 'ordinal', 'semantics', 'defaultValue', 'validations', 'includeInExport'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});

		LabelDataEditorModel.reapplyBaseFieldValidators({
			formGroup: formGroup?.get('data') as UntypedFormGroup,
			rootPath: `${rootPath}data.`,
			validationErrorModel: validationErrorModel
		});

		if (formGroup?.get('data').get('fieldType').value != undefined) {
			switch (formGroup?.get('data').get('fieldType').value) {
				case FieldType.UPLOAD:
					FieldUploadDataEditorModel.reapplyUploadDataValidators({
						formGroup: formGroup?.get('data') as UntypedFormGroup,
						rootPath: `${rootPath}data.`,
						validationErrorModel: validationErrorModel
					});
					break;
				case FieldType.RADIO_BOX:
					FieldRadioBoxDataEditorModel.reapplyRadioBoxValidators({
						formGroup: formGroup?.get('data') as UntypedFormGroup,
						rootPath: `${rootPath}data.`,
						validationErrorModel: validationErrorModel
					});
					break;
				case FieldType.SELECT:
					FieldSelectDataEditorModel.reapplySelectValidators({
						formGroup: formGroup?.get('data') as UntypedFormGroup,
						rootPath: `${rootPath}data.`,
						validationErrorModel: validationErrorModel
					});
					break;
				case FieldType.REFERENCE_TYPES:
					FieldReferenceTypeDataEditorModel.reapplyValidators({
						formGroup: formGroup?.get('data') as UntypedFormGroup,
						rootPath: `${rootPath}data.`,
						validationErrorModel: validationErrorModel
					});
					break;
                case FieldType.NUMBER:
                    FieldNumberDataEditorModel.reapplyValidators({
						formGroup: formGroup?.get('data') as UntypedFormGroup,
						rootPath: `${rootPath}data.`,
						validationErrorModel: validationErrorModel
					});
                    break;
                case FieldType.SYSTEM:
                    FieldSystemDataEditorModel.reapplyValidators({
						formGroup: formGroup?.get('data') as UntypedFormGroup,
						rootPath: `${rootPath}data.`,
						validationErrorModel: validationErrorModel
					});
                    break;

			}
		}

		if (formGroup?.get('defaultValue') != undefined) {
			DefaultValueEditorModel.reapplyValidators({
				formGroup: formGroup?.get('defaultValue') as UntypedFormGroup,
				rootPath: `${rootPath}defaultValue.`,
				validationErrorModel: validationErrorModel
			});
		}

		(formGroup.get('visibilityRules') as FormArray).controls?.forEach(
			(control, index) => RuleEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}visibilityRules[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);
	}

    static getSystemFieldControl(field: BaseField, formGroup: FormGroup<PlanEditorForm> | FormGroup<DescriptionEditorForm>): AbstractControl {
        switch((field.data as FieldSystemData).systemFieldType){
            case SystemFieldType.PlanTitle: 
            case SystemFieldType.PlanDescriptionTitle: 
                return (formGroup.controls.label);
            case SystemFieldType.PlanDescription:
            case SystemFieldType.PlanDescriptionDescription:
                return (formGroup.controls.description);
            case SystemFieldType.PlanLanguage: 
                return ((formGroup as FormGroup<PlanEditorForm>).controls.language);
            case SystemFieldType.PlanDescriptionTag:
                return ((formGroup as FormGroup<DescriptionEditorForm>).controls.tags);
            case SystemFieldType.PlanContact:
                return ((formGroup as FormGroup<PlanEditorForm>).controls.properties.controls.contacts);
            case SystemFieldType.PlanAccessRights:
                return ((formGroup as FormGroup<PlanEditorForm>).controls.accessType);
        }
    }


}

export interface DefaultValueForm {
    target: FormControl<string>;
	textValue: FormControl<string>;
	dateValue: FormControl<Date>;
	booleanValue: FormControl<boolean>;
	numberValue: FormControl<number>;
}
export class DefaultValueEditorModel implements DefaultValuePersist {
	target: string;
	textValue: string;
	dateValue: Date;
	booleanValue: boolean;
    numberValue: number;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: FieldDefaultValue | DefaultValueEditorModel): DefaultValueEditorModel {
		if (item) {
			this.textValue = item.textValue;
			this.dateValue = item.dateValue;
			this.booleanValue = item.booleanValue;
			this.numberValue = item.numberValue;
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
			context = DefaultValueEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			textValue: [{ value: this.textValue, disabled: disabled }, context.getValidation('textValue').validators],
			dateValue: [{ value: this.dateValue, disabled: disabled }, context.getValidation('dateValue').validators],
			booleanValue: [{ value: this.booleanValue, disabled: disabled }, context.getValidation('booleanValue').validators],
			numberValue: [{ value: this.numberValue, disabled: disabled }, context.getValidation('numberValue').validators]
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'textValue', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}textValue`)] });
		baseValidationArray.push({ key: 'dateValue', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}dateValue`)] });
		baseValidationArray.push({ key: 'booleanValue', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}booleanValue`)] });
		baseValidationArray.push({ key: 'numberValue', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}numberValue`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = DefaultValueEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['target', 'textValue', 'dateValue', 'booleanValue', 'numberValue'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}

export interface RuleForm {
    target: FormControl<string>;
	textValue: FormControl<string>;
	dateValue: FormControl<Date>;
	booleanValue: FormControl<boolean>;
    numberValue: FormControl<number>;
}

export class RuleEditorModel implements RulePersist {
	target: string;
	textValue: string;
	dateValue: Date;
	booleanValue: boolean;
    numberValue: number;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: FieldRule | RuleEditorModel): RuleEditorModel {
		if (item) {
			this.target = item.target;
			this.textValue = item.textValue;
			this.dateValue = item.dateValue;
			this.booleanValue = item.booleanValue;
			this.numberValue = item.numberValue;
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
			context = RuleEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			target: [{ value: this.target, disabled: disabled }, context.getValidation('target').validators],
			textValue: [{ value: this.textValue, disabled: disabled }, context.getValidation('textValue').validators],
			dateValue: [{ value: this.dateValue, disabled: disabled }, context.getValidation('dateValue').validators],
			booleanValue: [{ value: this.booleanValue, disabled: disabled }, context.getValidation('booleanValue').validators],
			numberValue: [{ value: this.numberValue, disabled: disabled }, context.getValidation('numberValue').validators]
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'target', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}target`)] });
		baseValidationArray.push({ key: 'textValue', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}textValue`)] });
		baseValidationArray.push({ key: 'dateValue', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}dateValue`)] });
		baseValidationArray.push({ key: 'booleanValue', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}booleanValue`)] });
		baseValidationArray.push({ key: 'numberValue', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}numberValue`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = RuleEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['target', 'textValue', 'dateValue', 'booleanValue', 'numberValue'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}

export interface DescriptionTemplateLabelDataForm {
	label: FormControl<string>;
	fieldType: FormControl<FieldType>;
}
export class LabelDataEditorModel implements FieldLabelDataPersist {
	label: string;
	fieldType: FieldType;
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: FieldLabelData): LabelDataEditorModel {
		if (item) {
			this.label = item.label;
			this.fieldType = item.fieldType;
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
			context = LabelDataEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}
		return this.formBuilder.group({
			label: [{ value: this.label, disabled: disabled }, context.getValidation('label').validators],
			fieldType: [{ value: this.fieldType, disabled: disabled }, context.getValidation('fieldType').validators],
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'label', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}label`)] });
		baseValidationArray.push({ key: 'fieldType', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}fieldType`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyBaseFieldValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = LabelDataEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['label', 'fieldType'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}

export class FieldLabelAndMultiplicityDataEditorModel extends LabelDataEditorModel implements FieldLabelAndMultiplicityDataPersist {
	multipleSelect: boolean = false;
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { super(validationErrorModel); }

	fromModel(item: FieldLabelAndMultiplicityDataPersist): FieldLabelAndMultiplicityDataEditorModel {
		if (item) {
			super.fromModel(item);
			this.multipleSelect = item.multipleSelect ?? false;
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
			context = FieldLabelAndMultiplicityDataEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		const formGroup = super.buildForm({ context, disabled, rootPath });
		formGroup.setControl('multipleSelect', new FormControl({ value: this.multipleSelect, disabled: disabled }, context.getValidation('multipleSelect').validators));
		return formGroup;
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;
		const baseContext: ValidationContext = super.createValidationContext({ rootPath, validationErrorModel });
		baseContext.validation.push({ key: 'multipleSelect', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}multipleSelect`)] });
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = FieldLabelAndMultiplicityDataEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['multipleSelect'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}

//
//
// External Dataset Field
//
//
export class FieldExternalDatasetDataEditorModel extends FieldLabelAndMultiplicityDataEditorModel implements FieldExternalDatasetDataPersist {
	type: FieldDataExternalDatasetType;
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { super(validationErrorModel); }

	fromModel(item: FieldExternalDatasetDataPersist): FieldExternalDatasetDataEditorModel {
		if (item) {
			super.fromModel(item);
			this.type = item.type;
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
			context = FieldExternalDatasetDataEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		const formGroup = super.buildForm({ context, disabled, rootPath });
		formGroup.setControl('type', new FormControl({ value: this.type, disabled: disabled }, context.getValidation('type').validators));
		return formGroup;
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;
		const baseContext: ValidationContext = super.createValidationContext({ rootPath, validationErrorModel });
		baseContext.validation.push({ key: 'type', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}type`)] });
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = FieldExternalDatasetDataEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['type'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}

//
//
// Reference Types
//
//
export class FieldReferenceTypeDataEditorModel extends FieldLabelAndMultiplicityDataEditorModel implements FieldReferenceTypePersist {
	referenceTypeId: Guid;
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { super(validationErrorModel); }

	fromModel(item: FieldReferenceTypeData): FieldReferenceTypeDataEditorModel {
		if (item) {
			super.fromModel(item);
			// a copied or moved input comes from the form, which holds the id, not the loaded reference type
			this.referenceTypeId = item.referenceType?.id ?? (item as unknown as FieldReferenceTypePersist).referenceTypeId;
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
			context = FieldReferenceTypeDataEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		const formGroup = super.buildForm({ context, disabled, rootPath });
		formGroup.setControl('referenceTypeId', new FormControl({ value: this.referenceTypeId, disabled: disabled }, context.getValidation('referenceTypeId').validators));
		return formGroup;
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;
		const baseContext: ValidationContext = super.createValidationContext({ rootPath, validationErrorModel });
		baseContext.validation.push({ key: 'referenceTypeId', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}referenceTypeId`)] });
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = FieldReferenceTypeDataEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['referenceTypeId'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}

//
//
// Radiobox Field
//
//
export class FieldRadioBoxDataEditorModel extends LabelDataEditorModel implements FieldRadioBoxDataPersist {
	options: FieldRadioBoxOptionEditorModel[] = [];
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { super(validationErrorModel); }

	fromModel(item: FieldRadioBoxDataPersist): FieldRadioBoxDataEditorModel {
		if (item) {
			super.fromModel(item);
			if (item.options) { item.options.map(x => this.options.push(new FieldRadioBoxOptionEditorModel(this.validationErrorModel).fromModel(x))); }
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
			context = FieldRadioBoxDataEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		const formGroup = super.buildForm({ context, disabled, rootPath });
		formGroup.setControl('options', this.formBuilder.array(
			(this.options ?? []).map(
				(item, index) => item.buildForm({
					rootPath: `${rootPath}options[${index}].`,
					disabled
				})
			), context.getValidation('options').validators
		));
		return formGroup;
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;
		const baseContext: ValidationContext = super.createValidationContext({ rootPath, validationErrorModel });
		baseContext.validation.push({ key: 'options', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}options`)] });
		return baseContext;
	}

	static reapplyRadioBoxValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {
		const { validationErrorModel, rootPath, formGroup } = params;
		(formGroup.get('options') as FormArray).controls?.forEach(
			(control, index) => FieldRadioBoxOptionEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}options[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);
	}
}

export class FieldRadioBoxOptionEditorModel implements FieldRadioBoxOptionPersist {
	label: string;
	value: string;
	/** The key values choosing the option stands for. */
	semantics: SemanticValueEditorModel[] = [];
	/** Choosing the option means nothing is exported for the question's object. */
	exportsNothing: boolean = false;
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: FieldRadioBoxOptionPersist): FieldRadioBoxOptionEditorModel {
		if (item) {
			this.label = item.label;
			this.value = item.value;
			this.exportsNothing = item.exportsNothing ?? false;
			this.semantics = (item.semantics ?? []).map(x => new SemanticValueEditorModel(this.validationErrorModel).fromModel(x));
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
			context = FieldRadioBoxOptionEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			label: [{ value: this.label, disabled: disabled }, context.getValidation('label').validators],
			value: [{ value: this.value, disabled: disabled }, context.getValidation('value').validators],
			exportsNothing: [{ value: this.exportsNothing ?? false, disabled: disabled }, context.getValidation('exportsNothing').validators],
			semantics: this.formBuilder.array(
				(this.semantics ?? []).map((item, index) => item.buildForm({ rootPath: `${rootPath}semantics[${index}].`, disabled })),
				context.getValidation('semantics').validators
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
		baseValidationArray.push({ key: 'label', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}label`)] });
		baseValidationArray.push({ key: 'value', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}value`)] });
		baseValidationArray.push({ key: 'exportsNothing', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}exportsNothing`)] });
		baseValidationArray.push({ key: 'semantics', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}semantics`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = FieldRadioBoxOptionEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['label', 'value', 'exportsNothing', 'semantics'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});

		(formGroup?.get('semantics') as FormArray)?.controls?.forEach(
			(control, index) => SemanticValueEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}semantics[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);
	}
}

//
//
// Select Field
//
//

export class FieldSelectDataEditorModel extends LabelDataEditorModel implements FieldSelectDataPersist {
	options: FieldSelectOptionEditorModel[] = [];
	multipleSelect: boolean = false;
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { super(validationErrorModel); }

	fromModel(item: FieldSelectDataPersist): FieldSelectDataEditorModel {
		if (item) {
			super.fromModel(item);
			this.multipleSelect = item.multipleSelect ?? false;
			if (item.options) { item.options.map(x => this.options.push(new FieldSelectOptionEditorModel(this.validationErrorModel).fromModel(x))); }
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
			context = FieldSelectDataEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		const formGroup = super.buildForm({ context, disabled, rootPath });
		formGroup.setControl('multipleSelect', new FormControl({ value: this.multipleSelect, disabled: disabled }, context.getValidation('multipleSelect').validators));
		formGroup.setControl('options', this.formBuilder.array(
			(this.options ?? []).map(
				(item, index) => item.buildForm({
					rootPath: `${rootPath}options[${index}].`,
					disabled
				})
			), context.getValidation('options').validators
		));
		return formGroup;
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;
		const baseContext: ValidationContext = super.createValidationContext({ rootPath, validationErrorModel });
		baseContext.validation.push({ key: 'options', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}options`)] });
		baseContext.validation.push({ key: 'multipleSelect', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}multipleSelect`)] });
		return baseContext;
	}

	static reapplySelectValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = FieldSelectDataEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['multipleSelect'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});

		(formGroup.get('options') as FormArray).controls?.forEach(
			(control, index) => FieldSelectOptionEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}options[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);
	}
}

export class FieldSelectOptionEditorModel implements FieldSelectOptionPersist {
	label: string;
	value: string;
	/** The key values choosing the option stands for. */
	semantics: SemanticValueEditorModel[] = [];
	/** Choosing the option means nothing is exported for the question's object. */
	exportsNothing: boolean = false;
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: FieldSelectOptionPersist): FieldSelectOptionEditorModel {
		if (item) {
			this.label = item.label;
			this.value = item.value;
			this.exportsNothing = item.exportsNothing ?? false;
			this.semantics = (item.semantics ?? []).map(x => new SemanticValueEditorModel(this.validationErrorModel).fromModel(x));
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
			context = FieldSelectOptionEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			label: [{ value: this.label, disabled: disabled }, context.getValidation('label').validators],
			value: [{ value: this.value, disabled: disabled }, context.getValidation('value').validators],
			exportsNothing: [{ value: this.exportsNothing ?? false, disabled: disabled }, context.getValidation('exportsNothing').validators],
			semantics: this.formBuilder.array(
				(this.semantics ?? []).map((item, index) => item.buildForm({ rootPath: `${rootPath}semantics[${index}].`, disabled })),
				context.getValidation('semantics').validators
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
		baseValidationArray.push({ key: 'label', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}label`)] });
		baseValidationArray.push({ key: 'value', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}value`)] });
		baseValidationArray.push({ key: 'exportsNothing', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}exportsNothing`)] });
		baseValidationArray.push({ key: 'semantics', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}semantics`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = FieldSelectOptionEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['label', 'value', 'exportsNothing', 'semantics'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});

		(formGroup?.get('semantics') as FormArray)?.controls?.forEach(
			(control, index) => SemanticValueEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}semantics[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);
	}
}


/** A semantic key and the value an answer option stands for; the key is required, the value is optional. */
export class SemanticValueEditorModel implements SemanticValuePersist {
	key: string;
	value: string;
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: SemanticValuePersist): SemanticValueEditorModel {
		if (item) {
			this.key = item.key;
			this.value = item.value;
		}
		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string
	}): UntypedFormGroup {
		const { disabled = false, rootPath } = params ?? {};
		let context = params?.context ?? null;
		if (context == null) {
			context = SemanticValueEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			key: [{ value: this.key, disabled: disabled }, context.getValidation('key').validators],
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
		baseValidationArray.push({ key: 'key', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}key`)] });
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
		const context = SemanticValueEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['key', 'value'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});
	}
}

export interface FieldUploadDataForm {
    types: FormArray<LabelValueForm>;
	maxFileSizeInMB: FormControl<number>;
}
//
//
// Upload Field
//
//
export class FieldUploadDataEditorModel extends LabelDataEditorModel implements FieldUploadDataPersist {
	types: FieldUploadOptionEditorModel[] = [];
	maxFileSizeInMB: number;
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { super(validationErrorModel); }

	fromModel(item: FieldUploadDataPersist): FieldUploadDataEditorModel {
		if (item) {
			super.fromModel(item);
			this.maxFileSizeInMB = item.maxFileSizeInMB;
			if (item.types) { item.types.map(x => this.types.push(new FieldUploadOptionEditorModel(this.validationErrorModel).fromModel(x))); }
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
			context = FieldUploadDataEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		const formGroup = super.buildForm({ context, disabled, rootPath });
		formGroup.setControl('maxFileSizeInMB', new FormControl({ value: this.maxFileSizeInMB, disabled: disabled }, context.getValidation('maxFileSizeInMB').validators));
		formGroup.setControl('types', this.formBuilder.array(
			(this.types ?? []).map(
				(item, index) => item.buildForm({
					rootPath: `${rootPath}types[${index}].`,
					disabled
				})
			), context.getValidation('types').validators
		));
		return formGroup;
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;
		const baseContext: ValidationContext = super.createValidationContext({ rootPath, validationErrorModel });
		baseContext.validation.push({ key: 'types', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}types`)] });
		baseContext.validation.push({ key: 'maxFileSizeInMB', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}maxFileSizeInMB`)] });
		return baseContext;
	}

	static reapplyUploadDataValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { validationErrorModel, rootPath, formGroup } = params;

		const context = FieldUploadDataEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		(formGroup.get('types') as FormArray).controls?.forEach(
			(control, index) => FieldUploadOptionEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}types[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);

		['maxFileSizeInMB'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});
	}
}


export class FieldUploadOptionEditorModel implements FieldUploadOptionPersist {
	label: string;
	value: string;
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: FieldUploadOptionPersist): FieldUploadOptionEditorModel {
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
			context = FieldUploadOptionEditorModel.createValidationContext({
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
		const context = FieldUploadOptionEditorModel.createValidationContext({
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

export class FieldNumberDataEditorModel extends LabelDataEditorModel implements FieldNumberDataPersist {
    protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { super(validationErrorModel); }

	fromModel(item: FieldNumberDataPersist): FieldNumberDataEditorModel {
		if (item) {
			super.fromModel(item);
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
			context = FieldNumberDataEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		const formGroup = super.buildForm({ context, disabled, rootPath });
		return formGroup;
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;
		const baseContext: ValidationContext = super.createValidationContext({ rootPath, validationErrorModel });
		baseContext.validation.push({ key: 'multipleSelect', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}multipleSelect`)] });
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = FieldNumberDataEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['multipleSelect'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}
export class FieldSystemDataEditorModel extends LabelDataEditorModel implements FieldSystemDataPersist {
    systemFieldType: SystemFieldType;
    protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { super(validationErrorModel); }

	fromModel(item: FieldSystemDataPersist): FieldSystemDataEditorModel {
		if (item) {
			super.fromModel(item);
			this.systemFieldType = item.systemFieldType;
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
			context = FieldSystemDataEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		const formGroup = super.buildForm({ context, disabled, rootPath });
		formGroup.setControl('systemFieldType', new FormControl({ value: this.systemFieldType, disabled: disabled }, context.getValidation('systemFieldType').validators));
		return formGroup;
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;
		const baseContext: ValidationContext = super.createValidationContext({ rootPath, validationErrorModel });
		baseContext.validation.push({ key: 'systemFieldType', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}systemFieldType`)] });
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = FieldSystemDataEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['systemFieldType'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}
export interface LabelValueForm extends FormGroup<{
    label: FormControl<string>;
	value: FormControl<string>;
}>{}