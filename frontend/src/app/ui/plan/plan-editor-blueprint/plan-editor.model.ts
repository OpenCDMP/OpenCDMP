import { FormArray, FormControl, FormGroup, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { PlanAccessType } from "@app/core/common/enum/plan-access-type";
import { PlanBlueprintFieldCategory } from "@app/core/common/enum/plan-blueprint-field-category";
import { PlanBlueprintSystemFieldType } from "@app/core/common/enum/plan-blueprint-system-field-type";
import { PlanStatusEnum } from "@app/core/common/enum/plan-status";
import { PlanUserRole } from "@app/core/common/enum/plan-user-role";
import { PlanUserType } from "@app/core/common/enum/plan-user-type";
import { IsActive } from "@app/core/common/enum/is-active.enum";
import { PlanBlueprint, PlanBlueprintDefinitionSection, ExtraFieldInSection, FieldInSection, ReferenceTypeFieldInSection, SystemFieldInSection, UploadFieldInSection } from "@app/core/model/plan-blueprint/plan-blueprint";
import { Plan, PlanBlueprintValue, PlanBlueprintValuePersist, PlanContact, PlanContactPersist, PlanDescriptionTemplate, PlanDescriptionTemplatePersist, PlanPersist, PlanProperties, PlanPropertiesPersist, PlanReferenceDataPersist, PlanReferencePersist, PlanUser, PlanUserPersist } from "@app/core/model/plan/plan";
import { PlanReference } from "@app/core/model/plan/plan-reference";
import { ReferencePersist } from "@app/core/model/reference/reference";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator } from '@common/forms/validation/custom-validator';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { Validation, ValidationContext } from '@common/forms/validation/validation-context';
import { Guid } from "@common/types/guid";
import { Description } from "@app/core/model/description/description";
import { DescriptionEditorForm, DescriptionEditorModel } from "@app/ui/description/editor/description-editor.model";
import { VisibilityRulesService } from "@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service";

