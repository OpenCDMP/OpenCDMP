import { UntypedFormBuilder, UntypedFormControl, UntypedFormGroup, Validators } from '@angular/forms';
import { RoleOrganizationType } from '@app/core/common/enum/role-organization-type';
import { ReferencePersist } from '@app/core/model/reference/reference';
import { User, UserAdditionalInfo, UserAdditionalInfoPersist, UserPersist } from '@app/core/model/user/user';
import { BaseEditorModel } from '@common/base/base-form-editor-model';
import { BackendErrorValidator } from '@common/forms/validation/custom-validator';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { Validation, ValidationContext } from '@common/forms/validation/validation-context';
import { Guid } from '@common/types/guid';


export class UserProfileEditorModel extends BaseEditorModel implements UserPersist {
	name: string;
	additionalInfo: UserAdditionalInfoEditorModel = new UserAdditionalInfoEditorModel();
	
	permissions: string[];

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super(); }

	public fromModel(item: User): UserProfileEditorModel {
		if (item) {
			super.fromModel(item);
			this.name = item.name;
			this.additionalInfo = new UserAdditionalInfoEditorModel(this.validationErrorModel).fromModel(item.additionalInfo);
		}
		return this;
	}

	buildForm(availableLanguages: any[], context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			name: [{ value: this.name, disabled: disabled }, context.getValidation('name').validators],
			additionalInfo: this.additionalInfo.buildForm({
				availableLanguages: availableLanguages,
				rootPath: `additionalInfo.`,
				disabled: disabled
			}),
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators]
		});
	}

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(this.validationErrorModel, 'id')] });
		baseValidationArray.push({ key: 'name', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'name')] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reApplySectionValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
	}): void {

		const { formGroup, validationErrorModel } = params;

		UserAdditionalInfoEditorModel.reapplyValidators({
			formGroup: formGroup?.get('additionalInfo') as UntypedFormGroup,
			rootPath: `additionalInfo.`,
			validationErrorModel: validationErrorModel
		});
		formGroup.updateValueAndValidity();
	}
}

export class UserAdditionalInfoEditorModel implements UserAdditionalInfoPersist {
	avatarUrl: String;
	timezone: String;
	culture: String;
	language: String;
	roleOrganization: RoleOrganizationType;
	organization: ReferencePersist;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: UserAdditionalInfo): UserAdditionalInfoEditorModel {
		if (item) {
			this.avatarUrl = item.avatarUrl;
			this.timezone = item.timezone;
			this.culture = item.culture;
			this.language = item.language;
			this.roleOrganization = item.roleOrganization
			if (item.organization){
				this.organization = {
					id: item.organization.id,
					label: item.organization.label,
					reference: item.organization.reference,
					source: item.organization.source,
					typeId: item.organization.type.id,
					description: item.organization.source,
					definition: item.organization.definition,
					abbreviation: item.organization.abbreviation,
					sourceType: item.organization.sourceType
				}
			}

		}
		return this;
	}

	buildForm(params?: {
		availableLanguages: any[],
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string
	}): UntypedFormGroup {
		let {availableLanguages = null, context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = UserAdditionalInfoEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			avatarUrl: [{ value: this.avatarUrl, disabled: disabled }, context.getValidation('avatarUrl').validators],
			timezone: [{ value: this.timezone, disabled: disabled }, context.getValidation('timezone').validators],
			culture: [{ value: this.culture, disabled: disabled }, context.getValidation('culture').validators],
			language: [{ value: this.language ? availableLanguages.filter(x => x === this.language).pop() : '', disabled: disabled }, context.getValidation('language').validators],
			roleOrganization: [{ value: this.roleOrganization, disabled: disabled }, context.getValidation('roleOrganization').validators],
			organization: [{ value: this.organization, disabled: disabled }, context.getValidation('organization').validators],
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'avatarUrl', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}avatarUrl`)] });
		baseValidationArray.push({ key: 'timezone', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}timezone`)] });
		baseValidationArray.push({ key: 'culture', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}culture`)] });
		baseValidationArray.push({ key: 'language', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}language`)] });
		baseValidationArray.push({ key: 'roleOrganization', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}roleOrganization`)] });
		baseValidationArray.push({ key: 'organization', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}organization`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {
		const { formGroup, rootPath, validationErrorModel } = params;
		const context = UserAdditionalInfoEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['avatarUrl', 'timezone', 'culture', 'language', 'roleOrganization', 'organization'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});

	}
}
