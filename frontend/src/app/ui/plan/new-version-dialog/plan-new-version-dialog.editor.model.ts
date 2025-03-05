import { UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { IsActive } from "@app/core/common/enum/is-active.enum";
import { PlanBlueprint } from "@app/core/model/plan-blueprint/plan-blueprint";
import { Plan, NewVersionPlanDescriptionPersist, NewVersionPlanPersist } from "@app/core/model/plan/plan";
import { BackendErrorValidator } from '@common/forms/validation/custom-validator';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { Validation, ValidationContext } from '@common/forms/validation/validation-context';
import { Guid } from "@common/types/guid";

export class PlanNewVersionDialogEditorModel implements NewVersionPlanPersist {
	id: Guid;
	label: string;
	description: String;
	blueprintId: Guid;
	descriptions: NewVersionPlanDescriptionEditorModel[] = [];
	hash?: string;

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { }

	public fromModel(item: Plan, blueprint: PlanBlueprint, label?: string, description?: string): PlanNewVersionDialogEditorModel {
		if (item) {
			this.id = item.id;
			this.label = label != undefined ? label : item.label;
			this.description = description != undefined ? description : item.description;
			this.blueprintId = blueprint?.id != undefined ? blueprint.id : item.blueprint.id;
			this.hash= item.hash;

		
			if (item.descriptions?.length > 0) {
				// initialize because blueprint changed
				this.descriptions = [];

				if (item.planDescriptionTemplates?.length > 0 && blueprint.id === item.blueprint.id) { // plan's first blueprint
					item.descriptions.forEach(description => {
                        const isActive = description.isActive === IsActive.Active;
						this.descriptions.push(new NewVersionPlanDescriptionEditorModel(this.validationErrorModel).fromModel(description.id, isActive ? description.planDescriptionTemplate.sectionId : null));
					})
				} else { // in case the user changes the blueprint from the dropdown and the new blueprint has prefilled templates
					const selectedBlueprintSections = blueprint.definition?.sections?.filter(x => x.hasTemplates) || null;
					if (selectedBlueprintSections != null){
						item.descriptions.forEach(description => {
                            const isActive = description.isActive === IsActive.Active;
							const matchingSection = selectedBlueprintSections.find(blueprintSection => blueprintSection.descriptionTemplates != null && blueprintSection.descriptionTemplates.map(y => y.descriptionTemplate?.groupId).includes(description.descriptionTemplate?.groupId)) || null;
							this.descriptions.push(new NewVersionPlanDescriptionEditorModel(this.validationErrorModel).fromModel(description.id, matchingSection != null && isActive ? matchingSection.id : null));
						})
					}
				}

			}
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			label: [{ value: this.label, disabled: disabled }, context.getValidation('label').validators],
			description: [{ value: this.description, disabled: disabled }, context.getValidation('description').validators],
			descriptions: this.formBuilder.array(
				(this.descriptions ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `descriptions[${index}].`,
						disabled: disabled
					})
				), context.getValidation('descriptions').validators
			),
			blueprintId: [{ value: this.blueprintId, disabled: disabled }, context.getValidation('blueprintId').validators],
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators]
		});
	}

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(this.validationErrorModel, 'id')] });
		baseValidationArray.push({ key: 'label', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'label')] });
		baseValidationArray.push({ key: 'description', validators: [BackendErrorValidator(this.validationErrorModel, 'description')] });
		baseValidationArray.push({ key: 'blueprintId', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'blueprintId')] });
		baseValidationArray.push({ key: 'descriptions', validators: [BackendErrorValidator(this.validationErrorModel, 'descriptions')] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}

export class NewVersionPlanDescriptionEditorModel implements NewVersionPlanDescriptionPersist {
	descriptionId: Guid;
	blueprintSectionId: Guid;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(descriptionId: Guid, blueprintSectionId: Guid): NewVersionPlanDescriptionEditorModel {
		this.descriptionId = descriptionId;
		this.blueprintSectionId = blueprintSectionId;

		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string
	}): UntypedFormGroup {
		let { context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = NewVersionPlanDescriptionEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			descriptionId: [{ value: this.descriptionId, disabled: disabled }, context.getValidation('descriptionId').validators],
			blueprintSectionId: [{ value: this.blueprintSectionId, disabled: disabled }, context.getValidation('blueprintSectionId').validators],
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'descriptionId', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}descriptionId`)] });
		baseValidationArray.push({ key: 'blueprintSectionId', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}blueprintSectionId`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}