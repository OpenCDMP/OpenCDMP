import { Component, computed, input, output, ChangeDetectionStrategy } from '@angular/core';
import { MatButtonToggleChange, MatButtonToggleGroup, MatButtonToggle } from '@angular/material/button-toggle';
import { PlanUserRole } from '@app/core/common/enum/plan-user-role';
import { PlanUserType } from '@app/core/common/enum/plan-user-type';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { PlanUserEditorModel } from '../../plan-editor-blueprint/plan-editor.model';
import { Plan } from '@app/core/model/plan/plan';
import { UserService } from '@app/core/services/user/user.service';
import { Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { EnqueueService } from '@app/core/services/enqueue.service';
import { PlanService } from '@app/core/services/plan/plan.service';
import { FormService } from '@common/forms/form-service';
import { BaseComponent } from '@common/base/base.component';
import { catchError, takeUntil, tap, throwError } from 'rxjs';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { HttpError, HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { MatIcon } from '@angular/material/icon';
import { MatFormField, MatLabel, MatError } from '@angular/material/form-field';
import { SingleAutoCompleteComponent } from '../../../../library/auto-complete/single/single-auto-complete.component';
import { ReactiveAsteriskDirective } from '../../../../../common/forms/reactive-asterisk-directive';
import { MatInput } from '@angular/material/input';
import { MatSelect, MatOption } from '@angular/material/select';
import { MatButton } from '@angular/material/button';

@Component({
    selector: 'app-invite-plan-user',
    templateUrl: './invite-plan-user.component.html',
    styleUrl: './invite-plan-user.component.scss',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatButtonToggleGroup, FormsModule, ReactiveFormsModule, MatButtonToggle, MatIcon, MatFormField, MatLabel, SingleAutoCompleteComponent, ReactiveAsteriskDirective, MatError, MatInput, MatSelect, MatOption, MatButton, TranslatePipe]
})
export class InvitePlanUserComponent extends BaseComponent{
    plan = input.required<Plan>();
    sections = computed(() => this.plan()?.blueprint?.definition?.sections ?? [])
    templateSections = computed(() => this.sections()?.filter(x => x.hasTemplates) ?? [])
    onClose = output<void>();
    onUserInvited = output<void>();

    editorModel = new PlanUserEditorModel().fromModel(null);
    inviteFormGroup = this.editorModel.buildForm();

    planUserRoleEnum = PlanUserRole;
    planUserRoleEnumValues = this.enumUtils.getEnumValues<PlanUserRole>(PlanUserRole);
    planUserTypeEnum = PlanUserType;
    planUserTypeEnumValues = this.enumUtils.getEnumValues<PlanUserType>(PlanUserType);
    
    constructor(
        protected enumUtils: EnumUtils,
        protected userService: UserService,
        private enqueueService: EnqueueService,
        private planService: PlanService,
        private formService: FormService,
        private uiNotificationService: UiNotificationService,
        private httpErrorHandlingService: HttpErrorHandlingService,
        private language: TranslateService
    ){
        super();
        this.inviteFormGroup.get('user').addValidators([Validators.required]);
        this.inviteFormGroup.get('email').addValidators([Validators.required]);
        this.inviteFormGroup.get('role').valueChanges.pipe(takeUntil(this._destroyed))
        .subscribe((value) => {
            if(value == PlanUserRole.Viewer || value == PlanUserRole.Owner){
                this.inviteFormGroup.get('sectionId').patchValue('', {emitEvent: false})
            }
        })
    }
    

    userTypeChange(type: MatButtonToggleChange){
        if (type.value === PlanUserType.Internal){
            this.inviteFormGroup.get('email').patchValue(null);
            this.inviteFormGroup.get('email').disable();
            this.inviteFormGroup.get('user').enable();
        } else {
            this.inviteFormGroup.get('user').patchValue(null);
            this.inviteFormGroup.get('user').disable();
            this.inviteFormGroup.get('email').enable();
        }
    }

    
    sendInvite(){
        this.formService.removeAllBackEndErrors(this.inviteFormGroup);
		this.formService.touchAllFormFields(this.inviteFormGroup);
        if (!this.inviteFormGroup.valid) { return; }
        this.enqueueService.enqueueExhaustChannel(
            this.planService.inviteUsers(this.plan().id, { users: [this.inviteFormGroup.value] })
            .pipe(
                takeUntil(this._destroyed),
                tap(() => {
                    this.onCallbackSuccess();
                    this.onUserInvited.emit();
                    this.onClose.emit();
                }),
                catchError((error) => {this.onCallbackError(error); return throwError(() => error)})
            )
        )
    }
    
    onCallbackSuccess(): void {
		this.uiNotificationService.snackBarNotification(this.language.instant('PLAN-USER-INVITATION-DIALOG.SUCCESS'), SnackBarNotificationLevel.Success);
	}

	onCallbackError(errorResponse: HttpErrorResponse) {
		const errorOverrides = new Map<number, string>();
		errorOverrides.set(-1, this.language.instant('PLAN-USER-INVITATION-DIALOG.ERROR'));
		this.httpErrorHandlingService.handleBackedRequestError(errorResponse, errorOverrides, SnackBarNotificationLevel.Error);

		const error: HttpError = this.httpErrorHandlingService.getError(errorResponse);
		if (error.statusCode === 400) {
			this.editorModel.validationErrorModel.fromJSONObject(errorResponse.error);
			this.formService.validateAllFormFields(this.inviteFormGroup);
		}
	}
}
