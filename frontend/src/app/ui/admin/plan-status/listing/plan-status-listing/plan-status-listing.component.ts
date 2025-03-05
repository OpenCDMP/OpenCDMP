import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PlanStatus } from '@app/core/model/plan-status/plan-status';
import { UserSettingsKey } from '@app/core/model/user-settings/user-settings.model';
import { DataTableDateTimeFormatPipe } from '@app/core/pipes/date-time-format.pipe';
import { PlanStatusLookup } from '@app/core/query/plan-status.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { PlanStatusService } from '@app/core/services/plan/plan-status.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { BaseListingComponent } from '@common/base/base-listing-component';
import { PipeService } from '@common/formatting/pipe.service';
import { IsActiveTypePipe } from '@common/formatting/pipes/is-active-type.pipe';
import { QueryResult } from '@common/model/query-result';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { ColumnDefinition, ColumnsChangedEvent, HybridListingComponent, PageLoadEvent } from '@common/modules/hybrid-listing/hybrid-listing.component';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { Observable, takeUntil } from 'rxjs';
import { nameof } from 'ts-simple-nameof';
import { PlanStatusEditorResolver } from '../../editor/plan-status-editor.resolver';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { PlanStatusEnum } from '@app/core/common/enum/plan-status';
import { AppPermission } from '@app/core/common/enum/permission.enum';

@Component({
    selector: 'app-plan-status-listing',
    templateUrl: './plan-status-listing.component.html',
    styleUrl: './plan-status-listing.component.scss',
    standalone: false
})
export class PlanStatusListingComponent extends BaseListingComponent<PlanStatus, PlanStatusLookup> implements OnInit{
    userSettingsKey: UserSettingsKey = {key: 'PlanStatusListingUserSettings'};

    publish = false;
	propertiesAvailableForOrder: ColumnDefinition[];

    PlanStatusEnum = PlanStatusEnum;

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
        private planStatusService: PlanStatusService,
        private analyticsService: AnalyticsService,
        private language: TranslateService,
        private dialog: MatDialog
    ){
        super(router, route, uiNotificationService, httpErrorHandlingService, queryParamsService);
        this.lookup = this.initializeLookup();
    }

    ngOnInit() {
		super.ngOnInit();
        this.analyticsService.trackPageView(AnalyticsService.PlanStatusListing);
	}

	protected canEdit(planStatus: PlanStatus): boolean {
		return this.authService.hasPermission(AppPermission.EditPlanStatus) && planStatus.belongsToCurrentTenant && planStatus?.isActive === IsActive.Active;
	}

	protected canDelete(planStatus: PlanStatus): boolean {
		return  this.authService.hasPermission(this.authService.permissionEnum.DeletePlanStatus) && planStatus.belongsToCurrentTenant && planStatus?.isActive === IsActive.Active;
	}

	private readonly lookupFields = PlanStatusEditorResolver.lookupFields();

    protected initializeLookup(): PlanStatusLookup {
		const lookup = new PlanStatusLookup();
		lookup.metadata = { countAll: true };
		lookup.page = { offset: 0, size: this.ITEMS_PER_PAGE };
		lookup.isActive = [IsActive.Active];
		lookup.order = { items: [this.toDescSortField(nameof<PlanStatus>(x => x.createdAt))] };
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

    protected loadListing(): Observable<QueryResult<PlanStatus>> {
        return this.planStatusService.query(this.lookup);
    }
    
    protected setupColumns() {
        this.gridColumns.push(...[
		{
			prop: nameof<PlanStatus>(x => x.name),
			sortable: true,
			languageName: 'PLAN-STATUS-LISTING.FIELDS.NAME'
		},
        {
            prop: nameof<PlanStatus>(x => x.description),
            languageName: 'PLAN-STATUS-LISTING.FIELDS.DESCRIPTION'
        },
		{
			prop: nameof<PlanStatus>(x => x.internalStatus),
			sortable: true,
			languageName: 'PLAN-STATUS-LISTING.FIELDS.INTERNAL-STATUS',
            cellTemplate: this.status
		},
		{
			prop: nameof<PlanStatus>(x => x.createdAt),
			sortable: true,
			languageName: 'PLAN-STATUS-LISTING.FIELDS.CREATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<PlanStatus>(x => x.updatedAt),
			sortable: true,
			languageName: 'PLAN-STATUS-LISTING.FIELDS.UPDATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<PlanStatus>(x => x.isActive),
			sortable: false,
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
					this.planStatusService.delete(id).pipe(takeUntil(this._destroyed))
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

    isDeleted(row: PlanStatus): boolean {
        return row?.isActive === IsActive.Inactive;
    }
}
