import { FormArray, FormControl, FormGroup, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { IsActive } from "@app/core/common/enum/is-active.enum";
import { PlanBlueprintStatus } from "@app/core/common/enum/plan-blueprint-status";
import { PlanBlueprintVersionStatus } from "@app/core/common/enum/plan-blueprint-version-status";
import { DescriptionTemplatesInSection, DescriptionTemplatesInSectionPersist, PlanBlueprint, PlanBlueprintDefinition, PlanBlueprintDefinitionPersist, PlanBlueprintPersist, RootSection, RootSectionPersist, PlanCreationConsentPersist, PlanCreationConsent } from "@app/core/model/plan-blueprint/plan-blueprint";
import { PluginRepositoryConfiguration } from "@app/core/model/plugin-configuration/plugin-configuration";
import { IconConfigurationEditorModel, IconConfigurationForm } from "@app/ui/iconconfiguration/icon-configuration-editor.model";
import { PluginConfigurationEditorModel, PluginConfigurationForm } from "@app/ui/plugin/plugin-editor.model";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator, PlanBlueprintHasDescriptionTemplatesValidator, PlanBlueprintSystemFieldRequiredValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";
import { Guid } from "@common/types/guid";
import { Subject } from "rxjs";
import { TemplateSectionEditorModel, TemplateSectionForm } from "@app/ui/admin/description-template/editor/description-template-editor.model";
import { BaseSection } from "@app/core/model/description-template/description-template";
import { EditorCustomValidators } from "../../description-template/editor/custom-validators/editor-custom-validators";

export class PlanBlueprintEditorModel extends BaseEditorModel implements PlanBlueprintPersist {
	label: string;
	code: string;
	type: Guid;
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
			this.type = item.type?.id;
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
			type: [{ value: this.type, disabled }, context.getValidation('type').validators],
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
		baseValidationArray.push({ key: 'type', validators: [BackendErrorValidator(this.validationErrorModel, 'type')] });
		baseValidationArray.push({ key: 'code', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'code')] });
		baseValidationArray.push({ key: 'status', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'status')] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	createChildSection(destroyRef: Subject<any>, index: number): UntypedFormGroup {
		const section: PlanBlueprintRootSectionEditorModel = new PlanBlueprintRootSectionEditorModel(this.validationErrorModel);
		section.id = Guid.create().toString();
		section.ordinal = index + 1;
		section.hasTemplates = false;
		section.prefillingSourcesEnabled = false;
		return section.buildForm({ destroyRef, rootPath: 'definition.sections[' + index + '].' });
	}

	createChildDescriptionTemplate(sectionIndex: number, index: number, item?: any): UntypedFormGroup {
		const descriptionTemplate: DescriptionTemplatesInSectionEditorModel = new DescriptionTemplatesInSectionEditorModel(this.validationErrorModel);
		return descriptionTemplate.buildForm({ rootPath: 'definition.sections[' + sectionIndex + '].descriptionTemplates[' + index + '].' });
	}

	static reApplyDefinitionValidators(params: {
		formGroup: FormGroup<PlanBlueprintForm>,
		validationErrorModel: ValidationErrorModel,
	}): void {

		const { formGroup, validationErrorModel } = params;
		const control = formGroup?.controls?.definition;
		PlanBlueprintDefinitionEditorModel.reapplySectionsValidators({
			formArray: control.controls.sections,
			rootPath: `definition.`,
			validationErrorModel: validationErrorModel
		});
        PlanBlueprintDefinitionEditorModel.reapplyPluginValidators({
            formGroup,
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
			type: null,
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
	sections: PlanBlueprintRootSectionEditorModel[] = [];
	pluginConfigurations: PluginConfigurationEditorModel[] = [];
	planCreationConsent: PlanCreationConsentEditorModel = new PlanCreationConsentEditorModel();
	iconConfiguration: IconConfigurationEditorModel = new IconConfigurationEditorModel(this.validationErrorModel);
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: PlanBlueprintDefinition, configuration?: PluginRepositoryConfiguration[]): PlanBlueprintDefinitionEditorModel {
		if (item) {
			if (item.sections) { item.sections.sort((a,b) => a.ordinal - b.ordinal).map(x => this.sections.push(new PlanBlueprintRootSectionEditorModel(this.validationErrorModel).fromModel(x))); }
			if (item.pluginConfigurations) { item.pluginConfigurations.map(x => this.pluginConfigurations.push(new PluginConfigurationEditorModel(this.validationErrorModel).fromModel(x, configuration))); }
			if (item.planCreationConsent) this.planCreationConsent = new PlanCreationConsentEditorModel(this.validationErrorModel).fromModel(item.planCreationConsent);
			if (item.iconConfiguration) this.iconConfiguration = new IconConfigurationEditorModel(this.validationErrorModel).fromModel(item.iconConfiguration);
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
			planCreationConsent: this.planCreationConsent.buildForm({
                destroyRef,
				rootPath: `${rootPath}planCreationConsent.`,
				disabled
			}),
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
		baseValidationArray.push({ key: 'sections', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}sections`), PlanBlueprintSystemFieldRequiredValidator(), PlanBlueprintHasDescriptionTemplatesValidator()] });
		baseValidationArray.push({ key: 'pluginConfigurations', validators: [ BackendErrorValidator(validationErrorModel, `${rootPath}pluginConfigurations`)] });
		baseValidationArray.push({ key: 'iconConfiguration', validators: [ BackendErrorValidator(validationErrorModel, `${rootPath}iconConfiguration`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplySectionsValidators(params: {
		formArray: FormArray<FormGroup<PlanBlueprintRootSectionForm>>,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {
		const { validationErrorModel, rootPath, formArray } = params;
		formArray?.controls?.forEach(
			(control, index) => PlanBlueprintRootSectionEditorModel.reapplySectionValidators({
				formGroup: control,
				rootPath: `${rootPath}sections[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);
	}

    static reapplyPluginValidators(params: {
		formGroup: FormGroup<PlanBlueprintForm>,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {
        const { validationErrorModel, rootPath, formGroup } = params;
        const definition = formGroup.controls.definition;
        definition.controls.pluginConfigurations?.controls?.forEach((control, index) => PluginConfigurationEditorModel.reapplyValidators({
            formGroup: control,
            rootPath: `${rootPath}pluginConfigurations[${index}].`,
            validationErrorModel: validationErrorModel
        }))
    }

}

export class PlanBlueprintRootSectionEditorModel extends TemplateSectionEditorModel implements RootSectionPersist {
	hasTemplates: boolean = false;
	canEditDescriptionTemplates: boolean = true;
	descriptionTemplates?: DescriptionTemplatesInSectionEditorModel[] = [];
	prefillingSourcesEnabled: boolean = false;
	prefillingSourcesIds: Guid[]= [];

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) {
        super();
    }

	public fromModel(item: RootSection): PlanBlueprintRootSectionEditorModel {
        super.fromModel(item);
		if (item) {
			this.hasTemplates = item.hasTemplates ?? false;
            this.canEditDescriptionTemplates = item.canEditDescriptionTemplates == null || item.canEditDescriptionTemplates;
			this.prefillingSourcesEnabled = item?.prefillingSourcesEnabled ?? false;
			if (item.descriptionTemplates) { item.descriptionTemplates.map(x => this.descriptionTemplates.push(new DescriptionTemplatesInSectionEditorModel(this.validationErrorModel).fromModel(x))); }
			if (item.prefillingSources) this.prefillingSourcesIds = item.prefillingSources.map(x => x.id);
		}
		return this;
	}

	buildForm(params: {
        destroyRef?: Subject<any>,
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string,
	}): UntypedFormGroup {
		let { context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = PlanBlueprintRootSectionEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}
        const formGroup = super.buildForm({context, disabled, rootPath, addSectionValidator: false});
        formGroup.setControl('hasTemplates', new FormControl({ value: this.hasTemplates, disabled: disabled }, context.getValidation('hasTemplates').validators));
        formGroup.setControl('canEditDescriptionTemplates', new FormControl({ value: this.canEditDescriptionTemplates, disabled: disabled }, context.getValidation('canEditDescriptionTemplates').validators));
        formGroup.setControl('prefillingSourcesEnabled', new FormControl({ value: this.prefillingSourcesEnabled, disabled: disabled }, context.getValidation('prefillingSourcesEnabled').validators));
		formGroup.setControl('descriptionTemplates', this.formBuilder.array(
            (this.descriptionTemplates ?? []).map(
                (item, index) => item.buildForm({
                    rootPath: `${rootPath}descriptionTemplates[${index}].`,
                    disabled: disabled
                })
            ), context.getValidation('descriptionTemplates').validators
        ));
		formGroup.setControl('prefillingSourcesIds', new FormControl({ value: this.prefillingSourcesIds, disabled: disabled }, context.getValidation('prefillingSourcesIds').validators));
        formGroup.removeValidators(EditorCustomValidators.sectionHasAtLeastOneChildOf('fieldSets', 'sections'));
        formGroup.addValidators(EditorCustomValidators.sectionHasTemplatesOrChildOf('fieldSets', 'sections'));
        formGroup.addValidators(EditorCustomValidators.sectionTemplatesRequired());
        return formGroup;
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
        const { rootPath = '', validationErrorModel } = params;
        const baseContext: ValidationContext = super.createValidationContext({rootPath, validationErrorModel});

		baseContext.validation.push({ key: 'id', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}id`)] });
		baseContext.validation.push({ key: 'label', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}label`)] });
		baseContext.validation.push({ key: 'ordinal', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}ordinal`)] });
		baseContext.validation.push({ key: 'description', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}description`)] });
		baseContext.validation.push({ key: 'extendedDescription', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}extendedDescription`)] });
		baseContext.validation.push({ key: 'hasTemplates', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}hasTemplates`)] });
		baseContext.validation.push({ key: 'canEditDescriptionTemplates', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}canEditDescriptionTemplates`)] });
		baseContext.validation.push({ key: 'fieldSets', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}fieldSets`)] });
		baseContext.validation.push({ key: 'sections', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}sections`)] });
		baseContext.validation.push({ key: 'descriptionTemplates', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}descriptionTemplates`)] });
		baseContext.validation.push({ key: 'prefillingSourcesIds', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}prefillingSourcesIds`)] });
		baseContext.validation.push({ key: 'prefillingSourcesEnabled', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}prefillingSourcesEnabled`)] });
		baseContext.validation.push({ key: 'hash', validators: [] });

		return baseContext;
	}

	static reapplySectionValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {
		const { formGroup, rootPath, validationErrorModel } = params;
        super.reapplyValidators({formGroup, validationErrorModel, rootPath});
		const context = PlanBlueprintRootSectionEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['hasTemplates', 'canEditDescriptionTemplates', 'prefillingSourcesIds'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});

		(formGroup.get('descriptionTemplates') as FormArray).controls?.forEach(
			(control, index) => DescriptionTemplatesInSectionEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}descriptionTemplates[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);
	}

    static createEmptySection(): RootSection {
        return {
            description: null,
            hasTemplates: null,
            canEditDescriptionTemplates: true,
            id: null,
            title: null,
            ordinal: null,
            prefillingSourcesEnabled: false,
            prefillingSources: null,
            fieldSets: [],
            sections: []
        }
    }

    static toRootSection(formGroup: FormGroup<TemplateSectionForm>, validationErrorModel: ValidationErrorModel, rootPath?: string): FormGroup<PlanBlueprintRootSectionForm> {
        const editorModel = new PlanBlueprintRootSectionEditorModel(validationErrorModel);
        const rootSection: RootSection = {
            ...formGroup.getRawValue(),
            canEditDescriptionTemplates: false,
            hasTemplates: false,
            prefillingSources: [],
            prefillingSourcesEnabled: false,
            
        }
        return editorModel.fromModel(rootSection).buildForm({rootPath})
    }

    static toBaseSection(formGroup: FormGroup<PlanBlueprintRootSectionForm>, validationErrorModel: ValidationErrorModel, rootPath?: string): FormGroup<TemplateSectionForm> {
        const editorModel = new TemplateSectionEditorModel(validationErrorModel);
        const section: BaseSection = {
            ...formGroup.getRawValue(),  
        }
        return editorModel.fromModel(section).buildForm({rootPath})
    }

}

