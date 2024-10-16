import { Location } from '@angular/common';
import { Component, computed, effect, input, Input, OnInit, Output } from '@angular/core';
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
import { PlanService } from '@app/core/services/plan/plan.service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { Lookup } from '@common/model/lookup';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { BehaviorSubject } from 'rxjs';
import { debounceTime, map, takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { ActivityListingType } from '../dashboard.component';

@Component({
	selector: 'app-recent-edited-activity',
	templateUrl: './recent-edited-activity.component.html',
	styleUrls: ['./recent-edited-activity.component.scss']
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
    
    currentType = input<ActivityListingType>();
    isActive = computed(() => this.currentType() === this.type);

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

	order = RecentActivityOrder;

	totalCount: number;
	offsetLess: number = 0;
	
	constructor(
		private route: ActivatedRoute,
		private router: Router,
		public enumUtils: EnumUtils,
		private authentication: AuthService,
		private dashboardService: DashboardService,
		private location: Location,
		private planService: PlanService,
		private analyticsService: AnalyticsService,
		private httpErrorHandlingService: HttpErrorHandlingService
	) {
		super();
        effect(() => {
            if(this.isActive()){  //on Type Changes
                this.updateUrl();
            }
        })
	}

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.RecentEditedActivity);
		this.route.queryParams.subscribe(params => {
			if (this.isActive()) {
				let page = (params['page'] === undefined) ? 0 : + params['page'];
				this.currentPage = (page <= 0) ? 0 : page;


				let order = params['order'];
				if (this.isAuthenticated()) {
					if (order === undefined || (order != this.order.UpdatedAt && order != this.order.Label && order != this.order.Status)) {
						order = this.order.UpdatedAt;
					}
				} else {
					//TODO refactor
					// if (order === undefined || (order != this.order.PUBLISHED && order != this.order.LABEL)) {
					// 	order = this.order.PUBLISHED;
					// }
				}
				this.formGroup.get('order').setValue(order);

				let keyword = (params['keyword'] === undefined || params['keyword'].length <= 0) ? "" : params['keyword'];
				this.formGroup.get("like").setValue(keyword);

				this.updateUrl();
			}
		});

		this.formGroup.get('like').valueChanges
		.pipe(takeUntil(this._destroyed), debounceTime(500))
		.pipe(map(value => value + '%'))
		.subscribe(x => {
			this.refresh()
		});
		
		if (!this.formGroup.get('order').value){
            this.formGroup.get('order').setValue(this.order.UpdatedAt);
        }
        
		this.formGroup.get('order').valueChanges
		.pipe(takeUntil(this._destroyed))
		.subscribe(x => {this.refresh()});

		this.refresh();
	}

	updateUrl() {
		let parametersArray: string[] = [
			...( !this.isDefault && this.isActive() ? ["type=" + this.type] : []),
			...(this.currentPage > 1 ? ["page=" + this.currentPage] : []),
			...(this.formGroup.get("like").value ? ["keyword=" + this.formGroup.get("like").value] : [])
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

	refresh(): void {
		this.lookup.onlyDraft = this.onlyDrafts ? this.onlyDrafts : null;
		this.lookup.onlyPlan = this.onlyPlans ? this.onlyPlans : null;
		this.lookup.onlyDescription = this.onlyDescriptions ? this.onlyDescriptions : null;
		this.lookup.orderField = this.formGroup.get('order').value ?? this.order.UpdatedAt;
		this.lookup.like = this.formGroup.get('like').value;
		this.lookup.project = {
			fields : [ 
				...(this.includePlans ? this._getPlanLookup() : []), 
				...(this.includeDescriptions ? this._getDescriptionLookup() : [])
			]
		};
		
		this.loadMore({ size: this.currentPage == 0 ? this.pageSize : this.pageSize*this.currentPage, offset: 0 });
	}

	loadMore(page?: Lookup.Paging) {
		if (!page) page = { size: this.pageSize, offset: this.pageSize*this.currentPage };
		this.lookup.page = page;
		this.loadItems(PaginationAction.LoadMore);
	}

	loadLess() {
		this.loadItems(PaginationAction.LoadLess);
	}

	addDescription(): void {
		this.addNewDescription.next(true);
	}

	private loadItems(action: PaginationAction){
		if (action == PaginationAction.LoadLess) {
			let latestBatchCount = this.listingItems.length%this.pageSize == 0 ? this.pageSize : this.listingItems.length%this.pageSize; 
			this.listingItems = this.listingItems.slice(0, this.listingItems.length-latestBatchCount);
			
			this._setPage(this.currentPage-1);
			
			return;
		}

        this.loading = true;
		this.dashboardService
		.getMyRecentActivityItems(this.lookup)
		.pipe(takeUntil(this._destroyed))
		.subscribe({
            next: (response) => {
                if (response == null) return;

                if (this.lookup.page.offset == 0) this.listingItems = [];
    
                response.forEach(item => {
                    if (item.plan){
                        if (item.plan.descriptions) {
                            if (item.plan.status == PlanStatusEnum.Finalized) {
                                item.plan.descriptions = item.plan.descriptions.filter(x => x.isActive === IsActive.Active && x.status === DescriptionStatusEnum.Finalized);
                            } else {
                                item.plan.descriptions = item.plan.descriptions.filter(x => x.isActive === IsActive.Active && x.status != DescriptionStatusEnum.Canceled);
                            }
                        }
                        item.plan.planUsers = item.plan.planUsers.filter(x=> x.isActive === IsActive.Active);
                        this.listingItems.push(item);
                    }
                    if (item.description){
                        if (item.description.status != DescriptionStatusEnum.Canceled) this.listingItems.push(item);
                    }
                })
    
                if (this.lookup.page.offset != 0 && response.length > 0 && this.listingItems.length >= this.currentPage*this.pageSize) this._setPage(this.currentPage+1);
                else this._resetPagination();

                this.loading = false;
            },
            error: (error) => {this.loading = false; this.httpErrorHandlingService.handleBackedRequestError(error)}
        });
	}
	
	private _resetPagination(): void {
		if (this.listingItems.length == 0) this.currentPage = 0;
		else this.currentPage = Math.ceil(this.listingItems.length/this.pageSize);
		this.updateUrl();
	}

	private _setPage(page: number) {
		this.currentPage = page;
		this.updateUrl();
	}
	
	private _getPlanLookup(): string[] {
		return [
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.id)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.label)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.description)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.status)].join('.'),
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
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.status)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.isActive)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.id)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.label)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.hasTemplates)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.id)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.label)].join('.'),
			[nameof<RecentActivityItem>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.descriptionTemplateGroupId)].join('.'),
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
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.status)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.updatedAt)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.isActive)].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.authorizationFlags), AppPermission.EditDescription].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.authorizationFlags), AppPermission.DeleteDescription].join('.'),
				[nameof<RecentActivityItem>(x => x.description), nameof<Description>(x => x.authorizationFlags), AppPermission.InvitePlanUsers].join('.'),
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

export enum PaginationAction {
	LoadMore = 0,
	LoadLess = 1
}
