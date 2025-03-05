import { FormArray, FormControl, FormGroup, UntypedFormArray, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ExternalFetcherApiHTTPMethodType } from "@app/core/common/enum/external-fetcher-api-http-method-type";
import { ExternalFetcherSourceType } from "@app/core/common/enum/external-fetcher-source-type";
import { AuthenticationConfiguration, AuthenticationConfigurationPersist, ExternalFetcherBaseSourceConfiguration, ExternalFetcherBaseSourceConfigurationPersist, HeaderConfig, HeadersConfigPersist, QueryCaseConfig, QueryCaseConfigPersist, QueryConfig, QueryConfigPersist, ResultFieldsMappingConfiguration, ResultFieldsMappingConfigurationPersist, ResultsConfiguration, ResultsConfigurationPersist, Static, StaticOption, StaticOptionPersist, StaticPersist } from "@app/core/model/external-fetcher/external-fetcher";
import { Guid } from "@common/types/guid";
import { ExternalFetcherApiHeaderType } from "@app/core/common/enum/ExternalFetcherApiHeader.enum";

export class ExternalFetcherBaseSourceConfigurationEditorModel implements ExternalFetcherBaseSourceConfigurationPersist {
	type: ExternalFetcherSourceType = ExternalFetcherSourceType.API;
	key: string;
	label: string;
	ordinal: number;

	url: string;
	results: ResultsConfigurationEditorModel = new ResultsConfigurationEditorModel(this.validationErrorModel);
	paginationPath: string;
	contentType: string;
	firstPage: string;
	httpMethod: ExternalFetcherApiHTTPMethodType;
	requestBody?: string;
	filterType?: string;
	auth: AuthenticationConfigurationEditorModel = new AuthenticationConfigurationEditorModel(this.validationErrorModel);
	queries?: QueryConfigEditorModel[] = [];
	headers?: HeaderConfigEditorModel[] = [];

	items: StaticEditorModel[] = [];