export class PlanCreationConsentEditorModel implements PlanCreationConsentPersist {
	enabled: boolean = false;
	message: string;
	details: string;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: PlanCreationConsent): PlanCreationConsentEditorModel {
		if (item) {
			if (item.enabled) this.enabled = item.enabled;
			this.message = item.message;
			this.details = item.details;
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
			context = PlanCreationConsentEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			enabled: [{ value: this.enabled, disabled: disabled }, context.getValidation('enabled').validators],
			message: [{ value: this.message, disabled: disabled }, context.getValidation('message').validators],
			details: [{ value: this.details, disabled: disabled }, context.getValidation('details').validators],

		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'enabled', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}enabled`)] });
		baseValidationArray.push({ key: 'message', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}message`)] });
		baseValidationArray.push({ key: 'details', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}details`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {
		const { formGroup, rootPath, validationErrorModel } = params;
		const context = PlanCreationConsentEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['enabled', 'message', 'details'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});

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
// export class BlueprintFieldSetEditorModel implements BlueprintFieldSetPersist {
// 	id: Guid;
// 	ordinal: number;
// 	title: string;
// 	description: string;
// 	extendedDescription: string;
// 	additionalInformation: string;
// 	multiplicity: MultiplicityEditorModel = new MultiplicityEditorModel(this.validationErrorModel);
// 	hasMultiplicity: boolean = false;
// 	hasCommentField: boolean = false;
// 	fields: (BlueprintSystemFieldEditorModel | BlueprintTemplateFieldEditorModel)[] = [];

// 	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

// 	constructor(
// 		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
// 	) { }

// 	fromModel(item: BlueprintFieldSet): BlueprintFieldSetEditorModel {
// 		if (item) {
// 			this.id = item.id;
// 			this.ordinal = item.ordinal;
// 			this.title = item.title;
// 			this.description = item.description;
// 			this.extendedDescription = item.extendedDescription;
// 			this.additionalInformation = item.additionalInformation;
// 			this.hasCommentField = item.hasCommentField;
// 			this.hasMultiplicity = item.hasMultiplicity;
// 			this.multiplicity = new MultiplicityEditorModel(this.validationErrorModel).fromModel(item.multiplicity as DescriptionTemplateMultiplicity);
// 			if (item.fields) {
// 				item.fields.forEach(f => {
// 					if (f.category === PlanBlueprintFieldCategory.System) {
// 						this.fields.push(new BlueprintSystemFieldEditorModel(this.validationErrorModel).fromModel(f as SystemFieldInSection));
// 					} else {
// 						this.fields.push(new BlueprintTemplateFieldEditorModel(this.validationErrorModel).fromModel(f as BlueprintTemplateFieldInSection));
// 					}
// 				});
// 			}
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
// 			context = BlueprintFieldSetEditorModel.createValidationContext({
// 				validationErrorModel: this.validationErrorModel,
// 				rootPath
// 			});
// 		}

// 		return this.formBuilder.group({
// 			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
// 			ordinal: [{ value: this.ordinal, disabled: disabled }, context.getValidation('ordinal').validators],
// 			title: [{ value: this.title, disabled: disabled }, context.getValidation('title').validators],
// 			description: [{ value: this.description, disabled: disabled }, context.getValidation('description').validators],
// 			extendedDescription: [{ value: this.extendedDescription, disabled: disabled }, context.getValidation('extendedDescription').validators],
// 			additionalInformation: [{ value: this.additionalInformation, disabled: disabled }, context.getValidation('additionalInformation').validators],
// 			hasCommentField: [{ value: this.hasCommentField, disabled: disabled }, context.getValidation('hasCommentField').validators],
// 			hasMultiplicity: [{ value: this.hasMultiplicity, disabled: disabled }, context.getValidation('hasMultiplicity').validators],
// 			multiplicity: this.multiplicity.buildForm({
// 				rootPath: `${rootPath}multiplicity.`,
// 				disabled
// 			}),
// 			fields: this.formBuilder.array(
// 				(this.fields ?? []).map(
// 					(item, index) => item.buildForm({
// 						rootPath: `${rootPath}fields[${index}].`,
// 						disabled
// 					})
// 				), context.getValidation('fields').validators
// 			)
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
// 		baseValidationArray.push({ key: 'title', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}title`)] });
// 		baseValidationArray.push({ key: 'description', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}description`)] });
// 		baseValidationArray.push({ key: 'extendedDescription', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}extendedDescription`)] });
// 		baseValidationArray.push({ key: 'additionalInformation', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}additionalInformation`)] });
// 		baseValidationArray.push({ key: 'hasCommentField', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}hasCommentField`)] });
// 		baseValidationArray.push({ key: 'hasMultiplicity', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}hasMultiplicity`)] });
// 		baseValidationArray.push({ key: 'fields', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}fields`)] });

