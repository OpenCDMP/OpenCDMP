import { Component, OnInit, ViewChild } from '@angular/core';
import { UntypedFormBuilder, UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { RecentActivityOrder } from '@app/core/common/enum/recent-activity-order';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { Description } from '@app/core/model/description/description';
import { DescriptionTemplatesInSection, PlanBlueprint, PlanBlueprintDefinition, PlanBlueprintDefinitionSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { BasePlan, Plan, PlanDescriptionTemplate, PlanUser } from '@app/core/model/plan/plan';
import { PlanReference } from '@app/core/model/plan/plan-reference';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { Reference } from '@app/core/model/reference/reference';
import { PlanLookup } from '@app/core/query/plan.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { PlanService } from '@app/core/services/plan/plan.service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { GuidedTour, Orientation } from '@app/library/guided-tour/guided-tour.constants';
import { GuidedTourService } from '@app/library/guided-tour/guided-tour.service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { PageLoadEvent, SortDirection } from '@common/modules/hybrid-listing/hybrid-listing.component';
import { TranslateService } from '@ngx-translate/core';
import { NgDialogAnimationService } from "ng-dialog-animation";
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
import { Lookup } from '@common/model/lookup';
import { MatSelectChange } from '@angular/material/select';

@Component({
	selector: 'app-plan-listing-component',
	templateUrl: 'plan-listing.component.html',
	styleUrls: ['./plan-listing.component.scss'],
})
export class PlanListingComponent extends BaseListingComponent<BasePlan, PlanLookup> implements OnInit {

	userSettingsKey = { key: 'PlanListingUserSettings' };

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

	groupId: Guid | null = null;
	mode;
	get isVersionsListing(): boolean {
		return this.mode == 'versions-listing';
	}

	scrollbar: boolean;

    RecentActivityOrder = RecentActivityOrder;
    sortBy: RecentActivityOrder;

	tenants: Tenant[];


	get isAscending(): boolean {
		return this.lookup?.order?.items?.[0].startsWith("-");
	}
	get isDescending(): boolean {
		return this.lookup?.order?.items?.[0].startsWith("+");
	}

	get sortingTooltipText(): string {
		return this.isAscending ? this.language.instant('PLAN-LISTING.ACTIONS.TOGGLE-ΑSCENDING') : this.language.instant('PLAN-LISTING.ACTIONS.TOGGLE-DESCENDING');
	}
	get hasListingItems(): boolean {
		return this.listingItems != null && this.listingItems.length > 0;
	}
	get hasFilters(): boolean {
		return (this.lookup.like != null && this.lookup.like != '') || this.lookup.statuses  != null ||
			this.lookup.planReferenceSubQuery  != null || this.lookup.planDescriptionTemplateSubQuery  != null ||
			this.lookup.planBlueprintSubQuery  != null || this.lookup.planUserSubQuery  != null ||
			this.lookup.tenantSubQuery != null;
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
		private planService: PlanService,
		public dialogAnimation: NgDialogAnimationService,
		public dialog: MatDialog,
		public enumUtils: EnumUtils,
		private language: TranslateService,
		private authService: AuthService,
		private guidedTourService: GuidedTourService,
		private analyticsService: AnalyticsService,
		private principalService: PrincipalService,
		private breadcrumbService: BreadcrumbService,
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

                this.sortBy = this._getRecentActivityOrder(this.lookup.order.items[0]?.substring(1))
				this.onPageLoad({ offset: this.lookup.page.offset / this.lookup.page.size } as PageLoadEvent);
			});
		
		if (!this.isPublic){
			this._loadUserTenants().pipe(takeUntil(this._destroyed)).subscribe( tenants => {
				this.tenants = tenants;
			});
		}
	}

	public dashboardTour: GuidedTour = {
		tourId: 'plan-description-tour',
		useOrb: true,
		steps: [
			{
				selector: '.plan-tour',
				content: 'Step 1',
				orientation: Orientation.Right,
				isStepUnique: false
			},
			{
				selector: '.description-tour',
				content: 'Step 2',
				orientation: Orientation.Right,
				isStepUnique: false,
				useHighlightPadding: true
			}
		]
	};

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
					this.listingItems.push(...result.items);
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
					if (this.lookup?.page?.offset === 0) this.listingItems = [];
					const plans = this._filterPlan([...result.items]);
					this.listingItems.push(...plans);
				}));
		}
	}

	protected initializeLookup(): PlanLookup {
		const lookup = new PlanLookup();
		lookup.metadata = { countAll: true };
		lookup.page = { offset: 0, size: this.ITEMS_PER_PAGE };
		lookup.isActive = [IsActive.Active];

		let recentActivityOrder = this.isPublic ? this.toDescSortField(nameof<Description>(x => x.finalizedAt)) : this.toDescSortField(nameof<Description>(x => x.updatedAt));
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

	openTour() {
		this.language.get('PLAN-LISTING.TEXT-INFO').pipe(takeUntil(this._destroyed)).subscribe((x: string) => {
			this.dashboardTour.steps[0].title = x + '\n\n' +
				this.language.instant('PLAN-LISTING.TEXT-INFO-QUESTION') + ' ' +
				this.language.instant('PLAN-LISTING.LINK-ZENODO') + ' ' +
				this.language.instant('PLAN-LISTING.GET-IDEA');

			this.dashboardTour.steps[1].title = this.language.instant('DESCRIPTION-LISTING.TEXT-INFO') + '\n\n' +
				this.language.instant('DESCRIPTION-LISTING.TEXT-INFO-QUESTION') + ' ' +
				this.language.instant('DESCRIPTION-LISTING.GET-IDEA');

			this.guidedTourService.startTour(this.dashboardTour);
		});
	}

	loadMore() {
		this.lookup.page = { size: this.pageSize, offset: this.lookup.page.offset + this.pageSize };

		this.filterChanged(this.lookup, true);
	}

	orderByChanged($event: MatSelectChange) {
        const value = $event.value as RecentActivityOrder;
        const directionPrefix = this.isAscending ? '-' : '+';
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

	openFiltersDialog(): void {
		let dialogRef: MatDialogRef<PlanFilterDialogComponent, PlanListingFilters> = this.dialog.open(PlanFilterDialogComponent, {
			width: '456px',
			height: '100%',
			id: 'filters',
			restoreFocus: false,
			position: { right: '0px;' },
			panelClass: 'dialog-side-panel',
			data: {
				isPublic: this.isPublic ?? true,
				hasSelectedTenant: this.authService.selectedTenant() != 'default',
                lookup: this.lookup,
				//filterForm: this._buildFormFromLookup(this.lookup),
				referencesWithTypeItems: this.referenceFilters ?? [],
			}
		});

		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (!result) return;

			this.updateDataFn(result);
		});
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

	public setDashboardTourPlanText(): void {
		const planText = this.language.instant('PLAN-LISTING.TEXT-INFO') + '\n\n' +
			this.language.instant('PLAN-LISTING.TEXT-INFO-QUESTION') + ' ' +
			this.language.instant('PLAN-LISTING.LINK-ZENODO') + ' ' +
			this.language.instant('PLAN-LISTING.GET-IDEA');
		this.dashboardTour.steps[0].title = planText;
	}

	public setDashboardTourDescriptionText(): void {
		const descriptionText = this.language.instant('DESCRIPTION-LISTING.TEXT-INFO') + '\n\n' +
			this.language.instant('DESCRIPTION-LISTING.TEXT-INFO-QUESTION') + ' ' +
			this.language.instant('DESCRIPTION-LISTING.GET-IDEA');
		this.dashboardTour.steps[1].title = descriptionText;
	}

	public restartTour(): void {
		this.setDashboardTourPlanText();
		this.setDashboardTourDescriptionText();
		this.guidedTourService.startTour(this.dashboardTour);
	}

	hasLikeFilters(): boolean {
		return this.lookup.like !== undefined && this.lookup.like !== null;
	}

	toggleSortDirection(): void {
        const orderBy = this.lookup?.order?.items[0]?.substring(1) ?? nameof<Plan>(x => x.updatedAt);
		const sortDirection = this.isAscending ? '+' : '-';
		this.lookup.order.items = [`${sortDirection}${orderBy}`];
        this.filterChanged(this.lookup);
	}

	private _loadUserTenants(): Observable<Array<Tenant>> {
		const params = new BaseHttpParams();
		params.interceptorContext = {
			excludedInterceptors: [InterceptorType.TenantHeaderInterceptor]
		};
		return this.principalService.myTenants({ params: params });
	}

	private _parseLookupFromParams(params: Params): PlanLookup {
		let lookup: PlanLookup = JSON.parse(params['lookup']);

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

		this.lookup.statuses = filters?.status != null ? [filters.status] : null;

		// Tenants
		let viewOnlyTenant = filters?.viewOnlyTenant ?? false;
		if (viewOnlyTenant) {
			let tenant = this.tenants?.find(t => t.code && t.code?.toString() == this.authService.selectedTenant());
			if (tenant && tenant?.code) {
				this.lookup.tenantSubQuery = PlanFilterService.initializeTenantLookup();
				this.lookup.tenantSubQuery.codes = [tenant.code]
			}
			else this.lookup.tenantSubQuery = null;
		} else this.lookup.tenantSubQuery = null;
		
		// Description Templates
		let descriptionTemplates = filters?.descriptionTemplates ?? null;
		if (descriptionTemplates && descriptionTemplates?.length > 0) {
			this.lookup.planDescriptionTemplateSubQuery = PlanFilterService.initializePlanDescriptionTemplateLookup();
			this.lookup.planDescriptionTemplateSubQuery.descriptionTemplateGroupIds = descriptionTemplates;
		} else this.lookup.planDescriptionTemplateSubQuery = null;

		// Blueprints

		let planBlueprints = filters?.planBlueprints ?? null;
		if (planBlueprints && planBlueprints?.length > 0) {
			this.lookup.planBlueprintSubQuery = PlanFilterService.initializePlanBlueprintLookup();
			this.lookup.planBlueprintSubQuery.ids = planBlueprints;
		} else this.lookup.planBlueprintSubQuery = null;

		// plans
		let roles = filters?.role != null ? [filters.role] : null;
		if (roles && roles?.length > 0) {
			this.lookup.planUserSubQuery = PlanFilterService.initializePlanUserLookup();
				this.lookup.planUserSubQuery.userRoles = roles;
		} else this.lookup.planUserSubQuery = null;

		let references: Guid[] = filters?.references
				?.filter((reference: ReferencesWithType) => reference.referenceTypeId != null && reference.referenceIds?.length > 0)
				?.flatMap((referencesWithType: ReferencesWithType) => referencesWithType.referenceIds) as Guid[];

		if (references && references?.length > 0) {
			this.lookup.planReferenceSubQuery = PlanFilterService.initializePlanReferenceLookup();
			this.lookup.planReferenceSubQuery.referenceIds = references;
		} else this.lookup.planReferenceSubQuery = null;


		return this.lookup;
	}

	_patchReferenceFilters(filters: PlanListingFilters): ReferencesWithType[] {
		return filters?.references?.filter(( referencesWithType: ReferencesWithType ) => referencesWithType != null && referencesWithType.referenceIds?.length > 0) ?? null;
	}



	private _countFilters(lookup: PlanLookup): number {
		let count = 0;

		if (lookup.statuses) count += lookup.statuses.length;
		if (lookup.tenantSubQuery) count += 1;
		if (lookup.planDescriptionTemplateSubQuery) count += lookup.planDescriptionTemplateSubQuery.descriptionTemplateGroupIds?.length;
		if (lookup.planBlueprintSubQuery) count += lookup.planBlueprintSubQuery.ids?.length;
		if (lookup.planUserSubQuery) count += lookup.planUserSubQuery.userRoles?.length;
		if (lookup.planReferenceSubQuery) count += lookup.planReferenceSubQuery.referenceIds?.length;

		return count;
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
			nameof<Plan>(x => x.status),
			nameof<Plan>(x => x.accessType),
			nameof<Plan>(x => x.version),
			nameof<Plan>(x => x.versionStatus),
			nameof<Plan>(x => x.groupId),
			nameof<Plan>(x => x.updatedAt),
			nameof<Plan>(x => x.belongsToCurrentTenant),
			nameof<Plan>(x => x.finalizedAt),
			nameof<Plan>(x => x.hash),
			nameof<Plan>(x => x.tenantId),
			[nameof<Plan>(x => x.authorizationFlags), AppPermission.CreateNewVersionPlan].join('.'),
			[nameof<Plan>(x => x.authorizationFlags), AppPermission.DeletePlan].join('.'),
			[nameof<Plan>(x => x.authorizationFlags), AppPermission.ClonePlan].join('.'),
			[nameof<Plan>(x => x.authorizationFlags), AppPermission.FinalizePlan].join('.'),
			[nameof<Plan>(x => x.authorizationFlags), AppPermission.ExportPlan].join('.'),
			[nameof<Plan>(x => x.authorizationFlags), AppPermission.InvitePlanUsers].join('.'),
			[nameof<Plan>(x => x.authorizationFlags), AppPermission.AssignPlanUsers].join('.'),
			[nameof<Plan>(x => x.authorizationFlags), AppPermission.EditPlan].join('.'),

			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.label)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.status)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.groupId)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.sectionId)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.isActive)].join('.'),

			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.label)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.label)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.hasTemplates)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.descriptionTemplateGroupId)].join('.'),

			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.id)].join('.'),
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