	referenceTypeDependencyIds: Guid[];

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: ExternalFetcherBaseSourceConfiguration): ExternalFetcherBaseSourceConfigurationEditorModel {
		if (item) {
			this.type = item.type;
			this.key = item.key;
			this.label = item.label;
			this.ordinal = item.ordinal;

			if (item.url) this.url = item.url;
			if (item.results) this.results = new ResultsConfigurationEditorModel(this.validationErrorModel).fromModel(item.results);
			if (item.paginationPath) this.paginationPath = item.paginationPath;
			if (item.contentType) this.contentType = item.contentType;
			if (item.firstPage) this.firstPage = item.firstPage;
			if (item.httpMethod != null) this.httpMethod = item.httpMethod;
			if (item.requestBody) this.requestBody = item.requestBody;
			if (item.filterType) this.filterType = item.filterType;
			if (item.auth) this.auth = new AuthenticationConfigurationEditorModel(this.validationErrorModel).fromModel(item.auth);
			if (item.queries) { item.queries.map(x => this.queries.push(new QueryConfigEditorModel(this.validationErrorModel).fromModel(x))); }
			if (item.headers) { item.headers.map(x => this.headers.push(new HeaderConfigEditorModel(this.validationErrorModel).fromModel(x))); }

			if (item.items) item.items.map(x => this.items.push(new StaticEditorModel(this.validationErrorModel).fromModel(x)));

			if (item.referenceTypeDependencies) { this.referenceTypeDependencyIds = item.referenceTypeDependencies.map(x => x.id)}
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
			context = ExternalFetcherBaseSourceConfigurationEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			type: [{ value: this.type, disabled: disabled }, context.getValidation('type').validators],
			key: [{ value: this.key, disabled: disabled }, context.getValidation('key').validators],
			label: [{ value: this.label, disabled: disabled }, context.getValidation('label').validators],
			ordinal: [{ value: this.ordinal, disabled: disabled }, context.getValidation('ordinal').validators],

			url: [{ value: this.url, disabled: disabled }, context.getValidation('url').validators],
			results: this.results.buildForm({
				rootPath: `${rootPath}results.`,
				disabled: disabled
			}),
			paginationPath: [{ value: this.paginationPath, disabled: disabled }, context.getValidation('paginationPath').validators],
			contentType: [{ value: this.contentType, disabled: disabled }, context.getValidation('contentType').validators],
			firstPage: [{ value: this.firstPage, disabled: disabled }, context.getValidation('firstPage').validators],
			httpMethod: [{ value: this.httpMethod, disabled: disabled }, context.getValidation('httpMethod').validators],
			requestBody: [{ value: this.requestBody, disabled: disabled }, context.getValidation('requestBody').validators],
			filterType: [{ value: this.filterType, disabled: disabled }, context.getValidation('filterType').validators],
			auth: this.auth.buildForm({
				rootPath: `${rootPath}auth.`,
				disabled: disabled
			}),
			queries: this.formBuilder.array(
				(this.queries ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}queries[${index}].`,
						disabled: disabled
					})
				), context.getValidation('queries').validators
			),
			headers: this.formBuilder.array(
				(this.headers ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}headers[${index}].`,
						disabled: disabled
					})
				), context.getValidation('headers').validators
			),
			items: this.formBuilder.array(
				(this.items ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}items[${index}].`,
						disabled: disabled
					})
				), context.getValidation('items').validators
			),
			referenceTypeDependencyIds: [{ value: this.referenceTypeDependencyIds, disabled: disabled }, context.getValidation('referenceTypeDependencyIds').validators],

		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'type', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}type`)] });
		baseValidationArray.push({ key: 'key', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}key`)] });
		baseValidationArray.push({ key: 'label', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}label`)] });
		baseValidationArray.push({ key: 'ordinal', validators: [Validators.required, Validators.pattern("^[0-9]*$"), BackendErrorValidator(validationErrorModel, `${rootPath}ordinal`)] });

		baseValidationArray.push({ key: 'url', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}url`)] });
		baseValidationArray.push({ key: 'paginationPath', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}paginationPath`)] });
		baseValidationArray.push({ key: 'contentType', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}contentType`)] });
		baseValidationArray.push({ key: 'firstPage', validators: [Validators.pattern("^[0-9]*$"), BackendErrorValidator(validationErrorModel, `${rootPath}firstPage`)] });
		baseValidationArray.push({ key: 'httpMethod', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}httpMethod`)] });
		baseValidationArray.push({ key: 'requestBody', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}requestBody`)] });
		baseValidationArray.push({ key: 'filterType', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}filterType`)] });
		baseValidationArray.push({ key: 'results', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}results`)] });
		baseValidationArray.push({ key: 'queries', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}queries`)] });
		baseValidationArray.push({ key: 'headers', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}headers`)] });


		baseValidationArray.push({ key: 'items', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}items`)] });

		baseValidationArray.push({ key: 'referenceTypeDependencyIds', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}referenceTypeDependencyIds`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = ExternalFetcherBaseSourceConfigurationEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['type', 'key', 'label', 'ordinal', 'url', 'paginationPath', 'contentType', 'firstPage', 'httpMethod', 'requestBody','filterType', 'referenceTypeDependencyIds'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});

		AuthenticationConfigurationEditorModel.reapplyAuthValidators({
			formGroup: formGroup?.get('auth') as UntypedFormGroup,
			rootPath: `${rootPath}auth.`,
			validationErrorModel: validationErrorModel
		});

		ResultsConfigurationEditorModel.reapplyValidators({
			formGroup: formGroup?.get('results') as UntypedFormGroup,
			rootPath: `${rootPath}results.`,
			validationErrorModel: validationErrorModel
		});

		(formGroup.get('items') as FormArray).controls?.forEach(
			(control, index) => StaticEditorModel.reapplyStaticValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}items[${index}].`,
				validationErrorModel: validationErrorModel
			}
			)
		);

		(formGroup.get('queries') as FormArray).controls?.forEach(
			(control, index) => QueryConfigEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}queries[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);

		(formGroup.get('headers') as FormArray).controls?.forEach(
			(control, index) => HeaderConfigEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}headers[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);

	}
}

export class ResultsConfigurationEditorModel implements ResultsConfigurationPersist {
	public resultsArrayPath: string;
	public fieldsMapping: ResultFieldsMappingConfigurationEditorModel[] = [];

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: ResultsConfiguration): ResultsConfigurationEditorModel {
		this.resultsArrayPath = item.resultsArrayPath;
		if (item.fieldsMapping) { item.fieldsMapping.map(x => this.fieldsMapping.push(new ResultFieldsMappingConfigurationEditorModel(this.validationErrorModel).fromModel(x))); }
		else {
			this.fieldsMapping.push(new ResultFieldsMappingConfigurationEditorModel(this.validationErrorModel).fromModel({ code: 'reference_id', responsePath: undefined }));
			this.fieldsMapping.push(new ResultFieldsMappingConfigurationEditorModel(this.validationErrorModel).fromModel({ code: 'label', responsePath: undefined }));
			this.fieldsMapping.push(new ResultFieldsMappingConfigurationEditorModel(this.validationErrorModel).fromModel({ code: 'description', responsePath: undefined }));
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
			context = ResultsConfigurationEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			resultsArrayPath: [{ value: this.resultsArrayPath, disabled: disabled }, context.getValidation('resultsArrayPath').validators],
			fieldsMapping: this.formBuilder.array(
				(this.fieldsMapping ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}fieldsMapping[${index}].`,
						disabled: disabled
					})
				), context.getValidation('fieldsMapping').validators
			)

		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'resultsArrayPath', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}resultsArrayPath`)] });
		baseValidationArray.push({ key: 'fieldsMapping', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}fieldsMapping`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = ResultsConfigurationEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['resultsArrayPath'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});

		(formGroup.get('fieldsMapping') as FormArray).controls?.forEach(
			(control, index) => ResultFieldsMappingConfigurationEditorModel.reapplyFieldsMappingValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}fieldsMapping[${index}].`,
				validationErrorModel: validationErrorModel
			}
			)
		);
	}
}