// 		baseContext.validation = baseValidationArray;
// 		return baseContext;
// 	}

// 	static reapplyValidators(params: {
// 		formGroup: UntypedFormGroup,
// 		validationErrorModel: ValidationErrorModel,
// 		rootPath: string
// 	}): void {
// 		const { formGroup, rootPath, validationErrorModel } = params;
// 		const context = BlueprintFieldSetEditorModel.createValidationContext({ rootPath, validationErrorModel });

// 		['id', 'ordinal', 'title', 'description', 'extendedDescription', 'additionalInformation', 'hasCommentField', 'hasMultiplicity'].forEach(keyField => {
// 			const control = formGroup?.get(keyField);
// 			control?.clearValidators();
// 			control?.addValidators(context.getValidation(keyField).validators);
// 		});

// 		MultiplicityEditorModel.reapplyValidators({
// 			formGroup: formGroup?.get('multiplicity') as UntypedFormGroup,
// 			rootPath: `${rootPath}multiplicity.`,
// 			validationErrorModel: validationErrorModel
// 		});

// 		(formGroup.get('fields') as FormArray).controls?.forEach(
// 			(control, index) => {
// 				const category = (control as UntypedFormGroup).get('category')?.value;
// 				if (category === PlanBlueprintFieldCategory.System) {
// 					BlueprintSystemFieldEditorModel.reapplyValidators({
// 						formGroup: control as UntypedFormGroup,
// 						rootPath: `${rootPath}fields[${index}].`,
// 						validationErrorModel: validationErrorModel
// 					});
// 				} else {
// 					BlueprintTemplateFieldEditorModel.reapplyValidators({
// 						formGroup: control as UntypedFormGroup,
// 						rootPath: `${rootPath}fields[${index}].`,
// 						validationErrorModel: validationErrorModel
// 					});
// 				}
// 			}
// 		);
// 	}
// }

