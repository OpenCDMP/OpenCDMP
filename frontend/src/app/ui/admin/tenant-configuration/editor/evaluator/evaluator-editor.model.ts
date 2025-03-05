import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { TenantConfigurationType } from "@app/core/common/enum/tenant-configuration-type";
import { EvaluatorSource, EvaluatorSourcePersist, EvaluatorTenantConfiguration, EvaluatorTenantConfigurationPersist, TenantConfiguration, TenantConfigurationPersist } from "@app/core/model/tenant-configuaration/tenant-configuration";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { UntypedFormArray, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";


export class TenantConfigurationEditorModel extends BaseEditorModel implements TenantConfigurationPersist {
    type: TenantConfigurationType;
    evaluatorPlugins: EvaluatorTenantConfigurationEditorModel = new EvaluatorTenantConfigurationEditorModel(this.validationErrorModel);

    public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
    protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

    constructor() { super(); this.type = TenantConfigurationType.EvaluatorPlugins; }

    public fromModel(item: TenantConfiguration): TenantConfigurationEditorModel {
        if(item){
            super.fromModel(item);
            this.type = item.type;
            if( item.evaluatorPlugins) this.evaluatorPlugins = new EvaluatorTenantConfigurationEditorModel(this.validationErrorModel).fromModel(item.evaluatorPlugins);
        } else {
            this.type = TenantConfigurationType.EvaluatorPlugins;
        }
        return this;
    }

    buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup{
        if(context == null) { context = this.createValidationContext();}

        return this.formBuilder.group({
            id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			type: [{ value: this.type, disabled: disabled }, context.getValidation('type').validators],
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators],
			evaluatorPlugins: this.evaluatorPlugins.buildForm({
				rootPath: `evaluatorPlugins.`,
			}),
		});
    }

    createValidationContext(): ValidationContext{
        const baseContext: ValidationContext = new ValidationContext;  
        const baseValidationArray: Validation[] = new Array<Validation>();
        baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(this.validationErrorModel, 'id')]});
        baseValidationArray.push({ key: 'type', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'type')] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
    }

    static reApplyEvaluatorSourcesValidators(params: {
        formGroup: UntypedFormGroup,
        validationErrorModel: ValidationErrorModel,
    }): void {

        const { formGroup, validationErrorModel } = params;
        const control = formGroup?.get('config').get('evaluator');
        EvaluatorTenantConfigurationEditorModel.reapplySourcesFieldsValidators({
            formArray: control.get('sources') as UntypedFormArray,
            rootPath: `evaluatorPlugins.`,
            validationErrorModel: validationErrorModel
        });
    }
}

export class EvaluatorTenantConfigurationEditorModel implements EvaluatorTenantConfigurationPersist{
    disableSystemSources: boolean = false;
    sources: EvaluatorSourceEditorModel[] = [];

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

    constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

    public fromModel(item: EvaluatorTenantConfiguration): EvaluatorTenantConfigurationEditorModel{
        if(item){
            this.disableSystemSources = item.disableSystemSources;
            if(item.sources) { item.sources.map(x => this.sources.push(new EvaluatorSourceEditorModel(this.validationErrorModel).fromModel(x)));}
        }
        return this;
    }

    buildForm(params?: {
        context?: ValidationContext,
        disabled?: boolean,
        rootPath?: string
    }): UntypedFormGroup {
        let{ context = null, disabled = false, rootPath} = params?? {}
        if(context == null){
            context = EvaluatorTenantConfigurationEditorModel.createValidationContext({
                validationErrorModel: this.validationErrorModel,
                rootPath
            });
        }

        const form: UntypedFormGroup = this.formBuilder.group({
            disableSystemSources: [{value: this.disableSystemSources, disabled: disabled}, context.getValidation('disableSystemSources').validators],
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
        const { rootPath = '', validationErrorModel} = params;

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
            (control, index) => EvaluatorSourceEditorModel.reapplyValidators({
                formGroup: control as UntypedFormGroup,
                rootPath: `${rootPath}sources[${index}].`,
                validationErrorModel: validationErrorModel
            })
        );
    }
}

export class EvaluatorSourceEditorModel implements EvaluatorSourcePersist{
    evaluatorId: string;
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
    ) {}

    public fromModel(item: EvaluatorSource): EvaluatorSourceEditorModel{
        if (item){
            this.evaluatorId = item.evaluatorId;
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
			context = EvaluatorSourceEditorModel.createValidationContext({
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
			evaluatorId: [{ value: this.evaluatorId, disabled: disabled }, context.getValidation('evaluatorId').validators],
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
		baseValidationArray.push({ key: 'evaluatorId', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}evaluatorId`)] });
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
		const context = EvaluatorSourceEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['url', 'evaluatorId', 'issuerUrl', 'clientId', 'clientSecret', 'scope'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}