import { Component, computed, effect, input, model, output, ChangeDetectionStrategy } from '@angular/core';
import { Plan, PlanUser, PlanUserPersist } from '@app/core/model/plan/plan';
import { EnqueueService } from '@app/core/services/enqueue.service';
import { PlanService } from '@app/core/services/plan/plan.service';
import { UserService } from '@app/core/services/user/user.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { FormService } from '@common/forms/form-service';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { GroupedPlanUser } from '../plan-authors.component';
import { AuthService } from '@app/core/services/auth/auth.service';
import { Guid } from '@common/types/guid';
import { PlanUserRole } from '@app/core/common/enum/plan-user-role';
import { MatSelectChange, MatSelect, MatOption } from '@angular/material/select';
import { MatOptionSelectionChange } from '@angular/material/core';
import { catchError, takeUntil, tap, throwError } from 'rxjs';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { HttpErrorResponse } from '@angular/common/http';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { ContactInfoType } from '@notification-service/core/enum/contact-info-type.enum';
import { FallbackAvatarDirective } from '../../../../../common/http/image/fallback-avatar.directive';
import { MatFormField } from '@angular/material/form-field';
import { FormsModule } from '@angular/forms';
import { MatIconButton, MatButton } from '@angular/material/button';
import { MatTooltip } from '@angular/material/tooltip';
import { MatMenuTrigger, MatMenu, MatMenuItem } from '@angular/material/menu';
import { MatIcon } from '@angular/material/icon';
import { MatDivider } from '@angular/material/list';
import { UserLogoPipe } from '../../../../../common/http/image/user-logo.pipe';

@Component({
    selector: 'app-existing-plan-users-editor',
    templateUrl: './existing-plan-users-editor.component.html',
    styleUrl: './existing-plan-users-editor.component.scss',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [FallbackAvatarDirective, MatFormField, MatSelect, FormsModule, MatOption, MatIconButton, MatTooltip, MatMenuTrigger, MatIcon, MatMenu, MatMenuItem, MatDivider, MatButton, TranslatePipe, UserLogoPipe]
})
export class ExistingPlanUsersEditorComponent extends BaseComponent{
    plan = input.required<Plan>();
    planUsers = model.required<GroupedPlanUser[]>();
    canEdit = input<boolean>(false);
    onUsersChanged = output<PlanUser[]>();
    
    sections = computed(() => this.plan()?.blueprint?.definition?.sections ?? [])
    templateSections = computed(() => this.sections()?.filter(x => x.hasTemplates) ?? [])

    PlanUserRole = PlanUserRole;
    planUserRoles = this.enumUtils.getEnumValues<PlanUserRole>(PlanUserRole);

    unsavedChanges = model<boolean>(false);

    otherRoles(planUser: GroupedPlanUser): PlanUserRole[] {
        return this.planUserRoles.filter(x => x != planUser.role)
    }

    constructor(
        protected enumUtils: EnumUtils,
        protected userService: UserService,
        private enqueueService: EnqueueService,
        private planService: PlanService,
        private formService: FormService,
        private uiNotificationService: UiNotificationService,
        private httpErrorHandlingService: HttpErrorHandlingService,
        private language: TranslateService,
        private authService: AuthService,
        private dialog: MatDialog
    ){
        super();
        effect(() => {
            //on planUsers update, reset unsavedChanges
            if(this.planUsers()){
                this.unsavedChanges.set(false);
            }
        })
    }


    userEmail(planUser: GroupedPlanUser): string {
        const contact = planUser?.user?.contacts?.find(c => c.type === ContactInfoType.Email);
        return contact?.value ? contact.value.toString() : null;
    }

    protected isCurrentUser(userId: Guid): boolean {
        if (this.authService.currentAccountIsAuthenticated()) {
            const principalId: Guid = this.authService.userId();
            return userId === principalId;
        } else return false;
    }

    changeRole(planUser: GroupedPlanUser, role: PlanUserRole){
        planUser.role = role;
        if(role == PlanUserRole.DescriptionContributor){
            planUser.sectionIds = planUser.sectionIds.filter(id => this.templateSections()?.some(x => x.id == id));
        }
        if(role == PlanUserRole.Viewer || role == PlanUserRole.Owner){
            planUser.sectionIds = [''];
            this.selectedAll = true;
        }
        this.unsavedChanges.set(true);
    }

    // optionChanged runs exactly before sectionChange as per mat-select usage
    selectedAll: boolean;
    optionChanged(event: MatOptionSelectionChange<string>){
        if(event.isUserInput && event.source.selected){
            event.source?.value == '' ? this.selectedAll = true : this.selectedAll = false;
            this.unsavedChanges.set(true);
        }
    }
    
    sectionChange(planUser: GroupedPlanUser, event: MatSelectChange<string[]>){
        if(this.selectedAll){
            planUser.sectionIds = ['']
        }else{
            planUser.sectionIds = event.value?.filter(x => x != '')
        }
    }

    get invalid(): boolean{
        return this.planUsers()?.some(x => 
            !x.sectionIds?.length || 
            (x.role == PlanUserRole.DescriptionContributor && x.sectionIds.some(id => !!id && !this.templateSections()?.some(x => x.id == id)))
        )
    }

    removeUser(planUser: GroupedPlanUser){
        let dialogRef;
        if (planUser?.user?.id == this.authService.userId() && this.planUsers()?.filter(x => x.user.id == this.authService.userId())?.length == 1) {
            dialogRef = this.dialog.open(ConfirmationDialogComponent, {
                data: {
                    message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-YOURSELF-FROM-ENTIRE-PLAN'),
                    confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.REMOVE'),
                    cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
                    isDeleteConfirmation: false
                }
            });
        } else {
            dialogRef = this.dialog.open(ConfirmationDialogComponent, {
                data: {
                    message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-USER'),
                    confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.REMOVE'),
                    cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
                    isDeleteConfirmation: false
                }
            });
        }
    
        dialogRef?.afterClosed()?.subscribe(result => {
            if (result) {
                const userIndex = this.planUsers()?.findIndex(x => x.user.id == planUser.user.id && x.role == planUser.role);
                if(userIndex >= 0){
                    this.planUsers().splice(userIndex, 1);
                }
                this.unsavedChanges.set(true);
            }
        });
    }

    saveChanges(){
        const planUserPersist: PlanUserPersist[] = [];
        this.planUsers()?.forEach((user, userIndex) => {
            user.sectionIds?.forEach((section, sectionIndex) => {
                const data: PlanUserPersist = {
                    email: null,
                    user: user.user.id,
                    ordinal: userIndex + sectionIndex,
                    role: user.role,
                    sectionId: section ? section as string : null 
                }
                planUserPersist.push(data);
            })
        });
        this.enqueueService.enqueueExhaustChannel(
            this.planService.assignUsers(this.plan().id, planUserPersist)
            .pipe(
                takeUntil(this._destroyed),
                tap((res) => {
                    this.onCallbackSuccess();
                    this.onUsersChanged.emit(res?.items);
                }),
                catchError((err) => {
                    this.onCallbackError(err);
                    return throwError(err)
                })
            )
        )
    }
    
    onCallbackSuccess(): void {
        this.unsavedChanges.set(false);
		this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
	}

	onCallbackError(errorResponse: HttpErrorResponse) {
		const errorOverrides = new Map<number, string>();
		errorOverrides.set(-1, this.language.instant('GENERAL.SNACK-BAR.GENERIC-ERROR'));
		this.httpErrorHandlingService.handleBackedRequestError(errorResponse, errorOverrides, SnackBarNotificationLevel.Error);
	}
}