// export class BlueprintSystemFieldEditorModel implements SystemFieldInSectionPersist {
// 	id: Guid;
// 	category: PlanBlueprintFieldCategory = PlanBlueprintFieldCategory.System;
// 	label: string;
// 	placeholder: string;
// 	description: string;
// 	semantics: string[];
// 	required: boolean = false;
// 	ordinal: number;
// 	systemFieldType: SystemFieldType;

// 	static get alwaysRequiredSystemFieldTypes(): SystemFieldType[] {
// 		return [SystemFieldType.PlanTitle, SystemFieldType.PlanDescription, SystemFieldType.PlanLanguage, SystemFieldType.PlanAccessRights];
// 	}

// 	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

// 	constructor(
// 		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
// 	) { }

// 	fromModel(item: SystemFieldInSection): BlueprintSystemFieldEditorModel {
// 		this.id = item.id;
// 		this.category = item.category;
// 		this.label = item.label;
// 		this.placeholder = item.placeholder;
// 		this.description = item.description;
// 		this.semantics = item.semantics;
// 		this.required = item.required;
// 		this.ordinal = item.ordinal;
// 		this.systemFieldType = item.systemFieldType;
// 		if (BlueprintSystemFieldEditorModel.alwaysRequiredSystemFieldTypes.includes(this.systemFieldType)) {
// 			this.required = true;
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
// 			context = BlueprintSystemFieldEditorModel.createValidationContext({
// 				validationErrorModel: this.validationErrorModel,
// 				rootPath
// 			});
// 		}

