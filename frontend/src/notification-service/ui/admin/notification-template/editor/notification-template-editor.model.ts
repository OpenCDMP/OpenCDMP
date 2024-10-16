import { FormArray, FormControl, UntypedFormArray, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { NotificationFieldInfo, NotificationFieldInfoPersist, NotificationFieldOptions, NotificationFieldOptionsPersist, NotificationTemplate, NotificationTemplatePersist, NotificationTemplateValue, NotificationTemplateValuePersist } from '@notification-service/core/model/notification-template.model';
import { BackendErrorValidator } from '@common/forms/validation/custom-validator';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { Validation, ValidationContext } from '@common/forms/validation/validation-context';
import { Guid } from '@common/types/guid';
import { BaseEditorModel } from '@common/base/base-form-editor-model';
import { NotificationTemplateChannel } from '@notification-service/core/enum/notification-template-channel.enum';
import { NotificationTemplateKind } from '@notification-service/core/enum/notification-template-kind.enum';
import { NotificationType } from '@notification-service/core/enum/notification-type.enum';
import { EmailOverrideMode } from '@notification-service/core/enum/email-override-mode';
import { NotificationDataType } from '@notification-service/core/enum/notification-data-type';

export class NotificationTemplateEditorModel extends BaseEditorModel implements NotificationTemplatePersist {
	channel: NotificationTemplateChannel;
	notificationType: NotificationType;
	kind: NotificationTemplateKind;
	languageCode: string;
	value: NotificationTemplateValueEditorModel = new NotificationTemplateValueEditorModel();
	permissions: string[];

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super();}

	public fromModel(item: NotificationTemplate): NotificationTemplateEditorModel {
		if(item){
			super.fromModel(item);
			this.channel = item.channel;
			this.notificationType = item.notificationType;
			this.kind = item.kind;
			if(item.languageCode && item.languageCode) this.languageCode = item.languageCode;
			if (item.value) { this.value = new NotificationTemplateValueEditorModel(this.validationErrorModel).fromModel(item.value); }
		}

		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			channel: [{ value: this.channel, disabled: disabled }, context.getValidation('channel').validators],
			notificationType: [{ value: this.notificationType, disabled: disabled }, context.getValidation('notificationType').validators],
			kind: [{ value: this.kind, disabled: disabled }, context.getValidation('kind').validators],
			languageCode: [{ value: this.languageCode, disabled: disabled }, context.getValidation('languageCode').validators],
			value: this.value.buildForm({
				rootPath: `value.`,
				disabled: disabled
			}),
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators],
		});
	}

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(this.validationErrorModel, 'id')] });
		baseValidationArray.push({ key: 'channel', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'channel')] });
		baseValidationArray.push({ key: 'notificationType', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'notificationType')] });
		baseValidationArray.push({ key: 'kind', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'kind')] });
		baseValidationArray.push({ key: 'languageCode', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'languageCode')] });
		baseValidationArray.push({ key: 'value', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'value')] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reApplyValueValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
	}): void {

		const { formGroup, validationErrorModel } = params;
		const control = formGroup?.get('value');
		NotificationTemplateValueEditorModel.reapplyValidators({
			formGroup: control as UntypedFormGroup,
			rootPath: `value.`,
			validationErrorModel: validationErrorModel
		});
	}
}

