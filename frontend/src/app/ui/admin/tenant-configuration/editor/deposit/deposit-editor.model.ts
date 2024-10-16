import { UntypedFormArray, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { TenantConfigurationType } from "@app/core/common/enum/tenant-configuration-type";
import { DepositSource, DepositSourcePersist, DepositTenantConfiguration, DepositTenantConfigurationPersist, TenantConfiguration, TenantConfigurationPersist } from "@app/core/model/tenant-configuaration/tenant-configuration";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";

export class TenantConfigurationEditorModel extends BaseEditorModel implements TenantConfigurationPersist {
	type: TenantConfigurationType;
	depositPlugins: DepositTenantConfigurationEditorModel = new DepositTenantConfigurationEditorModel(this.validationErrorModel);

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super(); this.type = TenantConfigurationType.DepositPlugins; }

	public fromModel(item: TenantConfiguration): TenantConfigurationEditorModel {
		if (item) {
			super.fromModel(item);
			this.type = item.type;
			if (item.depositPlugins) this.depositPlugins = new DepositTenantConfigurationEditorModel(this.validationErrorModel).fromModel(item.depositPlugins);
		} else {
			this.type = TenantConfigurationType.DepositPlugins;
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			type: [{ value: this.type, disabled: disabled }, context.getValidation('type').validators],
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators],
			depositPlugins: this.depositPlugins.buildForm({
				rootPath: `depositPlugins.`,
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

	static reApplyDepositSourcesValidators(params: {
        formGroup: UntypedFormGroup,
        validationErrorModel: ValidationErrorModel,
    }): void {

        const { formGroup, validationErrorModel } = params;
        const control = formGroup?.get('config').get('deposit');
        DepositTenantConfigurationEditorModel.reapplySourcesFieldsValidators({
            formArray: control.get('sources') as UntypedFormArray,
            rootPath: `depositPlugins.`,
            validationErrorModel: validationErrorModel
        });
    }
}

export class DepositTenantConfigurationEditorModel implements DepositTenantConfigurationPersist {
	disableSystemSources: boolean = false;
	sources: DepositSourceEditorModel[] = [];

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: DepositTenantConfiguration): DepositTenantConfigurationEditorModel {
		if (item) {
			this.disableSystemSources = item.disableSystemSources;
			if (item.sources) { item.sources.map(x => this.sources.push(new DepositSourceEditorModel(this.validationErrorModel).fromModel(x))); }
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
			context = DepositTenantConfigurationEditorModel.createValidationContext({
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
            (control, index) => DepositSourceEditorModel.reapplyValidators({
                formGroup: control as UntypedFormGroup,
                rootPath: `${rootPath}sources[${index}].`,
                validationErrorModel: validationErrorModel
            })
        );
    }
}


export class DepositSourceEditorModel implements DepositSourcePersist {
	repositoryId: string;
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

	public fromModel(item: DepositSource): DepositSourceEditorModel {
		if (item) {
			this.repositoryId = item.repositoryId;
			this.url = item.url;
			this.issuerUrl = item.issuerUrl;
			this.clientId = item.clientId;
			this.clientSecret = item.clientSecret;
			this.scope = item.scope;
			this.pdfTransformerId = item.pdfTransformerId;
			this.rdaTransformerId = item.rdaTransformerId;
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
			context = DepositSourceEditorModel.createValidationContext({
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
			repositoryId: [{ value: this.repositoryId, disabled: disabled }, context.getValidation('repositoryId').validators],
			pdfTransformerId: [{ value: this.pdfTransformerId, disabled: disabled }, context.getValidation('pdfTransformerId').validators],
			rdaTransformerId: [{ value: this.rdaTransformerId, disabled: disabled }, context.getValidation('rdaTransformerId').validators],
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
		baseValidationArray.push({ key: 'repositoryId', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}repositoryId`)] });
		baseValidationArray.push({ key: 'issuerUrl', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}issuerUrl`)] });
		baseValidationArray.push({ key: 'clientId', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}clientId`)] });
		baseValidationArray.push({ key: 'clientSecret', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}clientSecret`)] });
		baseValidationArray.push({ key: 'scope', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}scope`)] });
		baseValidationArray.push({ key: 'pdfTransformerId', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}pdfTransformerId`)] });
		baseValidationArray.push({ key: 'rdaTransformerId', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}rdaTransformerId`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = DepositSourceEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['url', 'repositoryId', 'issuerUrl', 'clientId', 'clientSecret', 'scope', 'pdfTransformerId', 'pdfTransformerId'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}