export class PlanEditorModel extends BaseEditorModel implements PlanPersist {
	label: string;
	statusId: Guid;
	properties: PlanPropertiesEditorModel = new PlanPropertiesEditorModel(this.validationErrorModel);
	description: String;
	language: String;
	blueprint: Guid;
	accessType: PlanAccessType;
	descriptionTemplates: PlanDescriptionTemplateEditorModel[] = [];
	users: PlanUserEditorModel[] = [];
	permissions: string[];


	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super(); }

	public fromModel(item: Plan): PlanEditorModel {
		if (item) {
			super.fromModel(item);
			this.label = item.label;
			this.statusId = item.status?.id;
			this.properties = new PlanPropertiesEditorModel(this.validationErrorModel).fromModel(item.properties, item.isActive != IsActive.Active? item.planReferences: item.planReferences?.filter(x => x.isActive === IsActive.Active), item.blueprint);
			this.description = item.description;
			this.language = item.language;
			this.blueprint = item.blueprint?.id;
			this.accessType = item.accessType;
			if (item?.planUsers) { 
				if (item.isActive != IsActive.Active) item.planUsers.map(x => this.users.push(new PlanUserEditorModel(this.validationErrorModel).fromModel(x)));
				else item.planUsers.filter(x => x.isActive === IsActive.Active).map(x => this.users.push(new PlanUserEditorModel(this.validationErrorModel).fromModel(x))); 
			}

			item.blueprint?.definition?.sections?.forEach(section => {
				if (section.hasTemplates) {
					const isNew = (item.id == null);
					let sectionTemplatesFromPlan: PlanDescriptionTemplate[];
					if (item.isActive != IsActive.Active) {
						sectionTemplatesFromPlan = item.planDescriptionTemplates?.filter(x => x.sectionId == section.id) || [];
					} else {
						sectionTemplatesFromPlan = item.planDescriptionTemplates?.filter(x => x.sectionId == section.id && x.isActive == IsActive.Active) || [];
					}

					if (sectionTemplatesFromPlan.length > 0) {
						sectionTemplatesFromPlan?.filter(x => x.sectionId == section.id).forEach(planDescriptionTemplate => {
							this.descriptionTemplates.push(new PlanDescriptionTemplateEditorModel(this.validationErrorModel).fromModel(
								{
									sectionId: section.id,
									descriptionTemplateGroupId: planDescriptionTemplate?.descriptionTemplateGroupId,
								}));
						});
					} else if (section.descriptionTemplates?.length > 0 && isNew) {
						section.descriptionTemplates.forEach(blueprintDefinedDescriptionTemplate => {
							this.descriptionTemplates.push(new PlanDescriptionTemplateEditorModel(this.validationErrorModel).fromModel(
								{
									sectionId: section.id,
									descriptionTemplateGroupId: blueprintDefinedDescriptionTemplate?.descriptionTemplate?.groupId,
								}));
						});
					} else {
						this.descriptionTemplates.push(new PlanDescriptionTemplateEditorModel(this.validationErrorModel).fromModel(
							{
								sectionId: section.id,
							}));
					}
				}

			});

		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): FormGroup<PlanEditorForm> {
		if (context == null) { context = this.createValidationContext(); }

		const formGroup: FormGroup<PlanEditorForm> = this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			label: [{ value: this.label, disabled: disabled }, context.getValidation('label').validators],
			statusId: [{ value: this.statusId, disabled: disabled }, context.getValidation('statusId').validators],
			properties: this.properties.buildForm({
				rootPath: `properties.`,
				disabled: disabled
			}),
			users: this.formBuilder.array(
				(this.users ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `users[${index}].`,
						disabled: disabled
					})
				), context.getValidation('users').validators
			),
			description: [{ value: this.description, disabled: disabled }, context.getValidation('description').validators],
			language: [{ value: this.language, disabled: disabled }, context.getValidation('language').validators],
			blueprint: [{ value: this.blueprint, disabled: disabled }, context.getValidation('blueprint').validators],
			accessType: [{ value: this.accessType, disabled: disabled }, context.getValidation('accessType').validators],
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators],
            descriptionTemplates: this.buildDescriptionTemplatesForm(context, disabled),
            descriptions: new FormArray([])
		});

		return formGroup;
	}

    buildDescriptionTemplatesForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
        const descriptionTemplatesFormGroup = this.formBuilder.group({});
		(this.descriptionTemplates ?? []).filter(x => x?.sectionId).map(x => x.sectionId).map(
			(item, index) => descriptionTemplatesFormGroup.addControl(item.toString(),
				new FormControl(this.descriptionTemplates?.filter(x => x.sectionId === item)?.filter(x => x.descriptionTemplateGroupId).map(x => x.descriptionTemplateGroupId) || [], context.getValidation('descriptionTemplates').validators))
		);
		if (disabled) descriptionTemplatesFormGroup.disable();
        return descriptionTemplatesFormGroup;
    }

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(this.validationErrorModel, 'id')] });
		baseValidationArray.push({ key: 'label', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'label')] });
		baseValidationArray.push({ key: 'statusId', validators: [BackendErrorValidator(this.validationErrorModel, 'status')] });
		baseValidationArray.push({ key: 'properties', validators: [BackendErrorValidator(this.validationErrorModel, 'properties')] });
		baseValidationArray.push({ key: 'description', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'description')] });
		baseValidationArray.push({ key: 'language', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'language')] });
		baseValidationArray.push({ key: 'blueprint', validators: [BackendErrorValidator(this.validationErrorModel, 'blueprint')] });
		baseValidationArray.push({ key: 'accessType', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'accessType')] });
		baseValidationArray.push({ key: 'descriptionTemplates', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'descriptionTemplates')] });

		baseValidationArray.push({ key: 'planDescriptionValidator', validators: [] });

		baseValidationArray.push({ key: 'users', validators: [BackendErrorValidator(this.validationErrorModel, `users`)] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	createChildContact(index: number): UntypedFormGroup {
		const contact: PlanContactEditorModel = new PlanContactEditorModel(this.validationErrorModel);
		return contact.buildForm({ rootPath: 'properties.contacts[' + index + '].' });
	}


	static reApplyPropertiesValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		blueprint: PlanBlueprint
	}): void {

		const { formGroup, validationErrorModel } = params;
		const control = formGroup?.get('properties');
		PlanPropertiesEditorModel.reapplyValidators({
			formGroup: control as UntypedFormGroup,
			rootPath: `properties.`,
			validationErrorModel: validationErrorModel,
			blueprint: params.blueprint
		});
	}

	static reApplyDescriptionTemplateValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
	}): void {

		const { formGroup, validationErrorModel } = params;

		const descriptionTemplates = formGroup?.get('descriptionTemplates') as UntypedFormGroup;
		const keys = Object.keys(descriptionTemplates.value as Object);
		keys.forEach((key) => {
			const control = descriptionTemplates?.get(key);
			PlanDescriptionTemplateEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `descriptionTemplates[${key}].`,
				validationErrorModel: validationErrorModel
			})
		});
	}

	static reApplyUsersValidators(params: {
		usersFormArray: FormArray,
		validationErrorModel: ValidationErrorModel,
	}): void {

		const { usersFormArray, validationErrorModel } = params;
		usersFormArray.controls?.forEach(
			(control, index) => PlanUserEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `users[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);
	}
}

export class PlanPropertiesEditorModel implements PlanPropertiesPersist {
	planBlueprintValues: Map<Guid, PlanBlueprintValueEditorModel> = new Map<Guid, PlanBlueprintValueEditorModel>;
	contacts: PlanContactEditorModel[] = [];

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: PlanProperties, planReferences: PlanReference[], planBlueprint: PlanBlueprint): PlanPropertiesEditorModel {


		planBlueprint?.definition?.sections?.forEach(section => {
			section.fields?.forEach(field => {
				if (field.category !== PlanBlueprintFieldCategory.System) {
					this.planBlueprintValues.set(field.id, new PlanBlueprintValueEditorModel(this.validationErrorModel).fromModel(
						{
							fieldId: field.id,
							fieldValue: item?.planBlueprintValues?.find(x => x.fieldId == field.id)?.fieldValue || undefined,
							dateValue: item?.planBlueprintValues?.find(x => x.fieldId == field.id)?.dateValue || undefined,
							numberValue: item?.planBlueprintValues?.find(x => x.fieldId == field.id)?.numberValue || undefined,
						}, planReferences, field));
				}
			});
		});
		if (item?.contacts) { item.contacts.map(x => this.contacts.push(new PlanContactEditorModel(this.validationErrorModel).fromModel(x))); }

		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string
	}, planReferences?: PlanReference[], planBlueprint?: PlanBlueprint): UntypedFormGroup {
		let { context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = PlanPropertiesEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		const formGroup = this.formBuilder.group({
			contacts: this.formBuilder.array(
				(this.contacts ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}contacts[${index}].`,
						disabled: disabled
					})
				), context.getValidation('contacts').validators
			),

		});


		const planBlueprintValuesFormGroup = this.formBuilder.group({});
		this.planBlueprintValues.forEach((value, key) => planBlueprintValuesFormGroup.addControl(key.toString(), value.buildForm({
			rootPath: `${rootPath}planBlueprintValues[${key}].`,
			disabled: disabled
		})), context.getValidation('planBlueprintValues')
		)
		formGroup.addControl('planBlueprintValues', planBlueprintValuesFormGroup);

		return formGroup;
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'planBlueprintValues', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}planBlueprintValues`)] });
		baseValidationArray.push({ key: 'contacts', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}contacts`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string,
		blueprint: PlanBlueprint
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;

		const planBlueprintValues = formGroup?.get('planBlueprintValues') as UntypedFormGroup;
		const keys = Object.keys(planBlueprintValues.value as Object);
		keys.forEach((key) => {
			const control = planBlueprintValues?.get(key);
			PlanBlueprintValueEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}planBlueprintValues[${key}].`,
				validationErrorModel: validationErrorModel,
				isRequired: params.blueprint.definition.sections.flatMap(x => x.fields).find(x => x.id.toString() == key).required,
				multipleSelect: (params.blueprint.definition.sections.flatMap(x => x.fields).find(x => x.id.toString() == key)  as ReferenceTypeFieldInSection).multipleSelect
			})
		});

		(formGroup.get('contacts') as FormArray).controls?.forEach(
			(control, index) => PlanContactEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}contacts[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);
	}
}
export class PlanBlueprintValueEditorModel implements PlanBlueprintValuePersist {
	fieldId: Guid;
	fieldValue: string;
	dateValue: Date;
	numberValue: number;
	references: PlanReferencePersist[] = [];
	reference: PlanReferencePersist;
	isRequired: boolean = false;
	multipleSelect: boolean = false;
	category: PlanBlueprintFieldCategory;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: PlanBlueprintValue, planReferences: PlanReference[], field: FieldInSection): PlanBlueprintValueEditorModel {
		this.fieldId = item.fieldId;
		this.fieldValue = item.fieldValue;
		this.dateValue = item.dateValue;
		this.numberValue = item.numberValue;
		const references = planReferences?.filter(x => x.data?.blueprintFieldId == this.fieldId).map(x => {
			return {
				data: x.data,
				reference: {
					id: x.reference.id,
					label: x.reference.label,
					reference: x.reference.reference,
					source: x.reference.source,
					typeId: x.reference.type?.id,
					description: x.reference.source,
					definition: x.reference.definition,
					abbreviation: x.reference.abbreviation,
					sourceType: x.reference.sourceType
				}
			}
		});
		if ((field as ReferenceTypeFieldInSection).multipleSelect) {
			this.references = references;
			this.multipleSelect = true;
		} else {
			if (references?.length == 1) this.reference = references[0];
			if (references?.length > 1) {
				console.error("multiple references on single reference field: " + references);
				this.reference = references[0];
			}
			this.multipleSelect = false;

		}
		this.isRequired = field.required;
		this.category = field.category;

		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string
	}): UntypedFormGroup {
		let { context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = PlanBlueprintValueEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath,
				isRequired: this.isRequired,
				multipleSelect: this.multipleSelect
			});
		}

		const formGroup = this.formBuilder.group({
			fieldId: [{ value: this.fieldId, disabled: disabled }, context.getValidation('fieldId').validators],
		});
		switch (this.category) {
			case PlanBlueprintFieldCategory.ReferenceType:
				formGroup.addControl('references', new FormControl({ value: this.references?.map(x => x.reference), disabled: disabled }, context.getValidation('references').validators));
				formGroup.addControl('reference', new FormControl({ value: this.reference?.reference, disabled: disabled }, context.getValidation('reference').validators));
				break;
			case PlanBlueprintFieldCategory.System:
			case PlanBlueprintFieldCategory.Extra:
				formGroup.addControl('fieldValue', new FormControl({ value: this.fieldValue, disabled: disabled }, context.getValidation('fieldValue').validators));
				formGroup.addControl('dateValue', new FormControl({ value: this.dateValue, disabled: disabled }, context.getValidation('dateValue').validators));
				formGroup.addControl('numberValue', new FormControl({ value: this.numberValue, disabled: disabled }, context.getValidation('numberValue').validators));
				break;
			case PlanBlueprintFieldCategory.Upload:
				formGroup.addControl('fieldValue', new FormControl({ value: this.fieldValue, disabled: disabled }, context.getValidation('fieldValue').validators));
		}

		return formGroup;
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel,
		multipleSelect: boolean,
		isRequired: boolean,
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'fieldId', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}fieldId`)] });
		baseValidationArray.push({ key: 'fieldValue', validators: params.isRequired ? [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}fieldValue`)] : [BackendErrorValidator(validationErrorModel, `${rootPath}fieldValue`)] });
		baseValidationArray.push({ key: 'dateValue', validators: params.isRequired ? [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}dateValue`)] : [BackendErrorValidator(validationErrorModel, `${rootPath}dateValue`)] });
		baseValidationArray.push({ key: 'numberValue', validators: params.isRequired ? [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}numberValue`)] : [BackendErrorValidator(validationErrorModel, `${rootPath}numberValue`)] });
		baseValidationArray.push({ key: 'references', validators: params.isRequired && params.multipleSelect ? [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}references`)] : [BackendErrorValidator(validationErrorModel, `${rootPath}references`)] });
		baseValidationArray.push({ key: 'reference', validators: params.isRequired && !params.multipleSelect ? [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}reference`)] : [BackendErrorValidator(validationErrorModel, `${rootPath}reference`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string,
		isRequired: boolean
		multipleSelect: boolean
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = PlanBlueprintValueEditorModel.createValidationContext({
			rootPath,
			validationErrorModel,
			isRequired: params.isRequired,
			multipleSelect: params.multipleSelect
		});

		['fieldId', 'fieldValue', 'dateValue', 'numberValue', 'references'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}

export class PlanContactEditorModel implements PlanContactPersist {
	firstName: string;
	lastName: string;
	email: string;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: PlanContact): PlanContactEditorModel {
		this.firstName = item.firstName;
		this.lastName = item.lastName;
		this.email = item.email;

		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string
	}): UntypedFormGroup {
		let { context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = PlanContactEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			firstName: [{ value: this.firstName, disabled: disabled }, context.getValidation('firstName').validators],
			lastName: [{ value: this.lastName, disabled: disabled }, context.getValidation('lastName').validators],
			email: [{ value: this.email, disabled: disabled }, context.getValidation('email').validators],
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'firstName', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}firstName`)] });
		baseValidationArray.push({ key: 'lastName', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}lastName`)] });
		baseValidationArray.push({ key: 'email', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}email`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = PlanContactEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['firstName', 'lastName', 'email'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}

export class PlanUserEditorModel implements PlanUserPersist {
	user: Guid;
	role: PlanUserRole;
	email: string;
	userType: PlanUserType = PlanUserType.Internal;
	sectionId: string = '';
	ordinal: number | null;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: PlanUser): PlanUserEditorModel {
		if (item?.user?.id) this.user = item.user.id;
		this.role = item.role;
		this.userType = (item == null || this.user != null) ? PlanUserType.Internal : PlanUserType.External;
		this.sectionId = item.sectionId?.toString() || ''; //Trick to allow a null option for all items.
		this.ordinal = item.ordinal;
		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string
	}): UntypedFormGroup {
		let { context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = PlanUserEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			user: [{ value: this.user, disabled: disabled }, context.getValidation('user').validators],
			role: [{ value: this.role, disabled: disabled }, context.getValidation('role').validators],
			email: [{ value: this.email, disabled: disabled }, context.getValidation('email').validators],
			sectionId: [{ value: this.sectionId, disabled: disabled }, context.getValidation('sectionId').validators],
			userType: [{ value: this.userType, disabled: disabled }, context.getValidation('userType').validators],
			ordinal: [{ value: this.ordinal, disabled: disabled }, context.getValidation('ordinal').validators],
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'user', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}user`)] });
		baseValidationArray.push({ key: 'role', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}role`)] });
		baseValidationArray.push({ key: 'email', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}email`)] });
		baseValidationArray.push({ key: 'sectionId', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}sectionId`)] });
		baseValidationArray.push({ key: 'userType', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}userType`)] });
		baseValidationArray.push({ key: 'ordinal', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}ordinal`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = PlanUserEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['user', 'role', 'email', 'sectionId', 'ordinal'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}

export class PlanReferenceEditorModel implements PlanReferencePersist {
	id: Guid;
	reference: ReferencePersist;
	referenceId: Guid;
	data: PlanReferenceDataPersist;
	hash: string;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: PlanReference): PlanReferenceEditorModel {
		this.id = item.id;
		// this.reference = item.reference; //TODO: refactor reference type
		this.data = item.data;
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
			context = PlanReferenceEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			reference: [{ value: this.reference, disabled: disabled }, context.getValidation('reference').validators],
			data: [{ value: this.data, disabled: disabled }, context.getValidation('data').validators],
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
		baseValidationArray.push({ key: 'data', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}data`)] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}

export class PlanDescriptionTemplateEditorModel implements PlanDescriptionTemplatePersist {
	descriptionTemplateGroupId: Guid;
	sectionId: Guid;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: PlanDescriptionTemplate): PlanDescriptionTemplateEditorModel {
		this.descriptionTemplateGroupId = item.descriptionTemplateGroupId;
		this.sectionId = item.sectionId;

		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string
	}): UntypedFormGroup {
		let { context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = PlanDescriptionTemplateEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath: rootPath,
			});
		}

		return this.formBuilder.group({
			descriptionTemplateGroupId: [{ value: this.descriptionTemplateGroupId, disabled: disabled }, context.getValidation('descriptionTemplateGroupId').validators],
			sectionId: [{ value: this.sectionId, disabled: disabled }, context.getValidation('sectionId').validators],
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel,
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'descriptionTemplateGroupId', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}descriptionTemplateGroupId`)] });
		baseValidationArray.push({ key: 'sectionId', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}sectionId`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = PlanDescriptionTemplateEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['descriptionTemplateGroupId', 'sectionId'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}

export class PlanFieldIndicator {

	private _fieldControls: PlanFieldIndicatorControls[];

	get fieldControls(): PlanFieldIndicatorControls[] {
		return this._fieldControls;
	}

	constructor(section: PlanBlueprintDefinitionSection) {
		this._fieldControls = [];

		if (section.hasTemplates) {
			this._fieldControls.push({formControlName: `descriptionTemplates.${section.id}`});
		}

        if (!section?.fields?.length) return;

        section.fields.forEach((field: FieldInSection) => {
            switch (field.category) {
                case PlanBlueprintFieldCategory.System:
                    this.buildSystemField(field as SystemFieldInSection);
                    break;
                case PlanBlueprintFieldCategory.ReferenceType:
                    this.buildReferenceTypeField(field as ReferenceTypeFieldInSection);
                    break;
                case PlanBlueprintFieldCategory.Extra:
                    this.buildExtraField(field as ExtraFieldInSection);
                    break;
                case PlanBlueprintFieldCategory.Upload:
                    this.buildUploadField(field as UploadFieldInSection);
            }
        });
	}

	buildSystemField(field: SystemFieldInSection): void {
		switch (field.systemFieldType) {
			case PlanBlueprintSystemFieldType.Title:
				this._fieldControls.push({
                    id: field.id,
                     formControlName: "label"
                });
				break;
			case PlanBlueprintSystemFieldType.Description:
				this._fieldControls.push({
                    id: field.id,
                     formControlName: "description"
                });
				break;
			case PlanBlueprintSystemFieldType.Language:
				this._fieldControls.push({
                    id: field.id,
                     formControlName: "language"
                });
				break;
			case PlanBlueprintSystemFieldType.Contact:
				this._fieldControls.push({
                    id: field.id,
                     formControlName: "properties.contacts"
                });
				break;
			case PlanBlueprintSystemFieldType.AccessRights:
				this._fieldControls.push({
                    id: field.id,
                     formControlName: "accessType"
                });
				break;
			case PlanBlueprintSystemFieldType.User:
				this._fieldControls.push({
                    id: field.id,
                     formControlName: "users"
                });
				break;
		}
	}

	buildReferenceTypeField(field: ReferenceTypeFieldInSection): void {
		if (field.multipleSelect) {
			this._fieldControls.push({
                id: field.id,
                 formControlName: `properties.planBlueprintValues.${field.id}.references`
            });
		} else {
			this._fieldControls.push({
                id: field.id,
                 formControlName: `properties.planBlueprintValues.${field.id}.reference`
            });
		}
	}

	buildExtraField(field: ExtraFieldInSection): void {
		this._fieldControls.push({
            id: field.id,
            formControlName: `properties.planBlueprintValues.${field.id}.fieldValue`
        });
	}

	buildUploadField(field: UploadFieldInSection): void {
		this._fieldControls.push({
            id: field.id,
            formControlName: `properties.planBlueprintValues.${field.id}.fieldValue`
        });
	}
}

export interface PlanFieldIndicatorControls {
    id?: Guid,
    formControlName: string;
}

export interface PlanEditorForm {
    id: FormControl<Guid>;
    hash: FormControl<string>;
    label: FormControl<string>;
	statusId: FormControl<Guid>;
	properties: UntypedFormGroup;
	description: FormControl<String>;
	language: FormControl<String>;
	blueprint: FormControl<Guid>;
	accessType: FormControl<PlanAccessType>;
	descriptionTemplates: FormArray<UntypedFormGroup>;
	users: FormArray<UntypedFormGroup>;
	permissions: FormControl<string[]>;

    descriptions: FormArray<FormGroup<DescriptionEditorForm>>;
}