export class NotificationTemplateValueEditorModel implements NotificationTemplateValuePersist {
	subjectText: string;
	subjectKey: string;
	subjectFieldOptions: NotificationFieldOptionsEditorModel = new NotificationFieldOptionsEditorModel();
	bodyText: string;
	bodyKey: string;
	priorityKey: string;
    allowAttachments: Boolean = false;
    cc: string[] = [];
    ccMode: EmailOverrideMode;
    bcc: string[] = [];
    bccMode: EmailOverrideMode;
    extraDataKeys: string[] = [];
	bodyFieldOptions: NotificationFieldOptionsEditorModel = new NotificationFieldOptionsEditorModel();

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: NotificationTemplateValue): NotificationTemplateValueEditorModel {
		if (item) {
			this.subjectText = item.subjectText;
			this.subjectKey = item.subjectKey;
			if (item.subjectFieldOptions) this.subjectFieldOptions = new NotificationFieldOptionsEditorModel(this.validationErrorModel).fromModel(item.subjectFieldOptions);
			this.bodyText = item.bodyText;
			this.bodyKey = item.bodyKey;
			this.priorityKey = item.priorityKey;
			this.allowAttachments = item.allowAttachments;
			this.cc = item.cc;
			this.ccMode = item.ccMode;
			this.bcc = item.bcc;
			this.bccMode = item.bccMode;
			this.extraDataKeys = item.extraDataKeys;
			if (item.bodyFieldOptions)  this.bodyFieldOptions = new NotificationFieldOptionsEditorModel(this.validationErrorModel).fromModel(item.bodyFieldOptions);
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
			context = NotificationTemplateValueEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}
		return this.formBuilder.group({
			subjectText: [{ value: this.subjectText, disabled: disabled }, context.getValidation('subjectText').validators],
			subjectKey: [{ value: this.subjectKey, disabled: disabled }, context.getValidation('subjectKey').validators],
			subjectFieldOptions: this.subjectFieldOptions.buildForm({
				rootPath: `${rootPath}subjectFieldOptions.`,
				disabled: disabled
			}),
			bodyText: [{ value: this.bodyText, disabled: disabled }, context.getValidation('bodyText').validators],
			bodyKey: [{ value: this.bodyKey, disabled: disabled }, context.getValidation('bodyKey').validators],
			priorityKey: [{ value: this.priorityKey, disabled: disabled }, context.getValidation('priorityKey').validators],
			allowAttachments: [{ value: this.allowAttachments, disabled: disabled }, context.getValidation('allowAttachments').validators],
			cc: [{ value: this.cc, disabled: disabled }, context.getValidation('cc').validators],
			ccMode: [{ value: this.ccMode, disabled: disabled }, context.getValidation('ccMode').validators],
			bcc: [{ value: this.bcc, disabled: disabled }, context.getValidation('bcc').validators],
			bccMode: [{ value: this.bccMode, disabled: disabled }, context.getValidation('bccMode').validators],
			extraDataKeys: [{ value: this.extraDataKeys, disabled: disabled }, context.getValidation('extraDataKeys').validators],
			bodyFieldOptions: this.bodyFieldOptions.buildForm({
				rootPath: `${rootPath}bodyFieldOptions.`,
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
		baseValidationArray.push({ key: 'subjectText', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}subjectText`)] });
		baseValidationArray.push({ key: 'subjectKey', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}subjectKey`)] });
		baseValidationArray.push({ key: 'bodyText', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}bodyText`)] });
		baseValidationArray.push({ key: 'bodyKey', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}bodyKey`)] });
		baseValidationArray.push({ key: 'priorityKey', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}priorityKey`)] });
		baseValidationArray.push({ key: 'allowAttachments', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}allowAttachments`)] });
		baseValidationArray.push({ key: 'cc', validators: [BackendErrorValidator(validationErrorModel,  `${rootPath}cc`)] });
		baseValidationArray.push({ key: 'ccMode', validators: [Validators.required, BackendErrorValidator(validationErrorModel,  `${rootPath}ccMode`)] });
		baseValidationArray.push({ key: 'bcc', validators: [BackendErrorValidator(validationErrorModel,  `${rootPath}bcc`)] });
		baseValidationArray.push({ key: 'bccMode', validators: [Validators.required, BackendErrorValidator(validationErrorModel,  `${rootPath}bccMode`)] });
		baseValidationArray.push({ key: 'extraDataKeys', validators: [BackendErrorValidator(validationErrorModel,  `${rootPath}extraDataKeys`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = NotificationTemplateValueEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['subjectText', 'subjectKey', 'bodyText', 'bodyKey', 'priorityKey', 'allowAttachments', 'cc', 'ccMode', 'bcc', 'bccMode','extraDataKeys'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});

		NotificationFieldOptionsEditorModel.reapplyValidators({
			formGroup: formGroup?.get('subjectFieldOptions') as UntypedFormGroup,
			rootPath: `${rootPath}subjectFieldOptions.`,
			validationErrorModel: validationErrorModel
		});

		NotificationFieldOptionsEditorModel.reapplyValidators({
			formGroup: formGroup?.get('bodyFieldOptions') as UntypedFormGroup,
			rootPath: `${rootPath}bodyFieldOptions.`,
			validationErrorModel: validationErrorModel
		});
	}
}

export class NotificationFieldOptionsEditorModel implements NotificationFieldOptionsPersist {
	mandatory?: string[] = [];
	optional?: NotificationFieldInfoEditorModel[] = [];
	formatting?: { [key: string]: string } = {};

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: NotificationFieldOptions): NotificationFieldOptionsEditorModel {
		if (item) {
			this.mandatory = item.mandatory;
			if(item.optional) { item.optional.map(x => this.optional.push(new NotificationFieldInfoEditorModel(this.validationErrorModel).fromModel(x))); }
			this.formatting = item.formatting;
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
			context = NotificationFieldOptionsEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			mandatory: [{ value: this.mandatory, disabled: disabled }, context.getValidation('mandatory').validators],
			formatting: [{ value: this.formatting, disabled: disabled }, context.getValidation('formatting').validators],
			optional:  this.formBuilder.array(
				(this.optional ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}optional[${index}].`,
						disabled: disabled
					})
				), context.getValidation('optional').validators
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
		baseValidationArray.push({ key: 'mandatory', validators: [BackendErrorValidator(validationErrorModel,`${rootPath}mandatory`)] });
		baseValidationArray.push({ key: 'optional', validators: [BackendErrorValidator(validationErrorModel,`${rootPath}optional`)] });
		baseValidationArray.push({ key: 'formatting', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}formatting`)] });
		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = NotificationFieldOptionsEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['mandatory', 'formatting'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});

		(formGroup.get('optional') as FormArray).controls?.forEach(
			(control, index) => NotificationFieldInfoEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}optional[${index}].`,
				validationErrorModel: validationErrorModel
			}
			)
		);
	}

}

export class NotificationFieldInfoEditorModel implements NotificationFieldInfoPersist {
	key: string;
	type: NotificationDataType;
	value: string;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: NotificationFieldInfo): NotificationFieldInfoEditorModel {
		if (item) {
			this.key = item.key;
			this.type = item.type;
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
			context = NotificationFieldInfoEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}
		return this.formBuilder.group({
			key: [{ value: this.key, disabled: disabled }, context.getValidation('key').validators],
			type: [{ value: this.type, disabled: disabled }, context.getValidation('type').validators],
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
		baseValidationArray.push({ key: 'type', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}type`)] });
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
		const context = NotificationFieldInfoEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['key', 'type', 'value'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});
	}

}