// 		return this.formBuilder.group({
// 			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
// 			category: [{ value: this.category, disabled: disabled }, context.getValidation('category').validators],
// 			label: [{ value: this.label, disabled: disabled }, context.getValidation('label').validators],
// 			placeholder: [{ value: this.placeholder, disabled: disabled }, context.getValidation('placeholder').validators],
// 			description: [{ value: this.description, disabled: disabled }, context.getValidation('description').validators],
// 			semantics: [{ value: this.semantics, disabled: disabled }, context.getValidation('semantics').validators],
// 			required: [{ value: this.required, disabled: disabled }, context.getValidation('required').validators],
// 			ordinal: [{ value: this.ordinal, disabled: disabled }, context.getValidation('ordinal').validators],
// 			systemFieldType: [{ value: this.systemFieldType, disabled: disabled }, context.getValidation('systemFieldType').validators],
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
// 		baseValidationArray.push({ key: 'category', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}category`)] });
// 		baseValidationArray.push({ key: 'label', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}label`)] });
// 		baseValidationArray.push({ key: 'placeholder', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}placeholder`)] });
// 		baseValidationArray.push({ key: 'description', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}description`)] });
// 		baseValidationArray.push({ key: 'semantics', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}semantics`)] });
// 		baseValidationArray.push({ key: 'required', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}required`)] });
// 		baseValidationArray.push({ key: 'ordinal', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}ordinal`)] });
// 		baseValidationArray.push({ key: 'systemFieldType', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}systemFieldType`)] });

