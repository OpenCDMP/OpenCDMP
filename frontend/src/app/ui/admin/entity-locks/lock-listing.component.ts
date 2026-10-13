import { Component, OnInit, TemplateRef, ViewChild, ChangeDetectionStrategy } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '@app/core/services/auth/auth.service';
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
import { Lock } from '@app/core/model/lock/lock.model';
import { LockLookup } from '@app/core/query/lock.lookup';
import { LockService } from '@app/core/services/lock/lock.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { LockTargetTypePipe } from '@common/formatting/pipes/lock-target-type.pipe';
import { User } from '@app/core/model/user/user';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { LockTargetType } from '@app/core/common/enum/lock-target-type';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { AdminPageHeaderComponent } from '../../../../common/ui/admin-page-header/admin-page-header.component';
import { AdminListingComponent } from '../../../../common/modules/admin-listing/admin-listing.component';
import { LockListingFiltersComponent } from './filters/lock-listing-filters.component';
import { UserSettingsPickerComponent } from '../../../../common/modules/user-settings/user-settings-picker/user-settings-picker.component';
import { TenantSharedChipComponent } from '../../../../common/ui/tenant-shared/tenant-shared-chip.component';
import { MatIconButton } from '@angular/material/button';
import { MatMenuTrigger, MatMenu, MatMenuItem } from '@angular/material/menu';
import { MatIcon } from '@angular/material/icon';
import { MatTooltip } from '@angular/material/tooltip';

@Component({
    templateUrl: './lock-listing.component.html',
    styleUrls: ['./lock-listing.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [AdminPageHeaderComponent, AdminListingComponent, LockListingFiltersComponent, UserSettingsPickerComponent, RouterLink, TenantSharedChipComponent, MatIconButton, MatMenuTrigger, MatIcon, MatMenu, MatMenuItem, MatTooltip, TranslatePipe]
})
export class LockListingComponent extends BaseListingComponent<Lock, LockLookup> implements OnInit {
	publish = false;
	userSettingsKey = { key: 'LockListingUserSettings' };
	/** Admins browse: a longer page than the author-facing listings. */
	protected override ITEMS_PER_PAGE = 25;
	protected override readonly tenantScoped = true;
	propertiesAvailableForOrder: ColumnDefinition[];

	@ViewChild('actions', { static: true }) actions?: TemplateRef<any>;
	@ViewChild('targetCell', { static: true }) targetCell?: TemplateRef<any>;

	private readonly lookupFields: string[] = [
		nameof<Lock>(x => x.id),
		nameof<Lock>(x => x.target),
		nameof<Lock>(x => x.targetLabel),
		nameof<Lock>(x => x.targetType),
		nameof<Lock>(x => x.lockedBy),
		[nameof<Lock>(x => x.lockedBy), nameof<User>(x => x.name)].join('.'),
		nameof<Lock>(x => x.lockedAt),
		nameof<Lock>(x => x.touchedAt),
		nameof<Lock>(x => x.belongsToCurrentTenant),
		nameof<Lock>(x => x.hash),
	];

	rowIdentity = x => x.id;

	constructor(
		protected router: Router,
		protected route: ActivatedRoute,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected queryParamsService: QueryParamsService,
		private lockService: LockService,
		public authService: AuthService,
		private pipeService: PipeService,
		public enumUtils: EnumUtils,
		private language: TranslateService,
		private dialog: MatDialog,
		private routerUtils: RouterUtilsService
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
			|| (this.lookup?.targetTypes?.length ?? 0) > 0
			|| (this.lookup?.userIds?.length ?? 0) > 0;
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

	// Plans and assets are private to their organisation, so one locked in another organisation can't be opened from here.
	protected targetLink(lock: Lock): string | null {
		const elsewhere = lock.belongsToCurrentTenant === false;
		switch (lock.targetType) {
			case LockTargetType.Plan: return elsewhere ? null : this.routerUtils.generateUrl(['/plans/overview/', lock.target.toString()]);
			case LockTargetType.Description: return elsewhere ? null : this.routerUtils.generateUrl(['/assets/overview/', lock.target.toString()]);
			case LockTargetType.PlanBlueprint: return this.routerUtils.generateUrl(['/plan-blueprints/', lock.target.toString()]);
			case LockTargetType.DescriptionTemplate: return this.routerUtils.generateUrl(['/asset-templates/', lock.target.toString()]);
		}
	}

	protected canDelete(lock: Lock): boolean {
		return this.authService.hasPermission(AppPermission.DeleteLock) && lock?.belongsToCurrentTenant;
	}

	protected initializeLookup(): LockLookup {
		const lookup = new LockLookup();
		lookup.metadata = { countAll: true };
		lookup.page = { offset: 0, size: this.ITEMS_PER_PAGE };
		// Admin listings open on the current tenant's own rows; the shared (default-tenant) rows are one toggle away.
		this.applyScope(lookup, 'own');
		lookup.order = { items: [this.toDescSortField(nameof<Lock>(x => x.touchedAt))] };
		this.updateOrderUiFields(lookup.order);

		lookup.project = {
			fields: this.lookupFields
		};

		return lookup;
	}

	/** The same query for an arbitrary lookup: the admin listing counts the other tenant scope with it. */
	protected override loadListingWith(lookup: LockLookup): Observable<QueryResult<any>> {
		return this.lockService.query(lookup);
	}

	protected setupColumns() {
		this.gridColumns.push(...[{
			prop: nameof<Lock>(x => x.target),
			sortable: false,
			languageName: 'LOCK-LISTING.FIELDS.TARGET',
			cellTemplate: this.targetCell
		},
		{
			prop: nameof<Lock>(x => x.targetType),
			sortable: true,
			languageName: 'LOCK-LISTING.FIELDS.TARGET-TYPE',
			pipe: this.pipeService.getPipe<LockTargetTypePipe>(LockTargetTypePipe)
		},
		{
			prop: nameof<Lock>(x => x.lockedBy.name),
			sortable: true,
			languageName: 'LOCK-LISTING.FIELDS.LOCKED-BY',
		},
		{
			prop: nameof<Lock>(x => x.lockedAt),
			sortable: true,
			languageName: 'LOCK-LISTING.FIELDS.LOCKED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<Lock>(x => x.touchedAt),
			sortable: true,
			languageName: 'LOCK-LISTING.FIELDS.TOUCHED-AT',
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

	protected loadListing(): Observable<QueryResult<Lock>> {
		return this.lockService.query(this.lookup);
	}

	public deleteType(id: Guid, target: Guid) {
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
					this.lockService.delete(id, target).pipe(takeUntil(this._destroyed))
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
