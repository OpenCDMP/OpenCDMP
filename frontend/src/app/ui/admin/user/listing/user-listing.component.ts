import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { User, UserAdditionalInfo, UserContactInfo, UserRole } from '@app/core/model/user/user';
import { UserLookup } from '@app/core/query/user.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { UserService } from '@app/core/services/user/user.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { FileUtils } from '@app/core/services/utilities/file-utils.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { BaseListingComponent } from '@common/base/base-listing-component';
import { PipeService } from '@common/formatting/pipe.service';
import { DataTableDateTimeFormatPipe } from '@app/core/pipes/date-time-format.pipe';
import { IsActiveTypePipe } from '@common/formatting/pipes/is-active-type.pipe';
import { QueryResult } from '@common/model/query-result';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { ColumnDefinition, ColumnsChangedEvent, HybridListingComponent, PageLoadEvent, RowActivateEvent } from '@common/modules/hybrid-listing/hybrid-listing.component';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import * as FileSaver from 'file-saver';
import { Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { UserInviteToTenantDialogComponent } from './user-invite-to-tenant-dialog/user-invite-to-tenant-dialog.component';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { TenantUserLookup } from '@app/core/query/tenant-user.lookup';

@Component({
    templateUrl: './user-listing.component.html',
    styleUrls: ['./user-listing.component.scss'],
    standalone: false
})
export class UserListingComponent extends BaseListingComponent<User, UserLookup> implements OnInit {
	publish = false;
	userSettingsKey = { key: 'UserListingUserSettings' };
	propertiesAvailableForOrder: ColumnDefinition[];
	isTenantAdminMode: boolean;

	@ViewChild('roleCellTemplate', { static: true }) roleCellTemplate?: TemplateRef<any>;
	@ViewChild('nameCellTemplate', { static: true }) nameCellTemplate?: TemplateRef<any>;
	@ViewChild(HybridListingComponent, { static: true }) hybridListingComponent: HybridListingComponent;

	private readonly lookupFields: string[] = [
		nameof<User>(x => x.id),
		nameof<User>(x => x.name),
		[nameof<User>(x => x.contacts), nameof<UserContactInfo>(x => x.id)].join('.'),
		[nameof<User>(x => x.contacts), nameof<UserContactInfo>(x => x.type)].join('.'),
		[nameof<User>(x => x.contacts), nameof<UserContactInfo>(x => x.value)].join('.'),
		[nameof<User>(x => x.globalRoles), nameof<UserRole>(x => x.id)].join('.'),
		[nameof<User>(x => x.globalRoles), nameof<UserRole>(x => x.role)].join('.'),
		[nameof<User>(x => x.tenantRoles), nameof<UserRole>(x => x.id)].join('.'),
		[nameof<User>(x => x.tenantRoles), nameof<UserRole>(x => x.role)].join('.'),
		[nameof<User>(x => x.additionalInfo), nameof<UserAdditionalInfo>(x => x.avatarUrl)].join('.'),
		nameof<User>(x => x.updatedAt),
		nameof<User>(x => x.createdAt),
		nameof<User>(x => x.hash),
		nameof<User>(x => x.isActive)
	];

	rowIdentity = x => x.id;


	constructor(
		protected router: Router,
		protected route: ActivatedRoute,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected queryParamsService: QueryParamsService,
		private userService: UserService,
		public authService: AuthService,
		private pipeService: PipeService,
		public enumUtils: EnumUtils,
		private language: TranslateService,
		private dialog: MatDialog,
		private fileUtils: FileUtils
	) {
		super(router, route, uiNotificationService, httpErrorHandlingService, queryParamsService);
		// Lookup setup
		// Default lookup values are defined in the user settings class.
		this.isTenantAdminMode = this.route.snapshot.data['tenantAdminMode'] ?? false;
		this.lookup = this.initializeLookup();
	}

	ngOnInit() {
		super.ngOnInit();
	}

	protected get hasTenantAdminMode(): boolean {
		return this.isTenantAdminMode && this.authService.hasPermission(AppPermission.ViewTenantUserPage);
	}

	protected initializeLookup(): UserLookup {
		const lookup = new UserLookup();
		lookup.metadata = { countAll: true };
		lookup.page = { offset: 0, size: this.ITEMS_PER_PAGE };
		lookup.isActive = [IsActive.Active];
		lookup.order = { items: [this.toDescSortField(nameof<User>(x => x.createdAt))] };
		this.updateOrderUiFields(lookup.order);

		lookup.project = {
			fields: this.lookupFields
		};

		return lookup;
	}

	protected setupColumns() {
		this.gridColumns.push(...[{
			prop: nameof<User>(x => x.name),
			sortable: true,
			languageName: 'USER-LISTING.FIELDS.NAME',
			cellTemplate: this.nameCellTemplate
		},
		{
			prop: nameof<User>(x => x.contacts),
			sortable: true,
			languageName: 'USER-LISTING.FIELDS.CONTACT-INFO',
			valueFunction: (item: User) => (item?.contacts ?? []).map(x => x.value).join(', ')
		},
		{
			prop: nameof<User>(x => x.createdAt),
			sortable: true,
			languageName: 'USER-LISTING.FIELDS.CREATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<User>(x => x.updatedAt),
			sortable: true,
			languageName: 'USER-LISTING.FIELDS.UPDATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<User>(x => x.globalRoles),
			languageName: 'USER-LISTING.FIELDS.ROLES',
			alwaysShown: true,
			maxWidth: 300,
			cellTemplate: this.roleCellTemplate
		},
		{
			prop: nameof<User>(x => x.isActive),
			sortable: false,
			languageName: 'USER-LISTING.FIELDS.IS-ACTIVE',
			pipe: this.pipeService.getPipe<IsActiveTypePipe>(IsActiveTypePipe)
		},
		]);
		this.propertiesAvailableForOrder = this.gridColumns.filter(x => x.sortable);
	}

	//
	// Listing Component functions
	//
	onColumnsChanged(event: ColumnsChangedEvent) {
		super.onColumnsChanged(event);
		this.onColumnsChangedInternal(event.properties.map(x => x.toString()));
	}

	private onColumnsChangedInternal(columns: string[]) {
		// Here are defined the projection fields that always requested from the api.
		const fields = new Set(this.lookupFields);
		this.gridColumns.map(x => x.prop)
			.filter(x => !columns?.includes(x as string))
			.forEach(item => {
				fields.delete(item as string)
			});
		this.lookup.project = { fields: [...fields] };
		this.onPageLoad({ offset: 0 } as PageLoadEvent);
	}

	protected loadListing(): Observable<QueryResult<User>> {
        let lookup = this.lookup;
        //TenantUserSubquery removed from lookup and added here so it is not part of the url params > removes bug of wrong tenantId in the subQuery when changing tenant
        if (this.authService.getSelectedTenantId() != null && this.isTenantAdminMode && this.authService.hasPermission(AppPermission.ViewTenantUserPage)){
            const tenantUserLookup: TenantUserLookup = new TenantUserLookup();
			tenantUserLookup.tenantIds = [this.authService.getSelectedTenantId()];
			tenantUserLookup.isActive = [IsActive.Active];
            lookup = {...this.lookup, tenantUserSubQuery: tenantUserLookup}
		}
		return this.userService.query(lookup);
	}

	public deleteType(id: Guid) {
		if (id) {
			const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
				data: {
					isDeleteConfirmation: true,
					message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-ITEM'),
					confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
					cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
				}
			});
			dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
				if (result) {
					this.userService.delete(id).pipe(takeUntil(this._destroyed))
						.subscribe(
							complete => this.onCallbackSuccess(),
							error => this.onCallbackError(error)
						);
				}
			});
		}
	}

	onCallbackSuccess(): void {
		this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-DELETE'), SnackBarNotificationLevel.Success);
		this.refresh();
	}

	onUserRowActivated(event: RowActivateEvent, baseRoute: string = null) {
		// Override default event to prevent click action
	}

	//
	// Export
	//

	export() { //TODO: send lookup to backend to export only filtered
		this.userService.exportCSV(this.hasTenantAdminMode)
			.pipe(takeUntil(this._destroyed))
			.subscribe(response => {
				const blob = new Blob([response.body], { type: 'application/csv' });
				const filename = this.fileUtils.getFilenameFromContentDispositionHeader(response.headers.get('Content-Disposition'));
				FileSaver.saveAs(blob, filename);
			},
			error => this.httpErrorHandlingService.handleBackedRequestError(error));
	}

	//
	// Avatar
	//
	public setDefaultAvatar(ev: Event) {
		(ev.target as HTMLImageElement).src = 'assets/images/profile-placeholder.png';
	}


	//
	// Invite
	//

	invite() {
		const dialogRef = this.dialog.open(UserInviteToTenantDialogComponent, {
			restoreFocus: false,
		});
	}

}
