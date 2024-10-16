import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { ReferenceTypeLookup } from '@app/core/query/reference-type.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
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
import { IsActiveTypePipe } from '@common/formatting/pipes/is-active-type.pipe';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { AppPermission } from '@app/core/common/enum/permission.enum';

@Component({
	templateUrl: './reference-type-listing.component.html',
	styleUrls: ['./reference-type-listing.component.scss']
})
export class ReferenceTypeListingComponent extends BaseListingComponent<ReferenceType, ReferenceTypeLookup> implements OnInit {
	publish = false;
	userSettingsKey = { key: 'ReferenceTypeListingUserSettings' };
	propertiesAvailableForOrder: ColumnDefinition[];

	@ViewChild('actions', { static: true }) actions?: TemplateRef<any>;
	@ViewChild(HybridListingComponent, { static: true }) hybridListingComponent: HybridListingComponent;

	private readonly lookupFields: string[] = [
		nameof<ReferenceType>(x => x.id),
		nameof<ReferenceType>(x => x.name),
		nameof<ReferenceType>(x => x.code),
		nameof<ReferenceType>(x => x.updatedAt),
		nameof<ReferenceType>(x => x.createdAt),
		nameof<ReferenceType>(x => x.belongsToCurrentTenant),
		nameof<ReferenceType>(x => x.isActive)
	];

	rowIdentity = x => x.id;

	constructor(
		public routerUtils: RouterUtilsService,
		protected router: Router,
		protected route: ActivatedRoute,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected queryParamsService: QueryParamsService,
		private referenceTypeService: ReferenceTypeService,
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

	protected canEdit(referenceType: ReferenceType): boolean {
		return this.authService.hasPermission(AppPermission.EditReferenceType) && referenceType?.belongsToCurrentTenant && referenceType?.isActive === IsActive.Active;
	}

	protected canDelete(referenceType: ReferenceType): boolean {
		return this.authService.hasPermission(AppPermission.DeleteReferenceType) && referenceType?.belongsToCurrentTenant && referenceType?.isActive === IsActive.Active;
	}

	protected initializeLookup(): ReferenceTypeLookup {
		const lookup = new ReferenceTypeLookup();
		lookup.metadata = { countAll: true };
		lookup.page = { offset: 0, size: this.ITEMS_PER_PAGE };
		lookup.isActive = [IsActive.Active];
		lookup.order = { items: [this.toDescSortField(nameof<ReferenceType>(x => x.createdAt))] };
		this.updateOrderUiFields(lookup.order);

		lookup.project = {
			fields: this.lookupFields
		};

		return lookup;
	}

	protected setupColumns() {
		this.gridColumns.push(...[{
			prop: nameof<ReferenceType>(x => x.name),
			sortable: true,
			languageName: 'REFERENCE-TYPE-LISTING.FIELDS.NAME'
		},
		{
			prop: nameof<ReferenceType>(x => x.code),
			sortable: true,
			languageName: 'REFERENCE-TYPE-LISTING.FIELDS.CODE',
		},
		{
			prop: nameof<ReferenceType>(x => x.createdAt),
			sortable: true,
			languageName: 'REFERENCE-TYPE-LISTING.FIELDS.CREATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<ReferenceType>(x => x.updatedAt),
			sortable: true,
			languageName: 'REFERENCE-TYPE-LISTING.FIELDS.UPDATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<ReferenceType>(x => x.isActive),
			sortable: true,
			languageName: 'REFERENCE-TYPE-LISTING.FIELDS.IS-ACTIVE',
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

	isDeleted(row: ReferenceType): boolean {
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

	protected loadListing(): Observable<QueryResult<ReferenceType>> {
		return this.referenceTypeService.query(this.lookup);
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
					this.referenceTypeService.delete(id).pipe(takeUntil(this._destroyed))
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
