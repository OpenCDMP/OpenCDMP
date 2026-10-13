import { Component, OnInit, TemplateRef, ViewChild, ChangeDetectionStrategy } from '@angular/core';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { Tenant } from '@app/core/model/tenant/tenant';
import { TenantLookup } from '@app/core/query/tenant.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { TenantService } from '@app/core/services/tenant/tenant.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { BaseListingComponent } from '@common/base/base-listing-component';
import { PipeService } from '@common/formatting/pipe.service';
import { DataTableDateTimeFormatPipe } from '@app/core/pipes/date-time-format.pipe';
import { QueryResult } from '@common/model/query-result';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { ColumnDefinition, ColumnsChangedEvent, PageLoadEvent } from '@common/modules/admin-listing/listing.types';
import { Guid } from '@common/types/guid';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { PrincipalService } from '@app/core/services/http/principal.service';
import { TenantHandlingService } from '@app/core/services/tenant/tenant-handling.service';
import { BaseHttpParams } from '@common/http/base-http-params';
import { InterceptorType } from '@common/http/interceptors/interceptor-type';
import { AdminPageHeaderComponent } from '../../../../../common/ui/admin-page-header/admin-page-header.component';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { AdminListingComponent } from '../../../../../common/modules/admin-listing/admin-listing.component';
import { TenantListingFiltersComponent } from './filters/tenant-listing-filters.component';
import { UserSettingsPickerComponent } from '../../../../../common/modules/user-settings/user-settings-picker/user-settings-picker.component';
import { TenantDrawerComponent } from './tenant-drawer/tenant-drawer.component';
import { MatMenuTrigger, MatMenu, MatMenuItem } from '@angular/material/menu';
import { NullifyValuePipe } from '../../../../../common/modules/admin-listing/pipes/nullify-value.pipe';

