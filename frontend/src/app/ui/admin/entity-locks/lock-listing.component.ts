import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '@app/core/services/auth/auth.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
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
import { Lock } from '@app/core/model/lock/lock.model';
import { LockLookup } from '@app/core/query/lock.lookup';
import { LockService } from '@app/core/services/lock/lock.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { LockTargetTypePipe } from '@common/formatting/pipes/lock-target-type.pipe';
import { User } from '@app/core/model/user/user';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { AppPermission } from '@app/core/common/enum/permission.enum';

@Component({
    templateUrl: './lock-listing.component.html',
    styleUrls: ['./lock-listing.component.scss'],
    standalone: false
})
export class LockListingComponent extends BaseListingComponent<Lock, LockLookup> implements OnInit {
	publish = false;
	userSettingsKey = { key: 'LockListingUserSettings' };
	propertiesAvailableForOrder: ColumnDefinition[];

	@ViewChild('actions', { static: true }) actions?: TemplateRef<any>;
	@ViewChild(HybridListingComponent, { static: true }) hybridListingComponent: HybridListingComponent;

	private readonly lookupFields: string[] = [
		nameof<Lock>(x => x.id),
		nameof<Lock>(x => x.target),
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

	protected canDelete(lock: Lock): boolean {
		return this.authService.hasPermission(AppPermission.DeleteLock) && lock?.belongsToCurrentTenant;
	}

	protected initializeLookup(): LockLookup {
		const lookup = new LockLookup();
		lookup.metadata = { countAll: true };
		lookup.page = { offset: 0, size: this.ITEMS_PER_PAGE };
		lookup.order = { items: [this.toDescSortField(nameof<Lock>(x => x.touchedAt))] };
		this.updateOrderUiFields(lookup.order);

		lookup.project = {
			fields: this.lookupFields
		};

		return lookup;
	}

	protected setupColumns() {
		this.gridColumns.push(...[{
			prop: nameof<Lock>(x => x.target),
			sortable: true,
			languageName: 'LOCK-LISTING.FIELDS.TARGET',
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
