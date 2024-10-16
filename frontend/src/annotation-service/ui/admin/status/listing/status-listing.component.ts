import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '@app/core/services/auth/auth.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { BaseListingComponent } from '@common/base/base-listing-component';
import { PipeService } from '@common/formatting/pipe.service';
import { DataTableDateTimeFormatPipe } from '@app/core/pipes/date-time-format.pipe';
import { QueryResult } from '@common/model/query-result';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { ColumnDefinition, ColumnsChangedEvent, HybridListingComponent, PageLoadEvent } from '@common/modules/hybrid-listing/hybrid-listing.component';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { Status } from '@annotation-service/core/model/status.model';
import { StatusLookup } from '@annotation-service/core/query/status.lookup';
import { StatusService } from '@annotation-service/services/http/status.service';
import { AnnotationServiceEnumUtils } from '@annotation-service/core/formatting/enum-utils.service';
import { InternalStatusTypePipe } from '@annotation-service/core/formatting/pipes/internal-status-type.pipe';
import { IsActiveTypePipe } from '@annotation-service/core/formatting/pipes/is-active-type.pipe';
import { AppPermission } from '@app/core/common/enum/permission.enum';

@Component({
	templateUrl: './status-listing.component.html',
	styleUrls: ['./status-listing.component.scss']
})
export class StatusListingComponent extends BaseListingComponent<Status, StatusLookup> implements OnInit {
	publish = false;
	userSettingsKey = { key: 'StatusListingUserSettings' };
	propertiesAvailableForOrder: ColumnDefinition[];

	@ViewChild('actions', { static: true }) actions?: TemplateRef<any>;
	@ViewChild(HybridListingComponent, { static: true }) hybridListingComponent: HybridListingComponent;

	private readonly lookupFields: string[] = [
		nameof<Status>(x => x.id),
		nameof<Status>(x => x.label),
		nameof<Status>(x => x.internalStatus),
		nameof<Status>(x => x.updatedAt),
		nameof<Status>(x => x.createdAt),
		nameof<Status>(x => x.hash),
		nameof<Status>(x => x.isActive),
		nameof<Status>(x => x.belongsToCurrentTenant)
	];

	rowIdentity = x => x.id;

	constructor(
		public routerUtils: RouterUtilsService,
		protected router: Router,
		protected route: ActivatedRoute,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected queryParamsService: QueryParamsService,
		private statusService: StatusService,
		public authService: AuthService,
		private pipeService: PipeService,
		public enumUtils: AnnotationServiceEnumUtils,
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

	protected canEdit(status: Status): boolean {
		return this.authService.hasPermission(AppPermission.EditStatus) && status?.belongsToCurrentTenant && status?.isActive === IsActive.Active;
	}

	protected canDelete(status: Status): boolean {
		return this.authService.hasPermission(AppPermission.EditStatus) && status?.belongsToCurrentTenant && status?.isActive === IsActive.Active;
	}

	protected initializeLookup(): StatusLookup {
		const lookup = new StatusLookup();
		lookup.metadata = { countAll: true };
		lookup.page = { offset: 0, size: this.ITEMS_PER_PAGE };
		lookup.isActive = [IsActive.Active];
		lookup.order = { items: [this.toDescSortField(nameof<Status>(x => x.createdAt))] };
		this.updateOrderUiFields(lookup.order);

		lookup.project = {
			fields: this.lookupFields
		};

		return lookup;
	}

	protected setupColumns() {
		this.gridColumns.push(...[{
			prop: nameof<Status>(x => x.label),
			sortable: true,
			languageName: 'ANNOTATION-SERVICE.STATUS-LISTING.FIELDS.LABEL',
		},
		{
			prop: nameof<Status>(x => x.internalStatus),
			sortable: true,
			languageName: 'ANNOTATION-SERVICE.STATUS-LISTING.FIELDS.INTERNAL-STATUS',
			pipe: this.pipeService.getPipe<InternalStatusTypePipe>(InternalStatusTypePipe)
		},
		{
			prop: nameof<Status>(x => x.createdAt),
			sortable: true,
			languageName: 'ANNOTATION-SERVICE.STATUS-LISTING.FIELDS.CREATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<Status>(x => x.updatedAt),
			sortable: true,
			languageName: 'ANNOTATION-SERVICE.STATUS-LISTING.FIELDS.UPDATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<Status>(x => x.isActive),
			sortable: true,
			languageName: 'ANNOTATION-SERVICE.STATUS-LISTING.FIELDS.IS-ACTIVE',
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

	isDeleted(row: Status): boolean {
		return row?.isActive === IsActive.Inactive;
	}

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

	protected loadListing(): Observable<QueryResult<Status>> {
		return this.statusService.query(this.lookup);
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
					this.statusService.delete(id).pipe(takeUntil(this._destroyed))
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
