import { Location } from '@angular/common';
import { Component, EventEmitter, input, Input, OnInit, Output, ChangeDetectionStrategy } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DescriptionStatusEnum } from '@app/core/common/enum/description-status';
import { PlanStatusEnum } from '@app/core/common/enum/plan-status';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { RecentActivityOrder } from '@app/core/common/enum/recent-activity-order';
import { RecentActivityItem } from '@app/core/model/dashboard/recent-activity-item';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { Description } from '@app/core/model/description/description';
import { DescriptionTemplatesInSection, PlanBlueprint, PlanBlueprintDefinition, RootSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { Plan, PlanAuthorizationInformation, PlanDescriptionTemplate, PlanUser } from '@app/core/model/plan/plan';
import { PlanReference } from '@app/core/model/plan/plan-reference';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { Reference } from '@app/core/model/reference/reference';
import { RecentActivityItemLookup, SortDirection } from '@app/core/query/recent-activity-item-lookup.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { DashboardService } from '@app/core/services/dashboard/dashboard.service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Observable } from 'rxjs';
import { debounceTime, takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { ActivityListingType } from '../dashboard.component';
import { DescriptionStatus, DescriptionStatusDefinition } from '@app/core/model/description-status/description-status';
import { PlanStatus, PlanStatusDefinition } from '@app/core/model/plan-status/plan-status';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { StorageFileService } from '@app/core/services/storage-file/storage-file.service';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { Guid } from '@common/types/guid';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { toObservable } from '@angular/core/rxjs-interop';
import { User } from '@app/core/model/user/user';
import { DescriptionTemplateType } from '@app/core/model/description-template-type/description-template-type';
import { PlanStatusService } from '@app/core/services/plan/plan-status.service';
import { DescriptionStatusService } from '@app/core/services/description-status/description-status.service';
import { EntityDoi } from '@app/core/model/entity-doi/entity-doi';
import { IconConfiguration } from '@app/core/model/icon-configuration/icon-configuration';
import { DescriptionLookup, ReferencesWithType } from '@app/core/query/description.lookup';
import { PlanLookup } from '@app/core/query/plan.lookup';
import { PlanFilterService } from '@app/ui/plan/listing/filtering/services/plan-filter.service';
import { PlanListingFilters } from '@app/ui/plan/listing/filtering/plan-filter.component';
import { DescriptionFilterService } from '@app/ui/description/listing/filtering/description-filter.service';
import { DescriptionListingFilters } from '@app/ui/description/listing/filtering/description-filter.component';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatFormField, MatPrefix, MatError } from '@angular/material/form-field';
import { MatIcon } from '@angular/material/icon';
import { MatInput } from '@angular/material/input';
import { MatButtonToggleGroup, MatButtonToggle } from '@angular/material/button-toggle';
import { MatTooltip } from '@angular/material/tooltip';
import { MatSelect, MatOption } from '@angular/material/select';
import { MatBadge } from '@angular/material/badge';
import { CdkTrapFocus } from '@angular/cdk/a11y';
import { DescriptionFilterDialogComponent } from '../../description/listing/filtering/description-filter-dialogue/description-filter-dialog.component';
import { PlanFilterDialogComponent } from '../../plan/listing/filtering/plan-filter-dialog/plan-filter-dialog.component';
import { ActiveReferenceFiltersComponent } from '../../reference/reference-usage-filter/active-reference-filters.component';
import { PlanListingItemComponent } from '../../plan/listing/listing-item/plan-listing-item.component';
import { DescriptionListingItemComponent } from '../../description/listing/listing-item/description-listing-item.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-recent-edited-activity',
    templateUrl: './recent-edited-activity.component.html',
    styleUrls: ['./recent-edited-activity.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatButton, MatFormField, MatIcon, MatPrefix, MatInput, FormsModule, ReactiveFormsModule, MatError, MatButtonToggleGroup, MatButtonToggle, MatTooltip, MatSelect, MatOption, MatIconButton, MatBadge, CdkTrapFocus, DescriptionFilterDialogComponent, PlanFilterDialogComponent, ActiveReferenceFiltersComponent, PlanListingItemComponent, DescriptionListingItemComponent, TranslatePipe]
})
export class RecentEditedActivityComponent extends BaseComponent implements OnInit {
    compactView: boolean = false;
	lookup: RecentActivityItemLookup = new RecentActivityItemLookup();

    loading = false;
    statuses: BaseStatus[] = [];

