import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { Reference } from '@app/core/model/reference/reference';
import { SumarizeTextPipe } from '@app/core/pipes/sumarize-text.pipe';
import { ReferenceLookup } from '@app/core/query/reference.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { ReferenceService } from '@app/core/services/reference/reference.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { BaseListingComponent } from '@common/base/base-listing-component';
import { PipeService } from '@common/formatting/pipe.service';
import { DataTableDateTimeFormatPipe } from '@app/core/pipes/date-time-format.pipe';
import { IsActiveTypePipe } from '@common/formatting/pipes/is-active-type.pipe';
import { ReferenceSourceTypePipe } from '@common/formatting/pipes/reference-source-type.pipe';
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
    templateUrl: './reference-listing.component.html',
    styleUrls: ['./reference-listing.component.scss'],
    standalone: false
})
export class ReferenceListingComponent extends BaseListingComponent<Reference, ReferenceLookup> implements OnInit {
	publish = false;
	userSettingsKey = { key: 'ReferenceListingUserSettings' };
	propertiesAvailableForOrder: ColumnDefinition[];

	@ViewChild('actions', { static: true }) actions?: TemplateRef<any>;
	@ViewChild(HybridListingComponent, { static: true }) hybridListingComponent: HybridListingComponent;

	private readonly lookupFields: string[] = [
		nameof<Reference>(x => x.id),
		nameof<Reference>(x => x.label),
		nameof<Reference>(x => x.source),
		nameof<Reference>(x => x.sourceType),
		nameof<Reference>(x => x.type.id),
		nameof<Reference>(x => x.type.name),
		nameof<Reference>(x => x.updatedAt),
		nameof<Reference>(x => x.createdAt),
		nameof<Reference>(x => x.hash),
		nameof<Reference>(x => x.belongsToCurrentTenant),
		nameof<Reference>(x => x.isActive)
	];

	rowIdentity = x => x.id;

	constructor(
		public routerUtils: RouterUtilsService,
		protected router: Router,
		protected route: ActivatedRoute,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected queryParamsService: QueryParamsService,
		private ReferenceService: ReferenceService,
		public authService: AuthService,
		private pipeService: PipeService,
		public enumUtils: EnumUtils,
		private language: TranslateService,
		private dialog: MatDialog,
		private sumarizeText: SumarizeTextPipe
	) {
		super(router, route, uiNotificationService, httpErrorHandlingService, queryParamsService);
		// Lookup setup
		// Default lookup values are defined in the user settings class.
		this.lookup = this.initializeLookup();
	}

	ngOnInit() {
		super.ngOnInit();
	}

	protected canEdit(reference: Reference): boolean {
		return this.authService.hasPermission(AppPermission.EditReference) && reference?.belongsToCurrentTenant && reference?.isActive === IsActive.Active;
	}

	protected canDelete(reference: Reference): boolean {
		return this.authService.hasPermission(AppPermission.DeleteReference) && reference?.belongsToCurrentTenant && reference?.isActive === IsActive.Active;
	}

	protected initializeLookup(): ReferenceLookup {
		const lookup = new ReferenceLookup();
		lookup.metadata = { countAll: true };
		lookup.page = { offset: 0, size: this.ITEMS_PER_PAGE };
		lookup.isActive = [IsActive.Active];
		lookup.order = { items: [this.toDescSortField(nameof<Reference>(x => x.updatedAt))] };
		this.updateOrderUiFields(lookup.order);

		lookup.project = {
			fields: this.lookupFields
		};

		return lookup;
	}

	protected setupColumns() {
		this.gridColumns.push(...[{
			prop: nameof<Reference>(x => x.label),
			sortable: true,
			languageName: 'REFERENCE-LISTING.FIELDS.LABEL',
			pipe: this.sumarizeText
		},
		{
			prop: nameof<Reference>(x => x.source),
			sortable: true,
			languageName: 'REFERENCE-LISTING.FIELDS.SOURCE',
		},
		{
			prop: nameof<Reference>(x => x.sourceType),
			sortable: true,
			languageName: 'REFERENCE-LISTING.FIELDS.SOURCE-TYPE',
			pipe: this.pipeService.getPipe<ReferenceSourceTypePipe>(ReferenceSourceTypePipe)
		},
		{
			prop: nameof<Reference>(x => x.type.name),
			sortable: true,
			languageName: 'REFERENCE-LISTING.FIELDS.TYPE',
			valueFunction: (item: Reference) => (item?.type?.name)
		},
		{
			prop: nameof<Reference>(x => x.createdAt),
			sortable: true,
			languageName: 'REFERENCE-LISTING.FIELDS.CREATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<Reference>(x => x.updatedAt),
			sortable: true,
			languageName: 'REFERENCE-LISTING.FIELDS.UPDATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<Reference>(x => x.isActive),
			sortable: false,
			languageName: 'REFERENCE-LISTING.FIELDS.IS-ACTIVE',
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

	isDeleted(row: Reference): boolean {
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

	protected loadListing(): Observable<QueryResult<Reference>> {
		return this.ReferenceService.query(this.lookup);
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
					this.ReferenceService.delete(id).pipe(takeUntil(this._destroyed))
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
