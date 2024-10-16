import { UntypedFormArray, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { TenantConfigurationType } from "@app/core/common/enum/tenant-configuration-type";
import { FileTransformerSource, FileTransformerSourcePersist, FileTransformerTenantConfiguration, FileTransformerTenantConfigurationPersist, TenantConfiguration, TenantConfigurationPersist } from "@app/core/model/tenant-configuaration/tenant-configuration";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";

export class TenantConfigurationEditorModel extends BaseEditorModel implements TenantConfigurationPersist {
	type: TenantConfigurationType;
	fileTransformerPlugins: FileTransformerTenantConfigurationEditorModel = new FileTransformerTenantConfigurationEditorModel(this.validationErrorModel);

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super(); this.type = TenantConfigurationType.FileTransformerPlugins; }

	public fromModel(item: TenantConfiguration): TenantConfigurationEditorModel {
		if (item) {
			super.fromModel(item);
			this.type = item.type;
			if (item.fileTransformerPlugins) this.fileTransformerPlugins = new FileTransformerTenantConfigurationEditorModel(this.validationErrorModel).fromModel(item.fileTransformerPlugins);
		} else {
			this.type = TenantConfigurationType.FileTransformerPlugins;
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			type: [{ value: this.type, disabled: disabled }, context.getValidation('type').validators],
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators],
			fileTransformerPlugins: this.fileTransformerPlugins.buildForm({
				rootPath: `fileTransformerPlugins.`,
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

	static reApplyFileTransformerSourcesValidators(params: {
        formGroup: UntypedFormGroup,
        validationErrorModel: ValidationErrorModel,
    }): void {

        const { formGroup, validationErrorModel } = params;
        const control = formGroup?.get('config').get('fileTransformer');
        FileTransformerTenantConfigurationEditorModel.reapplySourcesFieldsValidators({
            formArray: control.get('sources') as UntypedFormArray,
            rootPath: `fileTransformerPlugins.`,
            validationErrorModel: validationErrorModel
        });
    }
}

export class FileTransformerTenantConfigurationEditorModel implements FileTransformerTenantConfigurationPersist {
	disableSystemSources: boolean = false;
	sources: FileTransformerSourceEditorModel[] = [];

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: FileTransformerTenantConfiguration): FileTransformerTenantConfigurationEditorModel {
		if (item) {
			this.disableSystemSources = item.disableSystemSources;
			if (item.sources) { item.sources.map(x => this.sources.push(new FileTransformerSourceEditorModel(this.validationErrorModel).fromModel(x))); }
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
			context = FileTransformerTenantConfigurationEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		const form: UntypedFormGroup = this.formBuilder.group({
			disableSystemSources: [{ value: this.disableSystemSources, disabled: disabled }, context.getValidation('disableSystemSources').validators],
			sources: this.formBuilder.array(
                (this.sources ?? []).map(
                    (item, index) => item.buildForm({
                        rootPath: `${rootPath}sources[${index}].`
                    })
                ), context.getValidation('sources')
            ),
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
		baseValidationArray.push({ key: 'disableSystemSources', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}disableSystemSources`)] });
		baseValidationArray.push({ key: 'sources', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}sources`)] });
		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplySourcesFieldsValidators(params: {
        formArray: UntypedFormArray,
        validationErrorModel: ValidationErrorModel,
        rootPath: string
    }): void {
        const { validationErrorModel, rootPath, formArray } = params;
        formArray?.controls?.forEach(
            (control, index) => FileTransformerSourceEditorModel.reapplyValidators({
                formGroup: control as UntypedFormGroup,
                rootPath: `${rootPath}sources[${index}].`,
                validationErrorModel: validationErrorModel
            })
        );
    }
}


export class FileTransformerSourceEditorModel implements FileTransformerSourcePersist {
	transformerId: string;
	url: string;
	issuerUrl: string;
	clientId: string;
	clientSecret: string;
	scope: string;
	pdfTransformerId: string;
	rdaTransformerId: string;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: FileTransformerSource): FileTransformerSourceEditorModel {
		if (item) {
			this.transformerId = item.transformerId;
			this.url = item.url;
			this.issuerUrl = item.issuerUrl;
			this.clientId = item.clientId;
			this.clientSecret = item.clientSecret;
			this.scope = item.scope;
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
			context = FileTransformerSourceEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			url: [{ value: this.url, disabled: disabled }, context.getValidation('url').validators],
			issuerUrl: [{ value: this.issuerUrl, disabled: disabled }, context.getValidation('issuerUrl').validators],
			clientId: [{ value: this.clientId, disabled: disabled }, context.getValidation('clientId').validators],
			clientSecret: [{ value: this.clientSecret, disabled: disabled }, context.getValidation('clientSecret').validators],
			scope: [{ value: this.scope, disabled: disabled }, context.getValidation('scope').validators],
			transformerId: [{ value: this.transformerId, disabled: disabled }, context.getValidation('transformerId').validators],
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'url', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}url`)] });
		baseValidationArray.push({ key: 'transformerId', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}transformerId`)] });
		baseValidationArray.push({ key: 'issuerUrl', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}issuerUrl`)] });
		baseValidationArray.push({ key: 'clientId', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}clientId`)] });
		baseValidationArray.push({ key: 'clientSecret', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}clientSecret`)] });
		baseValidationArray.push({ key: 'scope', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}scope`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = FileTransformerSourceEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['url', 'transformerId', 'issuerUrl', 'clientId', 'clientSecret', 'scope'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}
