import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, UntypedFormBuilder, Validators } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { RecentActivityOrder } from '@app/core/common/enum/recent-activity-order';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { BaseDescription, Description } from '@app/core/model/description/description';
import { PlanBlueprint, PlanBlueprintDefinition, PlanBlueprintDefinitionSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { Plan, PlanDescriptionTemplate, PlanUser } from '@app/core/model/plan/plan';
import { PlanReference } from '@app/core/model/plan/plan-reference';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { Reference } from '@app/core/model/reference/reference';
import { DescriptionLookup, ReferencesWithType } from '@app/core/query/description.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { DescriptionService } from '@app/core/services/description/description.service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { GuidedTour, Orientation } from '@app/library/guided-tour/guided-tour.constants';
import { GuidedTourService } from '@app/library/guided-tour/guided-tour.service';
import { StartNewPlanDialogComponent } from '@app/ui/plan/new/start-new-plan-dialogue/start-new-plan-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { PageLoadEvent, SortDirection } from '@common/modules/hybrid-listing/hybrid-listing.component';
import { TranslateService } from '@ngx-translate/core';
import { debounceTime, takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { StartNewDescriptionDialogComponent } from '../start-new-description-dialog/start-new-description-dialog.component';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { DescriptionFilterDialogComponent } from './filtering/description-filter-dialogue/description-filter-dialog.component';
import { BaseListingComponent } from '@common/base/base-listing-component';
import { QueryResult } from '@common/model/query-result';
import { Observable } from 'rxjs';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { Guid } from '@common/types/guid';
import { DescriptionFilterService } from './filtering/description-filter.service';
import { Tenant } from '@app/core/model/tenant/tenant';
import { BaseHttpParams } from '@common/http/base-http-params';
import { InterceptorType } from '@common/http/interceptors/interceptor-type';
import { PrincipalService } from '@app/core/services/http/principal.service';
import { DescriptionListingFilters } from './filtering/description-filter.component';
import { MatSelectChange } from '@angular/material/select';

@Component({
	selector: 'app-description-listing-component',
	templateUrl: 'description-listing.component.html',
	styleUrls: ['./description-listing.component.scss']
})
export class DescriptionListingComponent extends BaseListingComponent<BaseDescription, DescriptionLookup> implements OnInit {

	userSettingsKey = { key: 'DescriptionListingUserSettings' };

	@ViewChild(MatPaginator, { static: true }) _paginator: MatPaginator;
	@ViewChild(MatSort) sort: MatSort;

	titlePrefix: String;
	planId: string;
	status: Number;
	totalCount: number;
	plansearchEnabled = true;
	listingItems: any[] = [];
	isLoading: boolean = false;
	isPublic: boolean = false;
	public isVisible = true;
	protected ITEMS_PER_PAGE = 5;
	filtersCount: number;
	pageSize: number = 5;
	lookup: DescriptionLookup;
	referenceFilters: ReferencesWithType[];

	tenants: Tenant[] = [];

	scrollbar: boolean;

	RecentActivityOrder = RecentActivityOrder;
	sortBy: RecentActivityOrder;

	planText: string;
	descriptionText: string;

	get isAscending(): boolean {
		return this.lookup?.order?.items?.[0].startsWith("-");
	}
	get isDescending(): boolean {
		return this.lookup?.order?.items?.[0].startsWith("+");
	}

	get sortingTooltipText(): string {
		return this.isAscending ? this.language.instant('DESCRIPTION-LISTING.ACTIONS.TOGGLE-ΑSCENDING') : this.language.instant('DESCRIPTION-LISTING.ACTIONS.TOGGLE-DESCENDING');
	}
	get hasListingItems(): boolean {
		return this.listingItems != null && this.listingItems.length > 0;
	}
	get hasFilters(): boolean {
		return (this.lookup.like != null && this.lookup.like != '') || this.lookup.statuses  != null ||
			this.lookup.planSubQuery  != null || this.lookup.descriptionTemplateSubQuery  != null ||
			this.lookup.descriptionTagSubQuery  != null || this.lookup.descriptionReferenceSubQuery  != null ||
			this.lookup.tenantSubQuery != null;
	}

	constructor(
		protected router: Router,
		protected route: ActivatedRoute,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected queryParamsService: QueryParamsService,
		public routerUtils: RouterUtilsService,
		private descriptionService: DescriptionService,
		public dialog: MatDialog,
		private language: TranslateService,
		private authService: AuthService,
		public enumUtils: EnumUtils,
		private guidedTourService: GuidedTourService,
		private analyticsService: AnalyticsService,
		private fb: UntypedFormBuilder,
		protected formBuilder: FormBuilder,
		private principalService: PrincipalService,
	) {
		super(router, route, uiNotificationService, httpErrorHandlingService, queryParamsService);
	}

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.DescriptionListing);
		this.isPublic = this.route.snapshot.data['isPublic'] ?? false;

		if (!this.isPublic && !this.authService.currentAccountIsAuthenticated()) {
			this.router.navigateByUrl("/explore-descriptions");
		}

		this.route.queryParamMap
			.pipe(takeUntil(this._destroyed))
			.subscribe(async (params: Params) => {
				const queryParams = this.route.snapshot.queryParams;

				if (!this.lookup && queryParams['lookup']) {
					this.lookup = this._parseLookupFromParams(queryParams);
					this.referenceFilters = [{
						referenceTypeId: null,
						referenceIds: this.lookup?.descriptionReferenceSubQuery?.referenceIds ?? [],
					}];
					this.filtersCount = this._countFilters(this.lookup);
				}
				else if (!this.lookup) this.lookup = this.initializeLookup();

				this.sortBy = this._getRecentActivityOrder(this.lookup.order.items[0]?.substring(1));
				this.onPageLoad({ offset: this.lookup.page.offset / this.lookup.page.size } as PageLoadEvent);
			});

		if (!this.isPublic){
			this._loadUserTenants().pipe(takeUntil(this._destroyed)).subscribe( tenants => {
				this.tenants = tenants;
			});
		}
	}

	ngAfterContentChecked(): void {
		this.scrollbar = this.hasScrollbar();
	}

	protected loadListing(): Observable<QueryResult<BaseDescription>> {
		this.isLoading = true;
		if (this.isPublic) {
			return this.descriptionService.publicQuery(this.lookup).pipe(takeUntil(this._destroyed))
				.pipe(tap(result => {
					this.isLoading = false;
					if (!result) { return []; }
					this.totalCount = result.count;
					if (this.lookup?.page?.offset === 0) this.listingItems = [];
					this.listingItems.push(...result.items);
				}));
		} else {
			return this.descriptionService.query(this.lookup).pipe(takeUntil(this._destroyed))
				.pipe(tap(result => {
					this.isLoading = false;
					if (!result) { return []; }
					this.totalCount = result.count;
					if (this.lookup?.page?.offset === 0) this.listingItems = [];
					this.listingItems.push(...result.items);
				}));
		}
	}

	protected initializeLookup(): DescriptionLookup {
		const lookup = new DescriptionLookup();
		lookup.metadata = { countAll: true };
		lookup.page = { offset: 0, size: this.ITEMS_PER_PAGE };
		lookup.isActive = [IsActive.Active];

		let recentActivityOrder = this.isPublic ? this.toDescSortField(nameof<Description>(x => x.finalizedAt)) : this.toDescSortField(nameof<Description>(x => x.updatedAt));
		lookup.order = { items: [recentActivityOrder] };

		this.updateOrderUiFields(lookup.order);

		lookup.project = {
			fields: this._lookupFields
		};

		return lookup;
	}

	protected setupColumns() { }

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

	controlModified(): void {
		this.lookup.page = { size: this.pageSize, offset: 0 };
		this.filterChanged(this.lookup, true);
	}

	loadMore() {
		this.lookup.page = { size: this.pageSize, offset: this.lookup.page.offset + this.pageSize };

		this.filterChanged(this.lookup, true);
	}

	orderByChanged($event: MatSelectChange) {
		const value = $event.value as RecentActivityOrder;
		const directionPrefix = this.isAscending ? '-' : '+';

		switch (value) {
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
		};

		this.filterChanged(this.lookup);
	}

	openFiltersDialog(): void {
		const dialogRef: MatDialogRef<DescriptionFilterDialogComponent, DescriptionListingFilters> = this.dialog.open(DescriptionFilterDialogComponent, {
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
				referencesWithTypeItems: this.referenceFilters ?? [],
			}
		});

		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (!result) return;

			this.updateDataFn(result);
		});
	}

	updateDataFn(filters: DescriptionListingFilters): void {
		this.referenceFilters = this._patchReferenceFilters(filters);
		this._patchLookupFromFilters(filters);
		this.filtersCount = this._countFilters(this.lookup);
		this.isLoading = false;
		this.filterChanged(this.lookup)
	}

	hasScrollbar(): boolean {
		return document.getElementById("main-page").scrollHeight > document.documentElement.clientHeight
	}

	public restartTour(): void {
		this.setDashboardTourPlanText();
		this.setDashboardTourDescriptionText();
		this.guidedTourService.startTour(this.dashboardTour);
	}

	public setDashboardTourPlanText(): void {
		this.planText = this.language.instant('PLAN-LISTING.TEXT-INFO') + '\n\n' +
			this.language.instant('PLAN-LISTING.TEXT-INFO-QUESTION') + ' ' +
			this.language.instant('PLAN-LISTING.LINK-ZENODO') + ' ' +
			this.language.instant('PLAN-LISTING.GET-IDEA');
		this.dashboardTour.steps[0].title = this.planText;
	}

	public setDashboardTourDescriptionText(): void {
		this.descriptionText = this.language.instant('DESCRIPTION-LISTING.TEXT-INFO') + '\n\n' +
			this.language.instant('DESCRIPTION-LISTING.TEXT-INFO-QUESTION') + ' ' +
			this.language.instant('DESCRIPTION-LISTING.GET-IDEA');
		this.dashboardTour.steps[1].title = this.descriptionText;
	}

	openNewPlanDialog() {
		if (this.dialog.openDialogs.length > 0) {
			this.dialog.closeAll();
		}
		else {
			const dialogRef = this.dialog.open(StartNewPlanDialogComponent, {
				disableClose: false,
				data: {
					isDialog: true
				}
			});
		}
	}

	addNewDescription() {
		const formGroup = this.fb.group({
			planId: this.fb.control(null, Validators.required),
		})

		const dialogRef = this.dialog.open(StartNewDescriptionDialogComponent, {
			disableClose: false,
			restoreFocus: false,
			data: {
				startNewPlan: false,
				formGroup: formGroup,
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result) {
				if (result.startNewPlan) {
					this.openNewPlanDialog();
				} else {
					this.router.navigate([this.routerUtils.generateUrl(['/plans/', 'edit/', result.formGroup.get('planId').value])]);
				}
			}
		});
	}

	hasLikeFilters(): boolean {
		return this.lookup.like !== undefined && this.lookup.like !== null;
	}

	toggleSortDirection(): void {
		const orderBy = this.lookup?.order?.items[0]?.substring(1) ?? nameof<Description>(x => x.updatedAt);
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

	private _parseLookupFromParams(params: Params): DescriptionLookup {
		let lookup: DescriptionLookup = JSON.parse(params['lookup']);

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
			case nameof<Description>(x => x.updatedAt): return RecentActivityOrder.UpdatedAt;
			case nameof<Description>(x => x.label): return RecentActivityOrder.Label;
			case nameof<Description>(x => x.finalizedAt): return RecentActivityOrder.PublishedAt;
			case nameof<Description>(x => x.status): return RecentActivityOrder.Status;
		}
	}

	_patchLookupFromFilters(filters: DescriptionListingFilters): DescriptionLookup {
		this.lookup.statuses = filters?.status != null ? [filters?.status] : null;

		// Tenants
		let viewOnlyTenant = filters?.viewOnlyTenant ?? false;
		if (viewOnlyTenant) {
				let tenant = this.tenants?.find(t => t.code && t.code?.toString() == this.authService.selectedTenant());
				if (tenant && tenant?.code) {
					this.lookup.tenantSubQuery = DescriptionFilterService.initializeTenantLookup();
					this.lookup.tenantSubQuery.codes = [tenant.code]
				} 
				else this.lookup.tenantSubQuery = null;
		} else this.lookup.tenantSubQuery = null;
		
		// Description Templates
		let descriptionTemplates = filters?.descriptionTemplates ?? null;
		if (descriptionTemplates && descriptionTemplates?.length > 0) {
			this.lookup.descriptionTemplateSubQuery = DescriptionFilterService.initializeDescriptionTemplateLookup();
			this.lookup.descriptionTemplateSubQuery.ids = descriptionTemplates;
		} else this.lookup.descriptionTemplateSubQuery = null;

		// Plans
		if (filters?.role || filters?.associatedPlanIds?.length) {
			this.lookup.planSubQuery = DescriptionFilterService.initializePlanLookup();

			if (filters?.associatedPlanIds?.length) this.lookup.planSubQuery.ids = filters?.associatedPlanIds ?? null;

			if (filters?.role) {
				this.lookup.planSubQuery.planUserSubQuery = DescriptionFilterService.initializePlanUserLookup();
				this.lookup.planSubQuery.planUserSubQuery.userRoles = [filters?.role];
			}
		} else this.lookup.planSubQuery = null;

		// Tags
		let tags = filters?.tags ?? null;
		if (tags && tags?.length > 0) {
			this.lookup.descriptionTagSubQuery = DescriptionFilterService.initializeTagLookup();
			this.lookup.descriptionTagSubQuery.tagIds = tags;
		} else this.lookup.descriptionTagSubQuery = null;

		// References
		let references: Guid[] = filters?.references
				?.filter((reference: ReferencesWithType) => reference.referenceTypeId != null && reference.referenceIds?.length > 0)
				?.flatMap((referencesWithType: ReferencesWithType) => referencesWithType.referenceIds) as Guid[];

		if (references && references?.length > 0) {
			this.lookup.descriptionReferenceSubQuery = DescriptionFilterService.initializeReferenceLookup();
			this.lookup.descriptionReferenceSubQuery.referenceIds = references;
		} else this.lookup.descriptionReferenceSubQuery = null;

		return this.lookup;
	}

	_patchReferenceFilters(filters: DescriptionListingFilters): ReferencesWithType[] {
		return filters?.references?.filter(( referencesWithType: ReferencesWithType ) => referencesWithType.referenceTypeId != null && referencesWithType.referenceIds?.length > 0) ?? null;
	}

	private _countFilters(lookup: DescriptionLookup): number {
		let count = 0;

		if (lookup.statuses) count += lookup.statuses.length;
		if (lookup.tenantSubQuery) count += 1;
		if (lookup.descriptionTemplateSubQuery) count += lookup.descriptionTemplateSubQuery.ids?.length;
		if (lookup.descriptionTagSubQuery) count += lookup.descriptionTagSubQuery.tagIds?.length;
		if (lookup.planSubQuery) {
			if (lookup.planSubQuery.ids) count += lookup.planSubQuery.ids?.length;
			if (lookup.planSubQuery.planUserSubQuery) count += lookup.planSubQuery.planUserSubQuery.userRoles?.length;
		}
		if (lookup.descriptionReferenceSubQuery) count += lookup.descriptionReferenceSubQuery.referenceIds?.length;

		return count;
	}

	private get _lookupFields(): string[] {
		return [
			nameof<Description>(x => x.id),
			nameof<Description>(x => x.tenantId),
			nameof<Description>(x => x.label),
			nameof<Description>(x => x.status),
			nameof<Description>(x => x.updatedAt),
			nameof<Description>(x => x.belongsToCurrentTenant),
			nameof<Description>(x => x.finalizedAt),

			[nameof<Description>(x => x.authorizationFlags), AppPermission.EditDescription].join('.'),
			[nameof<Description>(x => x.authorizationFlags), AppPermission.DeleteDescription].join('.'),
			[nameof<Description>(x => x.authorizationFlags), AppPermission.InvitePlanUsers].join('.'),

			[nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.id)].join('.'),
			[nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.label)].join('.'),
			[nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.groupId)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.label)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.status)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.accessType)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.finalizedAt)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.label)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.label)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.hasTemplates)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.role)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.isActive)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.label)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.reference)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.isActive)].join('.'),
			[nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.id)].join('.'),
			[nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.plan), nameof<Plan>(x => x.id)].join('.'),
			[nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.descriptionTemplateGroupId)].join('.'),
			[nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.sectionId)].join('.'),
		];
	}
}
