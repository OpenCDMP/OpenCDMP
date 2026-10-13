import { Component, OnInit, TemplateRef, ViewChild, ChangeDetectionStrategy } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { WebhookSubscription } from '@app/core/model/webhook/webhook';
import { WebhookSubscriptionLookup } from '@app/core/query/webhook-subscription.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { WebhookService } from '@app/core/services/webhook/webhook.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { DataTableDateTimeFormatPipe } from '@app/core/pipes/date-time-format.pipe';
import { BaseListingComponent } from '@common/base/base-listing-component';
import { PipeService } from '@common/formatting/pipe.service';
import { QueryResult } from '@common/model/query-result';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { ColumnDefinition, ColumnsChangedEvent, PageLoadEvent } from '@common/modules/admin-listing/listing.types';
import { Guid } from '@common/types/guid';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { WebhookDeliveriesDialogComponent } from './deliveries-dialog/webhook-deliveries-dialog.component';
import { AdminPageHeaderComponent } from '../../../../../common/ui/admin-page-header/admin-page-header.component';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { AdminListingComponent } from '../../../../../common/modules/admin-listing/admin-listing.component';
import { WebhookListingFiltersComponent } from './filters/webhook-listing-filters.component';
import { UserSettingsPickerComponent } from '../../../../../common/modules/user-settings/user-settings-picker/user-settings-picker.component';
import { TenantSharedChipComponent } from '../../../../../common/ui/tenant-shared/tenant-shared-chip.component';
import { MatMenuTrigger, MatMenu, MatMenuItem } from '@angular/material/menu';
import { MatTooltip } from '@angular/material/tooltip';
import { NullifyValuePipe } from '../../../../../common/modules/admin-listing/pipes/nullify-value.pipe';

