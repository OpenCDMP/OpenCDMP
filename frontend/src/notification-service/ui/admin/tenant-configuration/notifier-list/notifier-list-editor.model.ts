import { FormControl, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";
import { NotificationContactType } from "@notification-service/core/enum/notification-contact-type";
import { NotificationType } from "@notification-service/core/enum/notification-type.enum";
import { TenantConfigurationType } from "@notification-service/core/enum/tenant-configuration-type";
import { NotifierListTenantConfiguration, NotifierListTenantConfigurationPersist, TenantConfiguration, TenantConfigurationPersist } from "@notification-service/core/model/tenant-configuration";

export class TenantConfigurationEditorModel extends BaseEditorModel implements TenantConfigurationPersist {
	type: TenantConfigurationType;
	notifierList: NotifierListTenantConfigurationEditorModel = new NotifierListTenantConfigurationEditorModel(this.validationErrorModel);

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super(); this.type = TenantConfigurationType.NotifierList; }

	public fromModel(item: TenantConfiguration): TenantConfigurationEditorModel {
		if (item) {
			super.fromModel(item);
			this.type = item.type;
			if (item.notifierList) this.notifierList = new NotifierListTenantConfigurationEditorModel(this.validationErrorModel).fromModel(item.notifierList);
		} else {
			this.type = TenantConfigurationType.NotifierList;
		}
		return this;
	}

	buildForm(availableNotificationTypes: NotificationType[], availableNotifiers: { [key: string]: NotificationContactType[] }, context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			type: [{ value: this.type, disabled: disabled }, context.getValidation('type').validators],
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators],
			notifierList: this.notifierList.buildForm({
				availableNotificationTypes: availableNotificationTypes,
				availableNotifiers: availableNotifiers,
				rootPath: `NotifierList.`,
			}),
		});
	}

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(this.validationErrorModel, 'id')] });
		baseValidationArray.push({ key: 'type', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'type')] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}

export class NotifierListTenantConfigurationEditorModel implements NotifierListTenantConfigurationPersist {
	notifiers: { [key: string]: NotificationContactType[] };

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: NotifierListTenantConfiguration): NotifierListTenantConfigurationEditorModel {
		if (item) {
			this.notifiers = item.notifiers;
		}
		return this;
	}

	buildForm(params?: {
		availableNotificationTypes: NotificationType[],
		availableNotifiers: { [key: string]: NotificationContactType[] };
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string
	}): UntypedFormGroup {
		let { context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = NotifierListTenantConfigurationEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}


		const notifiersFormGroup = this.formBuilder.group({});
		if (params?.availableNotificationTypes) {
			params.availableNotificationTypes.forEach(type => {
				notifiersFormGroup.addControl(type, new FormControl(this.notifiers ? this.notifiers[type] : (params.availableNotifiers ? params.availableNotifiers[type] : undefined)));
			});
		}
		const form: UntypedFormGroup = this.formBuilder.group({
			notifiers: notifiersFormGroup,
		});

		return form;
	}


	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'notifiers', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}notifiers`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}