export class ResultFieldsMappingConfigurationEditorModel implements ResultFieldsMappingConfigurationPersist {
	public code: string;
	public responsePath: string;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: ResultFieldsMappingConfiguration): ResultFieldsMappingConfigurationEditorModel {
		this.code = item.code;
		this.responsePath = item.responsePath;

		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string
	}): UntypedFormGroup {
		let { context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = ResultFieldsMappingConfigurationEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			code: [{ value: this.code, disabled: disabled }, context.getValidation('code').validators],
			responsePath: [{ value: this.responsePath, disabled: disabled }, context.getValidation('responsePath').validators],
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'code', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}code`)] });
		baseValidationArray.push({ key: 'responsePath', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}responsePath`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyFieldsMappingValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = ResultFieldsMappingConfigurationEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['code', 'responsePath'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});
	}
}

export class AuthenticationConfigurationEditorModel implements AuthenticationConfigurationPersist {
	public enabled: boolean = false;
	public authUrl: string;
	public authMethod: ExternalFetcherApiHTTPMethodType;
	public authTokenPath: string;
	public authRequestBody: string;
	public type: string;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: AuthenticationConfiguration): AuthenticationConfigurationEditorModel {
		this.enabled = item.enabled;
		this.authUrl = item.authUrl;
		this.authMethod = item.authMethod;
		this.authTokenPath = item.authTokenPath;
		this.authRequestBody = item.authRequestBody;
		this.type = item.type;

		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string
	}): UntypedFormGroup {
		let { context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = AuthenticationConfigurationEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			enabled: [{ value: this.enabled, disabled: disabled }, context.getValidation('enabled').validators],
			authUrl: [{ value: this.authUrl, disabled: disabled }, context.getValidation('authUrl').validators],
			authMethod: [{ value: this.authMethod, disabled: disabled }, context.getValidation('authMethod').validators],
			authTokenPath: [{ value: this.authTokenPath, disabled: disabled }, context.getValidation('authTokenPath').validators],
			authRequestBody: [{ value: this.authRequestBody, disabled: disabled }, context.getValidation('authRequestBody').validators],
			type: [{ value: this.type, disabled: disabled }, context.getValidation('type').validators],
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'enabled', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}enabled`)] });
		baseValidationArray.push({ key: 'authUrl', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}authUrl`)] });
		baseValidationArray.push({ key: 'authMethod', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}authMethod`)] });
		baseValidationArray.push({ key: 'authTokenPath', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}authTokenPath`)] });
		baseValidationArray.push({ key: 'authRequestBody', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}authRequestBody`)] });
		baseValidationArray.push({ key: 'type', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}type`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyAuthValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = AuthenticationConfigurationEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['enabled', 'authUrl', 'authMethod', 'authTokenPath', 'authRequestBody', 'type'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}

export class QueryConfigEditorModel implements QueryConfigPersist {
	public name: string;
	public defaultValue: string;
	public cases: QueryCaseConfigEditorModel[] = [];

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: QueryConfig): QueryConfigEditorModel {
		this.name = item.name;
		this.defaultValue = item.defaultValue;
		if (item.cases) { item.cases.map(x => this.cases.push(new QueryCaseConfigEditorModel(this.validationErrorModel).fromModel(x))); }

		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string
	}): UntypedFormGroup {
		let { context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = QueryConfigEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			name: [{ value: this.name, disabled: disabled }, context.getValidation('name').validators],
			defaultValue: [{ value: this.defaultValue, disabled: disabled }, context.getValidation('defaultValue').validators],
			cases: this.formBuilder.array(
				(this.cases ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}cases[${index}].`,
						disabled: disabled
					})
				), context.getValidation('cases').validators
			)
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'name', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}name`)] });
		baseValidationArray.push({ key: 'defaultValue', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}defaultValue`)] });
		baseValidationArray.push({ key: 'cases', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}cases`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = QueryConfigEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['name', 'defaultValue'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});

		(formGroup.get('cases') as FormArray).controls?.forEach(
			(control, index) => QueryCaseConfigEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}cases[${index}].`,
				validationErrorModel: validationErrorModel
			}
			)
		);
	}
}

export class QueryCaseConfigEditorModel implements QueryCaseConfigPersist {
	public likePattern: string;
	public separator: string;
	public value: string;
	public referenceTypeId: Guid;
	public referenceTypeSourceKey: string;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: QueryCaseConfig): QueryCaseConfigEditorModel {
		if(item != null){
			this.likePattern = item.likePattern;
			this.separator = item.separator;
			this.value = item.value;
			if(item?.referenceType?.id) this.referenceTypeId = item.referenceType.id;
			this.referenceTypeSourceKey = item.referenceTypeSourceKey;
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
			context = QueryCaseConfigEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			likePattern: [{ value: this.likePattern, disabled: disabled }, context.getValidation('likePattern').validators],
			separator: [{ value: this.separator, disabled: disabled }, context.getValidation('separator').validators],
			value: [{ value: this.value, disabled: disabled }, context.getValidation('value').validators],
			referenceTypeId: [{ value: this.referenceTypeId, disabled: disabled }, context.getValidation('referenceTypeId').validators],
			referenceTypeSourceKey: [{ value: this.referenceTypeSourceKey, disabled: disabled }, context.getValidation('referenceTypeSourceKey').validators],
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'likePattern', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}likePattern`)] });
		baseValidationArray.push({ key: 'separator', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}separator`)] });
		baseValidationArray.push({ key: 'value', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}value`)] });
		baseValidationArray.push({ key: 'referenceTypeId', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}referenceTypeId`)] });
		baseValidationArray.push({ key: 'referenceTypeSourceKey', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}referenceTypeSourceKey`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = QueryCaseConfigEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['likePattern', 'separator', 'value', 'referenceTypeId', 'referenceTypeSourceKey'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}

export class HeaderConfigEditorModel implements HeadersConfigPersist {
	public key: ExternalFetcherApiHeaderType;
	public value: string;
	
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: HeaderConfig): HeaderConfigEditorModel {	
		if (item != null){ 
			this.key = item.key;
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
			context = HeaderConfigEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			key: [{ value: this.key, disabled: disabled }, context.getValidation('key').validators],
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
		const context = HeaderConfigEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['key', 'value'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});

	}
}

