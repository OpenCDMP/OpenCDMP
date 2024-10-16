import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormControl, FormGroup, UntypedFormGroup } from '@angular/forms';
import { User, UserRolePatchPersist } from '@app/core/model/user/user';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { UserService } from '@app/core/services/user/user.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseEditor } from '@common/base/base-editor';
import { BaseComponent } from '@common/base/base.component';
import { Validation, ValidationContext } from '@common/forms/validation/validation-context';
import { TranslateService } from '@ngx-translate/core';
import { takeUntil } from 'rxjs/operators';
import { UserRolePatchEditorModel } from './user-role-editor.model';
import { AuthService } from '@app/core/services/auth/auth.service';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { DescriptionTemplateTypeEditorService } from '@app/ui/admin/description-types/editor/description-template-type-editor.service';
import { UserRoleEditorService } from './user-role-editor.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { FormService } from '@common/forms/form-service';
import { HttpErrorResponse } from '@angular/common/http';
import { HttpError, HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { AppRole } from '@app/core/common/enum/app-role';
import { Guid } from '@common/types/guid';

@Component({
	selector: 'app-user-role-editor-component',
	templateUrl: './user-role-editor.component.html',
	styleUrls: ['./user-role-editor.component.scss'],
	providers: [UserRoleEditorService]
})
export class UserRoleEditorComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() public item: User;
	@Input() public hasTenantAdminMode: boolean = false;

	public formGroup: UntypedFormGroup = null;
	public nowEditing = false;
	lookupParams: any;
	editorModel: UserRolePatchEditorModel;
	appRole = AppRole;
	public appRoleEnumValues = this.enumUtils.getEnumValues<AppRole>(AppRole);

	constructor(
		private language: TranslateService,
		private userService: UserService,
		private formService: FormService,
		private logger: LoggingService,
		private enumUtils: EnumUtils,
		private uiNotificationService: UiNotificationService,
		private authService: AuthService,
		private userRoleEditorService: UserRoleEditorService,
		private httpErrorHandlingService: HttpErrorHandlingService,
	) { super(); }

	ngOnInit() {
		this.prepareForm(this.item);
	}

	ngOnChanges(changes: SimpleChanges): void {
		if (changes['item']) {
			this.prepareForm(this.item);
			this.nowEditing = false;
		}
	}

	prepareForm(data: User) {
		try {
			this.editorModel = data ? new UserRolePatchEditorModel().fromModel(data, this.hasTenantAdminMode) : new UserRolePatchEditorModel();
			this.buildForm();
		} catch (error) {
			this.logger.error('Could not parse user role item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

	buildForm() {
		this.formGroup = this.editorModel.buildForm(null, this.authService.hasPermission(AppPermission.EditUser));
		this.userRoleEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);
	}

	persistEntity(onSuccess?: (response) => void): void {
		const formData = this.formService.getValue(this.formGroup.value) as UserRolePatchPersist;

		this.userService.persistRoles(formData)
			.pipe(takeUntil(this._destroyed)).subscribe(
				complete => onSuccess ? onSuccess(complete) : this.onCallbackSuccess(),
				error => this.onCallbackError(error)
			);
	}

	formSubmit(): void {
		this.formService.touchAllFormFields(this.formGroup);
		if (!this.isFormValid()) {
			return;
		}

		this.persistEntity();
	}

	editItem(): void {
		this.formGroup.enable();
		this.nowEditing = true;
	}

	public isFormValid() {
		return this.formGroup.valid;
	}

	onCallbackSuccess() {
		this.nowEditing = false;
		this.formGroup.disable();
		this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
	}

	onCallbackError(errorResponse: HttpErrorResponse) {
		this.httpErrorHandlingService.handleBackedRequestError(errorResponse);
		
		const error: HttpError = this.httpErrorHandlingService.getError(errorResponse);
		if (error.statusCode === 400) {
			this.editorModel.validationErrorModel.fromJSONObject(errorResponse.error);
			this.formService.validateAllFormFields(this.formGroup);
		}
	}

	clearErrorModel() {
		this.editorModel.validationErrorModel.clear();
		this.formService.validateAllFormFields(this.formGroup);
	}
}