// 		baseContext.validation = baseValidationArray;
// 		return baseContext;
// 	}

// 	static reapplyValidators(params: {
// 		formGroup: UntypedFormGroup,
// 		validationErrorModel: ValidationErrorModel,
// 		rootPath: string
// 	}): void {
// 		const { formGroup, rootPath, validationErrorModel } = params;
// 		const context = BlueprintSystemFieldEditorModel.createValidationContext({ rootPath, validationErrorModel });

// 		['id', 'category', 'label', 'placeholder', 'description', 'semantics', 'required', 'ordinal', 'systemFieldType'].forEach(keyField => {
// 			const control = formGroup?.get(keyField);
// 			control?.clearValidators();
// 			control?.addValidators(context.getValidation(keyField).validators);
// 		});
// 	}
// }

// export class BlueprintTemplateFieldEditorModel implements BlueprintTemplateFieldInSectionPersist {
// 	id: Guid;
// 	category: PlanBlueprintFieldCategory = PlanBlueprintFieldCategory.Field;
// 	label: string;
// 	placeholder: string;
// 	description: string;
// 	semantics: string[];
// 	required: boolean = false;
// 	ordinal: number;
// 	visibilityRules: RuleEditorModel[] = [];
// 	validations: FieldValidationType[] = [];
// 	includeInExport: boolean = true;
// 	defaultValue: DefaultValueEditorModel = new DefaultValueEditorModel(this.validationErrorModel);
// 	data: LabelDataEditorModel = new LabelDataEditorModel(this.validationErrorModel);

// 	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

// 	constructor(
// 		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
// 	) { }

