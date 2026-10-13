import { Component, OnInit, TemplateRef, ViewChild, ChangeDetectionStrategy } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { Reference } from '@app/core/model/reference/reference';
import { SumarizeTextPipe } from '@app/core/pipes/sumarize-text.pipe';
import { ReferenceLookup } from '@app/core/query/reference.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { ReferenceService } from '@app/core/services/reference/reference.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { BaseListingComponent } from '@common/base/base-listing-component';
import { PipeService } from '@common/formatting/pipe.service';
import { DataTableDateTimeFormatPipe } from '@app/core/pipes/date-time-format.pipe';
import { ReferenceSourceTypePipe } from '@common/formatting/pipes/reference-source-type.pipe';
import { QueryResult } from '@common/model/query-result';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { ColumnDefinition, ColumnsChangedEvent, PageLoadEvent } from '@common/modules/admin-listing/listing.types';
import { Guid } from '@common/types/guid';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { AdminPageHeaderComponent } from '../../../../../common/ui/admin-page-header/admin-page-header.component';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { AdminListingComponent } from '../../../../../common/modules/admin-listing/admin-listing.component';
import { ReferenceListingFiltersComponent } from './filters/reference-listing-filters.component';
import { UserSettingsPickerComponent } from '../../../../../common/modules/user-settings/user-settings-picker/user-settings-picker.component';
import { TenantSharedChipComponent } from '../../../../../common/ui/tenant-shared/tenant-shared-chip.component';
import { MatMenuTrigger, MatMenu, MatMenuItem } from '@angular/material/menu';
import { MatTooltip } from '@angular/material/tooltip';
import { NullifyValuePipe } from '../../../../../common/modules/admin-listing/pipes/nullify-value.pipe';

@Component({
    templateUrl: './reference-listing.component.html',
    styleUrls: ['./reference-listing.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [AdminPageHeaderComponent, MatButton, RouterLink, MatIcon, AdminListingComponent, ReferenceListingFiltersComponent, UserSettingsPickerComponent, TenantSharedChipComponent, MatIconButton, MatMenuTrigger, MatMenu, MatMenuItem, MatTooltip, TranslatePipe, NullifyValuePipe]
})
export class ReferenceListingComponent extends BaseListingComponent<Reference, ReferenceLookup> implements OnInit {
	publish = false;
	protected override readonly tenantScoped = true;
	userSettingsKey = { key: 'ReferenceListingUserSettings' };
	/** Admins browse: a longer page than the author-facing listings. */
	protected override ITEMS_PER_PAGE = 25;
	propertiesAvailableForOrder: ColumnDefinition[];

	@ViewChild('actions', { static: true }) actions?: TemplateRef<any>;
	@ViewChild('nameCell', { static: true }) nameCell?: TemplateRef<any>;

	private readonly lookupFields: string[] = [
		nameof<Reference>(x => x.id),
		nameof<Reference>(x => x.label),
		nameof<Reference>(x => x.source),
		nameof<Reference>(x => x.sourceType),
		nameof<Reference>(x => x.type.id),
		nameof<Reference>(x => x.type.name),
		nameof<Reference>(x => x.updatedAt),
		nameof<Reference>(x => x.hash),
		nameof<Reference>(x => x.belongsToCurrentTenant),
		nameof<Reference>(x => x.isActive)
	];

	rowIdentity = x => x.id;

	constructor(
		public routerUtils: RouterUtilsService,
		protected router: Router,
		protected route: ActivatedRoute,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected queryParamsService: QueryParamsService,
		private ReferenceService: ReferenceService,
		public authService: AuthService,
		private pipeService: PipeService,
		public enumUtils: EnumUtils,
		private language: TranslateService,
		private dialog: MatDialog,
		private sumarizeText: SumarizeTextPipe
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
			|| (this.lookup?.typeIds?.length ?? 0) > 0
			|| (this.lookup?.sourceTypes?.length ?? 0) > 0
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

	protected canEdit(reference: Reference): boolean {
		return this.authService.hasPermission(AppPermission.EditReference) && reference?.belongsToCurrentTenant && reference?.isActive === IsActive.Active;
	}

	protected canDelete(reference: Reference): boolean {
		return this.authService.hasPermission(AppPermission.DeleteReference) && reference?.belongsToCurrentTenant && reference?.isActive === IsActive.Active;
	}

	protected initializeLookup(): ReferenceLookup {
		const lookup = new ReferenceLookup();
		lookup.metadata = { countAll: true };
		lookup.page = { offset: 0, size: this.ITEMS_PER_PAGE };
		lookup.isActive = [IsActive.Active];
		// Admin listings open on the current tenant's own rows; the shared (default-tenant) rows are one toggle away.
		this.applyScope(lookup, 'own');
		lookup.order = { items: [this.toDescSortField(nameof<Reference>(x => x.updatedAt))] };
		this.updateOrderUiFields(lookup.order);

		lookup.project = {
			fields: this.lookupFields
		};

		return lookup;
	}

	protected setupColumns() {
		this.gridColumns.push(...[{
			prop: nameof<Reference>(x => x.label),
			sortable: true,
			languageName: 'REFERENCE-LISTING.FIELDS.LABEL',
			cellTemplate: this.nameCell,
			pipe: this.sumarizeText
		},
		{
			prop: nameof<Reference>(x => x.source),
			sortable: true,
			languageName: 'REFERENCE-LISTING.FIELDS.SOURCE',
		},
		{
			prop: nameof<Reference>(x => x.sourceType),
			sortable: true,
			languageName: 'REFERENCE-LISTING.FIELDS.SOURCE-TYPE',
			pipe: this.pipeService.getPipe<ReferenceSourceTypePipe>(ReferenceSourceTypePipe)
		},
		{
			prop: nameof<Reference>(x => x.type.name),
			sortable: true,
			languageName: 'REFERENCE-LISTING.FIELDS.TYPE',
			valueFunction: (item: Reference) => (item?.type?.name)
		},
		{
			prop: nameof<Reference>(x => x.createdAt),
			sortable: true,
			languageName: 'REFERENCE-LISTING.FIELDS.CREATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<Reference>(x => x.updatedAt),
			sortable: true,
			languageName: 'REFERENCE-LISTING.FIELDS.UPDATED-AT',
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

	isDeleted(row: Reference): boolean {
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
	protected override loadListingWith(lookup: ReferenceLookup): Observable<QueryResult<any>> {
		return this.ReferenceService.query(lookup);
	}

	protected loadListing(): Observable<QueryResult<Reference>> {
		return this.ReferenceService.query(this.lookup);
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
					this.ReferenceService.delete(id).pipe(takeUntil(this._destroyed))
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
