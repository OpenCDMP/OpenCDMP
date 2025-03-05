import { Location } from '@angular/common';
import { Component, input, Input, OnInit, Output } from '@angular/core';
import { UntypedFormBuilder, UntypedFormControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DescriptionStatusEnum } from '@app/core/common/enum/description-status';
import { PlanStatusEnum } from '@app/core/common/enum/plan-status';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { RecentActivityOrder } from '@app/core/common/enum/recent-activity-order';
import { RecentActivityItem } from '@app/core/model/dashboard/recent-activity-item';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { Description } from '@app/core/model/description/description';
import { DescriptionTemplatesInSection, PlanBlueprint, PlanBlueprintDefinition, PlanBlueprintDefinitionSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { Plan, PlanDescriptionTemplate, PlanUser } from '@app/core/model/plan/plan';
import { PlanReference } from '@app/core/model/plan/plan-reference';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { Reference } from '@app/core/model/reference/reference';
import { RecentActivityItemLookup } from '@app/core/query/recent-activity-item-lookup.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { DashboardService } from '@app/core/services/dashboard/dashboard.service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { BehaviorSubject, Observable } from 'rxjs';
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

@Component({
    selector: 'app-recent-edited-activity',
    templateUrl: './recent-edited-activity.component.html',
    styleUrls: ['./recent-edited-activity.component.scss'],
    standalone: false
})
export class RecentEditedActivityComponent extends BaseComponent implements OnInit {
	
	lookup: RecentActivityItemLookup = new RecentActivityItemLookup();

    loading = false;

	currentPage: number = 0;
	pageSize: number = 5;
	listingItems: RecentActivityItem[]= [];

	@Input() type: ActivityListingType = ActivityListingType.Recent;
	@Input() hasPlans: boolean = false;
    
	@Output() addNewDescription: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
    
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
	
	pageLessSize= this.pageSize;

	public formGroup = new UntypedFormBuilder().group({
		like: new UntypedFormControl(),
		order: new UntypedFormControl()
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
        private filterService: FilterService
	) {
		super();
        this.tabChange = toObservable(this.isActive);
	}

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.RecentEditedActivity);
		this.route.queryParams.pipe(takeUntil(this._destroyed)).subscribe(params => {
			if ((params['type'] == null && this.isDefault) || params['type'] === this.type) {
				let page = (params['page'] === undefined) ? 0 : + params['page'];
				this.currentPage = (page <= 0) ? 0 : page;


				let order = params['order'];
                if (order == null || (order != this.RecentActivityOrder.UpdatedAt && order != this.RecentActivityOrder.Label && order != this.RecentActivityOrder.Status)) {
                    order = this.RecentActivityOrder.UpdatedAt;
                } else {
                    order = Number(order);
                }
				this.formGroup.get('order').setValue(order);
                this.formGroup.get('order').updateValueAndValidity();

				let keyword = (params['keyword'] === undefined || params['keyword'].length <= 0) ? "" : params['keyword'];
				this.formGroup.get("like").setValue(keyword);
			}

            if (this.formGroup.get('order').value == null){
                this.formGroup.get('order').setValue(this.RecentActivityOrder.UpdatedAt);
            }

            this.refresh();

            this.formGroup.get('like').valueChanges
            .pipe(takeUntil(this._destroyed), debounceTime(500))
            .subscribe(x => {
                this.refresh(true);
            });
            this.formGroup.get('order').valueChanges
            .pipe(takeUntil(this._destroyed))
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
		let parametersArray: string[] = [
			...( !this.isDefault && this.isActive() ? ["type=" + this.type] : []),
			...(this.currentPage > 1 ? ["page=" + this.currentPage] : []),
			...(this.formGroup.get("like").value ? ["keyword=" + this.formGroup.get("like").value] : []),
            ...(this.formGroup.get("order").value ? ["order=" + this.formGroup.get("order").value] : [])
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
		this.lookup.orderField = this.formGroup.get('order').value ?? this.RecentActivityOrder.UpdatedAt;
		this.lookup.like = this.formGroup.get('like').value ? this.filterService.transformLike(this.formGroup.get('like').value) : null;
		this.lookup.project = {
			fields : [ 
				...(this.includePlans ? this._getPlanLookup() : []), 
				...(this.includeDescriptions ? this._getDescriptionLookup() : [])
			]
		};
		
		this.lookup.page = { size: this.currentPage == 0 ? this.pageSize : this.pageSize*this.currentPage, offset: 0 };
        this.loadItems(refreshUrl);
	}

	loadMore() {
		this.lookup.page = { size: this.pageSize, offset: this.pageSize*this.currentPage };
		this.loadItems(true);
	}

	addDescription(): void {
		this.addNewDescription.next(true);
	}

	private loadItems(refreshUrl: boolean = false){
        this.loading = true;
		this.dashboardService
		.getMyRecentActivityItems(this.lookup)
		.pipe(takeUntil(this._destroyed))
		.subscribe({
            next: (response) => {
                if (response == null) return;

                if (this.lookup.page.offset == 0){
                    this.listingItems = [];
                    this.storageFileMap = new Map([])
                }
    
                response.forEach(item => {
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
    
                if (this.lookup.page.offset != 0 && response.length > 0 && this.listingItems.length >= this.currentPage*this.pageSize) this._setPage(this.currentPage+1);
                else this._resetPagination();
                if(refreshUrl){
                    this.updateUrl();
                }
                this.loading = false;

                this.downloadStorageFiles(this.listingItems.map(
                    (x) => x.description ? x.description?.status?.definition?.storageFile?.id : x.plan?.status?.definition?.storageFile?.id
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
            [nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.matIconName)].join('.'),			
            [nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),				
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.accessType)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.version)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.versionStatus)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.groupId)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.updatedAt)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.isActive)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.hash)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.authorizationFlags), AppPermission.CreateNewVersionPlan].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.authorizationFlags), AppPermission.DeletePlan].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.authorizationFlags), AppPermission.ClonePlan].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.authorizationFlags), AppPermission.FinalizePlan].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.authorizationFlags), AppPermission.ExportPlan].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.authorizationFlags), AppPermission.InvitePlanUsers].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.authorizationFlags), AppPermission.AssignPlanUsers].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.authorizationFlags), AppPermission.EditPlan].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.id)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.label)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.id)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.name)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.internalStatus)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.isActive)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.id)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.label)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.hasTemplates)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.id)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.label)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.groupId)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.id)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.id)].join('.'),
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
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.sectionId)].join('.'),
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
                [nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.matIconName)].join('.'),			
                [nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),			
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.updatedAt)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.isActive)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.authorizationFlags), AppPermission.EditDescription].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.authorizationFlags), AppPermission.DeleteDescription].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.authorizationFlags), AppPermission.InvitePlanUsers].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.authorizationFlags), AppPermission.CloneDescription].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.id)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.label)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.groupId)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.id)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.label)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.accessType)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.id)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.label)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.id)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.label)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.hasTemplates)].join('.'),
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