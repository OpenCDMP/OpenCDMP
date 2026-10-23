import { Location, NgTemplateOutlet, NgClass, LowerCasePipe } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output, ChangeDetectionStrategy } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router, RouterLink } from '@angular/router';
import { PlanAccessType } from '@app/core/common/enum/plan-access-type';
import { Plan } from '@app/core/model/plan/plan';
import { PlanService } from '@app/core/services/plan/plan.service';
import { FileTransformerService } from '@app/core/services/file-transformer/file-transformer.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { ReferenceService } from '@app/core/services/reference/reference.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { Guid } from '@common/types/guid';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { ReferenceTypeNamePipe } from '@app/core/pipes/reference-type-name.pipe';
import { takeUntil } from 'rxjs/operators';
import { PlanStatusEnum } from '../../../../core/common/enum/plan-status';
import { AuthService } from '../../../../core/services/auth/auth.service';
import { ClonePlanDialogComponent } from '../../clone-dialog/plan-clone-dialog.component';
import { NewVersionPlanDialogComponent } from '../../new-version-dialog/plan-new-version-dialog.component';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { FileTransformerEntityType } from '@app/core/common/enum/file-transformer-entity-type';
import { PlanVersionStatus } from '@app/core/common/enum/plan-version-status';
import { PlanDeleteDialogComponent } from '../../plan-delete-dialog/plan-delete-dialog.component';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { Tenant } from '@app/core/model/tenant/tenant';
import { PlanStatusAvailableActionType } from '@app/core/common/enum/plan-status-available-action-type';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { SafeUrl } from '@angular/platform-browser';
import {ReferenceType} from "@app/core/model/reference-type/reference-type";
import { PlanUserRole } from '@app/core/common/enum/plan-user-role';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { DepositService } from '@app/core/services/deposit/deposit.service';
import { DepositConfiguration } from '@app/core/model/deposit/deposit-configuration';
import { MatCard, MatCardContent } from '@angular/material/card';
import { MatMenu, MatMenuItem } from '@angular/material/menu';
import { MatIcon } from '@angular/material/icon';
import { DateTimeFormatPipe } from '../../../../core/pipes/date-time-format.pipe';

