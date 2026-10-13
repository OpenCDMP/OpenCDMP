import { Component, OnInit, TemplateRef, ViewChild, ChangeDetectionStrategy } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { PrefillingSource } from '@app/core/model/prefilling-source/prefilling-source';
import { PrefillingSourceLookup } from '@app/core/query/prefilling-source.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { PrefillingSourceService } from '@app/core/services/prefilling-source/prefilling-source.service';
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
import { AdminPageHeaderComponent } from '../../../../../common/ui/admin-page-header/admin-page-header.component';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { AdminListingComponent } from '../../../../../common/modules/admin-listing/admin-listing.component';
import { PrefillingSourceListingFiltersComponent } from './filters/prefilling-source-listing-filters.component';
import { UserSettingsPickerComponent } from '../../../../../common/modules/user-settings/user-settings-picker/user-settings-picker.component';
import { TenantSharedChipComponent } from '../../../../../common/ui/tenant-shared/tenant-shared-chip.component';
import { MatMenuTrigger, MatMenu, MatMenuItem } from '@angular/material/menu';
import { MatTooltip } from '@angular/material/tooltip';
import { NullifyValuePipe } from '../../../../../common/modules/admin-listing/pipes/nullify-value.pipe';

@Component({
    templateUrl: './prefilling-source-listing.component.html',
    styleUrls: ['./prefilling-source-listing.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [AdminPageHeaderComponent, MatButton, RouterLink, MatIcon, AdminListingComponent, PrefillingSourceListingFiltersComponent, UserSettingsPickerComponent, TenantSharedChipComponent, MatIconButton, MatMenuTrigger, MatMenu, MatMenuItem, MatTooltip, TranslatePipe, NullifyValuePipe]
})
export class PrefillingSourceListingComponent extends BaseListingComponent<PrefillingSource, PrefillingSourceLookup> implements OnInit {
	publish = false;
	protected override readonly tenantScoped = true;
	userSettingsKey = { key: 'PrefillingSourceListingUserSettings' };
	/** Admins browse: a longer page than the author-facing listings. */
	protected override ITEMS_PER_PAGE = 25;
	propertiesAvailableForOrder: ColumnDefinition[];

	@ViewChild('actions', { static: true }) actions?: TemplateRef<any>;
	@ViewChild('nameCell', { static: true }) nameCell?: TemplateRef<any>;

	private readonly lookupFields: string[] = [
		nameof<PrefillingSource>(x => x.id),
		nameof<PrefillingSource>(x => x.label),
		nameof<PrefillingSource>(x => x.code),
		nameof<PrefillingSource>(x => x.updatedAt),
		nameof<PrefillingSource>(x => x.hash),
		nameof<PrefillingSource>(x => x.belongsToCurrentTenant),
		nameof<PrefillingSource>(x => x.isActive)
	];

	rowIdentity = x => x.id;

	constructor(
		public routerUtils: RouterUtilsService,
		protected router: Router,
		protected route: ActivatedRoute,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected queryParamsService: QueryParamsService,
		private PrefillingSourceService: PrefillingSourceService,
		public authService: AuthService,
		private pipeService: PipeService,
		public enumUtils: EnumUtils,
		private language: TranslateService,
		private dialog: MatDialog
	) {
		super(router, route, uiNotificationService, httpErrorHandlingService, queryParamsService);
		// Lookup setup
		// Default lookup values are defined in the user settings class.
		this.lookup = this.initializeLookup();
	}

	ngOnInit() {
		super.ngOnInit();
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

	protected canEdit(prefillingSource: PrefillingSource): boolean {
		return this.authService.hasPermission(AppPermission.EditPrefillingSource) && prefillingSource?.belongsToCurrentTenant && prefillingSource?.isActive === IsActive.Active;
	}

	protected canDelete(prefillingSource: PrefillingSource): boolean {
		return this.authService.hasPermission(AppPermission.DeletePrefillingSource) && prefillingSource?.belongsToCurrentTenant && prefillingSource?.isActive === IsActive.Active;
	}


	protected initializeLookup(): PrefillingSourceLookup {
		const lookup = new PrefillingSourceLookup();
		lookup.metadata = { countAll: true };
		lookup.page = { offset: 0, size: this.ITEMS_PER_PAGE };
		lookup.isActive = [IsActive.Active];
		// Admin listings open on the current tenant's own rows; the shared (default-tenant) rows are one toggle away.
		this.applyScope(lookup, 'own');
		lookup.order = { items: [this.toDescSortField(nameof<PrefillingSource>(x => x.updatedAt))] };
		this.updateOrderUiFields(lookup.order);

		lookup.project = {
			fields: this.lookupFields
		};

		return lookup;
	}

	protected setupColumns() {
		this.gridColumns.push(...[{
			prop: nameof<PrefillingSource>(x => x.label),
			sortable: true,
			languageName: 'PREFILLING-SOURCE-LISTING.FIELDS.LABEL',
			cellTemplate: this.nameCell
		},		{
			prop: nameof<PrefillingSource>(x => x.createdAt),
			sortable: true,
			languageName: 'PREFILLING-SOURCE-LISTING.FIELDS.CREATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<PrefillingSource>(x => x.updatedAt),
			sortable: true,
			languageName: 'PREFILLING-SOURCE-LISTING.FIELDS.UPDATED-AT',
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

	isDeleted(row: PrefillingSource): boolean {
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

	/** The same query for an arbitrary lookup: the admin listing counts the other tenant scope with it. */
	protected override loadListingWith(lookup: PrefillingSourceLookup): Observable<QueryResult<any>> {
		return this.PrefillingSourceService.query(lookup);
	}

	protected loadListing(): Observable<QueryResult<PrefillingSource>> {
		return this.PrefillingSourceService.query(this.lookup);
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
					this.PrefillingSourceService.delete(id).pipe(takeUntil(this._destroyed))
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
