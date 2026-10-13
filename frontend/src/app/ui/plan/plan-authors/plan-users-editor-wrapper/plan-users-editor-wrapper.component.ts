import { Component, computed, effect, input, model, output, signal, ChangeDetectionStrategy, forwardRef } from '@angular/core';
import { GroupedPlanUser } from '../plan-authors.component';
import { Plan, PlanUser } from '@app/core/model/plan/plan';
import { UserContactInfo } from '@app/core/model/user/user';
import { PlanStatusEnum } from '@app/core/common/enum/plan-status';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { takeUntil } from 'rxjs';
import { BaseComponent } from '@common/base/base.component';
import { PlanService } from '@app/core/services/plan/plan.service';
import { nameof } from 'ts-simple-nameof';
import { MatIconButton, MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { InvitePlanUserComponent } from '../invite-plan-user/invite-plan-user.component';
import { ExistingPlanUsersEditorComponent } from '../existing-plan-users-editor/existing-plan-users-editor.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-plan-users-editor-wrapper',
    templateUrl: './plan-users-editor-wrapper.component.html',
    styleUrl: './plan-users-editor-wrapper.component.scss',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatIconButton, MatIcon, MatButton, InvitePlanUserComponent, forwardRef(() => ExistingPlanUsersEditorComponent), TranslatePipe]
})
export class PlanUsersEditorWrapperComponent extends BaseComponent{
    plan = input<Plan>();
    canAssignUsers = input<boolean>();
    canInviteUsers = input<boolean>();
    showInviteUsers = model<boolean>(false);
    onUsersChanged = output<PlanUser[]>();
    onEditorClose = output<void>();

    isFinalized = computed(() => this.plan()?.status?.internalStatus === PlanStatusEnum.Finalized);

    loadingData = false;

    unsavedChanges = signal<boolean>(false);

    constructor(private planService: PlanService){
        super();
        effect(() => {
            if(this.plan()){
                this.getPlanUsers(this.plan()?.planUsers);
            }
        })
    }

    getPlanUsers(users: PlanUser[]){
        const planUsers = users?.filter(x => x.isActive === IsActive.Active)
        if(!planUsers?.length){ return []; }
        this.groupedPlanUsers = planUsers.reduce((prev, curr) => {
            const existing = prev.findIndex(x => x.user.id == curr.user.id && x.role == curr.role);
            if(existing >= 0){
                prev[existing] = {
                    ...prev[existing],
                    sectionIds: [
                        ...prev[existing].sectionIds,
                        curr.sectionId ?? ''
                    ]
                }
                return prev;
            }else {
                return [...prev, {
                    user: curr.user,
                    role: curr.role,
                    plan: curr.plan,
                    sectionIds: [curr.sectionId ?? '']
                }] as GroupedPlanUser[]
            }
        }, [] as GroupedPlanUser[]);
        const distinctUsers = new Set([... this.groupedPlanUsers.map(x => x.user.id)]);
        this.distinctUserCount = distinctUsers.size;
    }
    groupedPlanUsers: GroupedPlanUser[] = [];
    distinctUserCount: number = 0;

    usersChanged(closeEditor: boolean = false){
        this.loadingData = true;
        this.planService.getUsers(this.plan().id, [
            nameof<PlanUser>(x => x.id),
            nameof<PlanUser>(x => x.sectionId),
            nameof<PlanUser>(x => x.user.id),
            nameof<PlanUser>(x => x.user.name),
            [nameof<PlanUser>(x => x.user.contacts), nameof<UserContactInfo>(x => x.value)].join('.'),
            [nameof<PlanUser>(x => x.user.contacts), nameof<UserContactInfo>(x => x.type)].join('.'),
            nameof<PlanUser>(x => x.role),
            nameof<PlanUser>(x => x.plan.id),
            nameof<PlanUser>(x => x.isActive),
            nameof<PlanUser>(x => x.ordinal),
        ]).pipe(takeUntil(this._destroyed))
        .subscribe({
            next: (users) => {
                this.loadingData = false;
                //because onUsersChanged only partially edits plan (plan.planUsers = ...)
                //this component's constructor effect is not called. That means we have to manually getPlanUsers again
                this.getPlanUsers(users);
                this.onUsersChanged.emit(users);      
                if(closeEditor){
                    this.onEditorClose.emit()
                }
            },
            error: (error) => {
                this.loadingData = false;
                if(closeEditor){
                    this.onEditorClose.emit()
                }
            }
        })
    }
}
