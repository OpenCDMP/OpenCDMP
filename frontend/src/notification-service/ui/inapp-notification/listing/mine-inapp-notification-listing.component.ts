import { Component, OnInit, TemplateRef, ViewChild, EventEmitter, Output, Input } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '@app/core/services/auth/auth.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { BaseListingComponent } from '@common/base/base-listing-component';
import { PipeService } from '@common/formatting/pipe.service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { ColumnDefinition, ColumnsChangedEvent, HybridListingComponent, PageLoadEvent } from '@common/modules/hybrid-listing/hybrid-listing.component';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { InAppNotificationLookup } from '@notification-service/core/query/inapp-notification.lookup';
import { InAppNotificationService } from '@notification-service/services/http/inapp-notification.service';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { nameof } from 'ts-simple-nameof';
import { Observable } from 'rxjs';
import { QueryResult } from '@common/model/query-result';
import { NotificationServiceEnumUtils } from '@notification-service/core/formatting/enum-utils.service';
import { TranslateService } from '@ngx-translate/core';
import { MatDialog } from '@angular/material/dialog';
import { Guid } from '@common/types/guid';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { takeUntil } from 'rxjs/operators';
import { NotificationTypePipe } from '@common/formatting/pipes/notification-type.pipe';
import { NotificationInAppTracking } from '@notification-service/core/enum/notification-inapp-tracking.enum';
import { InAppNotification } from '@notification-service/core/model/inapp-notification.model';
import { DataTableDateTimeFormatPipe } from '@app/core/pipes/date-time-format.pipe';

@Component({
	selector: 'app-mine-inapp-notification-listing',
	templateUrl: './mine-inapp-notification-listing.component.html',
	styleUrls: ['./mine-inapp-notification-listing.component.scss']
})
export class MineInAppNotificationListingComponent extends BaseListingComponent<InAppNotification, InAppNotificationLookup> implements OnInit {
	publish = false;
	userSettingsKey = { key: 'InAppNotificationListingUserSettings' };
	propertiesAvailableForOrder: ColumnDefinition[];
	notificationInAppTracking= NotificationInAppTracking;

	@ViewChild('actions', { static: true }) actions?: TemplateRef<any>;
	@ViewChild('state', { static: true }) state?: TemplateRef<any>;
	@ViewChild(HybridListingComponent, { static: true }) hybridListingComponent: HybridListingComponent;

	private readonly lookupFields: string[] = [
		nameof<InAppNotification>(x => x.id),
		nameof<InAppNotification>(x => x.subject),
		nameof<InAppNotification>(x => x.type),
		nameof<InAppNotification>(x => x.trackingState),
		nameof<InAppNotification>(x => x.createdAt),
		nameof<InAppNotification>(x => x.isActive)
	];

	rowIdentity = x => x.id;

	constructor(
		protected router: Router,
		protected route: ActivatedRoute,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected queryParamsService: QueryParamsService,
		private inAppNotificationService: InAppNotificationService,
		public authService: AuthService,
		private pipeService: PipeService,
		public enumUtils: NotificationServiceEnumUtils,
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

	protected initializeLookup(): InAppNotificationLookup {
		const lookup = new InAppNotificationLookup();
		lookup.metadata = { countAll: true };
		lookup.page = { offset: 0, size: this.ITEMS_PER_PAGE };
		lookup.isActive = [IsActive.Active];
		lookup.order = { items: [this.toDescSortField(nameof<InAppNotification>(x => x.createdAt))] };
		this.updateOrderUiFields(lookup.order);

		lookup.project = {
			fields: this.lookupFields
		};

		return lookup;
	}

	protected setupColumns() {
		this.gridColumns.push(...[{
			prop: nameof<InAppNotification>(x => x.trackingState),
			sortable: true,
			languageName: 'NOTIFICATION-SERVICE.INAPP-NOTIFICATION-LISTING.FIELDS.TRACKING-STATE',
			cellTemplate: this.state,
			maxWidth: 100
		},
		{
			prop: nameof<InAppNotification>(x => x.subject),
			sortable: true,
			languageName: 'NOTIFICATION-SERVICE.INAPP-NOTIFICATION-LISTING.FIELDS.SUBJECT'
		},
		{
			prop: nameof<InAppNotification>(x => x.type),
			sortable: true,
			languageName: 'NOTIFICATION-SERVICE.INAPP-NOTIFICATION-LISTING.FIELDS.NOTIFICATION-TYPE',
			pipe: this.pipeService.getPipe<NotificationTypePipe>(NotificationTypePipe)
		},
		{
			prop: nameof<InAppNotification>(x => x.createdAt),
			sortable: true,
			languageName: 'NOTIFICATION-SERVICE.INAPP-NOTIFICATION-LISTING.FIELDS.CREATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
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

	protected loadListing(): Observable<QueryResult<InAppNotification>> {
		return this.inAppNotificationService.query(this.lookup);
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
					this.inAppNotificationService.delete(id).pipe(takeUntil(this._destroyed))
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
