import { Location } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { UntypedFormBuilder, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { PlanAccessType } from '@app/core/common/enum/plan-access-type';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { Description, DescriptionPersist, DescriptionPropertyDefinitionPersist } from '@app/core/model/description/description';
import { AuthService } from '@app/core/services/auth/auth.service';
import { DescriptionService } from '@app/core/services/description/description.service';
import { PlanService } from '@app/core/services/plan/plan.service';
import { FileTransformerService } from '@app/core/services/file-transformer/file-transformer.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { ReferenceService } from '@app/core/services/reference/reference.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { FileUtils } from '@app/core/services/utilities/file-utils.service';
import { PlanInvitationDialogComponent } from '@app/ui/plan/invitation/dialog/plan-invitation-dialog.component';
import { BaseComponent } from '@common/base/base.component';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { takeUntil } from 'rxjs/operators';
import { DescriptionStatusEnum } from '../../../../core/common/enum/description-status';
import { CopyDialogReturnParams, DescriptionCopyDialogComponent } from '../../description-copy-dialog/description-copy-dialog.component';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { FileTransformerEntityType } from '@app/core/common/enum/file-transformer-entity-type';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { Tenant } from '@app/core/model/tenant/tenant';
import { DescriptionStatusAvailableActionType } from '@app/core/common/enum/description-status-available-action-type';
import { DescriptionEditorHelper } from '@app/ui/plan/plan-editor-blueprint/plan-description-editor/plan-description-editor-helper';
import { DescriptionPropertyDefinitionEditorModel } from '../../editor/description-editor.model';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { StorageFileService } from '@app/core/services/storage-file/storage-file.service';

@Component({
    selector: 'app-description-listing-item-component',
    templateUrl: './description-listing-item.component.html',
    styleUrls: ['./description-listing-item.component.scss'],
    standalone: false
})
export class DescriptionListingItemComponent extends BaseComponent implements OnInit {

	@Input() description: Description;
	@Input() showDivider: boolean = true;
	@Input() isPublic: boolean = false;
	@Input() tenants: Tenant[] = [];
    @Input() statusStorageFile: SafeUrl;
	@Output() onClick: EventEmitter<Description> = new EventEmitter();

	isDraft: boolean;
	isDeleted: boolean;
	isUserOwner: boolean;
	descriptionStatusEnum = DescriptionStatusEnum;
	fileTransformerEntityTypeEnum = FileTransformerEntityType;
	planAccessTypeEnum = PlanAccessType;
	canDelete: boolean = false;
	canEdit: boolean = false;
	canCopy: boolean = false;
	canInvitePlanUsers: boolean = false;
	canExport: boolean = false;

	constructor(
		public routerUtils: RouterUtilsService,
		private router: Router,
		private authentication: AuthService,
		public enumUtils: EnumUtils,
		private descriptionService: DescriptionService,
		public dialog: MatDialog,
		private language: TranslateService,
		private authService: AuthService,
		private uiNotificationService: UiNotificationService,
		private lockService: LockService,
		private location: Location,
		public planService: PlanService,
		public referenceService: ReferenceService,
		public referenceTypeService: ReferenceTypeService,
		public fileTransformerService: FileTransformerService,
		private analyticsService: AnalyticsService,
		private httpErrorHandlingService: HttpErrorHandlingService,
	) {
		super();
	}

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.DescriptionListingItem);
		if (this.description.isActive === IsActive.Inactive) {
			this.isDeleted = true;
		} else if (this.description?.status?.internalStatus === DescriptionStatusEnum.Draft) {
			this.isDraft = true;
			this.isDeleted = false;
		} else {
			this.isDraft = false;
			this.isDeleted = false;
		}

		this.canDelete = !this.isPublic && (this.authService.hasPermission(AppPermission.DeleteDescription) ||
			this.description.authorizationFlags?.some(x => x === AppPermission.DeleteDescription)) && !this.isDeleted &&this.description.belongsToCurrentTenant != false;

		this.canEdit = !this.isPublic && (this.authService.hasPermission(AppPermission.EditDescription) ||
			this.description.authorizationFlags?.some(x => x === AppPermission.EditDescription)) && this.description.belongsToCurrentTenant != false;

		this.canInvitePlanUsers = !this.isPublic && (this.authService.hasPermission(AppPermission.InvitePlanUsers) ||
			this.description.authorizationFlags?.some(x => x === AppPermission.InvitePlanUsers)) && !this.isDeleted &&  this.description.belongsToCurrentTenant != false;

		this.canExport = this.description.authorizationFlags?.some(x => x === AppPermission.ExportDescription) || this.authentication.hasPermission(AppPermission.ExportDescription) && !this.isDeleted &&
						this.description.status?.definition?.availableActions?.filter(x => x === DescriptionStatusAvailableActionType.Export).length > 0;

		this.canCopy = (this.authentication.hasPermission(AppPermission.PublicCloneDescription) && this.isPublic) || (this.description.authorizationFlags?.some(x => x === AppPermission.CloneDescription) && !this.isDeleted && this.description.belongsToCurrentTenant != false);
    }

	public getTenantName(id: Guid): string {
		return this.tenants?.find(t => t?.id == id)?.name;
	}

	isUserPlanRelated() {
		const principalId: Guid = this.authService.userId();
		return this.description.plan?.planUsers?.some(x => (x.user.id === principalId));
	}

	public isAuthenticated(): boolean {
		return this.authService.currentAccountIsAuthenticated();
	}

	getItemLink(): string {
		return this.isPublic ? this.routerUtils.generateUrl(['/explore-descriptions/overview/public/', this.description.id.toString()]) : 
        this.routerUtils.generateUrl(['/plans/overview/', this.description.plan.id.toString(), '/descriptions/', this.description.id.toString()]);
	}

	getPlanLink(): string[] {
		return this.isPublic ? [`/explore-plans/overview/public/${this.description.plan.id}`] : [`/plans/overview/${this.description.plan.id}`];
	}

	openShareDialog() {
		// TODO: This is a shared component. Put it in a seperate module.
		const dialogRef = this.dialog.open(PlanInvitationDialogComponent, {
			restoreFocus: false,
			data: {
				planId: this.description.plan.id,
				planName: this.description.plan.label,
				blueprint: this.description.plan.blueprint
			},
            minWidth: 'min(65rem, 90vw)'
		});
	}

	copyToPlan(description: Description) {
		const dialogRef = this.dialog.open<DescriptionCopyDialogComponent, any, CopyDialogReturnParams>(DescriptionCopyDialogComponent, {
			width: '500px',
			restoreFocus: false,
			data: {
				description,
				confirmButton: this.language.instant('DESCRIPTION-LISTING.COPY-DIALOG.COPY'),
				cancelButton: this.language.instant('DESCRIPTION-LISTING.COPY-DIALOG.CANCEL')
			}
		});

		dialogRef.afterClosed().pipe(takeUntil(this._destroyed))
			.subscribe((result) => {
				if (result) {
					this.router.navigate(
                        [this.routerUtils.generateUrl(['plans/edit', result.planId.toString()], '/')],
                        {queryParams: {
                            'descriptionCopyId': this.description.id,
                            'sectionId': result.sectionId
                        }}
                    );				
                }
			});
	}

	deleteClicked(id: Guid) {
		this.lockService.checkLockStatus(id).pipe(takeUntil(this._destroyed))
			.subscribe(lockStatus => {
				if (!lockStatus.status) {
					this.openDeleteDialog(id);
				} else {
					this.openLockedByUserDialog();
				}
			},
		error => {
			this.router.navigate([this.routerUtils.generateUrl('/descriptions')]);
			this.httpErrorHandlingService.handleBackedRequestError(error);
		});
	}

	openDeleteDialog(id: Guid): void {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			maxWidth: '300px',
			restoreFocus: false,
			data: {
				message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-ITEM'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.DELETE'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
				isDeleteConfirmation: true
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result) {
				this.descriptionService.delete(id)
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
				message: this.language.instant('DESCRIPTION-LISTING.LOCKED')
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
	
	canEditDescription(): boolean {
		return (this.isDraft) && (this.description.authorizationFlags?.some(x => x === AppPermission.EditDescription) || this.authentication.hasPermission(AppPermission.EditDescription)) && this.isPublic == false && this.description.belongsToCurrentTenant != false;
	}

    // private loadStatusLogo(){
    //     const status = (this.description as Description)?.status;
    //     if (status && status.definition?.storageFile?.id) {
    //         this.storageFileService.download(status.definition?.storageFile?.id).pipe(takeUntil(this._destroyed))
    //         .subscribe(response => {
    //             this.storageFileLogo = this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(response.body));
    //         });
    //     }			
    //     return 	
    // }
}
