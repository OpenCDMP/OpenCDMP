import { Component, Inject, ChangeDetectionStrategy, forwardRef } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { UserService } from '@app/core/services/user/user.service';
import { Plan, PlanUser } from '@app/core/model/plan/plan';
import { PlanUsersEditorWrapperComponent } from '../plan-users-editor-wrapper/plan-users-editor-wrapper.component';

@Component({
    selector: 'app-plan-authors-editor-dialog',
    templateUrl: './plan-authors-editor-dialog.component.html',
    styleUrl: './plan-authors-editor-dialog.component.scss',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [forwardRef(() => PlanUsersEditorWrapperComponent)]
})
export class PlanAuthorsEditorDialogComponent {
    plan: Plan;
    canAssignUsers: boolean;
    canInviteUsers: boolean;
    showInviteUsers: boolean = false;


    constructor(
        protected dialogRef: MatDialogRef<PlanAuthorsEditorDialogComponent>,
        protected enumUtils: EnumUtils,
        protected userService: UserService,
        @Inject(MAT_DIALOG_DATA) public data: PlanAuthorsEditorDialogParams,
    ){
        this.plan = data?.plan;
        this.canAssignUsers = data?.canAssignUsers;
        this.canInviteUsers = data?.canInviteUsers;
    }

    usersChanged(planUsers: PlanUser[]){
        this.plan.planUsers = planUsers;
    }

}
export interface PlanAuthorsEditorDialogParams {
    plan: Plan;
    canAssignUsers: boolean;
    canInviteUsers: boolean;
}
export interface PlanAuthorsEditorDialogReturn {
    users: PlanUser[];
}