@Component({
    selector: 'app-plan-listing-item-component',
    templateUrl: './plan-listing-item.component.html',
    styleUrls: ['./plan-listing-item.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatCard, MatCardContent, RouterLink, NgTemplateOutlet, MatMenu, MatMenuItem, NgClass, MatIcon, LowerCasePipe, TranslatePipe, DateTimeFormatPipe, ReferenceTypeNamePipe]
})
export class PlanListingItemComponent extends BaseComponent implements OnInit {

	@Input() plan: Plan;
	@Input() showAllVersionsAction: boolean = true;
	@Input() isPublic: boolean;
	@Input() tenants: Tenant[] = [];
    @Input() statusStorageFile: SafeUrl;
	@Input() orderedPlanPreferencesList:ReferenceType[] = [];
    @Input() compactView: boolean = false;
	@Output() onClick: EventEmitter<Plan> = new EventEmitter();
    @Output() onRefresh: EventEmitter<void> = new EventEmitter();

	isDeleted: boolean;
	isDraft: boolean;
	isFinalized: boolean;
	isPublished: boolean;
	planStatusEnum = PlanStatusEnum;
	fileTransformerEntityTypeEnum = FileTransformerEntityType;

	// get canEditPlan(): boolean {
	// 	return (this.isDraft) && (this.plan.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.EditPlan) || this.authentication.hasPermission(AppPermission.EditPlan)) && !this.isDeleted && !this.isPublic && this.plan.belongsToCurrentTenant != false;
	// }

	get canCreateNewVersion(): boolean {
		return (this.plan.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.CreateNewVersionPlan) || this.authentication.hasPermission(AppPermission.CreateNewVersionPlan)) && !this.isDeleted && this.plan.versionStatus === PlanVersionStatus.Current && !this.isPublic &&  this.plan.belongsToCurrentTenant != false;
	}

	get canDeletePlan(): boolean {
		return (this.plan.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.DeletePlan) || this.authentication.hasPermission(AppPermission.DeletePlan)) && !this.isPublic && !this.isDeleted &&this.plan.belongsToCurrentTenant != false && this.isNotFinalizedPlan;
	}

	get canClonePlan(): boolean {
		const authorizationFlags = !this.isPublic ? (this.plan as Plan).authorizationInformation?.globalAuthorizationFlags : [];
		return (
            (authorizationFlags?.some(x => x === AppPermission.ClonePlan) ||
            this.authentication.hasPermission(AppPermission.ClonePlan) ||
            (this.authentication.hasPermission(AppPermission.PublicClonePlan) && this.isPublic))
        );
	}

	get canFinalizePlan(): boolean {
		return (this.plan.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.FinalizePlan) || this.authentication.hasPermission(AppPermission.FinalizePlan)) && !this.isDeleted && !this.isPublic &&  this.plan.belongsToCurrentTenant != false;
	}

	get canExportPlan(): boolean {
		return (this.plan.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.ExportPlan) || this.authentication.hasPermission(AppPermission.ExportPlan)) && !this.isDeleted &&
		this.plan.status?.definition?.availableActions?.filter(x => x === PlanStatusAvailableActionType.Export).length > 0;;
	}

	get canInvitePlanUsers(): boolean {
		return (this.plan.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.InvitePlanUsers) || this.authentication.hasPermission(AppPermission.InvitePlanUsers)) && !this.isDeleted && !this.isPublic &&  this.plan.belongsToCurrentTenant != false;
	}

	get canAssignPlanUsers(): boolean {
		return (this.plan.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.AssignPlanUsers) || this.authentication.hasPermission(AppPermission.AssignPlanUsers)) && !this.isDeleted && !this.isPublic &&  this.plan.belongsToCurrentTenant != false;
	}

	get showActionsMenu(): boolean {
		return this.isAuthenticated() && (this.canCreateNewVersion || this.showAllVersionsAction || this.canDeletePlan) && !this.isDeleted;
	}

	get isNotFinalizedPlan(): boolean {
		return (this.plan.status?.internalStatus == null || this.plan.status?.internalStatus != PlanStatusEnum.Finalized) && !this.isDeleted;
	}

    get planOwners(): string {
        return this.plan.planUsers?.filter(x => x.isActive === IsActive.Active && x.role === PlanUserRole.Owner)?.map(x => x.user?.name).join(', ');
    }

    get collaboratorCount(): number {
        return this.plan.planUsers?.filter(x => x.isActive === IsActive.Active)?.length;
    }

	constructor(
		public routerUtils: RouterUtilsService,
		private router: Router,
		private dialog: MatDialog,
		private authentication: AuthService,
		public enumUtils: EnumUtils,
		private planService: PlanService,
		private language: TranslateService,
		private uiNotificationService: UiNotificationService,
		private lockService: LockService,
		private location: Location,
		public referenceService: ReferenceService,
		public referenceTypeService: ReferenceTypeService,
		public fileTransformerService: FileTransformerService,
		private analyticsService: AnalyticsService,
		private httpErrorHandlingService: HttpErrorHandlingService,
        private configurationService: ConfigurationService,
        private depositService: DepositService
	) {
		super();
	}

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.PlanListingItem);
		if (this.plan.status?.internalStatus == PlanStatusEnum.Draft) {
			this.isDraft = true;
			this.isFinalized = false;
			this.isPublished = false;
		}
		else if (this.plan.status?.internalStatus == PlanStatusEnum.Finalized) {
			this.isDraft = false;
			this.isFinalized = true;
			this.isPublished = false;
			if (this.plan.accessType === PlanAccessType.Public) { this.isPublished = true }
		}
		if (this.plan.isActive === IsActive.Inactive) {
			this.isDeleted = true;
		}

		if (this.plan.planReferences?.length > 0) this.plan.planReferences = this.plan.planReferences?.filter(x => x.isActive === IsActive.Active);
	}

    // private loadStatusLogo(){
	// 	const status = (this.plan as Plan)?.status;
	// 	if (status && status.definition?.storageFile?.id) {
    //         this.storageFileService.download(status.definition?.storageFile?.id).pipe(takeUntil(this._destroyed))
    //         .subscribe(response => {
    //             this.storageFileLogo = this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(response.body));
    //         });
	// 	}
	// 	return
	// }

	public isAuthenticated(): boolean {
		return this.authentication.currentAccountIsAuthenticated();
	}


	public getTenantName(id: Guid): string {
		return this.tenants?.find(t => t?.id == id)?.name;
	}

	// inviteToPlan() {
	// 	const dialogRef = this.dialog.open(PlanInvitationDialogComponent, {
	// 		restoreFocus: false,
	// 		data: {
	// 			planId: this.plan.id,
	// 			planName: this.plan.label,
	// 			blueprint: this.plan.blueprint
	// 		},
    //         minWidth: 'min(65rem, 90vw)'
	// 	});
	// }

	viewVersions(plan: Plan) {
		if (plan.accessType == PlanAccessType.Public && plan.status?.internalStatus == PlanStatusEnum.Finalized 
            && !this.plan.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.EditPlan)) {
			const url = this.router.createUrlTree(['/explore-plans/versions/', plan.groupId]);
			window.open(url.toString(), '_blank');
		} else {
			const url = this.router.createUrlTree(['/plans/versions/', plan.groupId]);
			window.open(url.toString(), '_blank');
		}
	}

	viewVersionsUrl(plan: Plan): string {
		if (plan.accessType == PlanAccessType.Public && plan.status?.internalStatus == PlanStatusEnum.Finalized 
            && !this.plan.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.EditPlan)) {
			const url = this.router.createUrlTree(['/explore-plans/versions/', plan.groupId]);
			return url.toString();
		} else {
			const url = this.router.createUrlTree(['/plans/versions/', plan.groupId]);
			return url.toString();
		}
	}

	isUserPlanRelated() {
		const principalId: Guid = this.authentication.userId();
		return this.plan.planUsers?.some(x => (x.user.id === principalId));
	}

	cloneClicked() {
		const dialogRef = this.dialog.open(ClonePlanDialogComponent, {
			maxWidth: '700px',
			maxHeight: '80vh',
			data: {
				plan: this.plan,
				isPublic: this.isPublic ? this.isPublic : false
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe((result: Plan) => {
			if (result) {
				this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
				this.router.navigate([this.routerUtils.generateUrl(['/plans/edit', result.id.toString()], '/')]);
			}
		});
	}

	newVersionClicked() {
		const dialogRef = this.dialog.open(NewVersionPlanDialogComponent, {
			maxWidth: '700px',
			maxHeight: '80vh',
			data: {
				plan: this.plan
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed))
        .subscribe((result) => {
            if (result) {
				this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
				this.router.navigate([this.routerUtils.generateUrl(['/plans/edit', result.id.toString()], '/')]);
			}
		});
	}

	deleteClicked(id: Guid) {
		this.lockService.checkLockStatus(Guid.parse(id.toString())).pipe(takeUntil(this._destroyed))
			.subscribe(lockStatus => {
				if (!lockStatus.status) {
					this.openDeleteDialog(id);
				} else {
					this.openLockedByUserDialog();
				}
			},
			error => this.httpErrorHandlingService.handleBackedRequestError(error));
	}

	openDeleteDialog(id: Guid) {
		let dialogRef: any;

		if (this.plan.descriptions && this.plan.descriptions.length > 0){
			dialogRef = this.dialog.open(PlanDeleteDialogComponent, {
				maxWidth: '300px',
				data: {
					descriptions: this.plan.descriptions,
				}
			});
		} else {
			dialogRef = this.dialog.open(ConfirmationDialogComponent, {
				maxWidth: '300px',
				data: {
					message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-ITEM'),
					confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.DELETE'),
					cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
					isDeleteConfirmation: true
				}
			});
		}

		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result) {
				this.planService.delete(id)
					.pipe(takeUntil(this._destroyed))
					.subscribe(
						complete => this.onDeleteCallbackSuccess(),
						error => this.onDeleteCallbackError(error)
					);
			}
		});
	}

	openLockedByUserDialog() {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			maxWidth: '400px',
			data: {
				message: this.language.instant('PLAN-EDITOR.LOCKED-DIALOG.PLAN-LOCKED')
			}
		});
	}

	reloadPage(): void {
		this.onRefresh.emit();
	}

	onDeleteCallbackSuccess(): void {
		this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-DELETE'), SnackBarNotificationLevel.Success);
		this.reloadPage();
	}

	onDeleteCallbackError(error) {
		this.uiNotificationService.snackBarNotification(error.error.message ? error.error.message : this.language.instant('GENERAL.SNACK-BAR.UNSUCCESSFUL-DELETE'), SnackBarNotificationLevel.Error);
	}

    get depositRepos(): DepositConfiguration[] {
        return this.depositService.availableDeposits();
    }

    createDoiLink(doiModel: any): string {
		const repository = this.depositRepos?.find(r => r.repositoryId == doiModel.repositoryId);
		return repository?.repositoryRecordUrl?.replace(this.configurationService.depositRecordUrlIdPlaceholder, doiModel.doi) ?? null;
	}
}