// 	fromModel(item: BlueprintTemplateFieldInSection): BlueprintTemplateFieldEditorModel {
// 		this.id = item.id;
// 		this.category = item.category;
// 		this.label = item.label;
// 		this.placeholder = item.placeholder;
// 		this.description = item.description;
// 		this.semantics = item.semantics;
// 		this.required = item.required;
// 		this.ordinal = item.ordinal;
// 		this.validations = item.validations ?? [];
// 		this.includeInExport = item.includeInExport ?? true;
// 		if (item.defaultValue) {
// 			this.defaultValue = new DefaultValueEditorModel(this.validationErrorModel).fromModel(item.defaultValue);
// 		}
// 		if (item.data) {
// 			this.data = new LabelDataEditorModel(this.validationErrorModel).fromModel(item.data);
// 		}
// 		if (item.visibilityRules) { item.visibilityRules.map(x => this.visibilityRules.push(new RuleEditorModel(this.validationErrorModel).fromModel(x as FieldRule))); }
// 		return this;
// 	}

// 	buildForm(params?: {
// 		context?: ValidationContext,
// 		disabled?: boolean,
// 		rootPath?: string
// 	}): UntypedFormGroup {
// 		let { context = null, disabled = false, rootPath } = params ?? {}
// 		if (context == null) {
// 			context = BlueprintTemplateFieldEditorModel.createValidationContext({
// 				validationErrorModel: this.validationErrorModel,
// 				rootPath
// 			});
// 		}

// 		return this.formBuilder.group({
// 			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
// 			category: [{ value: this.category, disabled: disabled }, context.getValidation('category').validators],
// 			label: [{ value: this.label, disabled: disabled }, context.getValidation('label').validators],
// 			placeholder: [{ value: this.placeholder, disabled: disabled }, context.getValidation('placeholder').validators],
// 			description: [{ value: this.description, disabled: disabled }, context.getValidation('description').validators],
// 			semantics: [{ value: this.semantics, disabled: disabled }, context.getValidation('semantics').validators],
// 			required: [{ value: this.required, disabled: disabled }, context.getValidation('required').validators],
// 			ordinal: [{ value: this.ordinal, disabled: disabled }, context.getValidation('ordinal').validators],
// 			validations: [{ value: this.validations, disabled: disabled }, context.getValidation('validations').validators],
// 			includeInExport: [{ value: this.includeInExport, disabled: disabled }, context.getValidation('includeInExport').validators],
// 			data: this.data.buildForm({
// 				rootPath: `${rootPath}data.`,
// 				disabled
// 			}),
// 			defaultValue: this.defaultValue.buildForm({
// 				rootPath: `${rootPath}defaultValue.`,
// 				disabled
// 			}),
// 			visibilityRules: this.formBuilder.array(
// 				(this.visibilityRules ?? []).map(
// 					(item, index) => item.buildForm({
// 						rootPath: `${rootPath}visibilityRules[${index}].`,
// 						disabled
// 					})
// 				), context.getValidation('visibilityRules').validators
// 			)
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
// 		baseValidationArray.push({ key: 'category', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}category`)] });
// 		baseValidationArray.push({ key: 'label', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}label`)] });
// 		baseValidationArray.push({ key: 'placeholder', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}placeholder`)] });
// 		baseValidationArray.push({ key: 'description', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}description`)] });
// 		baseValidationArray.push({ key: 'semantics', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}semantics`)] });
// 		baseValidationArray.push({ key: 'required', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}required`)] });
// 		baseValidationArray.push({ key: 'ordinal', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}ordinal`)] });
// 		baseValidationArray.push({ key: 'validations', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}validations`)] });
// 		baseValidationArray.push({ key: 'includeInExport', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}includeInExport`)] });
// 		baseValidationArray.push({ key: 'data', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}data`)] });
// 		baseValidationArray.push({ key: 'defaultValue', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}defaultValue`)] });
// 		baseValidationArray.push({ key: 'visibilityRules', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}visibilityRules`)] });

// 		baseContext.validation = baseValidationArray;
// 		return baseContext;
// 	}