@Component({
    templateUrl: './tenant-listing.component.html',
    styleUrls: ['./tenant-listing.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [AdminPageHeaderComponent, MatButton, MatIcon, AdminListingComponent, TenantListingFiltersComponent, UserSettingsPickerComponent, TenantDrawerComponent, MatIconButton, MatMenuTrigger, MatMenu, MatMenuItem, TranslatePipe, NullifyValuePipe]
})
export class TenantListingComponent extends BaseListingComponent<Tenant, TenantLookup> implements OnInit {
	publish = false;
	userSettingsKey = { key: 'TenantListingUserSettings' };
	/** Admins browse: a longer page than the author-facing listings. */
	protected override ITEMS_PER_PAGE = 25;
	propertiesAvailableForOrder: ColumnDefinition[];

	@ViewChild('actions', { static: true }) actions?: TemplateRef<any>;
	@ViewChild('nameCell', { static: true }) nameCell?: TemplateRef<any>;

	private readonly lookupFields: string[] = [
		nameof<Tenant>(x => x.id),
		nameof<Tenant>(x => x.name),
		nameof<Tenant>(x => x.code),
		nameof<Tenant>(x => x.memberCount),
		nameof<Tenant>(x => x.planCount),
		nameof<Tenant>(x => x.updatedAt),
		nameof<Tenant>(x => x.hash),
		nameof<Tenant>(x => x.isActive)
	];

	rowIdentity = x => x.id;

	/** The drawer's organisation: undefined while closed, null for a new one. */
	protected openedTenant: Guid | null | undefined = undefined;
	private memberOf = new Set<string>();

	constructor(
		public routerUtils: RouterUtilsService,
		protected router: Router,
		protected route: ActivatedRoute,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected queryParamsService: QueryParamsService,
		private TenantService: TenantService,
		public authService: AuthService,
		private pipeService: PipeService,
		public enumUtils: EnumUtils,
		private language: TranslateService,
		private dialog: MatDialog,
		private principalService: PrincipalService,
		private tenantHandlingService: TenantHandlingService,
		protected configurationService: ConfigurationService
	) {
		super(router, route, uiNotificationService, httpErrorHandlingService, queryParamsService);
		// Lookup setup
		// Default lookup values are defined in the user settings class.
		this.lookup = this.initializeLookup();
	}

	ngOnInit() {
		super.ngOnInit();
		const params = new BaseHttpParams();
		params.interceptorContext = { excludedInterceptors: [InterceptorType.TenantHeaderInterceptor] };
		this.principalService.myTenants({ params }).pipe(takeUntil(this._destroyed))
			.subscribe(tenants => this.memberOf = new Set((tenants ?? []).map(x => x.code)));
	}

	/** Switching only works into organisations the signed-in account belongs to. */
	protected canSwitch(tenant: Tenant): boolean {
		return tenant?.isActive === IsActive.Active && this.memberOf.has(tenant.code);
	}

	protected openTenant(tenant: Tenant): void {
		if (this.canEdit(tenant)) this.openedTenant = tenant.id;
	}

	protected switchTo(tenant: Tenant, path: string): void {
		this.authService.selectedTenant(tenant.code);
		window.location.href = this.tenantHandlingService.getBaseUrl() + this.tenantHandlingService.getUrlEnrichedWithTenantCode(path, tenant.code).substring(1);
	}

	/** Whether a search term or non-default filter is currently applied (drives the "no results" empty state). */
	public get hasActiveFilters(): boolean {
		return !!this.lookup?.like
			|| this.lookup?.isActive?.[0] === IsActive.Inactive;
	}

	/** Reset search & filters to their defaults while preserving column selection, ordering and page size. */
	public clearAllFilters(): void {
		const fresh = this.initializeLookup();
		fresh.order = this.lookup.order;
		fresh.project = this.lookup.project;
		fresh.page = { offset: 0, size: this.lookup.page.size };
		this.lookup = fresh;
		this.filterChanged(this.lookup);
	}

	protected canEdit(tenant: Tenant): boolean {
		return this.authService.hasPermission(AppPermission.EditTenant) && tenant?.isActive === IsActive.Active;
	}

	protected canDelete(tenant: Tenant): boolean {
		return this.authService.hasPermission(AppPermission.DeleteTenant) && tenant?.isActive === IsActive.Active;
	}

	protected initializeLookup(): TenantLookup {
		const lookup = new TenantLookup();
		lookup.metadata = { countAll: true };
		lookup.page = { offset: 0, size: this.ITEMS_PER_PAGE };
		lookup.isActive = [IsActive.Active];
		lookup.order = { items: [this.toDescSortField(nameof<Tenant>(x => x.updatedAt))] };
		this.updateOrderUiFields(lookup.order);

		lookup.project = {
			fields: this.lookupFields
		};

		return lookup;
	}

	protected setupColumns() {
		this.gridColumns.push(...[{
			prop: nameof<Tenant>(x => x.name),
			sortable: true,
			languageName: 'TENANT-LISTING.FIELDS.NAME',
			cellTemplate: this.nameCell
		},		{
			prop: nameof<Tenant>(x => x.createdAt),
			sortable: true,
			languageName: 'TENANT-LISTING.FIELDS.CREATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<Tenant>(x => x.memberCount),
			sortable: false,
			languageName: 'TENANT-LISTING.FIELDS.MEMBERS'
		},
		{
			prop: nameof<Tenant>(x => x.planCount),
			sortable: false,
			languageName: 'TENANT-LISTING.FIELDS.PLANS'
		},
		{
			prop: nameof<Tenant>(x => x.updatedAt),
			sortable: true,
			languageName: 'TENANT-LISTING.FIELDS.UPDATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			alwaysShown: true,
			sortable: false,
			cellTemplate: this.actions,
			maxWidth: 120
		}
		]);
		this.propertiesAvailableForOrder = this.gridColumns.filter(x => x.sortable);
	}

	//
	// Listing Component functions
	//

	isDeleted(row: Tenant): boolean {
		return row?.isActive === IsActive.Inactive;
	}

	onColumnsChanged(event: ColumnsChangedEvent) {
		super.onColumnsChanged(event);
		this.onColumnsChangedInternal(event.properties.map(x => x.toString()));
	}

	private onColumnsChangedInternal(columns: string[]) {
		// Here are defined the projection fields that always requested from the api.
		const fields = new Set(this.lookupFields);
		this.gridColumns.map(x => x.prop).filter(x => x != null).forEach(prop => {
			if (columns?.includes(prop as string)) { fields.add(prop as string); } else { fields.delete(prop as string); }
		});
		this.lookup.project = { fields: [...fields] };
		this.onPageLoad({ offset: 0 } as PageLoadEvent);
	}

	protected loadListing(): Observable<QueryResult<Tenant>> {
		return this.TenantService.query(this.lookup);
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
					this.TenantService.delete(id).pipe(takeUntil(this._destroyed))
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
}
