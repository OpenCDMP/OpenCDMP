import { UntypedFormArray, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { UserInviteToTenantRequest, UserTenantUsersInviteRequest } from "@app/core/model/user/user";
import { BackendErrorValidator } from '@common/forms/validation/custom-validator';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { Validation, ValidationContext } from '@common/forms/validation/validation-context';

export class UserTenantUsersInviteRequestEditorModel implements UserTenantUsersInviteRequest {
	users: UserInviteToTenantRequestEditorModel[] = [];

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { }

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			users: this.formBuilder.array(
				(this.users ?? []).map(
					(item, index) => item.buildForm({
						rootPath: `users[${index}].`,
						disabled: disabled
					})
				), context.getValidation('users').validators
			),
		});
	}

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'users', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'users')] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

    static reapplyValidators(params: {
		formArray: UntypedFormArray,
		validationErrorModel: ValidationErrorModel,
	}): void {
		const { validationErrorModel, formArray } = params;
		formArray?.controls?.forEach(
			(control, index) => UserInviteToTenantRequestEditorModel.reapplyValidators({
				formGroup: control as UntypedFormGroup,
				rootPath: `users[${index}].`,
				validationErrorModel: validationErrorModel
			})
		);
	}
}

export class UserInviteToTenantRequestEditorModel implements UserInviteToTenantRequest {
	email: string;
	roles: string[];

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string
	}): UntypedFormGroup {
		let { context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = UserInviteToTenantRequestEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			email: [{ value: this.email, disabled: disabled }, context.getValidation('email').validators],
			roles: [{ value: this.roles, disabled: disabled }, context.getValidation('roles').validators],
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();

		baseValidationArray.push({ key: 'email', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}email`)] });
		baseValidationArray.push({ key: 'roles', validators: [Validators.required, BackendErrorValidator(validationErrorModel, `${rootPath}roles`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {

		const { formGroup, rootPath, validationErrorModel } = params;
		const context = UserInviteToTenantRequestEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['email', 'roles'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		})
	}
}
