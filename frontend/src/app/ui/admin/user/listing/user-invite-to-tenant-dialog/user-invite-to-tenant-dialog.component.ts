
import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormArray, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MatDialogTitle, MatDialogContent, MatDialogActions } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { FormService } from '@common/forms/form-service';
import { HttpError, HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { takeUntil } from 'rxjs/operators';
import { UserInviteToTenantRequestEditorModel, UserTenantUsersInviteRequestEditorModel } from './user-invite-to-tenant-dialog-editor.model';
import { UserTenantUsersInviteRequest } from '@app/core/model/user/user';
import { UserService } from '@app/core/services/user/user.service';
import { AppRole } from '@app/core/common/enum/app-role';
import { MatIconButton, MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { MatFormField, MatLabel, MatError } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatSelect, MatOption } from '@angular/material/select';
import { MatTooltip } from '@angular/material/tooltip';

@Component({
    selector: 'app-user-invite-to-tenant-dialog.component',
    templateUrl: 'user-invite-to-tenant-dialog.component.html',
    styleUrls: ['./user-invite-to-tenant-dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatDialogTitle, MatIconButton, MatIcon, CdkScrollable, MatDialogContent, MatFormField, MatLabel, MatInput, FormsModule, ReactiveFormsModule, MatError, MatSelect, MatOption, MatTooltip, MatDialogActions, MatButton, TranslatePipe]
})
export class UserInviteToTenantDialogComponent extends BaseComponent implements OnInit {
   
	editorModel: UserTenantUsersInviteRequestEditorModel;
	formGroup: any;
	inProgressSendButton = false;
    appRoleEnum = AppRole;
	readonly separatorKeysCodes: number[] = [ENTER, COMMA];

	constructor(
		public enumUtils: EnumUtils,
		public route: ActivatedRoute,
		public router: Router,
		private language: TranslateService,
		public dialogRef: MatDialogRef<UserInviteToTenantDialogComponent>,
		private uiNotificationService: UiNotificationService,
		private httpErrorHandlingService: HttpErrorHandlingService,
		private userService: UserService,
		private formService: FormService,
	) {
		super();
	}

	ngOnInit() {
        this.editorModel = new UserTenantUsersInviteRequestEditorModel();
		this.formGroup = this.editorModel.buildForm();
        this.addUser();
	}

    addUser(): void {
		const formArray = this.formGroup.get("users") as UntypedFormArray;
		const user: UserInviteToTenantRequestEditorModel = new UserInviteToTenantRequestEditorModel(this.editorModel.validationErrorModel);
		formArray.push(user.buildForm({ rootPath: "users[" + formArray.length + "]." }));
	}

	removeUser(userIndex: number): void {
		(this.formGroup.get("users") as UntypedFormArray).removeAt(userIndex);

		UserTenantUsersInviteRequestEditorModel.reapplyValidators(
			{
				formArray: this.formGroup.get("users") as UntypedFormArray,
				validationErrorModel: this.editorModel.validationErrorModel,
			}
		);
		(this.formGroup.get("users") as UntypedFormArray).markAsDirty();
	}

	send() {
		this.formService.removeAllBackEndErrors(this.formGroup);
		this.formService.touchAllFormFields(this.formGroup);

		if (!this.formGroup.valid) { return; }
		this.inProgressSendButton = true;
		const userFormData = this.formGroup.value as UserTenantUsersInviteRequest;

		this.userService.inviteUsersToTenant(userFormData)
			.pipe(takeUntil(this._destroyed))
			.subscribe(
				complete => {
					this.dialogRef.close();
					this.onCallbackSuccess();
				},
				error => this.onCallbackError(error)
			);
	}

	closeDialog(): void {
		this.dialogRef.close();
	}

	onCallbackSuccess(): void {
		this.uiNotificationService.snackBarNotification(this.language.instant('USER-INVITE-TO-TENANT-DIALOG.SUCCESS'), SnackBarNotificationLevel.Success);
	}

	onCallbackError(errorResponse: HttpErrorResponse) {
		this.inProgressSendButton = false;
		const errorOverrides = new Map<number, string>();
		errorOverrides.set(-1, this.language.instant('USER-INVITE-TO-TENANT-DIALOG.ERROR'));
		this.httpErrorHandlingService.handleBackedRequestError(errorResponse, errorOverrides, SnackBarNotificationLevel.Error);

		const error: HttpError = this.httpErrorHandlingService.getError(errorResponse);
		if (error.statusCode === 400) {
			this.editorModel.validationErrorModel.fromJSONObject(errorResponse.error);
			this.formService.validateAllFormFields(this.formGroup);
		}
	}
}