export class StaticEditorModel implements StaticPersist {
	options: StaticOptionEditorModel[] = [];
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: Static): StaticEditorModel {
		if (item) {
			if (item.options) { 
				item.options.map(x => this.options.push(new StaticOptionEditorModel(this.validationErrorModel).fromModel(x))); 
			} else {
				this.options.push(new StaticOptionEditorModel(this.validationErrorModel).fromModel({ code: 'reference_id', value: undefined }));
				this.options.push(new StaticOptionEditorModel(this.validationErrorModel).fromModel({ code: 'label', value: undefined }));
				this.options.push(new StaticOptionEditorModel(this.validationErrorModel).fromModel({ code: 'description', value: undefined }));
			}
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
			context = StaticEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			options: this.formBuilder.array(
				(this.options ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `${rootPath}options[${index}].`,
						disabled: disabled
					})
				), context.getValidation('options').validators
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
		baseValidationArray.push({ key: 'options', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}options`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyStaticValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {
		const {formGroup, validationErrorModel, rootPath } = params;
		(formGroup.get('options') as FormArray).controls?.forEach(
			(control, index) => StaticOptionEditorModel.reapplyStaticOptionsValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `${rootPath}options[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);
	}

}

export class StaticOptionEditorModel implements StaticOptionPersist {
	public code: string;
	public value: string;


	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	fromModel(item: StaticOption): StaticOptionEditorModel {
		this.code = item.code;
		this.value = item.value;

		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string
	}): UntypedFormGroup {
		let { context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = StaticOptionEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			code: [{ value: this.code, disabled: disabled }, context.getValidation('code').validators],
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
		baseValidationArray.push({ key: 'code', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}code`)] });
		baseValidationArray.push({ key: 'value', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}value`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyStaticOptionsValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = StaticOptionEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['code', 'value'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});

	}
}

export interface ItemsFormGroup{
    options: FormArray<FormGroup<OptionsFormGroup>>
}
export interface OptionsFormGroup{
    code: FormControl<string>;
    value: FormControl<string>;
}

export interface FieldMappingFormGroup{
    code: FormControl<string>;
	responsePath: FormControl<string>;
}