import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router, ActivatedRoute } from '@angular/router';
import { DescriptionStatusEnum } from '@app/core/common/enum/description-status';
import { DescriptionStatus } from '@app/core/model/description-status/description-status';
import { UserSettingsKey } from '@app/core/model/user-settings/user-settings.model';
import { DataTableDateTimeFormatPipe } from '@app/core/pipes/date-time-format.pipe';
import { DescriptionStatusLookup } from '@app/core/query/description-status.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { DescriptionStatusService } from '@app/core/services/description-status/description-status.service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { UiNotificationService, SnackBarNotificationLevel } from '@app/core/services/notification/ui-notification-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { BaseListingComponent } from '@common/base/base-listing-component';
import { PipeService } from '@common/formatting/pipe.service';
import { IsActiveTypePipe } from '@common/formatting/pipes/is-active-type.pipe';
import { QueryResult } from '@common/model/query-result';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { ColumnDefinition, HybridListingComponent, ColumnsChangedEvent, PageLoadEvent } from '@common/modules/hybrid-listing/hybrid-listing.component';
import { TranslateService } from '@ngx-translate/core';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { Observable, takeUntil } from 'rxjs';
import { nameof } from 'ts-simple-nameof';
import { DescriptionStatusEditorResolver } from '../../editor/description-status-editor/description-status-editor.resolver';

@Component({
  selector: 'app-description-status-listing',
  templateUrl: './description-status-listing.component.html',
  styleUrl: './description-status-listing.component.scss'
})
export class DescriptionStatusListingComponent extends BaseListingComponent<DescriptionStatus, DescriptionStatusLookup> implements OnInit{
    userSettingsKey: UserSettingsKey = {key: 'DescriptionStatusListingUserSettings'};

    publish = false;
	propertiesAvailableForOrder: ColumnDefinition[];

    DescriptionStatusEnum = DescriptionStatusEnum;

	@ViewChild('actions', { static: true }) actions: TemplateRef<any>;
	@ViewChild('status', { static: true }) status: TemplateRef<any>;
	@ViewChild(HybridListingComponent, { static: true }) hybridListingComponent: HybridListingComponent;

    constructor(
        public routerUtils: RouterUtilsService,
        public authService: AuthService,
        protected router: Router,
		protected route: ActivatedRoute,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected queryParamsService: QueryParamsService,
        protected enumUtils: EnumUtils,
        private pipeService: PipeService,
        private descriptionStatusService: DescriptionStatusService,
        private analyticsService: AnalyticsService,
        private language: TranslateService,
        private dialog: MatDialog
    ){
        super(router, route, uiNotificationService, httpErrorHandlingService, queryParamsService);
        this.lookup = this.initializeLookup();
    }

    ngOnInit() {
		super.ngOnInit();
        this.analyticsService.trackPageView(AnalyticsService.DescriptionStatusListing);
	}

	protected canEdit(descriptionStatus: DescriptionStatus): boolean {
		return this.authService.hasPermission(this.authService.permissionEnum.EditDescriptionStatus) && descriptionStatus?.belongsToCurrentTenant && descriptionStatus?.isActive === IsActive.Active;
	}

	protected canDelete(descriptionStatus: DescriptionStatus): boolean {
			return  this.authService.hasPermission(this.authService.permissionEnum.DeleteDescriptionStatus) && descriptionStatus?.belongsToCurrentTenant && descriptionStatus?.isActive === IsActive.Active;   
	}

	private readonly lookupFields = DescriptionStatusEditorResolver.lookupFields();

    protected initializeLookup(): DescriptionStatusLookup {
		const lookup = new DescriptionStatusLookup();
		lookup.metadata = { countAll: true };
		lookup.page = { offset: 0, size: this.ITEMS_PER_PAGE };
		lookup.isActive = [IsActive.Active];
		lookup.order = { items: [this.toDescSortField(nameof<DescriptionStatus>(x => x.createdAt))] };
		this.updateOrderUiFields(lookup.order);

		lookup.project = {
			fields: this.lookupFields
		};

		return lookup;
	}

    onColumnsChanged(event: ColumnsChangedEvent) {
		super.onColumnsChanged(event);
		this.onColumnsChangedInternal(event.properties.map(x => x.toString()));
	}

	private onColumnsChangedInternal(columns: string[]) {
		const fields = new Set(this.lookupFields);
		this.gridColumns.map(x => x.prop)
			.filter(x => !columns?.includes(x as string))
			.forEach(item => {
				fields.delete(item as string)
			});
		this.lookup.project = { fields: [...fields] };
		this.onPageLoad({ offset: 0 } as PageLoadEvent);
	}

    protected loadListing(): Observable<QueryResult<DescriptionStatus>> {
        return this.descriptionStatusService.query(this.lookup);
    }
    
    protected setupColumns() {
        this.gridColumns.push(...[
		{
			prop: nameof<DescriptionStatus>(x => x.name),
			sortable: true,
			languageName: 'PLAN-STATUS-LISTING.FIELDS.NAME'
		},
        {
            prop: nameof<DescriptionStatus>(x => x.description),
            languageName: 'PLAN-STATUS-LISTING.FIELDS.DESCRIPTION'
        },
		{
			prop: nameof<DescriptionStatus>(x => x.internalStatus),
			sortable: true,
			languageName: 'PLAN-STATUS-LISTING.FIELDS.INTERNAL-STATUS',
            cellTemplate: this.status
		},
		{
			prop: nameof<DescriptionStatus>(x => x.createdAt),
			sortable: true,
			languageName: 'PLAN-STATUS-LISTING.FIELDS.CREATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<DescriptionStatus>(x => x.updatedAt),
			sortable: true,
			languageName: 'PLAN-STATUS-LISTING.FIELDS.UPDATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<DescriptionStatus>(x => x.isActive),
			sortable: true,
			languageName: 'PLAN-STATUS-LISTING.FIELDS.IS-ACTIVE',
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

    delete(id){
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
					this.descriptionStatusService.delete(id).pipe(takeUntil(this._destroyed))
						.subscribe({
                            complete: () => this.onCallbackSuccess(),
							error: (error) => this.onCallbackError(error)
                        }
						);
				}
			});
		}
    }

    onCallbackSuccess(): void {
		this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-DELETE'), SnackBarNotificationLevel.Success);
		this.refresh();
	}

    isDeleted(row: DescriptionStatus): boolean {
        return row?.isActive === IsActive.Inactive;
    }
}
