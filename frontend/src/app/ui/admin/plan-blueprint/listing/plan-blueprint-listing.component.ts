
import { Component, OnInit, TemplateRef, ViewChild, ChangeDetectionStrategy } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PlanBlueprintStatus } from '@app/core/common/enum/plan-blueprint-status';
import { PlanBlueprintVersionStatus } from '@app/core/common/enum/plan-blueprint-version-status';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { PlanBlueprint } from '@app/core/model/plan-blueprint/plan-blueprint';
import { PlanBlueprintLookup } from '@app/core/query/plan-blueprint.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { PlanBlueprintService } from '@app/core/services/plan/plan-blueprint.service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { FileUtils } from '@app/core/services/utilities/file-utils.service';
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
import * as FileSaver from 'file-saver';
import { Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { ImportPlanBlueprintDialogComponent } from './import-plan-blueprint/import-plan-blueprint.dialog.component';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { PlanBlueprintType } from '@app/core/model/plan-blueprint-type/plan-blueprint-type';
import { AdminPageHeaderComponent } from '../../../../../common/ui/admin-page-header/admin-page-header.component';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { AdminListingComponent } from '../../../../../common/modules/admin-listing/admin-listing.component';
import { PlanBlueprintListingFiltersComponent } from './filters/plan-blueprint-listing-filters.component';
import { UserSettingsPickerComponent } from '../../../../../common/modules/user-settings/user-settings-picker/user-settings-picker.component';
import { TenantSharedChipComponent } from '../../../../../common/ui/tenant-shared/tenant-shared-chip.component';
import { MatTooltip } from '@angular/material/tooltip';
import { MatMenuTrigger, MatMenu, MatMenuItem } from '@angular/material/menu';
import { NullifyValuePipe } from '../../../../../common/modules/admin-listing/pipes/nullify-value.pipe';


@Component({
    selector: 'app-plan-blueprint-listing-component',
    templateUrl: './plan-blueprint-listing.component.html',
    styleUrls: ['./plan-blueprint-listing.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [AdminPageHeaderComponent, MatButton, RouterLink, MatIcon, AdminListingComponent, PlanBlueprintListingFiltersComponent, UserSettingsPickerComponent, TenantSharedChipComponent, MatIconButton, MatTooltip, MatMenuTrigger, MatMenu, MatMenuItem, TranslatePipe, NullifyValuePipe]
})
export class PlanBlueprintListingComponent extends BaseListingComponent<PlanBlueprint, PlanBlueprintLookup> implements OnInit {
	publish = false;
	protected override readonly tenantScoped = true;
	userSettingsKey = { key: 'PlanBlueprintListingUserSettings' };
	/** Admins browse: a longer page than the author-facing listings. */
	protected override ITEMS_PER_PAGE = 25;
	/** Rows open under an absolute route (see onRowActivated in the template). */
	public override readonly rowLink = (row: any): string | null => row?.id ? this.routerUtils.generateUrl(['/plan-blueprints', String(row.id)], '/') : null;
	propertiesAvailableForOrder: ColumnDefinition[];
	planBlueprintStatuses = PlanBlueprintStatus;
	isActive = IsActive;

	@ViewChild('planBlueprintStatus', { static: true }) planBlueprintStatus?: TemplateRef<any>;
	@ViewChild('actions', { static: true }) actions?: TemplateRef<any>;
	@ViewChild('nameCell', { static: true }) nameCell?: TemplateRef<any>;

	private readonly lookupFields: string[] = [
		nameof<PlanBlueprint>(x => x.id),
		nameof<PlanBlueprint>(x => x.label),
		nameof<PlanBlueprint>(x => x.code),
		[nameof<PlanBlueprint>(x => x.type), nameof<PlanBlueprintType>(x => x.name)].join('.'),
		nameof<PlanBlueprint>(x => x.status),
		nameof<PlanBlueprint>(x => x.version),
		nameof<PlanBlueprint>(x => x.versionStatus),
		nameof<PlanBlueprint>(x => x.groupId),
		nameof<PlanBlueprint>(x => x.updatedAt),
		nameof<PlanBlueprint>(x => x.hash),
		nameof<PlanBlueprint>(x => x.isActive),
		nameof<PlanBlueprint>(x => x.belongsToCurrentTenant)
	];

	rowIdentity = x => x.id;

	constructor(
		public routerUtils: RouterUtilsService,
		protected router: Router,
		protected route: ActivatedRoute,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected queryParamsService: QueryParamsService,
		private planBlueprintService: PlanBlueprintService,
		public authService: AuthService,
		private pipeService: PipeService,
		public enumUtils: EnumUtils,
		private language: TranslateService,
		private dialog: MatDialog,
		private fileUtils: FileUtils,
		private analyticsService: AnalyticsService,
		private breadcrumbService: BreadcrumbService,
	) {
		super(router, route, uiNotificationService, httpErrorHandlingService, queryParamsService);
		// Lookup setup
		// Default lookup values are defined in the user settings class.
		this.lookup = this.initializeLookup();
	}

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.PlanBlueprintListing);
		super.ngOnInit();
	}

	/** Whether a search term or non-default filter is currently applied (drives the "no results" empty state). */
	public get hasActiveFilters(): boolean {
		return !!this.lookup?.like
			|| (this.lookup?.statuses?.length ?? 0) > 0
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

    protected isFinalized(blueprint: PlanBlueprint): boolean {
		return blueprint?.status == PlanBlueprintStatus.Finalized;
	}

    protected canEdit(blueprint: PlanBlueprint): boolean {
        return this.authService.hasPermission(AppPermission.EditPlanBlueprint) && blueprint?.belongsToCurrentTenant && !this.isFinalized(blueprint) && (blueprint?.isActive === IsActive.Active);
    }

    protected canCreateNewVersion(blueprint: PlanBlueprint): boolean {
        return blueprint?.status === PlanBlueprintStatus.Finalized && blueprint?.versionStatus == PlanBlueprintVersionStatus.Current && blueprint?.belongsToCurrentTenant != false && blueprint?.isActive === IsActive.Active;
    }

    protected canDelete(blueprint: PlanBlueprint): boolean {
        return this.authService.hasPermission(AppPermission.EditPlanBlueprint) && blueprint?.belongsToCurrentTenant && (blueprint?.isActive === IsActive.Active);
    }

    protected canDownloadXML(blueprint: PlanBlueprint): boolean {
        return this.authService.hasPermission(AppPermission.ExportPlanBlueprint);
    }

	protected initializeLookup(): PlanBlueprintLookup {
		const lookup = new PlanBlueprintLookup();
		lookup.metadata = { countAll: true };
		lookup.page = { offset: 0, size: this.ITEMS_PER_PAGE };
		lookup.isActive = [IsActive.Active];
		this.applyScope(lookup, 'own');
		lookup.versionStatuses = [PlanBlueprintVersionStatus.Current, PlanBlueprintVersionStatus.NotFinalized];
		lookup.order = { items: [this.toDescSortField(nameof<PlanBlueprint>(x => x.updatedAt))] };
		this.updateOrderUiFields(lookup.order);

		lookup.project = {
			fields: this.lookupFields
		};

		return lookup;
	}

	protected setupColumns() {
		this.gridColumns.push(...[{
			prop: nameof<PlanBlueprint>(x => x.label),
			sortable: true,
			languageName: 'PLAN-BLUEPRINT-LISTING.FIELDS.NAME',
			cellTemplate: this.nameCell
		},
		{
			prop: nameof<PlanBlueprint>(x => x.status),
			sortable: true,
			languageName: 'PLAN-BLUEPRINT-LISTING.FIELDS.STATUS',
			cellTemplate: this.planBlueprintStatus
		},
		{
			prop: [nameof<PlanBlueprint>(x => x.type), nameof<PlanBlueprintType>(x => x.name)].join('.'),
			sortable: false,
			languageName: 'PLAN-BLUEPRINT-LISTING.FIELDS.TYPE'
		},
		{
			prop: nameof<PlanBlueprint>(x => x.version),
			sortable: true,
			languageName: 'PLAN-BLUEPRINT-LISTING.FIELDS.VERSION'
		},
		{
			prop: nameof<PlanBlueprint>(x => x.createdAt),
			sortable: true,
			languageName: 'PLAN-BLUEPRINT-LISTING.FIELDS.CREATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<PlanBlueprint>(x => x.updatedAt),
			sortable: true,
			languageName: 'PLAN-BLUEPRINT-LISTING.FIELDS.UPDATED-AT',
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

	/** The same query for an arbitrary lookup: the admin listing counts the other tenant scope with it. */
	protected override loadListingWith(lookup: PlanBlueprintLookup): Observable<QueryResult<any>> {
		return this.planBlueprintService.query(lookup);
	}

	protected loadListing(): Observable<QueryResult<PlanBlueprint>> {
		return this.planBlueprintService.query(this.lookup).pipe(
			takeUntil(this._destroyed)
		);
	}

	protected delete(id: Guid) {
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
					this.planBlueprintService.delete(id).pipe(takeUntil(this._destroyed))
						.subscribe(
							complete => this.onCallbackSuccess(),
							error => this.onCallbackError(error)
						);
				}
			});
		}
	}

	private onCallbackSuccess(): void {
		this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-DELETE'), SnackBarNotificationLevel.Success);
		this.refresh();
	}

	protected export(event: PointerEvent, id: Guid): void {
		event?.stopPropagation();
		this.planBlueprintService.downloadXML(id)
			.pipe(takeUntil(this._destroyed))
			.subscribe(response => {
				const blob = new Blob([response.body], { type: 'application/xml' });
				const filename = this.fileUtils.getFilenameFromContentDispositionHeader(response.headers.get('Content-Disposition'));
				FileSaver.saveAs(blob, filename);
			},
			error => this.httpErrorHandlingService.handleBackedRequestError(error));
	}

	protected import(): void {
		const dialogRef = this.dialog.open(ImportPlanBlueprintDialogComponent, {
			data: {
				message: this.language.instant('PLAN-BLUEPRINT-LISTING.IMPORT.UPLOAD-XML-FILE-TITLE'),
				confirmButton: this.language.instant('PLAN-BLUEPRINT-LISTING.IMPORT.UPLOAD-XML'),
				cancelButton: this.language.instant('PLAN-BLUEPRINT-LISTING.IMPORT.UPLOAD-XML-FILE-CANCEL'),
				name: '',
				file: File,
				sucsess: false
			},
            width: 'min(600px, 90vw)'
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(data => {
			if (data && data.sucsess && data.name != null && data.file != null) {
				this.planBlueprintService.uploadFile(data.file, data.name)
					.pipe(takeUntil(this._destroyed))
					.subscribe(_ => {
						this.uiNotificationService.snackBarNotification(this.language.instant('PLAN-BLUEPRINT-LISTING.MESSAGES.BLUEPRINT-UPLOAD-SUCCESS'), SnackBarNotificationLevel.Success);
						this.refresh();
					},
						error => {
							this.httpErrorHandlingService.handleBackedRequestError(error);
						});
			}
		});
	}
}