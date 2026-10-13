
import { Component, computed, input, output, Signal, signal, ChangeDetectionStrategy } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { PlanUserRole } from '@app/core/common/enum/plan-user-role';
import { Plan, PlanUser } from '@app/core/model/plan/plan';
import { AuthService } from '@app/core/services/auth/auth.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { Guid } from '@common/types/guid';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { PlanAuthorsEditorDialogComponent, PlanAuthorsEditorDialogParams, PlanAuthorsEditorDialogReturn } from './plan-authors-editor-dialog/plan-authors-editor-dialog.component';
import { User, UserRole } from '@app/core/model/user/user';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { FallbackAvatarDirective } from '../../../../common/http/image/fallback-avatar.directive';
import { UserLogoPipe } from '../../../../common/http/image/user-logo.pipe';

@Component({
    selector: 'app-plan-authors',
    templateUrl: './plan-authors.component.html',
    styleUrl: './plan-authors.component.scss',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [FallbackAvatarDirective, TranslatePipe, UserLogoPipe]
})
export class PlanAuthorsComponent {
    plan = input.required<Plan>();
    isPublicView = input.required<boolean>();
    pageSize = input<number>(5);
    disabled = input<boolean>(false);
    usersChanged = output<PlanUser[]>();
    
    planUserRoleEnum = PlanUserRole;
    
    showMore = signal<boolean>(false);
    toggleShowMore(){
        this.showMore.update((value) => !value);
    }
    
    planUsers = computed(() => this.plan()?.planUsers?.filter(x => x.isActive === IsActive.Active));
    planBlueprint = computed(() => this.plan()?.blueprint)
    groupedPlanAuthors = computed(() => {
        if(!this.planUsers()?.length){ return []; }
        const groupedUsers: GroupedPlanUser[] = this.planUsers().reduce((prev, curr) => {
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
        return groupedUsers;
    })

    groupedBasicInfo: Signal<{user: User, roles: UserRole[]}[]> = computed(() => {
        if(!this.groupedPlanAuthors()?.length){ return []; }
        const groupedUsers: {user: User, roles: UserRole[]}[] = this.groupedPlanAuthors().reduce((prev, curr) => {
            const existing = prev.findIndex(x => x.user.id == curr.user.id);
            if(existing >= 0){
                prev[existing] = {
                    ...prev[existing],
                    roles: [
                        ...prev[existing].roles,
                        curr.role
                    ]
                }
                return prev;
            }else {
                return [...prev, {
                    user: curr.user,
                    roles: [curr.role],
                }] as {user: User, roles: UserRole[]}[]
            }
        }, []);
        return groupedUsers;
    })

    constructor(
        protected enumUtils: EnumUtils,
        private authentication: AuthService,
        private dialog: MatDialog,
        private language: TranslateService,
    ){
    }

    protected isCurrentUser(userId: Guid): boolean {
		if (this.isAuthenticated()) {
			const principalId: Guid = this.authentication.userId();
			return userId === principalId;
		} else return false;
	}

    protected isAuthenticated(): boolean {
		return this.authentication.currentAccountIsAuthenticated();
	}

    // protected removeUserFromPlan(author: PlanUser) {
    //     this.deleteAuthor.emit(author);
    // }

    // getSectionNameById(sectionId: Guid): string {
	// 	if (sectionId == null || !this.planBlueprint()) return '';
	// 	let sections: RootSection[] = this.planBlueprint()?.definition?.sections?.filter((section: RootSection) => sectionId === section.id);

	// 	return sections == null ? '' : sections[0].title;
	// }

    isAtLeastOnePlanOwner(planUser: PlanUser): boolean {
        return !(planUser.user?.id != null && planUser.role == PlanUserRole.Owner && planUser.sectionId == null && planUser.isActive == IsActive.Active) || this.planUsers()?.filter(x => x.user?.id != null && x.role == PlanUserRole.Owner && x.sectionId == null && x.isActive == IsActive.Active)?.length > 1;
    }

    openManageUsers(){
        this.dialog.open<PlanAuthorsEditorDialogComponent, PlanAuthorsEditorDialogParams, PlanAuthorsEditorDialogReturn>(PlanAuthorsEditorDialogComponent, {
            width: '440px',
            maxWidth: '100vw',
            height: '100%',
            id: 'author-editor',
            position: { right: '0px;' },
            panelClass: 'dialog-side-panel',
            ariaLabel: this.language.instant('PLAN-OVERVIEW.COLLABORATORS'),
            disableClose: true,
            data: {
                canAssignUsers: this.canAssignPlanUsers,
                canInviteUsers: this.canInvitePlanUsers,
                plan: this.plan(),
            }
        }).afterClosed().subscribe((res: PlanAuthorsEditorDialogReturn) => {
            if(res?.users){
                this.usersChanged.emit(res.users);
            }
        })
    }

    get canInvitePlanUsers(): boolean {
        if(this.disabled() || this.isPublicView() || !this.plan()?.belongsToCurrentTenant){ return false; }
        const authorizationFlags = (this.plan() as Plan).authorizationInformation?.globalAuthorizationFlags;
        return (
            this.plan()?.isActive == IsActive.Active &&
            (authorizationFlags?.some(x => x === AppPermission.InvitePlanUsers) || this.authentication.hasPermission(AppPermission.InvitePlanUsers)));
    }

    get canAssignPlanUsers(): boolean {
        if(this.disabled() || this.isPublicView() || !this.plan()?.belongsToCurrentTenant){ return false; }
        const authorizationFlags = (this.plan() as Plan).authorizationInformation?.globalAuthorizationFlags;
        return this.plan()?.isActive == IsActive.Active && (authorizationFlags?.some(x => x === AppPermission.AssignPlanUsers) || this.authentication.hasPermission(AppPermission.AssignPlanUsers));
    }
}
export interface GroupedPlanUser {
    user: User;
    plan: Plan;
    role: PlanUserRole;
    sectionIds: (Guid | string)[];
}