	currentPage: number = 0;
	pageSize: number = 5;
	listingItems: RecentActivityItem[]= [];
    ActivityListingType = ActivityListingType;
    SortDirection = SortDirection;

    private _type: ActivityListingType = ActivityListingType.Recent;
    get type(): ActivityListingType {
        return this._type;
    }
	@Input() set type(val: ActivityListingType){
        this._type = val;
        switch(val){
            case ActivityListingType.Plans:
                this.planStatusService.query(this.planStatusService.buildLookup())
                .pipe(takeUntil(this._destroyed))
                .subscribe((res) => this.statuses = res?.items?.map(x => ({name: x.name, id: x.id})))
                return;
            case ActivityListingType.Descriptions:
                this.descriptionStatusService.query(this.descriptionStatusService.buildLookup())
                .pipe(takeUntil(this._destroyed))
                .subscribe((res) => this.statuses = res?.items?.map(x => ({name: x.name, id: x.id})))
                return;
            default:
                this.statuses = []; 
                return;
        }
    }
	@Input() orderedPlanPreferencesList:ReferenceType[] = [];
	@Input() orderedDescriptionPreferencesList:ReferenceType[] = [];
	/** Fires after every successful load; the dashboard starts its first-visit tour on the first one, once the list is on screen. */
	@Output() loaded = new EventEmitter<void>();
    
    isActive = input<boolean>();

    get onlyDrafts(): boolean {
        return this.type === ActivityListingType.Drafts;
    }

    get includeDescriptions(): boolean {
        const activityListingTypes = [ActivityListingType.Recent, ActivityListingType.Drafts, ActivityListingType.Descriptions]
        return activityListingTypes.includes(this.type);
    }

    get includePlans(): boolean {
        const activityListingTypes = [ActivityListingType.Recent, ActivityListingType.Drafts, ActivityListingType.Plans]
        return activityListingTypes.includes(this.type);
    }
	
    get isDefault(): boolean {
        return this.type === ActivityListingType.Recent;
    }

	get onlyPlans(): boolean {
		return this.includePlans && !this.includeDescriptions;
	} 
	
	get onlyDescriptions(): boolean {
		return !this.includePlans && this.includeDescriptions;
	} 
	
	/** True while a search term, a status chip or the filter panel narrows the list. */
	get hasActiveFilters(): boolean {
		return !!this.formGroup.controls.like.value?.trim() || !!this.formGroup.controls.statuses.value?.length || !!this.filtersCount;
	}

	/** The filter panels are per entity, so only the Plans and Assets tabs offer them. */
	get supportsFilters(): boolean {
		return this.onlyPlans || this.onlyDescriptions;
	}

	get featuredReferenceTypes(): ReferenceType[] {
		return (this.onlyDescriptions ? this.orderedDescriptionPreferencesList : this.orderedPlanPreferencesList) ?? [];
	}

	/** What the My Plans / My Assets filter panel has set, kept as their lookups (only the filter parts are used). */
	planFilterLookup: PlanLookup = RecentEditedActivityComponent.emptyPlanFilter();
	descriptionFilterLookup: DescriptionLookup = RecentEditedActivityComponent.emptyDescriptionFilter();
	filterReferences: ReferencesWithType[] = [];
	filtersCount = 0;

	get filterReferenceIds(): Guid[] {
		return (this.onlyDescriptions ? this.descriptionFilterLookup?.descriptionReferenceSubQuery?.referenceIds : this.planFilterLookup?.planReferenceSubQuery?.referenceIds) ?? null;
	}

	private static emptyPlanFilter(): PlanLookup {
		const lookup = new PlanLookup();
		lookup.isActive = [IsActive.Active];
		return lookup;
	}

	private static emptyDescriptionFilter(): DescriptionLookup {
		const lookup = new DescriptionLookup();
		lookup.isActive = [IsActive.Active];
		return lookup;
	}

	filtersOpen = false;

	pageLessSize= this.pageSize;

	public formGroup = new FormGroup({
		like: new FormControl<string>(null),
		order: new FormControl<RecentActivityOrder>(null),
        direction: new FormControl<SortDirection>(SortDirection.DESC),
        statuses: new FormControl<Guid[]>([])
	});
	publicMode = false;

	RecentActivityOrder = RecentActivityOrder;

	totalCount: number;
	offsetLess: number = 0;

    tabChange: Observable<boolean>;
	
