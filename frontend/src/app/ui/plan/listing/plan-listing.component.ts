import { Component, OnInit, ViewChild, AfterContentChecked, inject, ChangeDetectionStrategy } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { RecentActivityOrder } from '@app/core/common/enum/recent-activity-order';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { Description } from '@app/core/model/description/description';
import { DescriptionTemplatesInSection, PlanBlueprint, PlanBlueprintDefinition, RootSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { BasePlan, Plan, PlanAuthorizationInformation, PlanDescriptionTemplate, PlanUser } from '@app/core/model/plan/plan';
import { PlanReference } from '@app/core/model/plan/plan-reference';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { Reference } from '@app/core/model/reference/reference';
import { PlanLookup } from '@app/core/query/plan.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { PlanService } from '@app/core/services/plan/plan.service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { TourStateService } from '@app/library/guided-tour/tour-state.service';
import { planListingPageTour } from './plan-listing.tour';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { PageLoadEvent } from '@common/modules/admin-listing/listing.types';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import {  takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { BaseListingComponent } from '@common/base/base-listing-component';
import { QueryResult } from '@common/model/query-result';
import { Observable } from 'rxjs';
import { UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { ReferencesWithType } from '@app/core/query/description.lookup';
import { Guid } from '@common/types/guid';
import { PlanFilterService } from './filtering/services/plan-filter.service';
import { Tenant } from '@app/core/model/tenant/tenant';
import { BaseHttpParams } from '@common/http/base-http-params';
import { InterceptorType } from '@common/http/interceptors/interceptor-type';
import { PrincipalService } from '@app/core/services/http/principal.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { PlanFilterDialogComponent } from './filtering/plan-filter-dialog/plan-filter-dialog.component';
import { PlanListingFilters } from './filtering/plan-filter.component';
import { MatSelectChange, MatSelect, MatOption } from '@angular/material/select';
import { DescriptionStatus } from '@app/core/model/description-status/description-status';
import { PlanStatus, PlanStatusDefinition } from '@app/core/model/plan-status/plan-status';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { StorageFileService } from '@app/core/services/storage-file/storage-file.service';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import {EntityDoi} from "@app/core/model/entity-doi/entity-doi";
import {TenantConfigurationType} from "@app/core/common/enum/tenant-configuration-type";
import {ViewPreferencesEditorResolver} from "@app/ui/admin/tenant-configuration/models/view-preferences/view-preferences-editor.resolver";
import {TenantConfigurationService} from "@app/core/services/tenant-configuration/tenant-configuration.service";
import { IconConfiguration } from '@app/core/model/icon-configuration/icon-configuration';
import { TextFilterComponent } from '../../../../common/modules/text-filter/text-filter.component';
import { MatButtonToggleGroup, MatButtonToggle } from '@angular/material/button-toggle';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIcon } from '@angular/material/icon';
import { MatFormField } from '@angular/material/form-field';
import { ReactiveAsteriskDirective } from '../../../../common/forms/reactive-asterisk-directive';
import { FormsModule } from '@angular/forms';
import { MatIconButton, MatButton } from '@angular/material/button';
import { MatBadge } from '@angular/material/badge';
import { ActiveReferenceFiltersComponent } from '../../reference/reference-usage-filter/active-reference-filters.component';
import { PlanListingItemComponent } from './listing-item/plan-listing-item.component';

@Component({
    selector: 'app-plan-listing-component',
    templateUrl: 'plan-listing.component.html',
    styleUrls: ['./plan-listing.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [TextFilterComponent, MatButtonToggleGroup, MatButtonToggle, MatTooltip, MatIcon, MatFormField, MatSelect, ReactiveAsteriskDirective, FormsModule, MatOption, MatIconButton, MatButton, MatBadge, ActiveReferenceFiltersComponent, PlanListingItemComponent, PlanFilterDialogComponent, TranslatePipe]
})
export class PlanListingComponent extends BaseListingComponent<BasePlan, PlanLookup> implements OnInit, AfterContentChecked {

	userSettingsKey = { key: 'PlanListingUserSettings' };

    @ViewChild('filterSideDialog') filterSideDialog: PlanFilterDialogComponent;
	@ViewChild(MatPaginator, { static: true }) _paginator: MatPaginator;

	@ViewChild(MatSort) sort: MatSort;
	lookup: PlanLookup;
	totalCount: number;
	listingItems: any[] = [];
	isPublic: boolean = false;
	isLoading: boolean;
	protected ITEMS_PER_PAGE = 5;
	pageSize: number = 5;
	filtersCount: number;

	referenceFilters: ReferencesWithType[];
	/** Reference type whose filter section opens on arrival (`?referenceType=<id>`, e.g. from the dashboard overview). */
	expandReferenceTypeId: Guid = null;

    compactView: boolean = false;

	groupId: Guid | null = null;
	mode;
	get isVersionsListing(): boolean {
		return this.mode == 'versions-listing';
	}

	scrollbar: boolean;

    RecentActivityOrder = RecentActivityOrder;
    sortBy: RecentActivityOrder;

	tenants: Tenant[];
	orderedPlanPreferencesList:ReferenceType[];

	get isDescending(): boolean {
		return this.lookup?.order?.items?.[0].startsWith("-");
	}


	get sortingTooltipText(): string {
		return this.isDescending ? this.language.instant('PLAN-LISTING.ACTIONS.TOGGLE-ASCENDING') : this.language.instant('PLAN-LISTING.ACTIONS.TOGGLE-DESCENDING');
	}

	get hasFilters(): boolean {
		return (this.lookup.like != null && this.lookup.like != '') || this.lookup.statusIds  != null ||
			this.lookup.planReferenceSubQuery  != null || this.lookup.planDescriptionTemplateSubQuery  != null ||
			this.lookup.planBlueprintSubQuery  != null || this.lookup.planUserSubQuery  != null ||
			this.lookup.tenantSubQuery != null || (this.lookup.isActive != null && this.lookup.isActive?.find(x => x == IsActive.Inactive)) || this.lookup.planUserSubQuery != null;
	}
	get versionsModeEnabled(): boolean {
		return this.groupId != null;
	}

	constructor(
		protected router: Router,
		protected route: ActivatedRoute,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected queryParamsService: QueryParamsService,
		protected authService: AuthService,
		public dialog: MatDialog,
		public enumUtils: EnumUtils,
		private planService: PlanService,
		private language: TranslateService,
		private analyticsService: AnalyticsService,
		private principalService: PrincipalService,
		private breadcrumbService: BreadcrumbService,
        private storageFileService: StorageFileService,
        private sanitizer: DomSanitizer,
		private tenantConfigurationService: TenantConfigurationService
	) {
		super(router, route, uiNotificationService, httpErrorHandlingService, queryParamsService);
		this.mode = this.route.snapshot?.data['mode'];
		this.breadcrumbService.addExcludedParam("versions", true);
	}

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.PlanListing);
		this.isPublic = this.route.snapshot.data['isPublic'] ?? false;

		if (!this.isPublic && !this.authService.currentAccountIsAuthenticated()) {
			this.router.navigateByUrl("/explore-plans");
		}
		if (!this.isPublic) {
			// "What's new" tour for the 2.0.0 listing. TourStateService waits for the anchors (the first
			// card renders after the query returns) and drops any that never appear, e.g. an empty list.
			this.tourState.registerPageTour(this.pageTour);
			this.tourState.startIfUnseen(this.pageTour);
		}

		this.route.queryParamMap
			.pipe(takeUntil(this._destroyed))
			.subscribe(async (params: Params) => {
				const queryParams = this.route.snapshot.queryParams;

				if (!this.lookup && queryParams['lookup']) {
					this.lookup = this._parseLookupFromParams(queryParams);
					this.referenceFilters = [{
						referenceTypeId: null,
						referenceIds: this.lookup?.planReferenceSubQuery?.referenceIds ?? [],
					}];
					this.filtersCount = this._countFilters(this.lookup);
				}
				else if (!this.lookup) this.lookup = this.initializeLookup();

				const referenceType = queryParams['referenceType'];
				if (referenceType && !this.isPublic && !this.expandReferenceTypeId) {
					this.expandReferenceTypeId = Guid.parse(referenceType);
					this.filtersDialogOpen = true;
				}

                this.sortBy = this._getRecentActivityOrder(this.lookup.order.items[0]?.substring(1))
				this.onPageLoad({ offset: this.lookup.page.offset / this.lookup.page.size } as PageLoadEvent);
			});
		if (this.authService.isLoggedIn()) {
			this.tenantConfigurationService.getActiveType(TenantConfigurationType.ViewPreferences, ViewPreferencesEditorResolver.lookupFields())
				.pipe(takeUntil(this._destroyed)).subscribe(
					data => {
						this.orderedPlanPreferencesList = data?.viewPreferences?.planPreferences?.filter(x => x.referenceType?.isActive === IsActive.Active)?.map(x => x.referenceType) || [];
					},
					error => {
						this.orderedPlanPreferencesList = []
					}
				);
		} else this.orderedPlanPreferencesList = []
		if (!this.isPublic){
			this._loadUserTenants().pipe(takeUntil(this._destroyed)).subscribe( tenants => {
				this.tenants = tenants;
			});
		}
	}

	private readonly tourState = inject(TourStateService);
	private readonly pageTour = planListingPageTour(inject(TranslateService));

	override ngOnDestroy(): void {
		this.tourState.unregisterPageTour(this.pageTour);
		super.ngOnDestroy();
	}

	public isAuthenticated(): boolean {
		return this.authService.currentAccountIsAuthenticated();
	}

	ngAfterContentChecked(): void {
		this.scrollbar = this.hasScrollbar();
	}


	protected loadListing(): Observable<QueryResult<BasePlan>> {
        this.isLoading = true;
		if (this.isPublic) {
			return this.planService.publicQuery(this.lookup).pipe(takeUntil(this._destroyed))
				.pipe(tap(result => {
                    this.isLoading = false;
					if (this.versionsModeEnabled) {
						const latestVersionPlan: BasePlan = this._getLatestVersion(result.items);
						this.breadcrumbService.addIdResolvedValue(this.groupId.toString(), latestVersionPlan.label);
					}

					if (!result) { return []; }
					this.totalCount = result.count;
					if (this.lookup?.page?.offset === 0) this.listingItems = [];
					const plans = this._filterPlan([...result.items]);
					this.listingItems.push(...plans);
                    this.downloadStorageFiles(plans.map((x: Plan) => x.status?.definition?.iconConfiguration?.storageFile?.id)?.filter((x) => x) ?? [])
				}));
		} else {
			return this.planService.query(this.lookup).pipe(takeUntil(this._destroyed))
				.pipe(tap(result => {
                    this.isLoading = false;
					if (this.versionsModeEnabled) {
						const latestVersionPlan: BasePlan = this._getLatestVersion(result.items);
						this.breadcrumbService.addIdResolvedValue(this.groupId.toString(), latestVersionPlan.label);
					}

					if (!result) { return []; }
					this.totalCount = result.count;
					if (this.lookup?.page?.offset === 0) {
                        this.listingItems = [];
                        this.storageFileMap = new Map([]);
                    };
					let plans: any[];
					if (this.lookup?.isActive[0] == IsActive.Inactive) {
						plans = result.items;
					} else {
						plans = this._filterPlan([...result.items]);
					}
					this.listingItems.push(...plans);
                    this.downloadStorageFiles(plans.map((x: Plan) => x.status?.definition?.iconConfiguration?.storageFile?.id)?.filter((x) => x) ?? [])
				}));
		}
	}

    protected storageFileMap = new Map<Guid, SafeUrl>([]);
    protected downloadStorageFiles(storageFileIds: Guid[]){
        const newIds = new Set(storageFileIds.filter((id) => !this.storageFileMap.has(id)));
        newIds.forEach((id) => {
            this.storageFileService.download(id).pipe(takeUntil(this._destroyed))
            .subscribe({
                next: response => {
                    this.storageFileMap.set(id, this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(response.body)));
                },
                error: error => this.onCallbackError(error)
            });
        })
    }

	protected initializeLookup(): PlanLookup {
		const lookup = new PlanLookup();
		lookup.metadata = { countAll: true };
		lookup.page = { offset: 0, size: this.ITEMS_PER_PAGE };
		lookup.isActive = [IsActive.Active];

		const recentActivityOrder = this.isPublic ? this.toDescSortField(nameof<Description>(x => x.finalizedAt)) : this.toDescSortField(nameof<Description>(x => x.updatedAt));
		lookup.order = { items: [recentActivityOrder] };

		if (this.mode && this.isVersionsListing) {
			this.groupId = Guid.parse(this.route.snapshot.paramMap.get('groupId'));
			lookup.groupIds = [this.groupId];

			this.breadcrumbService.addIdResolvedValue(this.groupId.toString(), "");
		} else {
			lookup.groupIds = null;
		}

		this.updateOrderUiFields(lookup.order);

		lookup.project = {
			fields: this._lookupFields
		};

		return lookup;
	}

	protected setupColumns() { }

	loadMore() {
		this.lookup.page = { size: this.pageSize, offset: this.lookup.page.offset + this.pageSize };

		this.filterChanged(this.lookup, true);
	}

	orderByChanged($event: MatSelectChange) {
        const value = $event.value as RecentActivityOrder;
        const directionPrefix = this.isDescending ? '-' : '+';
        switch(value){
            case RecentActivityOrder.Status: {
                this.lookup.order = { items: [directionPrefix + nameof<Plan>(x => x.status)] };
                break;
            }
            case RecentActivityOrder.Label: {
                this.lookup.order = { items: [directionPrefix + nameof<Plan>(x => x.label)] };
                break;
            }
            case RecentActivityOrder.PublishedAt: {
                this.lookup.order = { items: [directionPrefix + nameof<Plan>(x => x.finalizedAt)] };
                break;
            }
            default: {
                this.lookup.order = { items: [directionPrefix + nameof<Plan>(x => x.updatedAt)] };
                break;
            }
        }
		this.filterChanged(this.lookup);
	}

	controlModified(): void {
		this.lookup.page = { size: this.pageSize, offset: 0 };
		this.filterChanged(this.lookup, true);
	}

    filtersDialogOpen: boolean = false;
	toggleFilters(): void {
        this.filtersDialogOpen = !this.filtersDialogOpen;
		// let dialogRef: MatDialogRef<PlanFilterDialogComponent, PlanListingFilters> = this.dialog.open(PlanFilterDialogComponent, {
		// 	width: '400px',
		// 	height: '100%',
		// 	id: 'filters',
		// 	restoreFocus: false,
		// 	position: { right: '0px;' },
		// 	panelClass: 'dialog-side-panel',
		// 	data: {
		// 		isPublic: this.isPublic ?? true,
		// 		hasSelectedTenant: this.authService.selectedTenant() != 'default',
        //         lookup: this.lookup,
		// 		//filterForm: this._buildFormFromLookup(this.lookup),
		// 		referencesWithTypeItems: this.referenceFilters ?? [],
		// 	}
		// });

		// dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
        //     this.filtersDialogOpen = false;
		// 	if (!result) return;

		// 	this.updateDataFn(result);
		// });
	}

    onFiltersChanged(filters: PlanListingFilters){
        this.filtersDialogOpen = false;
        this.updateDataFn(filters);
    }

	updateDataFn(filters: PlanListingFilters): void {
		this.referenceFilters = this._patchReferenceFilters(filters);
		this._patchLookupFromFilters(filters);
		this.filtersCount = this._countFilters(this.lookup);
		this.isLoading = false;
		this.filterChanged(this.lookup)
	}

	hasScrollbar(): boolean {
		return document.getElementById("main-page").scrollHeight > document.documentElement.clientHeight
	}

	/** Kept for the (currently commented-out) in-page "Take a tour" prompt; Support > Take a tour uses the same. */
	public restartTour(): void {
		this.tourState.startPageTour();
	}

	hasLikeFilters(): boolean {
		return this.lookup.like !== undefined && this.lookup.like !== null;
	}

	toggleSortDirection(): void {
        const orderBy = this.lookup?.order?.items[0]?.substring(1) ?? nameof<Plan>(x => x.updatedAt);
		const sortDirection = this.isDescending ? '+' : '-';
		this.lookup.order.items = [`${sortDirection}${orderBy}`];
        this.filterChanged(this.lookup);
	}

	private _loadUserTenants(): Observable<Array<Tenant>> {
		const params = new BaseHttpParams();
		params.interceptorContext = {
			excludedInterceptors: [InterceptorType.TenantHeaderInterceptor]
		};
		return this.principalService.myTenantsTranslated({ params: params });
	}

	private _parseLookupFromParams(params: Params): PlanLookup {
		const lookup: PlanLookup = JSON.parse(params['lookup']);

		if (!lookup) return this.initializeLookup();
		lookup.like = lookup.like ?? null;

		const queryOffset = 0;
		const querySize = (lookup.page?.offset ?? 0) + this.pageSize;
		lookup.page = { size: querySize, offset: queryOffset };
		lookup.project = { fields: this._lookupFields };
		lookup.metadata = { countAll: true };

		if (lookup.tenantSubQuery && lookup.tenantSubQuery?.codes && lookup.tenantSubQuery?.codes?.length > 0) {
			const tenantFilter = lookup.tenantSubQuery?.codes[0];

			if (tenantFilter != this.authService.selectedTenant()) {
				lookup.tenantSubQuery = null;
				this.filterChanged(lookup);
				return lookup;
			}
		}

		return lookup;
	}

	private _getRecentActivityOrder(recentActivityOrderValue: string): RecentActivityOrder {
		switch (recentActivityOrderValue) {
			case nameof<Plan>(x => x.updatedAt): return RecentActivityOrder.UpdatedAt;
			case nameof<Plan>(x => x.label): return RecentActivityOrder.Label;
			case nameof<Plan>(x => x.finalizedAt): return RecentActivityOrder.PublishedAt;
			case nameof<Plan>(x => x.status): return RecentActivityOrder.Status;
		}
	}

	private _patchLookupFromFilters(filters: PlanListingFilters): PlanLookup {
		const tenant = this.tenants?.find(t => t.code && t.code?.toString() == this.authService.selectedTenant());
		return PlanFilterService.patchLookup(this.lookup, filters, { tenantCode: tenant?.code ?? null, principalId: this.authService.userId() });
	}

	/** Drops one reference from the active reference filter (the chip's remove button). */
	removeReferenceFilter(referenceId: Guid): void {
		const remaining = (this.lookup.planReferenceSubQuery?.referenceIds ?? []).filter(x => x?.toString() !== referenceId?.toString());
		this._setReferenceFilter(remaining);
	}

	clearReferenceFilters(): void {
		this._setReferenceFilter([]);
	}

	private _setReferenceFilter(referenceIds: Guid[]): void {
		PlanFilterService.setReferenceIds(this.lookup, referenceIds);
		this.referenceFilters = referenceIds.length ? [{ referenceTypeId: null, referenceIds }] : null;
		this.filtersCount = this._countFilters(this.lookup);
		this.lookup.page = { size: this.pageSize, offset: 0 };
		this.filterChanged(this.lookup);
	}

	_patchReferenceFilters(filters: PlanListingFilters): ReferencesWithType[] {
		return filters?.references?.filter(( referencesWithType: ReferencesWithType ) => referencesWithType != null && referencesWithType.referenceIds?.length > 0) ?? null;
	}



	private _countFilters(lookup: PlanLookup): number {
		return PlanFilterService.countFilters(lookup);
	}

	private _filterPlan(plans: BasePlan[]): BasePlan[] {
		plans.forEach((plan: BasePlan) => {
			plan.descriptions = plan.descriptions?.filter(d => d.isActive == IsActive.Active) ?? [];
		})
		return plans;
	}

	private get _lookupFields(): string[] {
		return [
			nameof<Plan>(x => x.id),
			nameof<Plan>(x => x.label),
			nameof<Plan>(x => x.description),
			nameof<Plan>(x => x.isActive),
			nameof<Plan>(x => x.createdAt),

			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.name)].join('.'),
			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.internalStatus)].join('.'),
			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.statusColor)].join('.'),
			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.availableActions)].join('.'),
			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.matIconName)].join('.'),
			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),

			nameof<Plan>(x => x.accessType),
			nameof<Plan>(x => x.version),
			nameof<Plan>(x => x.versionStatus),
			nameof<Plan>(x => x.groupId),
			nameof<Plan>(x => x.updatedAt),
			nameof<Plan>(x => x.belongsToCurrentTenant),
			nameof<Plan>(x => x.finalizedAt),
			nameof<Plan>(x => x.hash),
			nameof<Plan>(x => x.tenantId),
			nameof<Plan>(x => x.entityDois),
			[nameof<Plan>(x => x.entityDois), nameof<EntityDoi>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.entityDois), nameof<EntityDoi>(x => x.doi)].join('.'),
			[nameof<Plan>(x => x.entityDois), nameof<EntityDoi>(x => x.repositoryId)].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.CreateNewVersionPlan].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.DeletePlan].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.ClonePlan].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.FinalizePlan].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.ExportPlan].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.InvitePlanUsers].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.AssignPlanUsers].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.EditPlan].join('.'),

			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.label)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.name)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.internalStatus)].join('.'),

			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.groupId)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.sectionId)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.isActive)].join('.'),

			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.label)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.title)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.sections)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.hasTemplates)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.groupId)].join('.'),

			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.id)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.name)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.role)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.plan.id)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.isActive)].join('.'),

			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.label)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.isActive)].join('.'),

			[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.descriptionTemplateGroupId)].join('.'),
			[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.sectionId)].join('.'),
			[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.descriptionTemplateGroupId)].join('.'),
			[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.isActive)].join('.'),
		];
	}

	private _getLatestVersion(plans: BasePlan[]): BasePlan {
		const maxVersion: number = Math.max(...plans.map(p => p.version));

		return plans.find(p => p.version == maxVersion);
	}
}