@Component({
    templateUrl: './webhook-listing.component.html',
    styleUrls: ['./webhook-listing.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [AdminPageHeaderComponent, MatButton, RouterLink, MatIcon, AdminListingComponent, WebhookListingFiltersComponent, UserSettingsPickerComponent, TenantSharedChipComponent, MatIconButton, MatMenuTrigger, MatMenu, MatMenuItem, MatTooltip, TranslatePipe, NullifyValuePipe]
})
export class WebhookListingComponent extends BaseListingComponent<WebhookSubscription, WebhookSubscriptionLookup> implements OnInit {
	publish = false;
	userSettingsKey = { key: 'WebhookListingUserSettings' };
	/** Admins browse: a longer page than the author-facing listings. */
	protected override ITEMS_PER_PAGE = 25;
	protected override readonly tenantScoped = true;
	propertiesAvailableForOrder: ColumnDefinition[];

	@ViewChild('actions', { static: true }) actions?: TemplateRef<any>;
	@ViewChild('nameCell', { static: true }) nameCell?: TemplateRef<any>;

	private readonly lookupFields: string[] = [
		nameof<WebhookSubscription>(x => x.id),
		nameof<WebhookSubscription>(x => x.label),
		nameof<WebhookSubscription>(x => x.url),
		nameof<WebhookSubscription>(x => x.eventTypes),
		nameof<WebhookSubscription>(x => x.enabled),
		nameof<WebhookSubscription>(x => x.updatedAt),
		nameof<WebhookSubscription>(x => x.hash),
		nameof<WebhookSubscription>(x => x.isActive),
		nameof<WebhookSubscription>(x => x.belongsToCurrentTenant)
	];

	rowIdentity = x => x.id;

	constructor(
		public routerUtils: RouterUtilsService,
		protected router: Router,
		protected route: ActivatedRoute,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected queryParamsService: QueryParamsService,
		private webhookService: WebhookService,
		public authService: AuthService,
		private pipeService: PipeService,
		public enumUtils: EnumUtils,
		private language: TranslateService,
		private dialog: MatDialog
	) {
		super(router, route, uiNotificationService, httpErrorHandlingService, queryParamsService);
		this.lookup = this.initializeLookup();
	}

	ngOnInit() {
		super.ngOnInit();
	}

	public get hasActiveFilters(): boolean {
		return !!this.lookup?.like || this.lookup?.isActive?.[0] === IsActive.Inactive;
	}

	public clearAllFilters(): void {
		const fresh = this.initializeLookup();
		fresh.order = this.lookup.order;
		fresh.project = this.lookup.project;
		fresh.page = { offset: 0, size: this.lookup.page.size };
		this.lookup = fresh;
		this.filterChanged(this.lookup);
	}

	protected canEdit(item: WebhookSubscription): boolean {
		return this.authService.hasPermission(AppPermission.EditWebhookSubscription) && item?.belongsToCurrentTenant && item?.isActive === IsActive.Active;
	}

	protected canDelete(item: WebhookSubscription): boolean {
		return this.authService.hasPermission(AppPermission.DeleteWebhookSubscription) && item?.belongsToCurrentTenant && item?.isActive === IsActive.Active;
	}

	protected initializeLookup(): WebhookSubscriptionLookup {
		const lookup = new WebhookSubscriptionLookup();
		lookup.metadata = { countAll: true };
		lookup.page = { offset: 0, size: this.ITEMS_PER_PAGE };
		lookup.isActive = [IsActive.Active];
		// Admin listings open on the current tenant's own rows; the shared (default-tenant) rows are one toggle away.
		this.applyScope(lookup, 'own');
		lookup.order = { items: [this.toDescSortField(nameof<WebhookSubscription>(x => x.updatedAt))] };
		this.updateOrderUiFields(lookup.order);

		lookup.project = {
			fields: this.lookupFields
		};

		return lookup;
	}

	/** The same query for an arbitrary lookup: the admin listing counts the other tenant scope with it. */
	protected override loadListingWith(lookup: WebhookSubscriptionLookup): Observable<QueryResult<any>> {
		return this.webhookService.query(lookup);
	}

	protected setupColumns() {
		this.gridColumns.push(...[{
			prop: nameof<WebhookSubscription>(x => x.label),
			sortable: true,
			languageName: 'WEBHOOK-LISTING.FIELDS.LABEL',
			cellTemplate: this.nameCell
		}, {
			prop: nameof<WebhookSubscription>(x => x.url),
			sortable: true,
			languageName: 'WEBHOOK-LISTING.FIELDS.URL'
		},
		{
			prop: nameof<WebhookSubscription>(x => x.eventTypes),
			sortable: false,
			languageName: 'WEBHOOK-LISTING.FIELDS.EVENT-TYPES',
			valueFunction: (item: WebhookSubscription) => item?.eventTypes?.map(x => this.enumUtils.toWebhookEventTypeString(x)).join(', ')
		},
		{
			prop: nameof<WebhookSubscription>(x => x.enabled),
			sortable: true,
			languageName: 'WEBHOOK-LISTING.FIELDS.ENABLED',
			valueFunction: (item: WebhookSubscription) => this.language.instant(item?.enabled ? 'WEBHOOK-LISTING.ENABLED' : 'WEBHOOK-LISTING.DISABLED')
		},
		{
			prop: nameof<WebhookSubscription>(x => x.createdAt),
			sortable: true,
			languageName: 'WEBHOOK-LISTING.FIELDS.CREATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<WebhookSubscription>(x => x.updatedAt),
			sortable: true,
			languageName: 'WEBHOOK-LISTING.FIELDS.UPDATED-AT',
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

	isDeleted(row: WebhookSubscription): boolean {
		return row?.isActive === IsActive.Inactive;
	}

	onColumnsChanged(event: ColumnsChangedEvent) {
		super.onColumnsChanged(event);
		this.onColumnsChangedInternal(event.properties.map(x => x.toString()));
	}

	private onColumnsChangedInternal(columns: string[]) {
		const fields = new Set(this.lookupFields);
		this.gridColumns.map(x => x.prop).filter(x => x != null).forEach(prop => {
			if (columns?.includes(prop as string)) { fields.add(prop as string); } else { fields.delete(prop as string); }
		});
		this.lookup.project = { fields: [...fields] };
		this.onPageLoad({ offset: 0 } as PageLoadEvent);
	}

	protected loadListing(): Observable<QueryResult<WebhookSubscription>> {
		return this.webhookService.query(this.lookup);
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
					this.webhookService.delete(id).pipe(takeUntil(this._destroyed))
						.subscribe(
							complete => this.onCallbackSuccess(),
							error => this.onCallbackError(error)
						);
				}
			});
		}
	}

	public ping(id: Guid) {
		if (id) {
			this.webhookService.ping(id).pipe(takeUntil(this._destroyed))
				.subscribe({
					next: () => this.uiNotificationService.snackBarNotification(this.language.instant('WEBHOOK-LISTING.PING-QUEUED'), SnackBarNotificationLevel.Success),
					error: (error) => this.onCallbackError(error)
				});
		}
	}

	public showDeliveries(row: WebhookSubscription) {
		this.dialog.open(WebhookDeliveriesDialogComponent, {
			data: { subscriptionId: row.id, subscriptionLabel: row.label },
			maxWidth: '60rem',
			autoFocus: 'dialog'
		});
	}

	onCallbackSuccess(): void {
		this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-DELETE'), SnackBarNotificationLevel.Success);
		this.refresh();
	}
}