	constructor(
		private route: ActivatedRoute,
		private router: Router,
		public enumUtils: EnumUtils,
		private authentication: AuthService,
		private dashboardService: DashboardService,
		private location: Location,
		private analyticsService: AnalyticsService,
		private httpErrorHandlingService: HttpErrorHandlingService,
        private storageFileService: StorageFileService,
        private sanitizer: DomSanitizer,
        private filterService: FilterService,
        private planStatusService: PlanStatusService,
        private descriptionStatusService: DescriptionStatusService
	) {
		super();
        this.tabChange = toObservable(this.isActive);
	}

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.RecentEditedActivity);
		this.route.queryParams.pipe(takeUntil(this._destroyed)).subscribe(params => {
			if ((params['type'] == null && this.isDefault) || params['type'] === this.type) {
                const compact = params['compact'];
                if(compact){
                    this.compactView = true; 
                }

				const page = (params['page'] === undefined) ? 0 : + params['page'];
				this.currentPage = (page <= 0) ? 0 : page;


				let order = params['order'];
                if (order == null || (order != this.RecentActivityOrder.UpdatedAt && order != this.RecentActivityOrder.Label && order != this.RecentActivityOrder.Status)) {
                    order = this.RecentActivityOrder.UpdatedAt;
                } else {
                    order = Number(order);
                }
				this.formGroup.controls.order.setValue(order);
                this.formGroup.controls.order.updateValueAndValidity();

                let direction = params['direction'];
                if (direction == null || direction != SortDirection.ASC) {
                    direction = SortDirection.DESC;
                } else {
                    direction = SortDirection.ASC;
                }
				this.formGroup.controls.direction.setValue(direction);
                this.formGroup.controls.direction.updateValueAndValidity();

				const keyword = (params['keyword'] === undefined || params['keyword'].length <= 0) ? "" : params['keyword'];
				this.formGroup.get("like").setValue(keyword);

                const filters = params['filters'];
                if (filters && this.supportsFilters) this.restoreFilters(filters);

                const statuses = params['statuses'];
                if(statuses != null && statuses.length){
                    try {
                        this.formGroup.controls.statuses.setValue(statuses.split(','));
                    } catch {
                        this.formGroup.controls.statuses.setValue([]);
                    }
                }
			}

            if (this.formGroup.controls.order.value == null){
                this.formGroup.controls.order.setValue(this.RecentActivityOrder.UpdatedAt);
            }
            if (this.formGroup.controls.direction.value == null){
                this.formGroup.controls.direction.setValue(SortDirection.DESC);
            }

            this.refresh();

            this.formGroup.valueChanges
            .pipe(takeUntil(this._destroyed), debounceTime(500))
            .subscribe(x => {
                this.refresh(true);
            });
            
            this.tabChange.pipe(takeUntil(this._destroyed))
            .subscribe((isActive) => {
                if(isActive){
                    this.updateUrl();
                }
            })
		});
	}

	updateUrl() {
        if(!this.isActive()){ return; }
		const parametersArray: string[] = [
			...( !this.isDefault && this.isActive() ? ["type=" + this.type] : []),
			...(this.currentPage > 1 ? ["page=" + this.currentPage] : []),
			...(this.formGroup.controls.like.value ? ["keyword=" + this.formGroup.controls.like.value] : []),
            ...(this.formGroup.controls.order.value ? ["order=" + this.formGroup.controls.order.value] : []),
            ...(this.formGroup.controls.direction.value == SortDirection.ASC ? ["direction=" + this.formGroup.controls.direction.value] : []),
            ...(this.formGroup.controls.statuses.value?.length ? ["statuses=" + this.formGroup.controls.statuses.value] : []),
            ...(this.supportsFilters && this.filtersCount ? ["filters=" + encodeURIComponent(JSON.stringify(this.onlyDescriptions ? this.descriptionFilterLookup : this.planFilterLookup))] : []),
            ...(this.compactView ? ["compact=true"] : [])
		]

		let parameters = "";
		
		if (parametersArray.length > 0){
			parameters = "?";

			parameters += parametersArray.join('&');
		} 

		this.location.go(this.router.url.split('?')[0] + parameters);
	}

	public isAuthenticated(): boolean {
		return this.authentication.currentAccountIsAuthenticated();
	}

	refresh(refreshUrl: boolean = false): void {
		this.lookup.onlyDraft = this.onlyDrafts ? this.onlyDrafts : null;
		this.lookup.onlyPlan = this.onlyPlans ? this.onlyPlans : null;
		this.lookup.onlyDescription = this.onlyDescriptions ? this.onlyDescriptions : null;
        this.lookup.statusIds = this.formGroup.controls.statuses.value?.length ? this.formGroup.controls.statuses.value : null;
        this.lookup.planFilter = this.onlyPlans && this.filtersCount ? this.planFilterLookup : null;
        this.lookup.descriptionFilter = this.onlyDescriptions && this.filtersCount ? this.descriptionFilterLookup : null;
		this.lookup.order = {
            orderField: this.formGroup.controls.order.value ?? this.RecentActivityOrder.UpdatedAt,
            sortDirection: this.formGroup.controls.direction.value ?? SortDirection.DESC
        }
		this.lookup.like = this.formGroup.controls.like.value ? this.filterService.transformLike(this.formGroup.controls.like.value) : null;
		this.lookup.project = {
			fields : [ 
				...(this.includePlans ? this._getPlanLookup() : []), 
				...(this.includeDescriptions ? this._getDescriptionLookup() : [])
			]
		};
		
		this.lookup.page = { size: this.currentPage == 0 ? this.pageSize : this.pageSize*this.currentPage, offset: 0 };
        this.loadItems(refreshUrl);
	}

    /** The filter panel's Apply: same mapping as My Plans / My Assets, applied to this tab's feed. */
    onFiltersChanged(filters: PlanListingFilters | DescriptionListingFilters) {
        const context = { principalId: this.authentication.userId() };
        if (this.onlyDescriptions) DescriptionFilterService.patchLookup(this.descriptionFilterLookup, filters as DescriptionListingFilters, context);
        else PlanFilterService.patchLookup(this.planFilterLookup, filters as PlanListingFilters, context);
        this.filterReferences = (filters?.references ?? []).filter(x => x?.referenceIds?.length);
        this.filtersOpen = false;
        this.filtersApplied();
    }

    /** Closes the filter drawer and puts focus back on the button that opened it. */
    closeFilters(button?: { focus(): void }) {
        if (!this.filtersOpen) return;
        this.filtersOpen = false;
        button?.focus();
    }

    removeReferenceFilter(referenceId: Guid) {
        this.setFilterReferenceIds((this.filterReferenceIds ?? []).filter(x => x?.toString() !== referenceId?.toString()));
    }

    setFilterReferenceIds(referenceIds: Guid[]) {
        if (this.onlyDescriptions) DescriptionFilterService.setReferenceIds(this.descriptionFilterLookup, referenceIds);
        else PlanFilterService.setReferenceIds(this.planFilterLookup, referenceIds);
        this.filterReferences = referenceIds.length ? [{ referenceTypeId: null, referenceIds }] : [];
        this.filtersApplied();
    }

    private filtersApplied() {
        this.filtersCount = this.onlyDescriptions ? DescriptionFilterService.countFilters(this.descriptionFilterLookup) : PlanFilterService.countFilters(this.planFilterLookup);
        this.currentPage = 0;
        this.refresh(true);
    }

    private restoreFilters(serialized: string) {
        try {
            const parsed = JSON.parse(serialized);
            if (this.onlyDescriptions) this.descriptionFilterLookup = Object.assign(RecentEditedActivityComponent.emptyDescriptionFilter(), parsed);
            else this.planFilterLookup = Object.assign(RecentEditedActivityComponent.emptyPlanFilter(), parsed);
            const referenceIds = this.filterReferenceIds ?? [];
            this.filterReferences = referenceIds.length ? [{ referenceTypeId: null, referenceIds }] : [];
            this.filtersCount = this.onlyDescriptions ? DescriptionFilterService.countFilters(this.descriptionFilterLookup) : PlanFilterService.countFilters(this.planFilterLookup);
        } catch {
            this.filtersCount = 0;
        }
    }

    statusSelected(statusId: Guid){
        const form = this.formGroup.controls.statuses;
        if(form.value.includes(statusId)){
            form.setValue(form.value.filter(x => x != statusId));
        } else {
            form.setValue([...form.value, statusId]);
        }
    }

	loadMore() {
		this.lookup.page = { size: this.pageSize, offset: this.pageSize*this.currentPage };
		this.loadItems(true);
	}


	private loadItems(refreshUrl: boolean = false){
        this.loading = true;
		this.dashboardService
		.getMyRecentActivityItems(this.lookup)
		.pipe(takeUntil(this._destroyed))
		.subscribe({
            next: (response) => {
                if (response == null) return;

                this.totalCount = response.count ?? 0;
                this.loaded.emit();
                
                if (this.lookup.page.offset == 0){
                    this.listingItems = [];
                    this.storageFileMap = new Map([])
                }
    
                response.items?.forEach(item => {
                    if (item.plan){
                        if (item.plan.descriptions) {
                            if (item.plan.status.internalStatus == PlanStatusEnum.Finalized) {
                                item.plan.descriptions = item.plan.descriptions.filter(x => x.isActive === IsActive.Active && x.status?.internalStatus === DescriptionStatusEnum.Finalized);
                            } else {
                                item.plan.descriptions = item.plan.descriptions.filter(x => x.isActive === IsActive.Active && x.status?.internalStatus != DescriptionStatusEnum.Canceled);
                            }
                        }
                        item.plan.planUsers = item.plan.planUsers.filter(x=> x.isActive === IsActive.Active);
                        this.listingItems.push(item);
                    }
                    if (item.description){
                        if (item.description?.status?.internalStatus != DescriptionStatusEnum.Canceled) this.listingItems.push(item);
                    }
                })
    
                if (this.lookup.page.offset != 0 && response.items?.length > 0 && this.listingItems.length >= this.currentPage*this.pageSize) this._setPage(this.currentPage+1);
                else this._resetPagination();
                if(refreshUrl){
                    this.updateUrl();
                }
                this.loading = false;

                this.downloadStorageFiles(this.listingItems.map(
                    (x) => x.description ? x.description?.status?.definition?.iconConfiguration?.storageFile?.id : x.plan?.status?.definition?.iconConfiguration?.storageFile?.id
                ).filter((x) => x));
            },
            error: (error) => {this.loading = false; this.httpErrorHandlingService.handleBackedRequestError(error)}
        });
	}
	
	private _resetPagination(): void {
		if (this.listingItems.length == 0) this.currentPage = 0;
		else this.currentPage = Math.ceil(this.listingItems.length/this.pageSize);
	}

	private _setPage(page: number) {
		this.currentPage = page;
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
                error: error => this.httpErrorHandlingService.handleBackedRequestError(error)
            });
        })
    }
	
	private _getPlanLookup(): string[] {
		return [
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.id)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.label)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.description)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.id)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.name)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.internalStatus)].join('.'),			
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.availableActions)].join('.'),
            [nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.statusColor)].join('.'),	
            [nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.matIconName)].join('.'),			
            [nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),				
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.accessType)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.version)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.versionStatus)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.groupId)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.updatedAt)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.createdAt)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.isActive)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.hash)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.CreateNewVersionPlan].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.DeletePlan].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.ClonePlan].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.FinalizePlan].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.ExportPlan].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.InvitePlanUsers].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.AssignPlanUsers].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.EditPlan].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.id)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.label)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.id)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.name)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.internalStatus)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.isActive)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.id)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.label)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.hasTemplates)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.id)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.title)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.groupId)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.id)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user), nameof<User>(x => x.id)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user), nameof<User>(x => x.name)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.role)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.isActive)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.id)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.id)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.label)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.id)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.isActive)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.sectionId)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.descriptionTemplateGroupId)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.isActive)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.groupId)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.id)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.label)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.sectionId)].join('.'),
            [nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.entityDois), nameof<EntityDoi>(x => x.id)].join('.'),
            [nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.entityDois), nameof<EntityDoi>(x => x.doi)].join('.'),
            [nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.entityDois), nameof<EntityDoi>(x => x.repositoryId)].join('.'),
		];
	}

	private _getDescriptionLookup(): string[] {
		return [
			[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.id)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.label)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.id)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.name)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.internalStatus)].join('.'),	
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.availableActions)].join('.'),
                [nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.statusColor)].join('.'),	
                [nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.matIconName)].join('.'),			
                [nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),			
                [nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.id)].join('.'),			
                [nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.label)].join('.'),			
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.updatedAt)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.createdAt)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.isActive)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.authorizationFlags), AppPermission.EditDescription].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.authorizationFlags), AppPermission.DeleteDescription].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.authorizationFlags), AppPermission.InvitePlanUsers].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.authorizationFlags), AppPermission.CloneDescription].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.id)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.label)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.groupId)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>(x => x.id)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>(x => x.name)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.id)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.label)].join('.'),
                [nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.id)].join('.'),
			    [nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user), nameof<User>(x => x.id)].join('.'),
			    [nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user), nameof<User>(x => x.name)].join('.'),
			    [nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.role)].join('.'),
			    [nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.isActive)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.accessType)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.id)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.label)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.id)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.title)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.hasTemplates)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.sections)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.id)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.id)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.role)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.isActive)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.id)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.id)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.label)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.id)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.reference)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.isActive)].join('.'),
		]
	}
}

interface BaseStatus {
    name: string;
    id: Guid;
}