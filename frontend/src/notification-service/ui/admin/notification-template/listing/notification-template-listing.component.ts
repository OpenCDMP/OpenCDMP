import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '@app/core/services/auth/auth.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { NotificationServiceEnumUtils } from '@notification-service/core/formatting/enum-utils.service';
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
import { IsActiveTypePipe } from '@common/formatting/pipes/is-active-type.pipe';
import { NotificationTemplate } from '@notification-service/core/model/notification-template.model';
import { NotificationTemplateLookup } from '@notification-service/core/query/notification-template.lookup';
import { NotificationTemplateService } from '@notification-service/services/http/notification-template.service';
import { NotificationTemplateChannelPipe } from '@common/formatting/pipes/notification-template-channel.pipe';
import { NotificationTemplateKindPipe } from '@common/formatting/pipes/notification-template-kind.pipe';
import { Language } from '@app/core/model/language/language';
import { NotificationTypePipe } from '@common/formatting/pipes/notification-type.pipe';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { AppPermission } from '@app/core/common/enum/permission.enum';

@Component({
    templateUrl: './notification-template-listing.component.html',
    styleUrls: ['./notification-template-listing.component.scss'],
    standalone: false
})
export class NotificationTemplateListingComponent extends BaseListingComponent<NotificationTemplate, NotificationTemplateLookup> implements OnInit {
	publish = false;
	userSettingsKey = { key: 'NotificationTemplateListingUserSettings' };
	propertiesAvailableForOrder: ColumnDefinition[];

	@ViewChild('actions', { static: true }) actions?: TemplateRef<any>;
	@ViewChild(HybridListingComponent, { static: true }) hybridListingComponent: HybridListingComponent;

	private readonly lookupFields: string[] = [
		nameof<NotificationTemplate>(x => x.id),
		nameof<NotificationTemplate>(x => x.notificationType),
		nameof<NotificationTemplate>(x => x.kind),
		nameof<NotificationTemplate>(x => x.channel),
		nameof<NotificationTemplate>(x => x.updatedAt),
		nameof<NotificationTemplate>(x => x.createdAt),
		nameof<NotificationTemplate>(x => x.hash),
		nameof<NotificationTemplate>(x => x.isActive),
		nameof<NotificationTemplate>(x => x.belongsToCurrentTenant)
	];

	rowIdentity = x => x.id;

	constructor(
		public routerUtils: RouterUtilsService,
		protected router: Router,
		protected route: ActivatedRoute,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected queryParamsService: QueryParamsService,
		private notificationTemplateService: NotificationTemplateService,
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

	protected canEdit(notificationTemplate: NotificationTemplate): boolean {
		return this.authService.hasPermission(AppPermission.EditNotificationTemplate) && notificationTemplate?.belongsToCurrentTenant && notificationTemplate?.isActive === IsActive.Active;
	}

	protected canDelete(notificationTemplate: NotificationTemplate): boolean {
		return this.authService.hasPermission(AppPermission.DeleteNotificationTemplate) && notificationTemplate?.belongsToCurrentTenant && notificationTemplate?.isActive === IsActive.Active;
	}

	protected initializeLookup(): NotificationTemplateLookup {
		const lookup = new NotificationTemplateLookup();
		lookup.metadata = { countAll: true };
		lookup.page = { offset: 0, size: this.ITEMS_PER_PAGE };
		lookup.isActive = [IsActive.Active];
		lookup.order = { items: [this.toDescSortField(nameof<NotificationTemplate>(x => x.updatedAt))] };
		this.updateOrderUiFields(lookup.order);

		lookup.project = {
			fields: this.lookupFields
		};

		return lookup;
	}

	protected setupColumns() {
		this.gridColumns.push(...[{
			prop: nameof<NotificationTemplate>(x => x.notificationType),
			sortable: true,
			languageName: 'NOTIFICATION-SERVICE.NOTIFICATION-TEMPLATE-LISTING.FIELDS.NOTIFICATION-TYPE',
			pipe: this.pipeService.getPipe<NotificationTypePipe>(NotificationTypePipe)
		},
		{
			prop: nameof<NotificationTemplate>(x => x.kind),
			sortable: true,
			languageName: 'NOTIFICATION-SERVICE.NOTIFICATION-TEMPLATE-LISTING.FIELDS.KIND',
			pipe: this.pipeService.getPipe<NotificationTemplateKindPipe>(NotificationTemplateKindPipe)
		}, 
		{
			prop: nameof<NotificationTemplate>(x => x.channel),
			sortable: true,
			languageName: 'NOTIFICATION-SERVICE.NOTIFICATION-TEMPLATE-LISTING.FIELDS.CHANNEL',
			pipe: this.pipeService.getPipe<NotificationTemplateChannelPipe>(NotificationTemplateChannelPipe)
		},
		{
			prop: nameof<NotificationTemplate>(x => x.createdAt),
			sortable: true,
			languageName: 'NOTIFICATION-SERVICE.NOTIFICATION-TEMPLATE-LISTING.FIELDS.CREATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<NotificationTemplate>(x => x.updatedAt),
			sortable: true,
			languageName: 'NOTIFICATION-SERVICE.NOTIFICATION-TEMPLATE-LISTING.FIELDS.UPDATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<NotificationTemplate>(x => x.isActive),
			sortable: false,
			languageName: 'NOTIFICATION-SERVICE.NOTIFICATION-TEMPLATE-LISTING.FIELDS.IS-ACTIVE',
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

	isDeleted(row: NotificationTemplate): boolean {
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

	protected loadListing(): Observable<QueryResult<NotificationTemplate>> {
		return this.notificationTemplateService.query(this.lookup);
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
					this.notificationTemplateService.delete(id).pipe(takeUntil(this._destroyed))
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
