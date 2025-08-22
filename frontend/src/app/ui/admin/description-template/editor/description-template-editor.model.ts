import { FormArray, FormControl, FormGroup, UntypedFormArray, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { DescriptionTemplateFieldDataExternalDatasetType } from "@app/core/common/enum/description-template-field-data-external-dataset-type";
import { DescriptionTemplateFieldType } from "@app/core/common/enum/description-template-field-type";
import { DescriptionTemplateFieldValidationType } from "@app/core/common/enum/description-template-field-validation-type";
import { DescriptionTemplateStatus } from "@app/core/common/enum/description-template-status";
import { IsActive } from "@app/core/common/enum/is-active.enum";
import { UserDescriptionTemplateRole } from "@app/core/common/enum/user-description-template-role";
import { DescriptionTemplate, DescriptionTemplateDefaultValue, DescriptionTemplateDefinition, DescriptionTemplateField, DescriptionTemplateFieldSet, DescriptionTemplateLabelData, DescriptionTemplateMultiplicity, DescriptionTemplatePage, DescriptionTemplateReferenceTypeData, DescriptionTemplateRule, DescriptionTemplateSection, UserDescriptionTemplate } from "@app/core/model/description-template/description-template";
import { DescriptionTemplateDefaultValuePersist, DescriptionTemplateDefinitionPersist, DescriptionTemplateExternalDatasetDataPersist, DescriptionTemplateFieldPersist, DescriptionTemplateFieldSetPersist, DescriptionTemplateLabelAndMultiplicityDataPersist, DescriptionTemplateLabelDataPersist, DescriptionTemplateMultiplicityPersist, DescriptionTemplatePagePersist, DescriptionTemplatePersist, DescriptionTemplateRadioBoxDataPersist, DescriptionTemplateRadioBoxOptionPersist, DescriptionTemplateReferenceTypeFieldPersist, DescriptionTemplateRulePersist, DescriptionTemplateSectionPersist, DescriptionTemplateSelectDataPersist, DescriptionTemplateSelectOptionPersist, DescriptionTemplateUploadDataPersist, DescriptionTemplateUploadOptionPersist, UserDescriptionTemplatePersist } from "@app/core/model/description-template/description-template-persist";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";
import { Guid } from "@common/types/guid";
import { EditorCustomValidators } from "./custom-validators/editor-custom-validators";
import { PluginRepositoryConfiguration } from "@app/core/model/plugin-configuration/plugin-configuration";
import { PluginConfigurationEditorModel } from "@app/ui/plugin/plugin-editor.model";
import { PluginType } from "@app/core/common/enum/plugin-type";

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
		DescriptionTemplateDefinitionEditorModel.reapplyPagesValidators({
			formArray: control.get('pages') as UntypedFormArray,
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
    pages: FormArray<FormGroup<DescriptionTemplatePageForm>>;
	pluginConfigurations?: FormArray<FormGroup<PluginConfigurationForm>>;
}

export interface PluginConfigurationForm{
    pluginCode: FormControl<string>;
	pluginType: FormControl<PluginType>;
    fields: FormArray<FormGroup<PluginConfigurationFieldForm>>;
}

export interface PluginConfigurationFieldForm{
    code: FormControl<string>;
	fileValue: FormControl<Guid>;
    textValue: FormControl<string>;
}

export class DescriptionTemplateDefinitionEditorModel implements DescriptionTemplateDefinitionPersist {
	pages: DescriptionTemplatePageEditorModel[] = [];
	pluginConfigurations: PluginConfigurationEditorModel[] = [];

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: DescriptionTemplateDefinition, configuration?: PluginRepositoryConfiguration[]): DescriptionTemplateDefinitionEditorModel {
		if (item) {
			if (item.pages) { item.pages.map(x => this.pages.push(new DescriptionTemplatePageEditorModel(this.validationErrorModel).fromModel(x))); }
			if (item.pluginConfigurations) { item.pluginConfigurations.map(x => this.pluginConfigurations.push(new PluginConfigurationEditorModel(this.validationErrorModel).fromModel(x, configuration))); }
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
			pages: this.formBuilder.array(
				(this.pages ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}pages[${index}].`,
						disabled
					})
				), context.getValidation('pages').validators
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
		baseValidationArray.push({ key: 'pages', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}pages`)] });
		baseValidationArray.push({ key: 'pluginConfigurations', validators: [ BackendErrorValidator(validationErrorModel, `${rootPath}pluginConfigurations`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyPagesValidators(params: {
		formArray: UntypedFormArray,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {
		const { validationErrorModel, rootPath, formArray } = params;
		formArray?.controls?.forEach(
			(control, index) => DescriptionTemplatePageEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}pages[${index}].`,
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

export interface DescriptionTemplatePageForm{
    id: FormControl<string>;
    ordinal: FormControl<number>;
    title: FormControl<string>;
    sections: FormArray<FormGroup<DescriptionTemplateSectionForm>>;
}

export class DescriptionTemplatePageEditorModel implements DescriptionTemplatePagePersist {
	id: string;
	ordinal: number;
	title: string;
	sections: DescriptionTemplateSectionEditorModel[] = [];

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: DescriptionTemplatePage): DescriptionTemplatePageEditorModel {
		if (item) {
			this.id = item.id;
			this.ordinal = item.ordinal;
			this.title = item.title;
			if (item.sections) { item.sections.map(x => this.sections.push(new DescriptionTemplateSectionEditorModel(this.validationErrorModel).fromModel(x))); }
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
			context = DescriptionTemplatePageEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			ordinal: [{ value: this.ordinal, disabled: disabled }, context.getValidation('ordinal').validators],
			title: [{ value: this.title, disabled: disabled }, context.getValidation('title').validators],
			sections: this.formBuilder.array(
				(this.sections ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}sections[${index}].`,
						disabled
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
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}id`)] });
		baseValidationArray.push({ key: 'ordinal', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}ordinal`)] });
		baseValidationArray.push({ key: 'title', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}title`)] });
		baseValidationArray.push({ key: 'sections', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}sections`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplySectionsValidators(params: {
		formArray: UntypedFormArray,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {
		const { validationErrorModel, rootPath, formArray } = params;

	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = DescriptionTemplatePageEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		(formGroup.get('sections') as FormArray)?.controls?.forEach(
			(control, index) => DescriptionTemplateSectionEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}sections[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);

		['id', 'ordinal', 'title'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}

}

export interface DescriptionTemplateSectionForm {
    id: FormControl<string>;
	ordinal: FormControl<number>;
	title: FormControl<string>;
	description: FormControl<string>;
	sections: FormArray<FormGroup<DescriptionTemplateSectionForm>>;
	fieldSets: FormArray<FormGroup<DescriptionTemplateFieldSetForm>>;
}
export class DescriptionTemplateSectionEditorModel implements DescriptionTemplateSectionPersist {
	id: string;
	ordinal: number;
	title: string;
	description: string;
	sections: DescriptionTemplateSectionEditorModel[] = [];
	fieldSets: DescriptionTemplateFieldSetEditorModel[] = [];

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: DescriptionTemplateSection | DescriptionTemplateSectionEditorModel): DescriptionTemplateSectionEditorModel {
		if (item) {
			this.id = item.id;
			this.ordinal = item.ordinal;
			this.title = item.title;
			this.description = item.description;
			if (item.sections) { item.sections.map(x => this.sections.push(new DescriptionTemplateSectionEditorModel(this.validationErrorModel).fromModel(x))); }
			if (item.fieldSets) { item.fieldSets.map(x => this.fieldSets.push(new DescriptionTemplateFieldSetEditorModel(this.validationErrorModel).fromModel(x))); }
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
			context = DescriptionTemplateSectionEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		const formGroup= this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			ordinal: [{ value: this.ordinal, disabled: disabled }, context.getValidation('ordinal').validators],
			title: [{ value: this.title, disabled: disabled }, context.getValidation('title').validators],
			description: [{ value: this.description, disabled: disabled }, context.getValidation('description').validators],
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

		formGroup.setValidators(EditorCustomValidators.sectionHasAtLeastOneChildOf('fieldSets','sections'));

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
		const context = DescriptionTemplateSectionEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['id', 'ordinal', 'page', 'title', 'description'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});

		(formGroup.get('sections') as FormArray).controls?.forEach(
			(control, index) => DescriptionTemplateSectionEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}sections[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);

		(formGroup.get('fieldSets') as FormArray).controls?.forEach(
			(control, index) => DescriptionTemplateFieldSetEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}fieldSets[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);
	}

}

export interface DescriptionTemplateFieldSetForm {
    id: FormControl<string>;
	ordinal: FormControl<number>;
	title: FormControl<string>;
	description: FormControl<string>;
	extendedDescription: FormControl<string>;
	additionalInformation: FormControl<string>;
	multiplicity: FormGroup<DescriptionTemplateMultiplicityForm>;
	hasMultiplicity: FormControl<boolean>;
	hasCommentField: FormControl<boolean>;
	fields: FormArray<FormGroup<DescriptionTemplateFieldForm>>;
}
export class DescriptionTemplateFieldSetEditorModel implements DescriptionTemplateFieldSetPersist {
	id: string;
	ordinal: number;
	title: string;
	description: string;
	extendedDescription: string;
	additionalInformation: string;
	multiplicity: DescriptionTemplateMultiplicityEditorModel = new DescriptionTemplateMultiplicityEditorModel(this.validationErrorModel);
	hasMultiplicity: boolean = false;
	hasCommentField: boolean = false;
	fields: DescriptionTemplateFieldEditorModel[] = [];

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: DescriptionTemplateFieldSet | DescriptionTemplateFieldSetEditorModel): DescriptionTemplateFieldSetEditorModel {
		if (item) {
			this.id = item.id;
			this.ordinal = item.ordinal;
			this.title = item.title;
			this.description = item.description;
			this.extendedDescription = item.extendedDescription;
			this.additionalInformation = item.additionalInformation;
			this.hasCommentField = item.hasCommentField;
			this.hasMultiplicity = item.hasMultiplicity;

			this.multiplicity = new DescriptionTemplateMultiplicityEditorModel(this.validationErrorModel).fromModel(item.multiplicity);
			if (item.fields) { item.fields.map(x => this.fields.push(new DescriptionTemplateFieldEditorModel(this.validationErrorModel).fromModel(x))); }
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
			context = DescriptionTemplateFieldSetEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			ordinal: [{ value: this.ordinal, disabled: disabled }, context.getValidation('ordinal').validators],
			title: [{ value: this.title, disabled: disabled }, context.getValidation('title').validators],
			description: [{ value: this.description, disabled: disabled }, context.getValidation('description').validators],
			extendedDescription: [{ value: this.extendedDescription, disabled: disabled }, context.getValidation('extendedDescription').validators],
			additionalInformation: [{ value: this.additionalInformation, disabled: disabled }, context.getValidation('additionalInformation').validators],
			hasCommentField: [{ value: this.hasCommentField, disabled: disabled }, context.getValidation('hasCommentField').validators],
			hasMultiplicity: [{ value: this.hasMultiplicity, disabled: disabled }, context.getValidation('hasMultiplicity').validators],
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

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = DescriptionTemplateFieldSetEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['id', 'ordinal', 'title', 'description', 'extendedDescription', 'additionalInformation', 'hasCommentField', 'hasMultiplicity'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});

		DescriptionTemplateMultiplicityEditorModel.reapplyValidators({
			formGroup: formGroup?.get('multiplicity') as UntypedFormGroup,
			rootPath: `${rootPath}multiplicity.`,
			validationErrorModel: validationErrorModel
		});

		(formGroup.get('fields') as FormArray).controls?.forEach(
			(control, index) => DescriptionTemplateFieldEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}fields[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);
	}
}

export interface DescriptionTemplateMultiplicityForm {
    min: FormControl<number>;
	max: FormControl<number>;
	placeholder: FormControl<string>;
	tableView: FormControl<boolean>;
}
export class DescriptionTemplateMultiplicityEditorModel implements DescriptionTemplateMultiplicityPersist {
	min: number;
	max: number;
	placeholder: string;
	tableView: boolean;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: DescriptionTemplateMultiplicity): DescriptionTemplateMultiplicityEditorModel {
		if (item) {
			this.min = item.min;
			this.max = item.max;
			this.placeholder = item.placeholder;
			this.tableView = item.tableView;
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
			context = DescriptionTemplateMultiplicityEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			min: [{ value: this.min, disabled: disabled }, context.getValidation('min').validators],
			max: [{ value: this.max, disabled: disabled }, context.getValidation('max').validators],
			placeholder: [{ value: this.placeholder, disabled: disabled }, context.getValidation('placeholder').validators],
			tableView: [{ value: this.tableView, disabled: disabled }, context.getValidation('tableView').validators],
		});
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

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = DescriptionTemplateMultiplicityEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['min', 'max', 'placeholder', 'tableView'].forEach(keyField => {
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

export interface DescriptionTemplateFieldForm {
    id: FormControl<string>;
	ordinal: FormControl<number>;
	semantics: FormControl<string[]>;
	defaultValue: FormGroup<DescriptionTemplateDefaultValueForm>;
	visibilityRules: FormArray<FormGroup<DescriptionTemplateRuleForm>>;
	validations: FormControl<DescriptionTemplateFieldValidationType[]>;
	includeInExport: FormControl<boolean>;
	data: FormGroup<DescriptionTemplateLabelDataForm>;
} 
export class DescriptionTemplateFieldEditorModel implements DescriptionTemplateFieldPersist {
	id: string;
	ordinal: number;
	semantics: string[];
	defaultValue: DescriptionTemplateDefaultValueEditorModel = new DescriptionTemplateDefaultValueEditorModel(this.validationErrorModel);
	visibilityRules: DescriptionTemplateRuleEditorModel[] = [];
	validations: DescriptionTemplateFieldValidationType[] = [];
	includeInExport: boolean = true;
	data: DescriptionTemplateLabelDataEditorModel = new DescriptionTemplateLabelDataEditorModel(this.validationErrorModel);

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) {
	}

	fromModel(item: DescriptionTemplateField | DescriptionTemplateFieldEditorModel): DescriptionTemplateFieldEditorModel {
		if (item) {
			this.id = item.id;
			this.ordinal = item.ordinal;
			this.semantics = item.semantics;
			this.defaultValue = new DescriptionTemplateDefaultValueEditorModel(this.validationErrorModel).fromModel(item.defaultValue);
			this.validations = item.validations;
			this.includeInExport = item.includeInExport;

			this.data = this.getFieldEditorModel(item.data.fieldType).fromModel(item.data);
			if (item.visibilityRules) { item.visibilityRules.map(x => this.visibilityRules.push(new DescriptionTemplateRuleEditorModel(this.validationErrorModel).fromModel(x))); }
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
			context = DescriptionTemplateFieldEditorModel.createValidationContext({
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
			data: this.data.buildForm({
				rootPath: `${rootPath}data.`,
				disabled
			}),
			defaultValue: this.defaultValue.buildForm({
				rootPath: `${rootPath}defaultValue.`,
				disabled
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

	private getFieldEditorModel(fieldType: DescriptionTemplateFieldType): DescriptionTemplateLabelDataEditorModel {
		switch (fieldType) {
			case DescriptionTemplateFieldType.REFERENCE_TYPES:
				return new DescriptionTemplateReferenceTypeDataEditorModel(this.validationErrorModel);
			case DescriptionTemplateFieldType.RADIO_BOX:
				return new DescriptionTemplateRadioBoxDataEditorModel(this.validationErrorModel);
			case DescriptionTemplateFieldType.SELECT:
				return new DescriptionTemplateSelectDataEditorModel(this.validationErrorModel);
			case DescriptionTemplateFieldType.BOOLEAN_DECISION:
			case DescriptionTemplateFieldType.CHECK_BOX:
			case DescriptionTemplateFieldType.FREE_TEXT:
			case DescriptionTemplateFieldType.TEXT_AREA:
			case DescriptionTemplateFieldType.RICH_TEXT_AREA:
			case DescriptionTemplateFieldType.DATE_PICKER:
			case DescriptionTemplateFieldType.TAGS:
			case DescriptionTemplateFieldType.DATASET_IDENTIFIER:
			case DescriptionTemplateFieldType.VALIDATION:
				return new DescriptionTemplateLabelDataEditorModel(this.validationErrorModel);
			case DescriptionTemplateFieldType.INTERNAL_ENTRIES_PLANS:
			case DescriptionTemplateFieldType.INTERNAL_ENTRIES_DESCRIPTIONS:
				return new DescriptionTemplateLabelAndMultiplicityDataEditorModel(this.validationErrorModel);
			case DescriptionTemplateFieldType.UPLOAD:
				return new DescriptionTemplateUploadDataEditorModel(this.validationErrorModel);
		}
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = DescriptionTemplateFieldEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['id', 'ordinal', 'semantics', 'defaultValue', 'validations', 'includeInExport'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});

		DescriptionTemplateLabelDataEditorModel.reapplyBaseFieldValidators({
			formGroup: formGroup?.get('data') as UntypedFormGroup,
			rootPath: `${rootPath}data.`,
			validationErrorModel: validationErrorModel
		});

		if (formGroup?.get('data').get('fieldType').value != undefined) {
			switch (formGroup?.get('data').get('fieldType').value) {
				case DescriptionTemplateFieldType.UPLOAD:
					DescriptionTemplateUploadDataEditorModel.reapplyUploadDataValidators({
						formGroup: formGroup?.get('data') as UntypedFormGroup,
						rootPath: `${rootPath}data.`,
						validationErrorModel: validationErrorModel
					});
					break;
				case DescriptionTemplateFieldType.RADIO_BOX:
					DescriptionTemplateRadioBoxDataEditorModel.reapplyRadioBoxValidators({
						formGroup: formGroup?.get('data') as UntypedFormGroup,
						rootPath: `${rootPath}data.`,
						validationErrorModel: validationErrorModel
					});
					break;
				case DescriptionTemplateFieldType.SELECT:
					DescriptionTemplateSelectDataEditorModel.reapplySelectValidators({
						formGroup: formGroup?.get('data') as UntypedFormGroup,
						rootPath: `${rootPath}data.`,
						validationErrorModel: validationErrorModel
					});
					break;
				case DescriptionTemplateFieldType.REFERENCE_TYPES:
					DescriptionTemplateReferenceTypeDataEditorModel.reapplyValidators({
						formGroup: formGroup?.get('data') as UntypedFormGroup,
						rootPath: `${rootPath}data.`,
						validationErrorModel: validationErrorModel
					});
					break;

			}
		}

		if (formGroup?.get('defaultValue') != undefined) {
			DescriptionTemplateDefaultValueEditorModel.reapplyValidators({
				formGroup: formGroup?.get('defaultValue') as UntypedFormGroup,
				rootPath: `${rootPath}defaultValue.`,
				validationErrorModel: validationErrorModel
			});
		}

		(formGroup.get('visibilityRules') as FormArray).controls?.forEach(
			(control, index) => DescriptionTemplateRuleEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}visibilityRules[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);
	}
}

export interface DescriptionTemplateDefaultValueForm {
    target: FormControl<string>;
	textValue: FormControl<string>;
	dateValue: FormControl<Date>;
	booleanValue: FormControl<boolean>;
}
export class DescriptionTemplateDefaultValueEditorModel implements DescriptionTemplateDefaultValuePersist {
	target: string;
	textValue: string;
	dateValue: Date;
	booleanValue: boolean;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: DescriptionTemplateDefaultValue | DescriptionTemplateDefaultValueEditorModel): DescriptionTemplateDefaultValueEditorModel {
		if (item) {
			this.textValue = item.textValue;
			this.dateValue = item.dateValue;
			this.booleanValue = item.booleanValue;
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
			context = DescriptionTemplateDefaultValueEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			textValue: [{ value: this.textValue, disabled: disabled }, context.getValidation('textValue').validators],
			dateValue: [{ value: this.dateValue, disabled: disabled }, context.getValidation('dateValue').validators],
			booleanValue: [{ value: this.booleanValue, disabled: disabled }, context.getValidation('booleanValue').validators]
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

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = DescriptionTemplateDefaultValueEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['target', 'textValue', 'dateValue', 'booleanValue'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}

export interface DescriptionTemplateRuleForm {
    target: FormControl<string>;
	textValue: FormControl<string>;
	dateValue: FormControl<Date>;
	booleanValue: FormControl<boolean>;
}

export class DescriptionTemplateRuleEditorModel implements DescriptionTemplateRulePersist {
	target: string;
	textValue: string;
	dateValue: Date;
	booleanValue: boolean;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: DescriptionTemplateRule | DescriptionTemplateRuleEditorModel): DescriptionTemplateRuleEditorModel {
		if (item) {
			this.target = item.target;
			this.textValue = item.textValue;
			this.dateValue = item.dateValue;
			this.booleanValue = item.booleanValue;
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
			context = DescriptionTemplateRuleEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			target: [{ value: this.target, disabled: disabled }, context.getValidation('target').validators],
			textValue: [{ value: this.textValue, disabled: disabled }, context.getValidation('textValue').validators],
			dateValue: [{ value: this.dateValue, disabled: disabled }, context.getValidation('dateValue').validators],
			booleanValue: [{ value: this.booleanValue, disabled: disabled }, context.getValidation('booleanValue').validators]
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

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = DescriptionTemplateRuleEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['target', 'textValue', 'dateValue', 'booleanValue'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}

export interface DescriptionTemplateLabelDataForm {
	label: FormControl<string>;
	fieldType: FormControl<DescriptionTemplateFieldType>;
}
export class DescriptionTemplateLabelDataEditorModel implements DescriptionTemplateLabelDataPersist {
	label: string;
	fieldType: DescriptionTemplateFieldType;
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: DescriptionTemplateLabelData): DescriptionTemplateLabelDataEditorModel {
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
			context = DescriptionTemplateLabelDataEditorModel.createValidationContext({
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
		const context = DescriptionTemplateLabelDataEditorModel.createValidationContext({
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

export class DescriptionTemplateLabelAndMultiplicityDataEditorModel extends DescriptionTemplateLabelDataEditorModel implements DescriptionTemplateLabelAndMultiplicityDataPersist {
	multipleSelect: boolean = false;
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { super(validationErrorModel); }

	fromModel(item: DescriptionTemplateLabelAndMultiplicityDataPersist): DescriptionTemplateLabelAndMultiplicityDataEditorModel {
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
			context = DescriptionTemplateLabelAndMultiplicityDataEditorModel.createValidationContext({
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
		const context = DescriptionTemplateLabelAndMultiplicityDataEditorModel.createValidationContext({
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
export class DescriptionTemplateExternalDatasetDataEditorModel extends DescriptionTemplateLabelAndMultiplicityDataEditorModel implements DescriptionTemplateExternalDatasetDataPersist {
	type: DescriptionTemplateFieldDataExternalDatasetType;
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { super(validationErrorModel); }

	fromModel(item: DescriptionTemplateExternalDatasetDataPersist): DescriptionTemplateExternalDatasetDataEditorModel {
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
			context = DescriptionTemplateExternalDatasetDataEditorModel.createValidationContext({
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
		const context = DescriptionTemplateExternalDatasetDataEditorModel.createValidationContext({
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
export class DescriptionTemplateReferenceTypeDataEditorModel extends DescriptionTemplateLabelAndMultiplicityDataEditorModel implements DescriptionTemplateReferenceTypeFieldPersist {
	referenceTypeId: Guid;
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { super(validationErrorModel); }

	fromModel(item: DescriptionTemplateReferenceTypeData): DescriptionTemplateReferenceTypeDataEditorModel {
		if (item) {
			super.fromModel(item);
			this.referenceTypeId = item.referenceType?.id;
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
			context = DescriptionTemplateReferenceTypeDataEditorModel.createValidationContext({
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
		const context = DescriptionTemplateReferenceTypeDataEditorModel.createValidationContext({
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
export class DescriptionTemplateRadioBoxDataEditorModel extends DescriptionTemplateLabelDataEditorModel implements DescriptionTemplateRadioBoxDataPersist {
	options: DescriptionTemplateRadioBoxOptionEditorModel[] = [];
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { super(validationErrorModel); }

	fromModel(item: DescriptionTemplateRadioBoxDataPersist): DescriptionTemplateRadioBoxDataEditorModel {
		if (item) {
			super.fromModel(item);
			if (item.options) { item.options.map(x => this.options.push(new DescriptionTemplateRadioBoxOptionEditorModel(this.validationErrorModel).fromModel(x))); }
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
			context = DescriptionTemplateRadioBoxDataEditorModel.createValidationContext({
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
			(control, index) => DescriptionTemplateRadioBoxOptionEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}options[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);
	}
}

export class DescriptionTemplateRadioBoxOptionEditorModel implements DescriptionTemplateRadioBoxOptionPersist {
	label: string;
	value: string;
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: DescriptionTemplateRadioBoxOptionPersist): DescriptionTemplateRadioBoxOptionEditorModel {
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
			context = DescriptionTemplateRadioBoxOptionEditorModel.createValidationContext({
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
		baseValidationArray.push({ key: 'label', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}label`)] });
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
		const context = DescriptionTemplateRadioBoxOptionEditorModel.createValidationContext({
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

//
//
// Select Field
//
//

export class DescriptionTemplateSelectDataEditorModel extends DescriptionTemplateLabelDataEditorModel implements DescriptionTemplateSelectDataPersist {
	options: DescriptionTemplateSelectOptionEditorModel[] = [];
	multipleSelect: boolean = false;
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { super(validationErrorModel); }

	fromModel(item: DescriptionTemplateSelectDataPersist): DescriptionTemplateSelectDataEditorModel {
		if (item) {
			super.fromModel(item);
			this.multipleSelect = item.multipleSelect ?? false;
			if (item.options) { item.options.map(x => this.options.push(new DescriptionTemplateSelectOptionEditorModel(this.validationErrorModel).fromModel(x))); }
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
			context = DescriptionTemplateSelectDataEditorModel.createValidationContext({
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
		const context = DescriptionTemplateSelectDataEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['multipleSelect'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});

		(formGroup.get('options') as FormArray).controls?.forEach(
			(control, index) => DescriptionTemplateSelectOptionEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}options[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);
	}
}

export class DescriptionTemplateSelectOptionEditorModel implements DescriptionTemplateSelectOptionPersist {
	label: string;
	value: string;
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: DescriptionTemplateSelectOptionPersist): DescriptionTemplateSelectOptionEditorModel {
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
			context = DescriptionTemplateSelectOptionEditorModel.createValidationContext({
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
		const context = DescriptionTemplateSelectOptionEditorModel.createValidationContext({
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


export interface DescriptionTemplateUploadDataForm {
    types: FormArray<LabelValueForm>;
	maxFileSizeInMB: FormControl<number>;
}
//
//
// Upload Field
//
//
export class DescriptionTemplateUploadDataEditorModel extends DescriptionTemplateLabelDataEditorModel implements DescriptionTemplateUploadDataPersist {
	types: DescriptionTemplateUploadOptionEditorModel[] = [];
	maxFileSizeInMB: number;
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { super(validationErrorModel); }

	fromModel(item: DescriptionTemplateUploadDataPersist): DescriptionTemplateUploadDataEditorModel {
		if (item) {
			super.fromModel(item);
			this.maxFileSizeInMB = item.maxFileSizeInMB;
			if (item.types) { item.types.map(x => this.types.push(new DescriptionTemplateUploadOptionEditorModel(this.validationErrorModel).fromModel(x))); }
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
			context = DescriptionTemplateUploadDataEditorModel.createValidationContext({
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

		const context = DescriptionTemplateUploadDataEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		(formGroup.get('types') as FormArray).controls?.forEach(
			(control, index) => DescriptionTemplateUploadOptionEditorModel.reapplyValidators({
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


export class DescriptionTemplateUploadOptionEditorModel implements DescriptionTemplateUploadOptionPersist {
	label: string;
	value: string;
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: DescriptionTemplateUploadOptionPersist): DescriptionTemplateUploadOptionEditorModel {
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
			context = DescriptionTemplateUploadOptionEditorModel.createValidationContext({
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
		baseValidationArray.push({ key: 'label', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}label`)] });
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
		const context = DescriptionTemplateUploadOptionEditorModel.createValidationContext({
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

export interface LabelValueForm extends FormGroup<{
    label: FormControl<string>;
	value: FormControl<string>;
}>{}