// 	static reapplyValidators(params: {
// 		formGroup: UntypedFormGroup,
// 		validationErrorModel: ValidationErrorModel,
// 		rootPath: string
// 	}): void {
// 		const { formGroup, rootPath, validationErrorModel } = params;
// 		const context = BlueprintTemplateFieldEditorModel.createValidationContext({ rootPath, validationErrorModel });

// 		['id', 'category', 'label', 'placeholder', 'description', 'semantics', 'required', 'ordinal', 'validations', 'includeInExport'].forEach(keyField => {
// 			const control = formGroup?.get(keyField);
// 			control?.clearValidators();
// 			control?.addValidators(context.getValidation(keyField).validators);
// 		});

// 		(formGroup.get('visibilityRules') as FormArray).controls?.forEach(
// 			(control, index) => RuleEditorModel.reapplyValidators({
// 				formGroup: control as UntypedFormGroup,
// 				rootPath: `${rootPath}visibilityRules[${index}].`,
// 				validationErrorModel: validationErrorModel
// 			})
// 		);
// 	}
// }



export interface PlanBlueprintForm {
    id: FormControl<Guid>;
    hash: FormControl<string>;
    label: FormControl<string>;
	code: FormControl<string>;
	type: FormControl<Guid>;
	definition: FormGroup<PlanBlueprintDefinitionForm>;
	status: FormControl<PlanBlueprintStatus>;
	versionStatus: FormControl<PlanBlueprintVersionStatus>;
	description: FormControl<string>;
	permissions: FormControl<string[]>;
}

export interface PlanBlueprintDefinitionForm {
    sections: FormArray<FormGroup<PlanBlueprintRootSectionForm>>;
    pluginConfigurations: FormArray<PluginConfigurationForm>,
	iconConfiguration: IconConfigurationForm;
}

export interface PlanBlueprintRootSectionForm extends TemplateSectionForm{
	hasTemplates: FormControl<boolean>;
    canEditDescriptionTemplates: FormControl<boolean>;
	descriptionTemplates?: FormArray<FormGroup<PlanBlueprintSectionDescriptionTemplatesForm>>;
	prefillingSourcesEnabled: FormControl<boolean>;
	prefillingSourcesIds: FormControl<Guid[]>;
}

export interface PlanBlueprintSectionDescriptionTemplatesForm {
	descriptionTemplateGroupId: FormControl<Guid>;
	label: FormControl<string>;
	minMultiplicity: FormControl<number>;
	maxMultiplicity: FormControl<number>;
}


// export interface BlueprintFieldSetForm {
// 	id: FormControl<Guid>;
// 	ordinal: FormControl<number>;
// 	title: FormControl<string>;
// 	description: FormControl<string>;
// 	extendedDescription: FormControl<string>;
// 	additionalInformation: FormControl<string>;
// 	hasMultiplicity: FormControl<boolean>;
// 	hasCommentField: FormControl<boolean>;
// 	multiplicity: FormGroup;
// 	fields: FormArray;
// }

// export interface BlueprintSystemFieldForm {
// 	id: FormControl<Guid>;
// 	category: FormControl<PlanBlueprintFieldCategory>;
// 	label: FormControl<string>;
// 	placeholder: FormControl<string>;
// 	description: FormControl<string>;
// 	semantics: FormControl<string[]>;
// 	required: FormControl<boolean>;
// 	ordinal: FormControl<number>;
// 	systemFieldType: FormControl<SystemFieldType>;
// }

// export interface BlueprintTemplateFieldForm {
// 	id: FormControl<Guid>;
// 	category: FormControl<PlanBlueprintFieldCategory>;
// 	label: FormControl<string>;
// 	placeholder: FormControl<string>;
// 	description: FormControl<string>;
// 	semantics: FormControl<string[]>;
// 	required: FormControl<boolean>;
// 	ordinal: FormControl<number>;
// 	validations: FormControl<FieldValidationType[]>;
// 	includeInExport: FormControl<boolean>;
// 	data: FormGroup;
// 	defaultValue: FormGroup;
// 	visibilityRules: FormArray;
// }

