import { Component, OnInit, ViewChild, AfterContentChecked, inject, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, UntypedFormBuilder, Validators, FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { ActivatedRoute, Params, Router, RouterLink } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { RecentActivityOrder } from '@app/core/common/enum/recent-activity-order';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { BaseDescription, Description, DescriptionReference } from '@app/core/model/description/description';
import { PlanBlueprint, PlanBlueprintDefinition, RootSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { Plan, PlanDescriptionTemplate, PlanUser } from '@app/core/model/plan/plan';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { Reference } from '@app/core/model/reference/reference';
import { DescriptionLookup, ReferencesWithType } from '@app/core/query/description.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { DescriptionService } from '@app/core/services/description/description.service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { TourStateService } from '@app/library/guided-tour/tour-state.service';
import { descriptionListingPageTour } from './description-listing.tour';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { PageLoadEvent } from '@common/modules/admin-listing/listing.types';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { StartNewDescriptionDialogComponent } from '../start-new-description-dialog/start-new-description-dialog.component';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
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
import { MatSelectChange, MatSelect, MatOption } from '@angular/material/select';
import { DescriptionStatus, DescriptionStatusDefinition } from '@app/core/model/description-status/description-status';
import { PlanStatus } from '@app/core/model/plan-status/plan-status';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import {TenantConfigurationType} from "@app/core/common/enum/tenant-configuration-type";
import {ViewPreferencesEditorResolver} from "@app/ui/admin/tenant-configuration/models/view-preferences/view-preferences-editor.resolver";
import {TenantConfigurationService} from "@app/core/services/tenant-configuration/tenant-configuration.service";
import { DescriptionTemplateType } from '@app/core/model/description-template-type/description-template-type';
import { IconConfiguration } from '@app/core/model/icon-configuration/icon-configuration';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { StorageFileService } from '@app/core/services/storage-file/storage-file.service';
import { MatButton, MatIconButton } from '@angular/material/button';
import { TextFilterComponent } from '../../../../common/modules/text-filter/text-filter.component';
import { MatButtonToggleGroup, MatButtonToggle } from '@angular/material/button-toggle';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIcon } from '@angular/material/icon';
import { MatFormField } from '@angular/material/form-field';
import { ReactiveAsteriskDirective } from '../../../../common/forms/reactive-asterisk-directive';
import { MatBadge } from '@angular/material/badge';
import { ActiveReferenceFiltersComponent } from '../../reference/reference-usage-filter/active-reference-filters.component';
import { DescriptionListingItemComponent } from './listing-item/description-listing-item.component';
import { DescriptionFilterDialogComponent } from './filtering/description-filter-dialogue/description-filter-dialog.component';

@Component({
    selector: 'app-description-listing-component',
    templateUrl: 'description-listing.component.html',
    styleUrls: ['./description-listing.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [RouterLink, MatButton, TextFilterComponent, MatButtonToggleGroup, MatButtonToggle, MatTooltip, MatIcon, MatFormField, MatSelect, ReactiveAsteriskDirective, FormsModule, MatOption, MatIconButton, MatBadge, ActiveReferenceFiltersComponent, DescriptionListingItemComponent, DescriptionFilterDialogComponent, TranslatePipe]
})
export class DescriptionListingComponent extends BaseListingComponent<BaseDescription, DescriptionLookup> implements OnInit, AfterContentChecked {

	userSettingsKey = { key: 'DescriptionListingUserSettings' };

	@ViewChild(MatPaginator, { static: true }) _paginator: MatPaginator;
	@ViewChild(MatSort) sort: MatSort;

	titlePrefix: string;
	planId: string;
	status: number;
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
	/** Reference type whose filter section opens on arrival (`?referenceType=<id>`). */
	expandReferenceTypeId: Guid = null;

	tenants: Tenant[] = [];

    compactView: boolean = false;

	scrollbar: boolean;

	RecentActivityOrder = RecentActivityOrder;
	sortBy: RecentActivityOrder;

	orderedDescriptionPreferencesList:ReferenceType[];

	get isDescending(): boolean {
		return this.lookup?.order?.items?.[0].startsWith("-");
	}


	get sortingTooltipText(): string {
		return this.isDescending ? this.language.instant('DESCRIPTION-LISTING.ACTIONS.TOGGLE-ASCENDING') : this.language.instant('DESCRIPTION-LISTING.ACTIONS.TOGGLE-DESCENDING');
	}
	get hasListingItems(): boolean {
		return this.listingItems != null && this.listingItems.length > 0;
	}
	get hasFilters(): boolean {
		return (this.lookup.like != null && this.lookup.like != '') || this.lookup.statusIds  != null ||
			this.lookup.planSubQuery  != null || this.lookup.descriptionTemplateSubQuery  != null ||
			this.lookup.descriptionTagSubQuery  != null || this.lookup.descriptionReferenceSubQuery  != null ||
			this.lookup.tenantSubQuery != null || this.lookup.isActive != null;
	}

	constructor(
		protected router: Router,
		protected route: ActivatedRoute,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected queryParamsService: QueryParamsService,
		protected authService: AuthService,
		public routerUtils: RouterUtilsService,
		private descriptionService: DescriptionService,
		public dialog: MatDialog,
		private language: TranslateService,
		public enumUtils: EnumUtils,
		private analyticsService: AnalyticsService,
		private fb: UntypedFormBuilder,
		protected formBuilder: FormBuilder,
		private principalService: PrincipalService,
		private tenantConfigurationService: TenantConfigurationService,
        private storageFileService: StorageFileService,
        private sanitizer: DomSanitizer
	) {
		super(router, route, uiNotificationService, httpErrorHandlingService, queryParamsService);
	}

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.DescriptionListing);
		this.isPublic = this.route.snapshot.data['isPublic'] ?? false;

		if (!this.isPublic && !this.authService.currentAccountIsAuthenticated()) {
			this.router.navigateByUrl("/explore-assets");
		}
		if (!this.isPublic) {
			// "What's new" tour for the 2.0.0 listing; TourStateService waits for the first card to render.
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
						referenceIds: this.lookup?.descriptionReferenceSubQuery?.referenceIds ?? [],
					}];
					this.filtersCount = this._countFilters(this.lookup);
				}
				else if (!this.lookup) this.lookup = this.initializeLookup();

				const referenceType = queryParams['referenceType'];
				if (referenceType && !this.isPublic && !this.expandReferenceTypeId) {
					this.expandReferenceTypeId = Guid.parse(referenceType);
					this.filtersDialogOpen = true;
				}

				this.sortBy = this._getRecentActivityOrder(this.lookup.order.items[0]?.substring(1));
				this.onPageLoad({ offset: this.lookup.page.offset / this.lookup.page.size } as PageLoadEvent);
			});
		if (this.authService.isLoggedIn()) {
			this.tenantConfigurationService.getActiveType(TenantConfigurationType.ViewPreferences, ViewPreferencesEditorResolver.lookupFields())
				.pipe(takeUntil(this._destroyed)).subscribe(
				data => {
					this.orderedDescriptionPreferencesList = data?.viewPreferences?.descriptionPreferences?.filter(x => x.referenceType?.isActive === IsActive.Active)?.map(x => x.referenceType) || [];
				},
				error => {
					this.orderedDescriptionPreferencesList = []
				}
			);
		} else this.orderedDescriptionPreferencesList = [];
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
                    this.downloadStorageFiles(this.listingItems.map((x: Plan) => x.status?.definition?.iconConfiguration?.storageFile?.id)?.filter((x) => x) ?? [])
				}));
		} else {
			return this.descriptionService.query(this.lookup).pipe(takeUntil(this._destroyed))
				.pipe(tap(result => {
					this.isLoading = false;
					if (!result) { return []; }
					this.totalCount = result.count;
					if (this.lookup?.page?.offset === 0) this.listingItems = [];
					this.listingItems.push(...result.items);
                    this.downloadStorageFiles(this.listingItems.map((x: Plan) => x.status?.definition?.iconConfiguration?.storageFile?.id)?.filter((x) => x) ?? [])
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

	protected initializeLookup(): DescriptionLookup {
		const lookup = new DescriptionLookup();
		lookup.metadata = { countAll: true };
		lookup.page = { offset: 0, size: this.ITEMS_PER_PAGE };
		lookup.isActive = [IsActive.Active];

		const recentActivityOrder = this.isPublic ? this.toDescSortField(nameof<Description>(x => x.finalizedAt)) : this.toDescSortField(nameof<Description>(x => x.updatedAt));
		lookup.order = { items: [recentActivityOrder] };

		this.updateOrderUiFields(lookup.order);

		lookup.project = {
			fields: this._lookupFields
		};

		return lookup;
	}

	protected setupColumns() { }

	private readonly tourState = inject(TourStateService);
	private readonly pageTour = descriptionListingPageTour(inject(TranslateService));

	override ngOnDestroy(): void {
		this.tourState.unregisterPageTour(this.pageTour);
		super.ngOnDestroy();
	}

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
		const directionPrefix = this.isDescending ? '-' : '+';

		switch (value) {
			case RecentActivityOrder.Status: {
				this.lookup.order = { items: [directionPrefix + nameof<Description>(x => x.status)] };
				break;
			}
			case RecentActivityOrder.Label: {
				this.lookup.order = { items: [directionPrefix + nameof<Description>(x => x.label)] };
				break;
			}
			case RecentActivityOrder.PublishedAt: {
				this.lookup.order = { items: [directionPrefix + nameof<Description>(x => x.finalizedAt)] };
				break;
			}
			default: {
				this.lookup.order = { items: [directionPrefix + nameof<Description>(x => x.updatedAt)] };
				break;
			}
		};

		this.filterChanged(this.lookup);
	}

    filtersDialogOpen: boolean = false;
    toggleFilters(): void {
        this.filtersDialogOpen = !this.filtersDialogOpen;
        // let dialogRef: MatDialogRef<DescriptionFilterDialogComponent, DescriptionListingFilters> = this.dialog.open(DescriptionFilterDialogComponent, {
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

    onFiltersChanged(filters: DescriptionListingFilters){
        this.filtersDialogOpen = false;
        this.updateDataFn(filters);
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

	/** In-page "Take a tour" prompt; Support > Take a tour uses the same replay. */
	public restartTour(): void {
		this.tourState.startPageTour();
	}

	// openNewPlanDialog() {
	// 	if (this.dialog.openDialogs.length > 0) {
	// 		this.dialog.closeAll();
	// 	}
	// 	else {
	// 		const dialogRef = this.dialog.open(StartNewPlanDialogComponent, {
	// 			disableClose: false,
	// 			data: {
	// 				isDialog: true
	// 			},
    //             maxWidth: 'min(95vw, 33rem)'
	// 		});
	// 	}
	// }

	addNewDescription() {
		const formGroup = this.fb.group({
			planId: this.fb.control(null, Validators.required),
		})

		const dialogRef = this.dialog.open(StartNewDescriptionDialogComponent, {
			disableClose: false,
			data: {
				startNewPlan: false,
				formGroup: formGroup,
			},
            maxWidth: '600px'
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result?.formGroup) {
				this.router.navigate([this.routerUtils.generateUrl(['/plans/', 'edit/', result.formGroup.get('planId').value])]);
			}
		});
	}

	hasLikeFilters(): boolean {
		return this.lookup.like !== undefined && this.lookup.like !== null;
	}

	toggleSortDirection(): void {
		const orderBy = this.lookup?.order?.items[0]?.substring(1) ?? nameof<Description>(x => x.updatedAt);
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

	private _parseLookupFromParams(params: Params): DescriptionLookup {
		const lookup: DescriptionLookup = JSON.parse(params['lookup']);

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
		const tenant = this.tenants?.find(t => t.code && t.code?.toString() == this.authService.selectedTenant());
		return DescriptionFilterService.patchLookup(this.lookup, filters, { tenantCode: tenant?.code ?? null, principalId: this.authService.userId() });
	}

	/** Drops one reference from the active reference filter (the chip's remove button). */
	removeReferenceFilter(referenceId: Guid): void {
		const remaining = (this.lookup.descriptionReferenceSubQuery?.referenceIds ?? []).filter(x => x?.toString() !== referenceId?.toString());
		this._setReferenceFilter(remaining);
	}

	clearReferenceFilters(): void {
		this._setReferenceFilter([]);
	}

	private _setReferenceFilter(referenceIds: Guid[]): void {
		DescriptionFilterService.setReferenceIds(this.lookup, referenceIds);
		this.referenceFilters = referenceIds.length ? [{ referenceTypeId: null, referenceIds }] : null;
		this.filtersCount = this._countFilters(this.lookup);
		this.lookup.page = { size: this.pageSize, offset: 0 };
		this.filterChanged(this.lookup);
	}

	_patchReferenceFilters(filters: DescriptionListingFilters): ReferencesWithType[] {
		return filters?.references?.filter(( referencesWithType: ReferencesWithType ) => referencesWithType?.referenceIds?.length > 0) ?? null;
	}

	private _countFilters(lookup: DescriptionLookup): number {
		return DescriptionFilterService.countFilters(lookup);
	}

	private get _lookupFields(): string[] {
		return [
			nameof<Description>(x => x.id),
			nameof<Description>(x => x.tenantId),
			nameof<Description>(x => x.label),
			nameof<Description>(x => x.isActive),
			[nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.id)].join('.'),
			[nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.name)].join('.'),
			[nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.internalStatus)].join('.'),
			[nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.availableActions)].join('.'),
			[nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.statusColor)].join('.'),
            [nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.matIconName)].join('.'),
            [nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),

			nameof<Description>(x => x.createdAt),
			nameof<Description>(x => x.updatedAt),
			nameof<Description>(x => x.belongsToCurrentTenant),
			nameof<Description>(x => x.finalizedAt),

			[nameof<Description>(x => x.authorizationFlags), AppPermission.EditDescription].join('.'),
			[nameof<Description>(x => x.authorizationFlags), AppPermission.DeleteDescription].join('.'),
			[nameof<Description>(x => x.authorizationFlags), AppPermission.InvitePlanUsers].join('.'),
			[nameof<Description>(x => x.authorizationFlags), AppPermission.CloneDescription].join('.'),

			[nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.id)].join('.'),
			[nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.label)].join('.'),
			[nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.groupId)].join('.'),
            [nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>(x => x.id)].join('.'),
            [nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>(x => x.name)].join('.'),
			[nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.id)].join('.'),
			[nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.reference), nameof<Reference>(x => x.id)].join('.'),
			[nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.reference), nameof<Reference>(x => x.label)].join('.'),
			[nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.reference), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.id)].join('.'),
			[nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.reference), nameof<Reference>(x => x.reference)].join('.'),
			[nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.isActive)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.label)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.name)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.internalStatus)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.accessType)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.finalizedAt)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.label)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.title)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.sections)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.hasTemplates)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.name)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.role)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.isActive)].join('.'),
			[nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.id)].join('.'),
			[nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.plan), nameof<Plan>(x => x.id)].join('.'),
			[nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.descriptionTemplateGroupId)].join('.'),
			[nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.sectionId)].join('.'),
		];
	}
}
