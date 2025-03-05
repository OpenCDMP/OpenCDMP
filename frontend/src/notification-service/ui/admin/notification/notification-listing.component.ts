import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '@app/core/services/auth/auth.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { BaseListingComponent } from '@common/base/base-listing-component';
import { PipeService } from '@common/formatting/pipe.service';
import { QueryResult } from '@common/model/query-result';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { ColumnDefinition, ColumnsChangedEvent, HybridListingComponent, PageLoadEvent } from '@common/modules/hybrid-listing/hybrid-listing.component';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { IsActiveTypePipe } from '@common/formatting/pipes/is-active-type.pipe';
import { Notification } from '@notification-service/core/model/notification.model';
import { NotificationLookup } from '@notification-service/core/query/notification.lookup';
import { NotificationService } from '@notification-service/services/http/notification-service';
import { NotificationTypePipe } from '@common/formatting/pipes/notification-type.pipe';
import { NotificationContactTypePipe } from '@common/formatting/pipes/notification-contact-type.pipe';
import { NotificationNotifyStatePipe } from '@common/formatting/pipes/notification-notify-state.pipe';
import { NotificationTrackingStatePipe } from '@common/formatting/pipes/notification-tracking-state.pipe';
import { NotificationTrackingProcessPipe } from '@common/formatting/pipes/notification-tracking-process.pipe';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { DataTableDateTimeFormatPipe } from '@app/core/pipes/date-time-format.pipe';
import { AppPermission } from '@app/core/common/enum/permission.enum';

@Component({
    templateUrl: './notification-listing.component.html',
    styleUrls: ['./notification-listing.component.scss'],
    standalone: false
})
export class NotificationListingComponent extends BaseListingComponent<Notification, NotificationLookup> implements OnInit {
	publish = false;
	userSettingsKey = { key: 'NotificationListingUserSettings' };
	propertiesAvailableForOrder: ColumnDefinition[];

	@ViewChild('actions', { static: true }) actions?: TemplateRef<any>;
	@ViewChild(HybridListingComponent, { static: true }) hybridListingComponent: HybridListingComponent;

	private readonly lookupFields: string[] = [
		nameof<Notification>(x => x.id),
		nameof<Notification>(x => x.type),
		nameof<Notification>(x => x.retryCount),
		nameof<Notification>(x => x.trackingState),
		nameof<Notification>(x => x.trackingProcess),
		nameof<Notification>(x => x.contactTypeHint),
		nameof<Notification>(x => x.notifyState),
		nameof<Notification>(x => x.notifiedWith),
		nameof<Notification>(x => x.notifiedAt),
		nameof<Notification>(x => x.user.id),
		nameof<Notification>(x => x.user.name),
		nameof<Notification>(x => x.createdAt),
		nameof<Notification>(x => x.updatedAt),
		nameof<Notification>(x => x.hash),
		nameof<Notification>(x => x.isActive),
		nameof<Notification>(x => x.belongsToCurrentTenant)
	];

	rowIdentity = x => x.id;

	constructor(
		protected router: Router,
		protected route: ActivatedRoute,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected queryParamsService: QueryParamsService,
		private notificationService: NotificationService,
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

	protected canDelete(notification: Notification): boolean {
		return notification?.belongsToCurrentTenant && notification?.isActive === IsActive?.Active;
	}

	protected initializeLookup(): NotificationLookup {
		const lookup = new NotificationLookup();
		lookup.metadata = { countAll: true };
		lookup.page = { offset: 0, size: this.ITEMS_PER_PAGE };
		lookup.isActive = [IsActive.Active];
		lookup.order = { items: [this.toDescSortField(nameof<Notification>(x => x.createdAt))] };
		this.updateOrderUiFields(lookup.order);

		lookup.project = {
			fields: this.lookupFields
		};

		return lookup;
	}

	protected setupColumns() {
		this.gridColumns.push(...[{
			prop: nameof<Notification>(x => x.type),
			sortable: true,
			languageName: 'NOTIFICATION-SERVICE.NOTIFICATION-LISTING.FIELDS.NOTIFICATION-TYPE',
			pipe: this.pipeService.getPipe<NotificationTypePipe>(NotificationTypePipe)
		},
		{
			prop: nameof<Notification>(x => x.contactTypeHint),
			sortable: true,
			languageName: 'NOTIFICATION-SERVICE.NOTIFICATION-LISTING.FIELDS.CONTACT-TYPE',
			pipe: this.pipeService.getPipe<NotificationContactTypePipe>(NotificationContactTypePipe)
		}, 
		{
			prop: nameof<Notification>(x => x.notifyState),
			sortable: true,
			languageName: 'NOTIFICATION-SERVICE.NOTIFICATION-LISTING.FIELDS.NOTIFY-STATE',
			pipe: this.pipeService.getPipe<NotificationNotifyStatePipe>(NotificationNotifyStatePipe)
		},
		{
			prop: nameof<Notification>(x => x.notifiedWith),
			sortable: true,
			languageName: 'NOTIFICATION-SERVICE.NOTIFICATION-LISTING.FIELDS.NOTIFIED-WITH',
			pipe: this.pipeService.getPipe<NotificationContactTypePipe>(NotificationContactTypePipe)
		},
		{
			prop: nameof<Notification>(x => x.notifiedAt),
			sortable: true,
			languageName: 'NOTIFICATION-SERVICE.NOTIFICATION-LISTING.FIELDS.NOTIFIED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<Notification>(x => x.trackingState),
			sortable: true,
			languageName: 'NOTIFICATION-SERVICE.NOTIFICATION-LISTING.FIELDS.TRACKING-STATE',
			pipe: this.pipeService.getPipe<NotificationTrackingStatePipe>(NotificationTrackingStatePipe)
		},
		{
			prop: nameof<Notification>(x => x.user.name),
			sortable: true,
			languageName: 'NOTIFICATION-SERVICE.NOTIFICATION-LISTING.FIELDS.USER',
		},
		{
			prop: nameof<Notification>(x => x.trackingProcess),
			sortable: true,
			languageName: 'NOTIFICATION-SERVICE.NOTIFICATION-LISTING.FIELDS.TRACKING-PROCESS',
			pipe: this.pipeService.getPipe<NotificationTrackingProcessPipe>(NotificationTrackingProcessPipe)
		},
		{
			prop: nameof<Notification>(x => x.retryCount),
			sortable: true,
			languageName: 'NOTIFICATION-SERVICE.NOTIFICATION-LISTING.FIELDS.RETRY-COUNT',
		},
		{
			prop: nameof<Notification>(x => x.createdAt),
			sortable: true,
			languageName: 'NOTIFICATION-SERVICE.NOTIFICATION-LISTING.FIELDS.CREATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<Notification>(x => x.updatedAt),
			sortable: true,
			languageName: 'NOTIFICATION-SERVICE.NOTIFICATION-LISTING.FIELDS.UPDATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<Notification>(x => x.isActive),
			sortable: false,
			languageName: 'NOTIFICATION-SERVICE.NOTIFICATION-LISTING.FIELDS.IS-ACTIVE',
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

	isDeleted(row: Notification): boolean {
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

	protected loadListing(): Observable<QueryResult<Notification>> {
		return this.notificationService.query(this.lookup);
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
					this.notificationService.delete(id).pipe(takeUntil(this._destroyed))
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
