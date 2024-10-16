import { UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { UsageLimitPeriodicityRange } from "@app/core/common/enum/usage-limit-periodicity-range";
import { UsageLimitTargetMetric } from "@app/core/common/enum/usage-limit-target-metric";
import { UsageLimit, UsageLimitDefinition, UsageLimitDefinitionPersist, UsageLimitPersist } from "@app/core/model/usage-limit/usage-limit";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";

export class UsageLimitEditorModel extends BaseEditorModel implements UsageLimitPersist {
	label: string;
	targetMetric: UsageLimitTargetMetric;
	value: number;
	permissions: string[];
	definition: DefinitionEditorModel = new DefinitionEditorModel(this.validationErrorModel);

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super(); }

	public fromModel(item: UsageLimit): UsageLimitEditorModel {
		if (item) {
			super.fromModel(item);
			this.label = item.label;
			this.targetMetric = item.targetMetric;
			this.value = item.value;
			if (item.definition) this.definition = new DefinitionEditorModel(this.validationErrorModel).fromModel(item.definition);
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			label: [{ value: this.label, disabled: disabled }, context.getValidation('label').validators],
			targetMetric: [{ value: this.targetMetric, disabled: disabled }, context.getValidation('targetMetric').validators],
			value: [{ value: this.value, disabled: disabled }, context.getValidation('value').validators],
			definition: this.definition.buildForm({
				rootPath: `definition.`,
				disabled: disabled
			}),
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators]
		});
	}

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(this.validationErrorModel, 'id')] });
		baseValidationArray.push({ key: 'label', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'label')] });
		baseValidationArray.push({ key: 'targetMetric', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'targetMetric')] });
		baseValidationArray.push({ key: 'value', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'value')] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}

export class DefinitionEditorModel implements UsageLimitDefinitionPersist {
	hasPeriodicity: Boolean = false;
	periodicityRange: UsageLimitPeriodicityRange;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: UsageLimitDefinition): DefinitionEditorModel {
		if (item) {
			this.hasPeriodicity = item.hasPeriodicity;
			this.periodicityRange = item.periodicityRange;
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
			hasPeriodicity: [{ value: this.hasPeriodicity, disabled: disabled }, context.getValidation('hasPeriodicity').validators],
			periodicityRange: [{ value: this.periodicityRange, disabled: disabled }, context.getValidation('periodicityRange').validators],
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'hasPeriodicity', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}hasPeriodicity`)] });
		baseValidationArray.push({ key: 'periodicityRange', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}periodicityRange`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}
