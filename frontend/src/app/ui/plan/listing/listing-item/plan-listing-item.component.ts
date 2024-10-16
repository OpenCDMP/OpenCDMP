import { Location } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
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
import { TranslateService } from '@ngx-translate/core';
import { takeUntil } from 'rxjs/operators';
import { PlanStatusEnum } from '../../../../core/common/enum/plan-status';
import { AuthService } from '../../../../core/services/auth/auth.service';
import { ClonePlanDialogComponent } from '../../clone-dialog/plan-clone-dialog.component';
import { PlanInvitationDialogComponent } from '../../invitation/dialog/plan-invitation-dialog.component';
import { NewVersionPlanDialogComponent } from '../../new-version-dialog/plan-new-version-dialog.component';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { FileTransformerEntityType } from '@app/core/common/enum/file-transformer-entity-type';
import { PlanVersionStatus } from '@app/core/common/enum/plan-version-status';
import { PlanDeleteDialogComponent } from '../../plan-delete-dialog/plan-delete-dialog.component';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { Tenant } from '@app/core/model/tenant/tenant';

@Component({
	selector: 'app-plan-listing-item-component',
	templateUrl: './plan-listing-item.component.html',
	styleUrls: ['./plan-listing-item.component.scss'],
})
export class PlanListingItemComponent extends BaseComponent implements OnInit {

	@Input() plan: Plan;
	@Input() showDivider: boolean = true;
	@Input() showAllVersionsAction: boolean = true;
	@Input() isPublic: boolean;
	@Input() tenants: Tenant[] = [];
	@Output() onClick: EventEmitter<Plan> = new EventEmitter();

	isDraft: boolean;
	isFinalized: boolean;
	isPublished: boolean;
	planStatusEnum = PlanStatusEnum;
	fileTransformerEntityTypeEnum = FileTransformerEntityType;

	get canEditPlan(): boolean {
		return (this.isDraft) && (this.plan.authorizationFlags?.some(x => x === AppPermission.EditPlan) || this.authentication.hasPermission(AppPermission.EditPlan)) && !this.isPublic && this.plan.belongsToCurrentTenant != false;
	}

	get canCreateNewVersion(): boolean {
		return (this.plan.authorizationFlags?.some(x => x === AppPermission.CreateNewVersionPlan) || this.authentication.hasPermission(AppPermission.CreateNewVersionPlan)) && this.plan.versionStatus === PlanVersionStatus.Current && !this.isPublic &&  this.plan.belongsToCurrentTenant != false;
	}

	get canDeletePlan(): boolean {
		return (this.plan.authorizationFlags?.some(x => x === AppPermission.DeletePlan) || this.authentication.hasPermission(AppPermission.DeletePlan)) && !this.isPublic &&  this.plan.belongsToCurrentTenant != false && this.isDraftPlan;
	}

	get canClonePlan(): boolean {
		return this.plan.authorizationFlags?.some(x => x === AppPermission.ClonePlan) || this.authentication.hasPermission(AppPermission.ClonePlan) || (this.authentication.hasPermission(AppPermission.PublicClonePlan) && this.isPublic);
	}

	get canFinalizePlan(): boolean {
		return (this.plan.authorizationFlags?.some(x => x === AppPermission.FinalizePlan) || this.authentication.hasPermission(AppPermission.FinalizePlan)) && !this.isPublic &&  this.plan.belongsToCurrentTenant != false;
	}

	get canExportPlan(): boolean {
		return this.plan.authorizationFlags?.some(x => x === AppPermission.ExportPlan) || this.authentication.hasPermission(AppPermission.ExportPlan);
	}

	get canInvitePlanUsers(): boolean {
		return (this.plan.authorizationFlags?.some(x => x === AppPermission.InvitePlanUsers) || this.authentication.hasPermission(AppPermission.InvitePlanUsers)) && !this.isPublic &&  this.plan.belongsToCurrentTenant != false;
	}

	get canAssignPlanUsers(): boolean {
		return (this.plan.authorizationFlags?.some(x => x === AppPermission.AssignPlanUsers) || this.authentication.hasPermission(AppPermission.AssignPlanUsers)) && !this.isPublic &&  this.plan.belongsToCurrentTenant != false;
	}

	get showActionsMenu(): boolean {
		return this.isAuthenticated() && (this.canCreateNewVersion || this.showAllVersionsAction || this.canDeletePlan)
	}

	get isDraftPlan(): boolean {
		return this.plan.status == PlanStatusEnum.Draft;
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
	) {
		super();
	}

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.PlanListingItem);
		if (this.plan.status == PlanStatusEnum.Draft) {
			this.isDraft = true;
			this.isFinalized = false;
			this.isPublished = false;
		}
		else if (this.plan.status == PlanStatusEnum.Finalized) {
			this.isDraft = false;
			this.isFinalized = true;
			this.isPublished = false;
			if (this.plan.status === PlanStatusEnum.Finalized && this.plan.accessType === PlanAccessType.Public) { this.isPublished = true }
		}
	}

	public isAuthenticated(): boolean {
		return this.authentication.currentAccountIsAuthenticated();
	}

	
	public getTenantName(id: Guid): string {
		return this.tenants?.find(t => t?.id == id)?.name;
	}

	inviteToPlan() {
		const dialogRef = this.dialog.open(PlanInvitationDialogComponent, {
			autoFocus: false,
			restoreFocus: false,
			data: {
				planId: this.plan.id,
				planName: this.plan.label,
				blueprint: this.plan.blueprint
			}
		});
	}

	viewVersions(plan: Plan) {
		if (plan.accessType == PlanAccessType.Public && plan.status == PlanStatusEnum.Finalized && !this.plan.authorizationFlags?.some(x => x === AppPermission.EditPlan)) {
			let url = this.router.createUrlTree(['/explore-plans/versions/', plan.groupId]);
			window.open(url.toString(), '_blank');
		} else {
			let url = this.router.createUrlTree(['/plans/versions/', plan.groupId]);
			window.open(url.toString(), '_blank');
		}
	}

	viewVersionsUrl(plan: Plan): string {
		if (plan.accessType == PlanAccessType.Public && plan.status == PlanStatusEnum.Finalized && !this.plan.authorizationFlags?.some(x => x === AppPermission.EditPlan)) {
			let url = this.router.createUrlTree(['/explore-plans/versions/', plan.groupId]);
			return url.toString();
		} else {
			let url = this.router.createUrlTree(['/plans/versions/', plan.groupId]);
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
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe((result: Plan) => {
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
				restoreFocus: false,
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
			restoreFocus: false,
			data: {
				message: this.language.instant('PLAN-EDITOR.ACTIONS.LOCK')
			}
		});
	}

	reloadPage(): void {
		const path = this.location.path();
		this.router.navigateByUrl('/reload', { skipLocationChange: true }).then(() => {
			this.router.navigate([this.routerUtils.generateUrl(path)]);
		});
	}

	onDeleteCallbackSuccess(): void {
		this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-DELETE'), SnackBarNotificationLevel.Success);
		this.reloadPage();
	}

	onDeleteCallbackError(error) {
		this.uiNotificationService.snackBarNotification(error.error.message ? error.error.message : this.language.instant('GENERAL.SNACK-BAR.UNSUCCESSFUL-DELETE'), SnackBarNotificationLevel.Error);
	}
}
