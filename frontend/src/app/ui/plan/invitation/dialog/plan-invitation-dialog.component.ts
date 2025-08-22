
import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, Inject, OnInit } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { PlanUserRole } from '@app/core/common/enum/plan-user-role';
import { PlanBlueprint } from '@app/core/model/plan-blueprint/plan-blueprint';
import { PlanUserPersist } from '@app/core/model/plan/plan';
import { PlanService } from '@app/core/services/plan/plan.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { FormService } from '@common/forms/form-service';
import { HttpError, HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { catchError, takeUntil, tap } from 'rxjs/operators';
import { PlanEditorModel } from '../../plan-editor-blueprint/plan-editor.model';
import { EnqueueService } from '@app/core/services/enqueue.service';
import { of, throwError } from 'rxjs';

@Component({
    selector: 'app-invitation-dialog-component',
    templateUrl: 'plan-invitation-dialog.component.html',
    styleUrls: ['./plan-invitation-dialog.component.scss'],
    standalone: false
})
export class PlanInvitationDialogComponent extends BaseComponent implements OnInit {
   
	planId: Guid;
	editorModel: PlanEditorModel;
	formGroup: UntypedFormGroup;
	planUserRoleEnum = PlanUserRole;
	selectedBlueprint: PlanBlueprint;
	readonly separatorKeysCodes: number[] = [ENTER, COMMA];
    isLoading = this.enqueueService.exhaustPipelineBusy;
    
	constructor(
		public enumUtils: EnumUtils,
		public route: ActivatedRoute,
		public router: Router,
		private language: TranslateService,
		public dialogRef: MatDialogRef<PlanInvitationDialogComponent>,
		private uiNotificationService: UiNotificationService,
		private httpErrorHandlingService: HttpErrorHandlingService,
		private planService: PlanService,
		private formService: FormService,
        private enqueueService: EnqueueService,
		@Inject(MAT_DIALOG_DATA) public data: any
	) {
		super();
		this.planId = data.planId;
		this.editorModel = data ? new PlanEditorModel().fromModel(data) : new PlanEditorModel();
		this.selectedBlueprint = data?.blueprint;
	}

	ngOnInit() {
		this.formGroup = this.editorModel.buildForm();
	}

	send() {
		this.formService.removeAllBackEndErrors(this.formGroup.get("users"));
		this.formService.touchAllFormFields(this.formGroup.get("users"));

		if (!this.formGroup.get("users").valid) { return; }
		const userFormData = this.formGroup.get("users").value as PlanUserPersist[];
        this.enqueueService.enqueueExhaustChannel(
            this.planService.inviteUsers(this.planId, { users: userFormData })
                .pipe(
                    takeUntil(this._destroyed),
                    tap(() => {
                        this.dialogRef.close();
                        this.onCallbackSuccess();
                    }),
                    catchError((error) => {this.onCallbackError(error); return throwError(() => error)})
                )
        )
	}

	closeDialog(): void {
		this.dialogRef.close();
	}

	hasValue(): boolean {
		return this.formGroup.get('users') && this.formGroup.get('users').value && this.formGroup.get('users').value.length > 0;
	}

	onCallbackSuccess(): void {
		this.uiNotificationService.snackBarNotification(this.language.instant('PLAN-USER-INVITATION-DIALOG.SUCCESS'), SnackBarNotificationLevel.Success);
	}

	onCallbackError(errorResponse: HttpErrorResponse) {
		let errorOverrides = new Map<number, string>();
		errorOverrides.set(-1, this.language.instant('PLAN-USER-INVITATION-DIALOG.ERROR'));
		this.httpErrorHandlingService.handleBackedRequestError(errorResponse, errorOverrides, SnackBarNotificationLevel.Error);

		const error: HttpError = this.httpErrorHandlingService.getError(errorResponse);
		if (error.statusCode === 400) {
			this.editorModel.validationErrorModel.fromJSONObject(errorResponse.error);
			this.formService.validateAllFormFields(this.formGroup);
		}
	}
}
