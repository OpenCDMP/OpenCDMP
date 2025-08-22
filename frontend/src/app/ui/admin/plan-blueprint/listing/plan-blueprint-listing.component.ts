
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
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
import { IsActiveTypePipe } from '@common/formatting/pipes/is-active-type.pipe';
import { QueryResult } from '@common/model/query-result';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { ColumnDefinition, ColumnsChangedEvent, HybridListingComponent, PageLoadEvent } from '@common/modules/hybrid-listing/hybrid-listing.component';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import * as FileSaver from 'file-saver';
import { Observable } from 'rxjs';
import { takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { ImportPlanBlueprintDialogComponent } from './import-plan-blueprint/import-plan-blueprint.dialog.component';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';


@Component({
    selector: 'app-plan-blueprint-listing-component',
    templateUrl: './plan-blueprint-listing.component.html',
    styleUrls: ['./plan-blueprint-listing.component.scss'],
    standalone: false
})
export class PlanBlueprintListingComponent extends BaseListingComponent<PlanBlueprint, PlanBlueprintLookup> implements OnInit {
	publish = false;
	userSettingsKey = { key: 'PlanBlueprintListingUserSettings' };
	propertiesAvailableForOrder: ColumnDefinition[];
	planBlueprintStatuses = PlanBlueprintStatus;
	isActive = IsActive;
	mode;

	groupId: Guid | null = null;

	@ViewChild('planBlueprintStatus', { static: true }) planBlueprintStatus?: TemplateRef<any>;
	@ViewChild('actions', { static: true }) actions?: TemplateRef<any>;
	@ViewChild(HybridListingComponent, { static: true }) hybridListingComponent: HybridListingComponent;

	private readonly lookupFields: string[] = [
		nameof<PlanBlueprint>(x => x.id),
		nameof<PlanBlueprint>(x => x.label),
		nameof<PlanBlueprint>(x => x.code),
		nameof<PlanBlueprint>(x => x.status),
		nameof<PlanBlueprint>(x => x.version),
		nameof<PlanBlueprint>(x => x.versionStatus),
		nameof<PlanBlueprint>(x => x.groupId),
		nameof<PlanBlueprint>(x => x.updatedAt),
		nameof<PlanBlueprint>(x => x.createdAt),
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
		this.mode = this.route.snapshot?.data['mode'];
		this.lookup = this.initializeLookup();
		this.breadcrumbService.addExcludedParam("versions", true);
	}

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.PlanBlueprintListing);
		super.ngOnInit();
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
		lookup.order = { items: [this.toDescSortField(nameof<PlanBlueprint>(x => x.updatedAt))] };
		if (this.mode && this.mode == 'versions-listing') {
			this.groupId = Guid.parse(this.route.snapshot.paramMap.get('groupid'));
			this.breadcrumbService.addIdResolvedValue(this.groupId.toString(), "");
			lookup.groupIds = [this.groupId];
			lookup.versionStatuses = null;
		} else {
			this.groupId = null;
			lookup.versionStatuses = [PlanBlueprintVersionStatus.Current, PlanBlueprintVersionStatus.NotFinalized];
		}
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
			languageName: 'PLAN-BLUEPRINT-LISTING.FIELDS.NAME'
		},
		{
			prop: nameof<PlanBlueprint>(x => x.code),
			sortable: true,
			languageName: 'PLAN-BLUEPRINT-LISTING.FIELDS.CODE'
		},
		{
			prop: nameof<PlanBlueprint>(x => x.status),
			sortable: true,
			languageName: 'PLAN-BLUEPRINT-LISTING.FIELDS.STATUS',
			cellTemplate: this.planBlueprintStatus
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
			prop: nameof<PlanBlueprint>(x => x.isActive),
			sortable: false,
			languageName: 'PLAN-BLUEPRINT-LISTING.FIELDS.IS-ACTIVE',
			pipe: this.pipeService.getPipe<IsActiveTypePipe>(IsActiveTypePipe)
		},
		{
			alwaysShown: true,
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
		this.gridColumns.map(x => x.prop)
			.filter(x => !columns?.includes(x as string))
			.forEach(item => {
				fields.delete(item as string)
			});
		this.lookup.project = { fields: [...fields] };
		this.onPageLoad({ offset: 0 } as PageLoadEvent);
	}

	protected loadListing(): Observable<QueryResult<PlanBlueprint>> {
		return this.planBlueprintService.query(this.lookup).pipe(
			takeUntil(this._destroyed),
			tap((blueprints: QueryResult<PlanBlueprint>) => {
				const groupCode = blueprints.items?.[0]?.code;
				if (this.groupId != null && groupCode != null) {
					this.breadcrumbService.addIdResolvedValue(this.groupId.toString(), groupCode);
				}
			})
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
			restoreFocus: false,
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