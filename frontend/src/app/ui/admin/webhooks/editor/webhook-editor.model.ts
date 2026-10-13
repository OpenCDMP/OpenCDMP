import { UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { WebhookEventType } from "@app/core/common/enum/webhook-event-type.enum";
import { WebhookSubscription, WebhookSubscriptionPersist } from "@app/core/model/webhook/webhook";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";

export class WebhookEditorModel extends BaseEditorModel implements WebhookSubscriptionPersist {
	label: string;
	url: string;
	eventTypes: WebhookEventType[];
	enabled: boolean = true;
	permissions: string[];

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super(); }

	public fromModel(item: WebhookSubscription): WebhookEditorModel {
		if (item) {
			super.fromModel(item);
			this.label = item.label;
			this.url = item.url;
			this.eventTypes = item.eventTypes;
			this.enabled = item.enabled;
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			label: [{ value: this.label, disabled: disabled }, context.getValidation('label').validators],
			url: [{ value: this.url, disabled: disabled }, context.getValidation('url').validators],
			eventTypes: [{ value: this.eventTypes, disabled: disabled }, context.getValidation('eventTypes').validators],
			enabled: [{ value: this.enabled, disabled: disabled }, context.getValidation('enabled').validators],
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators]
		});
	}

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(this.validationErrorModel, 'id')] });
		baseValidationArray.push({ key: 'label', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'label')] });
		baseValidationArray.push({ key: 'url', validators: [Validators.required, Validators.pattern(/^https?:\/\/.+/), BackendErrorValidator(this.validationErrorModel, 'url')] });
		baseValidationArray.push({ key: 'eventTypes', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'eventTypes')] });
		baseValidationArray.push({ key: 'enabled', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'enabled')] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}
