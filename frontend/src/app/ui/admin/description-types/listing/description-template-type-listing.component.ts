import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { DescriptionTemplateTypeStatus } from '@app/core/common/enum/description-template-type-status';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { DescriptionTemplateType } from '@app/core/model/description-template-type/description-template-type';
import { DescriptionTemplateTypeLookup } from '@app/core/query/description-template-type.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { DescriptionTemplateTypeService } from '@app/core/services/description-template-type/description-template-type.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
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
import { Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { AppPermission } from '@app/core/common/enum/permission.enum';

@Component({
    templateUrl: './description-template-type-listing.component.html',
    styleUrls: ['./description-template-type-listing.component.scss'],
    standalone: false
})
export class DescriptionTemplateTypeListingComponent extends BaseListingComponent<DescriptionTemplateType, DescriptionTemplateTypeLookup> implements OnInit {
	publish = false;
	userSettingsKey = { key: 'DescriptionTemplateTypeListingUserSettings' };
	propertiesAvailableForOrder: ColumnDefinition[];
	descriptionTemplateTypeStatuses = DescriptionTemplateTypeStatus;

	@ViewChild('descriptionTemplateTypeStatus', { static: true }) descriptionTemplateTypeStatus?: TemplateRef<any>;
	@ViewChild('actions', { static: true }) actions?: TemplateRef<any>;
	@ViewChild(HybridListingComponent, { static: true }) hybridListingComponent: HybridListingComponent;

	private readonly lookupFields: string[] = [
		nameof<DescriptionTemplateType>(x => x.id),
		nameof<DescriptionTemplateType>(x => x.name),
		nameof<DescriptionTemplateType>(x => x.code),
		nameof<DescriptionTemplateType>(x => x.status),
		nameof<DescriptionTemplateType>(x => x.updatedAt),
		nameof<DescriptionTemplateType>(x => x.createdAt),
		nameof<DescriptionTemplateType>(x => x.hash),
		nameof<DescriptionTemplateType>(x => x.belongsToCurrentTenant),
		nameof<DescriptionTemplateType>(x => x.isActive)
	];

	rowIdentity = x => x.id;

	constructor(
		public routerUtils: RouterUtilsService,
		protected router: Router,
		protected route: ActivatedRoute,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected queryParamsService: QueryParamsService,
		private descriptionTemplateTypeService: DescriptionTemplateTypeService,
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

	protected canEdit(descriptionType: DescriptionTemplateType): boolean {
		return this.authService.hasPermission(AppPermission.EditDescriptionTemplateType) && descriptionType.belongsToCurrentTenant && !this.isFinalized(descriptionType) && descriptionType.isActive === IsActive.Active;
	}

	protected canDelete(descriptionType: DescriptionTemplateType): boolean { 
		return this.authService.hasPermission(AppPermission.DeleteDescriptionTemplateType) && descriptionType.belongsToCurrentTenant && descriptionType.isActive === IsActive.Active;
	}

	protected initializeLookup(): DescriptionTemplateTypeLookup {
		const lookup = new DescriptionTemplateTypeLookup();
		lookup.metadata = { countAll: true };
		lookup.page = { offset: 0, size: this.ITEMS_PER_PAGE };
		lookup.isActive = [IsActive.Active];
		lookup.order = { items: [this.toDescSortField(nameof<DescriptionTemplateType>(x => x.createdAt))] };
		this.updateOrderUiFields(lookup.order);

		lookup.project = {
			fields: this.lookupFields
		};

		return lookup;
	}

	protected setupColumns() {
		this.gridColumns.push(...[{
			prop: nameof<DescriptionTemplateType>(x => x.name),
			sortable: true,
			languageName: 'DESCRIPTION-TEMPLATE-TYPE-LISTING.FIELDS.NAME'
		},
		{
			prop: nameof<DescriptionTemplateType>(x => x.code),
			sortable: true,
			languageName: 'DESCRIPTION-TEMPLATE-TYPE-LISTING.FIELDS.CODE',
		},
		{
			prop: nameof<DescriptionTemplateType>(x => x.status),
			sortable: true,
			languageName: 'DESCRIPTION-TEMPLATE-TYPE-LISTING.FIELDS.STATUS',
			cellTemplate: this.descriptionTemplateTypeStatus
		},
		{
			prop: nameof<DescriptionTemplateType>(x => x.createdAt),
			sortable: true,
			languageName: 'DESCRIPTION-TEMPLATE-TYPE-LISTING.FIELDS.CREATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<DescriptionTemplateType>(x => x.updatedAt),
			sortable: true,
			languageName: 'DESCRIPTION-TEMPLATE-TYPE-LISTING.FIELDS.UPDATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<DescriptionTemplateType>(x => x.isActive),
			sortable: false,
			languageName: 'DESCRIPTION-TEMPLATE-TYPE-LISTING.FIELDS.IS-ACTIVE',
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

	isDeleted(row: DescriptionTemplateType): boolean {
		return row?.isActive === IsActive.Inactive;
}

	isFinalized(item: DescriptionTemplateType): boolean {
		return item.status === DescriptionTemplateTypeStatus.Finalized
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

	protected loadListing(): Observable<QueryResult<DescriptionTemplateType>> {
		return this.descriptionTemplateTypeService.query(this.lookup);
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
					this.descriptionTemplateTypeService.delete(id).pipe(takeUntil(this._destroyed))
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
