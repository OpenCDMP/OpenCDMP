import { Location, NgTemplateOutlet, NgClass, UpperCasePipe } from '@angular/common';
import { Component, effect, inject, OnInit, QueryList, signal, ViewChild, ViewChildren, ChangeDetectionStrategy } from '@angular/core';
import { TourStateService } from '@app/library/guided-tour/tour-state.service';
import { planOverviewPageTour } from './plan-overview.tour';
import { MatDialog } from '@angular/material/dialog';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { DescriptionStatusEnum } from '@app/core/common/enum/description-status';
import { EvaluatorEntityType } from '@app/core/common/enum/evaluator-entity-type';
import { FileTransformerEntityType } from '@app/core/common/enum/file-transformer-entity-type';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { PlanAccessType } from '@app/core/common/enum/plan-access-type';
import { PlanStatusEnum } from '@app/core/common/enum/plan-status';
import { PlanStatusAvailableActionType } from '@app/core/common/enum/plan-status-available-action-type';
import { PlanStatusPermission } from '@app/core/common/enum/plan-status-permission.enum';
import { PlanUserRole } from '@app/core/common/enum/plan-user-role';
import { PlanVersionStatus } from '@app/core/common/enum/plan-version-status';
import { DepositConfiguration } from '@app/core/model/deposit/deposit-configuration';
import { DescriptionStatus, DescriptionStatusDefinition } from '@app/core/model/description-status/description-status';
import { BaseField, BaseFieldData, BaseFieldSet, BaseSection, DescriptionTemplate, Multiplicity, FieldSystemData, FieldReferenceTypeData, FieldRule } from '@app/core/model/description-template/description-template';
import { Description, DescriptionExternalIdentifier, DescriptionSectionPermissionResolver, PropertyDefinitionField, PropertyDefinitionFieldSet, PropertyDefinitionFieldSetItem } from '@app/core/model/description/description';
import { EntityDoi } from '@app/core/model/entity-doi/entity-doi';
import { RankResultModel } from '@app/core/model/evaluator/evaluator-plan-model.model';
import {
	DescriptionTemplatesInSection,
	PlanBlueprint,
	PlanBlueprintDefinition,
	RootSection,
} from '@app/core/model/plan-blueprint/plan-blueprint';
import { PlanStatus, PlanStatusDefinition } from '@app/core/model/plan-status/plan-status';
import {
	BasePlan,
	Plan, PlanAuthorizationInformation, PlanAuthorizationInformationPerSection, PlanContact,
	PlanDescriptionTemplate,
	PlanProperties,
	PlanUser
} from '@app/core/model/plan/plan';
import { PlanReference, PlanReferenceData } from '@app/core/model/plan/plan-reference';
import { ReferenceType, ReferenceTypeDefinition } from '@app/core/model/reference-type/reference-type';
import { Reference } from '@app/core/model/reference/reference';
import { User, UserContactInfo } from '@app/core/model/user/user';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { DepositService } from '@app/core/services/deposit/deposit.service';
import { EvaluatorService } from '@app/core/services/evaluator/evaluator.service';
import { FileTransformerService } from '@app/core/services/file-transformer/file-transformer.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { PlanService } from '@app/core/services/plan/plan.service';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { ReferenceService } from '@app/core/services/reference/reference.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { UserService } from '@app/core/services/user/user.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { PopupNotificationDialogComponent } from '@app/library/notification/popup/popup-notification.component';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseComponent } from '@common/base/base.component';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { catchError, map, takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { ClonePlanDialogComponent } from '../clone-dialog/plan-clone-dialog.component';
import { NewVersionPlanDialogComponent } from '../new-version-dialog/plan-new-version-dialog.component';
import { PlanVersionDiffDialogComponent, PlanVersionDiffDialogData } from '../plan-version-diff/plan-version-diff-dialog.component';
import { PlanDeleteDialogComponent } from '../plan-delete-dialog/plan-delete-dialog.component';
import { EvaluateDialogComponent } from '../../evaluation/evaluate-dialog/evaluate-dialog.component';
import { PlanFinalizeDialogComponent, PlanFinalizeDialogOutput } from '../plan-finalize-dialog/plan-finalize-dialog.component';
import { RankConfig } from '@app/core/model/evaluator/rank-config';
import { BenchmarkConfiguration, EvaluatorConfiguration } from '@app/core/model/evaluator/evaluator-configuration';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { StorageFileService } from '@app/core/services/storage-file/storage-file.service';
import { DescriptionService } from '@app/core/services/description/description.service';
import { EvaluationService } from '@app/core/services/evaluation/evaluation.service';
import { EntityType } from '@app/core/common/enum/entity-type';
import { Evaluation } from '@app/core/model/evaluation/evaluation';
import {PlanBlueprintFieldCategory} from "@app/core/common/enum/plan-blueprint-field-category";
import {SystemFieldType} from "@app/core/common/enum/system-field-type";
import {PlanBlueprintExtraFieldDataType} from "@app/core/common/enum/plan-blueprint-field-type";
import { BenchmarkDialogComponent, BenchmarkDialogResult } from '@app/ui/evaluation/benchmark-dialog/benchmark-dialog.component';
import { PluginEntityType } from '@app/core/common/enum/plugin-entity-type';
import { LanguageInfoService } from '@app/core/services/culture/language-info-service';
import { PlanBlueprintVersionStatus } from '@app/core/common/enum/plan-blueprint-version-status';
import { IconConfiguration } from '@app/core/model/icon-configuration/icon-configuration';
import { AnnotationService } from '@annotation-service/services/http/annotation.service';
import { AnnotationLookup } from '@annotation-service/core/query/annotation.lookup';
import { Annotation } from '@annotation-service/core/model/annotation.model';
import { MatAccordion, MatExpansionPanel, MatExpansionPanelHeader, MatExpansionPanelTitle } from '@angular/material/expansion';
import { PlanEditorEntityResolver } from '../plan-editor-blueprint/resolvers/plan-editor-enitity.resolver';
import { ExternalFetcherBaseSourceConfiguration } from '@app/core/model/external-fetcher/external-fetcher';
import { AnnotationEntityType } from '@app/core/common/enum/annotation-entity-type';
import { Tag } from '@app/core/model/tag/tag';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { FieldType } from '@app/core/common/enum/field-type';
import { forkJoin, Observable, of } from 'rxjs';
import { FieldValuePipe } from '@app/core/pipes/field-value.pipe';
import { EnqueueService } from '@app/core/services/enqueue.service';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatMenuTrigger, MatMenu, MatMenuItem } from '@angular/material/menu';
import { MatIcon } from '@angular/material/icon';
import { MatTooltip } from '@angular/material/tooltip';
import { PlanActiveUsersComponent } from '../plan-active-users/plan-active-users.component';
import { MatCard, MatCardContent } from '@angular/material/card';
import { MatDivider } from '@angular/material/list';
import { PlanAuthorsComponent } from '../plan-authors/plan-authors.component';
import { PlanEvaluationHistoryComponent } from '../listing/plan-evaluation-history/plan-evaluation-history.component';
import { DescriptionPreviewComponent } from '../../description/overview/description-preview/description-preview.component';
import { PlanDepositDropdown } from '../editor/plan-deposit-dropdown/plan-deposit-dropdown.component';
import { ReferenceCardComponent } from '../../reference/reference-card/reference-card.component';
import { DateTimeFormatPipe } from '../../../core/pipes/date-time-format.pipe';

@Component({
    selector: 'app-plan-overview',
    templateUrl: './plan-overview.component.html',
    styleUrls: ['./plan-overview.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    providers: [VisibilityRulesService],
    imports: [NgTemplateOutlet, MatButton, MatMenuTrigger, MatIcon, MatMenu, MatMenuItem, MatIconButton, MatTooltip, PlanActiveUsersComponent, MatCard, MatCardContent, MatDivider, MatAccordion, MatExpansionPanel, MatExpansionPanelHeader, MatExpansionPanelTitle, PlanAuthorsComponent, PlanEvaluationHistoryComponent, DescriptionPreviewComponent, PlanDepositDropdown, NgClass, ReferenceCardComponent, UpperCasePipe, TranslatePipe, DateTimeFormatPipe]
})
export class PlanOverviewComponent extends BaseComponent implements OnInit {
    @ViewChild('blueprintAccordion') accordion: MatAccordion;
    @ViewChildren('blueprintExpansionPanel') expansionPanels: QueryList<MatExpansionPanel>;
    @ViewChildren('descriptionExpansionPanel') descriptionExpansionPanels: QueryList<MatExpansionPanel>;
	isDraft = false;
	plan: Plan;
	selectedBlueprint: PlanBlueprint;
	// researchers: PlanReference[] = [];
	isNew = true;
	isFinalized = false;
	isPublicView = true;
	hasPublishButton: boolean = true;
	isLocked: boolean;
	textMessage: any;
	fileTransformerEntityTypeEnum = FileTransformerEntityType;
	evaluatorEntityTypeEnum = EvaluatorEntityType
	logos: Map<string, SafeResourceUrl> = new Map<string, SafeResourceUrl>();
	statuslogos: Map<Guid, SafeResourceUrl> = new Map<Guid, SafeResourceUrl>();

	allDescriptions: Description[] = [];

	descriptionStatusEnum = DescriptionStatusEnum;
	planAccessTypeEnum = PlanAccessType;
	planStatusEnum = PlanStatusEnum;
	planUserRoleEnum = PlanUserRole;
	planVersionStatusEnum = PlanVersionStatus;
    SystemFieldType = SystemFieldType; 
    FieldType = FieldType;
	authorFocus: string;
	userName: string;

    planEvaluations: Evaluation[];

    // REFERENCE_PAGE_SIZE = 8;
    // showMoreResearchers = signal<boolean>(false);

	planBlueprintSectionFieldCategoryEnum = PlanBlueprintFieldCategory;
	planBlueprintExtraFieldDataTypeEnum = PlanBlueprintExtraFieldDataType;
	// toggleShowMoreResearchers(){
    //     this.showMoreResearchers.update((value) => !value);
    // }
    
    private DESCRIPTION_PAGE_SIZE = 100;
    minimizedDescription: string;
    showLongDescription = signal<boolean>(true);
    toggleShowDescription(){
        this.showLongDescription.update((value) => !value);
    }

    // "What's new" tour for the 2.0.0 overview: registered for Support > Take a tour, auto-started once
    // per user after the plan (and its role-gated buttons) has rendered. Not on the public view.
    private readonly tourState = inject(TourStateService);
    private readonly pageTour = planOverviewPageTour(inject(TranslateService));

	isLoading = this.enqueueService.exhaustPipelineBusy;

	constructor(
		public routerUtils: RouterUtilsService,
		private route: ActivatedRoute,
		private router: Router,
		private planService: PlanService,
        private descriptionService: DescriptionService,
		private depositService: DepositService,
		private authentication: AuthService,
		private dialog: MatDialog,
		private language: TranslateService,
		private uiNotificationService: UiNotificationService,
		private configurationService: ConfigurationService,
		private location: Location,
		private lockService: LockService,
		private languageInfoService: LanguageInfoService,
		public referenceService: ReferenceService,
		public enumUtils: EnumUtils,
		public fileTransformerService: FileTransformerService,
		private referenceTypeService: ReferenceTypeService,
		private analyticsService: AnalyticsService,
		private breadcrumbService: BreadcrumbService,
		private httpErrorHandlingService: HttpErrorHandlingService,
		private userService: UserService,
		private evaluatorService: EvaluatorService,
        private evaluationService: EvaluationService,
		private logger: LoggingService,
		private sanitizer: DomSanitizer,
		private storageFileService: StorageFileService,
        private annotationService: AnnotationService,
        protected visibilityRulesService: VisibilityRulesService,
         private fieldValuePipe: FieldValuePipe,
		private enqueueService: EnqueueService,
	) {
		super();
        effect(() => {
            if(this.evaluatorService.availableEvaluators()){
                this.getEvaluators();
            }
        })
	}

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.PlanOverview);
		// No "what's new" tour on the public, unauthenticated view — same rule as the listing pages.
		if (!this.route.snapshot.data['isPublic']) {
			this.tourState.registerPageTour(this.pageTour);
		}
		// Gets plan data using parameter id
		this.route.params
			.pipe(takeUntil(this._destroyed))
			.subscribe((params: Params) => {
				const itemId = params['id'];
				const publicId = params['publicId'];
				this.getItem({itemId, publicId});
			});
	}

    getItem(params?: {itemId?: Guid; publicId?: Guid}){
        const {itemId, publicId} = params ?? {};
        if (itemId != null) {
            this.isNew = false;
            this.isPublicView = false;
            this.planService.getSingle(itemId, this.lookupFields())
            .pipe(takeUntil(this._destroyed))
            .subscribe({
                next: (data) => {
                    this.breadcrumbService.addIdResolvedValue(data.id?.toString(), data.label);

                    this.plan = data;
                    if (this.plan.status?.internalStatus == PlanStatusEnum.Draft) {
                        this.isDraft = true;
                    }
                    this.plan.planUsers = this.isActive ? data?.planUsers?.filter((x) => x.isActive === IsActive.Active) : data?.planUsers;
                    this.plan.planUsers = this.plan.planUsers?.sort((a,b) => +new Date(a.createdAt) - +new Date(b.createdAt));
                    this.plan.planReferences = this.isActive ? data?.planReferences?.filter((x) => x.isActive === IsActive.Active) : data?.planReferences;
                    this.plan.otherPlanVersions = data.otherPlanVersions?.filter(x => x.isActive === IsActive.Active) || null;
                    this.tourState.startIfUnseen(this.pageTour);
                    if(this.plan.description && this.plan.description.split(' ')?.length > this.DESCRIPTION_PAGE_SIZE) {
                        this.minimizedDescription = this.plan.description.split(' ').slice(0, this.DESCRIPTION_PAGE_SIZE).join(' ') + '...';
                        this.showLongDescription.set(false);
                    } else {
                        this.minimizedDescription = null;
                        this.showLongDescription.set(true);
                    }
                    if (this.plan.descriptions && this.isActive) {
                        this.allDescriptions = data.descriptions;
                        if (this.plan.status?.internalStatus == PlanStatusEnum.Finalized) {
                            this.plan.descriptions = data.descriptions.filter(x => x.isActive === IsActive.Active && x.status?.internalStatus === DescriptionStatusEnum.Finalized);
                        } else {
                            this.plan.descriptions = data.descriptions.filter(x => x.isActive === IsActive.Active && x.status?.internalStatus !== DescriptionStatusEnum.Canceled);
                        }
                    }
                    const descriptionSectionPermissionResolverModel: DescriptionSectionPermissionResolver = {
                        planId: this.plan.id,
                        sectionIds: this.plan?.blueprint?.definition?.sections?.map(x => x.id),
                        permissions: [AppPermission.EditDescription]
                    };

                    if (data.entityDois && data.entityDois.length > 0) this.plan.entityDois = data.entityDois.filter(x => x.isActive === IsActive.Active);
                    this.selectedBlueprint = data.blueprint;
                    // this.researchers = this.referenceService.getReferencesForTypes(this.plan.planReferences, this.isActive, [this.referenceTypeService.getResearcherReferenceType()]);
                    this.loadStatusLogo();
                    this.loadReferenceData();
                    this.checkLockStatus(this.plan.id);
                    this.getEvaluations(this.plan.id);
                    this.getAnnotationCount();
                    this.visibilityRulesService.setContext(this.plan.blueprint.definition, null, this.plan.properties);

                    this.plan.blueprint?.definition?.sections?.forEach((section) => this.initSectionFieldSetValues(section))
                },
                error: (error: any) => {
                    this.httpErrorHandlingService.handleBackedRequestError(error);

                    if (error.status === 404) {
                        return this.onFetchingDeletedCallbackError('/plans/');
                    }
                    if (error.status === 403) {
                        return this.onFetchingForbiddenCallbackError('/plans/');
                    }
                }
            });
        }
        else if (publicId != null) {
            this.isNew = false;
            this.isFinalized = true;
            this.isPublicView = true;
            this.planService.getPublicSingle(publicId, this.lookupFields())
            .pipe(takeUntil(this._destroyed))
            .subscribe({
                next: (data) => {
                    this.breadcrumbService.addExcludedParam('public', true);
                    this.breadcrumbService.addIdResolvedValue(data.id?.toString(), data.label);

                    this.plan = data;
                    this.selectedBlueprint = data.blueprint;
                    if (this.plan.status?.internalStatus == PlanStatusEnum.Draft) {
                        this.isDraft = true;
                    }
                    this.plan.planReferences = this.isActive ? data?.planReferences?.filter((x) => x.isActive === IsActive.Active) : data?.planReferences;
                    // this.researchers = this.referenceService.getReferencesForTypes(this.plan?.planReferences, this.isActive, [this.referenceTypeService.getResearcherReferenceType()]); //data.planReferences is of wrong type!
                    this.plan.planUsers = this.isActive ? data?.planUsers?.filter((x) => x.isActive === IsActive.Active) : data?.planUsers;
                    this.plan.planUsers = this.plan.planUsers?.sort((a,b) => +new Date(a.createdAt) - +new Date(b.createdAt));
                    this.plan.otherPlanVersions = data.otherPlanVersions?.filter(x => x.isActive === IsActive.Active) || null;
                    if(this.plan.description && this.plan.description.split(' ')?.length > this.DESCRIPTION_PAGE_SIZE) {
                        this.minimizedDescription = this.plan.description.split(' ').slice(0, this.DESCRIPTION_PAGE_SIZE).join(' ') + '...';
                        this.showLongDescription.set(false);
                    } else {
                        this.minimizedDescription = null;
                        this.showLongDescription.set(true);
                    }
                    this.loadReferenceData();
                    this.visibilityRulesService.setContext(this.plan.blueprint.definition, null, this.plan.properties);
                    this.plan.blueprint?.definition?.sections?.forEach((section) => this.initSectionFieldSetValues(section))
                },
                error: (error: any) => {
                    this.httpErrorHandlingService.handleBackedRequestError(error);

                    if (error.status === 404) {
                        return this.onFetchingDeletedCallbackError('/explore-plans');
                    }
                    if (error.status === 403) {
                        return this.onFetchingForbiddenCallbackError('/explore-plans');
                    }
                }
            });
        }
    }

    onUsersChanged(newUsers: PlanUser[]){
        this.plan = {...this.plan, planUsers: newUsers?.filter((x) => x.isActive === IsActive.Active) ?? []}; //resetting plan to trigger signal updates
    }

    availableEvaluators: EvaluatorConfiguration[] = [];
    getEvaluators(){
        if (this.isAuthenticated) {
			this.availableEvaluators = this.evaluatorService.availableEvaluatorsFor(EvaluatorEntityType.Plan);
			if (this.availableEvaluators?.length > 0) {
				this.availableEvaluators.forEach(repo => {
                    if (repo.hasLogo) {
                        this.evaluatorService.getLogo(repo.evaluatorId).pipe(takeUntil(this._destroyed)).subscribe(
                            (responseLogo) => {
                                this.logos.set(repo.evaluatorId, this.sanitizer.bypassSecurityTrustResourceUrl('data:image/png;base64, ' + responseLogo.body));
                            },
                            error => {
                                this.logger.error("Error fetching evaluator logo:", error);
                                }
                            );
                    }
                })
			}
			

			this.userService.getSingle(this.authentication.userId(), [
				nameof<User>(x => x.id),
				nameof<User>(x => x.name)])
				.pipe(map(u => u.name), takeUntil(this._destroyed)).subscribe(name => this.userName = name);
		} else {
			this.userName = '';
		}
    }
  

	exportPlanAs(fileTransformerId: string, format: string): void {
		const sections = this.plan?.blueprint?.definition?.sections
			?.map(x => ({ id: x.id.toString(), label: x.title })) ?? [];
		const descriptions = (this.plan?.descriptions ?? [])
			.filter(x => x.id)
			.map(x => ({ id: x.id.toString(), label: x.label }));
		this.fileTransformerService.exportPlan(this.plan.id, fileTransformerId, format, this.isPublicView, { sections: sections, descriptions: descriptions });
	}

	get isActive(): boolean {
		return this.plan?.isActive != IsActive.Inactive;
	}

	get selectedPlanVersion(): number {
		return this.plan?.version;
	}

	get otherPlanVersions(): (Plan)[] {
		return this.plan?.otherPlanVersions?.filter((x) => x.version !== this.plan.version);
	}


	get canEditStatus(): boolean {
		return this.isAuthenticated && this.isActive && !this.isLocked && this.plan.belongsToCurrentTenant &&
        (this.plan as Plan).statusAuthorizationFlags?.some(x => x.toLowerCase() === PlanStatusPermission.Edit.toLowerCase()) &&
        !this.hasDoi() && (this.plan as Plan).availableStatuses?.length && (this.plan as Plan).versionStatus != PlanVersionStatus.Previous;
	}

	onFetchingDeletedCallbackError(redirectRoot: string) {
		this.router.navigate([this.routerUtils.generateUrl(redirectRoot)]);
	}

	onFetchingForbiddenCallbackError(redirectRoot: string) {
		this.router.navigate([this.routerUtils.generateUrl(redirectRoot)]);
	}

	isUserAuthor(userId: Guid): boolean {
		if (this.isAuthenticated) {
			const principalId: Guid = this.authentication.userId();
			return this.userName && userId === principalId;
		} else return false;
	}

	focusOnAuthor(planUserId: Guid, order: number): void {
		this.authorFocus = `${planUserId}-${order}`;
	}

	resetAuthorFocus(): void {
		this.authorFocus = null;
	}

	isFocusedOnUser(planUserId: Guid, order: number): boolean {
		return `${planUserId}-${order}` == this.authorFocus;
	}

	newVersionDisabledReason(): string {
		return (this.plan as Plan)?.versionStatus === PlanVersionStatus.Current ? 'PLAN-OVERVIEW.DISABLED-TOOLTIP.NEW-VERSION-PERMISSION' : 'PLAN-OVERVIEW.DISABLED-TOOLTIP.NEW-VERSION';
	}

	canCreateNewVersion(): boolean {
		const authorizationFlags = (this.plan as Plan).authorizationInformation?.globalAuthorizationFlags;
		const versionStatus = (this.plan as Plan)?.versionStatus;
		return this.isActive && (authorizationFlags?.some(x => x === AppPermission.CreateNewVersionPlan) || this.authentication.hasPermission(AppPermission.CreateNewVersionPlan)) && versionStatus === PlanVersionStatus.Current && this.isPublicView == false && this.plan.belongsToCurrentTenant != false;
	}

    get canEditPlan(): boolean {
        if(!this.plan){ return; }
		const authorizationFlags = !this.isPublicView ? (this.plan as Plan).authorizationInformation?.globalAuthorizationFlags : [];
        const canEditSection = (this.plan as Plan).authorizationInformation?.authorizationInformationPerSections?.some((x) => 
            [AppPermission.EditPlan, AppPermission.DeleteDescription, AppPermission.EditDescription].some((y) => x.authorizationFlags.includes(y))
        )
		return this.isActive && (this.isNotFinalizedPlan()) && !this.isPublicView && this.plan.belongsToCurrentTenant && (
            authorizationFlags?.some(x => x === AppPermission.EditPlan) || 
            this.authentication.hasPermission(AppPermission.EditPlan) || 
            canEditSection
        )
            
	}

	get canDeletePlan(): boolean {
        if(!this.plan){ return; }
		const authorizationFlags = !this.isPublicView ? (this.plan as Plan).authorizationInformation?.globalAuthorizationFlags : [];
		return (
			this.isActive &&
			(authorizationFlags?.some(x => x === AppPermission.DeletePlan) || this.authentication.hasPermission(AppPermission.DeletePlan)) &&
			this.isPublicView == false && this.plan.belongsToCurrentTenant != false &&
			!this.hasDoi()
		)
	}

	get canClonePlan(): boolean {
        if(!this.plan){ return; }
		const authorizationFlags = !this.isPublicView ? (this.plan as Plan).authorizationInformation?.globalAuthorizationFlags : [];
		return (
			(authorizationFlags?.some(x => x === AppPermission.ClonePlan) ||
			this.authentication.hasPermission(AppPermission.ClonePlan) ||
			(this.authentication.hasPermission(AppPermission.PublicClonePlan) && this.isPublicView))
		);
	}

	// canFinalizePlan(): boolean {
	// 	const authorizationFlags = !this.isPublicView ? (this.plan as Plan).authorizationFlags : [];
	// 	return (
	// 		this.isActive &&
	// 		(authorizationFlags?.some(x => x === AppPermission.FinalizePlan) || this.authentication.hasPermission(AppPermission.FinalizePlan))) &&
	// 		this.isPublicView == false && this.plan.belongsToCurrentTenant != false;
	// }

	get canExportPlan(): boolean {
        if(!this.plan){ return; }
		const authorizationFlags = !this.isPublicView ? (this.plan as Plan).authorizationInformation?.globalAuthorizationFlags : [];
		return this.isActive && (authorizationFlags?.some(x => x === AppPermission.ExportPlan) || this.authentication.hasPermission(AppPermission.ExportPlan)) &&
			this.plan?.status?.definition?.availableActions?.filter(x => x === PlanStatusAvailableActionType.Export).length > 0;

	}

	canViewBlueprintVersionMessage(): boolean {
		const authorizationFlags = (this.plan as Plan).authorizationInformation?.globalAuthorizationFlags;
		return this.isActive && (this.plan as Plan)?.blueprint?.versionStatus == PlanBlueprintVersionStatus.Previous && (authorizationFlags?.some(x => x === AppPermission.CreateNewVersionPlan) || this.authentication.hasPermission(AppPermission.CreateNewVersionPlan)) && this.isPublicView == false && this.plan.belongsToCurrentTenant != false;
	}

	get depositRepos(): DepositConfiguration[] {
        return this.depositService.availableDeposits();
    }

	canEvaluatePlan(): boolean {
		const authorizationFlags = !this.isPublicView ? (this.plan as Plan).authorizationInformation?.globalAuthorizationFlags : [];
		return this.isActive && (authorizationFlags?.some(x => x === AppPermission.EvaluatePlan) || this.authentication.hasPermission(AppPermission.EvaluatePlan)) &&
			this.plan?.status?.definition?.availableActions?.filter(x => x === PlanStatusAvailableActionType.Evaluate).length > 0;
	}

	onEvaluatePlan(planId: Guid, evaluatorId: string, format: string, rankConfig: RankConfig, availableBenchmarks: BenchmarkConfiguration[] = [], availableTests: BenchmarkConfiguration[] = [], isPublicView: boolean) {
		
		availableBenchmarks = availableBenchmarks?.filter(x => x.appliesTo.includes(PluginEntityType.Plan)) || [];
		availableTests = availableTests?.filter(x => x.appliesTo.includes(PluginEntityType.Plan)) || [];

		if (availableBenchmarks?.length > 1 || availableTests?.length > 1) {
			const dialogRef = this.dialog.open(BenchmarkDialogComponent, {
				width: '500px',
				data: {
					availableBenchmarks: availableBenchmarks,
					availableTests: availableTests,
					evaluatorId: evaluatorId
				}
			});
			dialogRef.afterClosed().subscribe((result: BenchmarkDialogResult) => {
				if (result?.values?.length > 0) {
					const benchmarkIds = result.type === 'benchmarks' ? result.values : null;
					const testIds = result.type === 'tests' ? result.values : null;
					this.rankPlan(planId, evaluatorId, format, rankConfig, benchmarkIds, testIds);
				}
			});
		} else if (availableBenchmarks?.length == 1 && !(availableTests?.length > 0)){
			this.rankPlan(planId, evaluatorId, format, rankConfig, [availableBenchmarks[0].id], null)
		} else if (availableTests?.length == 1 && !(availableBenchmarks?.length > 0)){
			this.rankPlan(planId, evaluatorId, format, rankConfig, null, [availableTests[0].id])
		} else {
			this.rankPlan(planId, evaluatorId, format, rankConfig, null, null)
		}

	}

	private rankPlan(planId: Guid, evaluatorId: string, format: string, rankConfig: RankConfig, benchmarkIds: string[] | null, testIds: string[] | null) {
		this.evaluatorService.rankPlan(planId, evaluatorId, format, benchmarkIds ?? undefined, testIds ?? undefined).subscribe(
			(response: RankResultModel[]) => {
				const dialogRef = this.dialog.open(EvaluateDialogComponent, {
					maxWidth: '90vw',
					data: {
						rankData: response,
						rankConfig: rankConfig,
                        evaluatorId,
						// Lets the dialog link each result to the asset it covers.
						planId: planId
					}
				});

				dialogRef.afterClosed().subscribe(result => {
					this.logger.debug("Dialog closed with result:", result);
                    this.getEvaluations(planId);
				});
			},
			error => {
				this.logger.error("Error ranking plan:", error);
			}
		);
	}

    getEvaluations(id: Guid) {
        this.evaluationService.query(this.evaluationService.buildEvaluationLookup({
            entityIds: [id],
            entityTypes: [EntityType.Plan]
        }))
        .pipe(takeUntil(this._destroyed), map((res) => res.items))
        .subscribe({
            next: (result: Evaluation[]) => {
                this.planEvaluations = result;
            },
            error: (error: any) => this.httpErrorHandlingService.handleBackedRequestError(error)
        })
    }

	get canInvitePlanUsers(): boolean {
        if(this.isPublicView || !this.plan.belongsToCurrentTenant){ return false; }
		const authorizationFlags = (this.plan as Plan).authorizationInformation?.globalAuthorizationFlags;
		return (
			this.isActive &&
			(authorizationFlags?.some(x => x === AppPermission.InvitePlanUsers) || this.authentication.hasPermission(AppPermission.InvitePlanUsers)));
        }

	get canAssignPlanUsers(): boolean {
        if(this.isPublicView || !this.plan.belongsToCurrentTenant){ return false; }
		const authorizationFlags = (this.plan as Plan).authorizationInformation?.globalAuthorizationFlags;
		return this.isActive && (authorizationFlags?.some(x => x === AppPermission.AssignPlanUsers) || this.authentication.hasPermission(AppPermission.AssignPlanUsers))
	}

	canDepositPlan(): boolean {
        if(this.isPublicView || !this.plan.belongsToCurrentTenant){ return false; }
		const authorizationFlags = (this.plan as Plan).authorizationInformation?.globalAuthorizationFlags;
		return this.isActive && (authorizationFlags?.some(x => x === AppPermission.DepositPlan) || this.authentication.hasPermission(AppPermission.DepositPlan)) &&
			this.plan?.status?.definition?.availableActions?.filter(x => x === PlanStatusAvailableActionType.Deposit).length > 0;
	}

	canShowPidFinalizeMessage(): boolean {
        if(this.isPublicView || !this.plan.belongsToCurrentTenant){ return false; }
		const authorizationFlags = (this.plan as Plan).authorizationInformation?.globalAuthorizationFlags;
		return this.inputRepos?.length > 0 && !this.hasDoi() && this.isActive && (authorizationFlags?.some(x => x === AppPermission.DepositPlan) || this.authentication.hasPermission(AppPermission.DepositPlan));
	}


	editClicked() {
		this.router.navigate([this.routerUtils.generateUrl(['/plans/edit', this.plan.id.toString()], '/')]);
	}

	cloneClicked() {
		const dialogRef = this.dialog.open(ClonePlanDialogComponent, {
			maxWidth: '700px',
			maxHeight: '80vh',
			data: {
				plan: this.plan,
				isPublic: this.isPublicView ? this.isPublicView : false
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe((result: Plan) => {
			if (result) {
				this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
				this.router.navigate([this.routerUtils.generateUrl(['/plans/edit', result.id.toString()], '/')]);
			}
		});
	}

	newVersionClicked(){
		const dialogRef = this.dialog.open(NewVersionPlanDialogComponent, {
			maxWidth: '700px',
			maxHeight: '80vh',
			data: {
				plan: {
                    ...this.plan,
                    descriptions: this.allDescriptions.filter(x => x.isActive === IsActive.Active || (x.status?.internalStatus === DescriptionStatusEnum.Canceled))
                }
			}
		});
		return dialogRef.afterClosed().pipe(takeUntil(this._destroyed))
        .subscribe((result) => {
            if (result) {
                this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
                this.router.navigate([this.routerUtils.generateUrl(['/plans/edit', result.id.toString()], '/')]);
            }
        });
    }

	deleteClicked() {
		let dialogRef: any;
		if (this.plan.descriptions && this.plan.descriptions.length > 0) {
			dialogRef = this.dialog.open(PlanDeleteDialogComponent, {
				maxWidth: '300px',
				data: {
					descriptions: this.plan.descriptions,
				}
			});
		} else {
			dialogRef = this.dialog.open(ConfirmationDialogComponent, {
				maxWidth: '300px',
				data: {
					message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-ITEM'),
					confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.DELETE'),
					cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
					isDeleteConfirmation: true
				}
			});
		}

		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result) {
				this.planService.delete(this.plan.id)
					.pipe(takeUntil(this._destroyed))
					.subscribe({
						complete: () => this.onDeleteCallbackSuccess(),
						error: (error) => this.onDeleteCallbackError(error)
					});
			}
		});
	}

	onDeleteCallbackSuccess(): void {
		this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-DELETE'), SnackBarNotificationLevel.Success);
		this.router.navigate([this.routerUtils.generateUrl('/plans')]);
	}

	onDeleteCallbackError(error) {
		this.httpErrorHandlingService.handleBackedRequestError(error);
	}

	onUpdateCallbackSuccess(): void {
		this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
		this.reloadPage();
	}

	onUpdateCallbackError(error) {
		this.httpErrorHandlingService.handleBackedRequestError(error);
	}

	isUserPlanRelated(): boolean {
		const principalId: Guid = this.authentication.userId();
		return this.plan.planUsers?.some(x => (x.user.id === principalId));
	}

	isNotFinalizedPlan() {
		return this.plan.status?.internalStatus != PlanStatusEnum.Finalized;
	}

	isPublishedPlan() {
		return (this.plan.status?.internalStatus == PlanStatusEnum.Finalized && this.plan.accessType === PlanAccessType.Public);
	}

	hasDoi() {
		return (this.plan.entityDois?.length > 0);
	}

	afterDeposit(result: EntityDoi[]) {
		if (result.length > 0) {
			this.plan.entityDois = result;
		}
	}

	get inputRepos() {
		return this.depositRepos?.filter(repo => !this.plan.entityDois?.find(doi => doi.repositoryId === repo.repositoryId));
	}

	moreDeposit() {
		return (this.plan.entityDois?.length < this.depositRepos?.length);
	}

	getLanguageInfo(code: string): string {
		return this.languageInfoService.getLanguageInfoValues()?.find(x => x.code === code)?.name || code;
	}

	persistStatus(status: PlanStatus) {
		if (status.internalStatus != null && status.internalStatus === PlanStatusEnum.Finalized) {
			this.finalize(status.id);
		} else if (this.plan.status.internalStatus === PlanStatusEnum.Finalized) {
			this.reverseFinalization(status.id);
		} else {
			// other statuses
			this.enqueueService.enqueueExhaustChannel(this.planService.setStatus(this.plan.id, status.id)).pipe(takeUntil(this._destroyed))
				.subscribe({
					complete: () => { this.reloadPage(); this.onUpdateCallbackSuccess() },
					error: (error: any) => {
						this.onUpdateCallbackError(error)
					}
				});
		}
	}

	finalize(newStatusId) {
		const dialogRef = this.dialog.open(PlanFinalizeDialogComponent, {
			maxWidth: '500px',
			data: {
				plan: this.plan
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe((result: PlanFinalizeDialogOutput) => {
			if (result && !result.cancelled) {

				this.planService.setStatus(this.plan.id, newStatusId, result.descriptionsToBeFinalized)
					.pipe(takeUntil(this._destroyed))
					.subscribe({
						complete: () => {
							this.reloadPage();
							this.onUpdateCallbackSuccess()
						},
						error: (error: any) => {
							this.onUpdateCallbackError(error)
						}
					});
			}
		});

	}

	public get isAuthenticated(): boolean {
		return this.authentication.currentAccountIsAuthenticated();
	}

	versionChanged(versionId: string): void {
		if (this.isPublicView) {
			this.router.navigate([this.routerUtils.generateUrl(['/explore-plans/overview/public/', versionId])]);
		} else {
			this.router.navigate([this.routerUtils.generateUrl(['/plans/overview/', versionId])]);
		}
	}

	compareVersionClicked(otherPlanId: Guid): void {
		this.dialog.open(PlanVersionDiffDialogComponent, {
			data: {
				planId: this.plan.id,
				otherPlanId: otherPlanId
			} as PlanVersionDiffDialogData,
			maxWidth: '52rem',
			autoFocus: 'dialog'
		});
	}

	// openShareDialog(rowId: any, rowName: any) {
	// 	const dialogRef = this.dialog.open(PlanInvitationDialogComponent, {
	// 		restoreFocus: false,
	// 		data: {
	// 			planId: rowId,
	// 			planName: rowName,
	// 			blueprint: this.selectedBlueprint
	// 		},
    //         minWidth: 'min(65rem, 90vw)'
	// 	});
	// }

    createDoiLink(doiModel: any): string {
		const repository = this.depositRepos?.find(r => r.repositoryId == doiModel.repositoryId);
		return repository?.repositoryRecordUrl?.replace(this.configurationService.depositRecordUrlIdPlaceholder, doiModel.doi) ?? null;
	}

	reverseFinalization(newStatusId: Guid) {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('PLAN-OVERVIEW.UNDO-FINALIZATION-DIALOG.TITLE'),
				confirmButton: this.language.instant('PLAN-OVERVIEW.UNDO-FINALIZATION-DIALOG.CONFIRM'),
				cancelButton: this.language.instant('PLAN-OVERVIEW.UNDO-FINALIZATION-DIALOG.NEGATIVE'),
				isDeleteConfirmation: false
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result) {
				this.planService.setStatus(this.plan.id, newStatusId).pipe(takeUntil(this._destroyed))
					.subscribe({
						complete: () => { this.reloadPage(); this.onUpdateCallbackSuccess() },
						error: (error: any) => {
							this.onUpdateCallbackError(error)
						}
					});
			}
		});
	}

	goBack(): void {
		this.location.back();
	}

	reloadPage(): void {
        this.getItem({
            itemId: this.isPublicView ? null : this.plan.id,
            publicId: this.isPublicView ? this.plan.id : null,
        })
		this.getEvaluators();
	}

	// removeUserFromPlanDialog(planUser: PlanUser) {
	// 	let dialogRef;
	// 	if (planUser?.user?.id == this.authentication.userId() && !this.plan?.planUsers?.find(x => x.id != planUser.id && x.user?.id === planUser?.user?.id)) {
	// 		dialogRef = this.dialog.open(ConfirmationDialogComponent, {
	// 			data: {
	// 				message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-YOURSELF-FROM-ENTIRE-PLAN'),
	// 				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.REMOVE'),
	// 				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
	// 				isDeleteConfirmation: false
	// 			}
	// 		});
	// 	} else {
	// 		dialogRef = this.dialog.open(ConfirmationDialogComponent, {
	// 			data: {
	// 				message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-USER'),
	// 				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.REMOVE'),
	// 				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
	// 				isDeleteConfirmation: false
	// 			}
	// 		});
	// 	}

	// 	dialogRef?.afterClosed()?.subscribe(result => {
	// 		if (result) {
	// 			this.removeUserFromPlan(planUser);
	// 		}
	// 	});
	// }

	// removeUserFromPlan(planUser: PlanUser) {
	// 	const planUserRemovePersist: PlanUserRemovePersist = {
	// 		id: planUser.id,
	// 		planId: this.plan.id,
	// 		role: planUser.role
	// 	};

	// 	this.planService.removeUser(planUserRemovePersist, this.lookupFields()).pipe(takeUntil(this._destroyed))
	// 		.subscribe({
	// 			next: (plan) => {
	// 				if ((plan?.planUsers?.find(x => x?.user?.id == planUser?.user?.id && x.isActive === IsActive.Active)) || (plan?.authorizationFlags?.some(x => x === AppPermission.AssignPlanUsers) || this.authentication.hasPermission(AppPermission.AssignPlanUsers))) {
	// 					this.onUpdateCallbackSuccess();
	// 				} else {
	// 					this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
	// 					this.router.navigate([this.routerUtils.generateUrl('/plans')]);
	// 				}
	// 			},
	// 			error: (error) => {
	// 				this.onUpdateCallbackError(error);
	// 			}
	// 		});
	// }

	copyDoi(doi) {
		navigator.clipboard.writeText(doi).then(() => {
			this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-COPY-TO-CLIPBOARD'), SnackBarNotificationLevel.Success);
		});
	}

	// getSectionNameById(sectionId: Guid): string {
	// 	if (sectionId == null) return '';
	// 	const blueprint = this.isPublicView ? null : (this.plan as Plan)?.blueprint;
	// 	let sections: PlanBlueprintDefinitionSection[] = blueprint?.definition?.sections?.filter((section: PlanBlueprintDefinitionSection) => sectionId === section.id);

	// 	return sections == null ? '' : sections[0].label;
	// }

	checkLockStatus(id: Guid) {
		this.lockService.checkLockStatus(Guid.parse(id.toString())).pipe(takeUntil(this._destroyed))
			.subscribe({
				next: (lockStatus) => {
					this.isLocked = lockStatus.status;
					if (this.isLocked) {
						this.dialog.open(PopupNotificationDialogComponent, {
							data: {
								title: this.language.instant('PLAN-OVERVIEW.LOCKED-DIALOG.TITLE'),
								message: this.language.instant('PLAN-OVERVIEW.LOCKED-DIALOG.MESSAGE')
							}, maxWidth: '30em'
						});
					}
				},
				error: (error) => {
					this.router.navigate([this.routerUtils.generateUrl('/plans')]);
					this.httpErrorHandlingService.handleBackedRequestError(error);
				}
			});
	}

	private loadStatusLogo(){
		this.statusObjectUrls.forEach(u => URL.revokeObjectURL(u));
		this.statusObjectUrls = [];
		const availableStatus = [...((this.plan as Plan).availableStatuses ?? []), this.plan.status];
		if (availableStatus?.length > 0) {
			availableStatus.forEach(x => {
				if (x.definition?.iconConfiguration?.storageFile?.id) {
					this.storageFileService.download(x.definition?.iconConfiguration.storageFile?.id).pipe(takeUntil(this._destroyed))
					.subscribe(response => {
						const url = URL.createObjectURL(response.body);
						this.statusObjectUrls.push(url);
						this.statuslogos.set(x.definition?.iconConfiguration.storageFile?.id, this.sanitizer.bypassSecurityTrustUrl(url));
					});		
				}
			})
		}			
	}

    referenceLogos:  Map<Guid, SafeResourceUrl>;            // keyed by storageFileId (dedups downloads)
    referenceStorageFileByRefId: Map<Guid, Guid>;           // reference.id -> storageFileId
    referencesByFieldId: Map<string, PlanReference[]>;      // blueprintFieldId -> plan references
    referenceNavigationUrls: Map<Guid, string>;             // reference.id -> navigation url
    referenceTooltips: Map<Guid, string>;                   // reference.id -> tooltip
    private referenceObjectUrls: string[] = [];
    private statusObjectUrls: string[] = [];

    override ngOnDestroy(): void {
        this.tourState.unregisterPageTour(this.pageTour);
        this.referenceObjectUrls.forEach(u => URL.revokeObjectURL(u));
        this.statusObjectUrls.forEach(u => URL.revokeObjectURL(u));
        super.ngOnDestroy();
    }

    private loadReferenceData(){
        this.referenceObjectUrls.forEach(u => URL.revokeObjectURL(u));
        this.referenceObjectUrls = [];
        this.descriptionsBySectionId = null;
        this.referenceLogos = new Map<Guid, SafeResourceUrl>([]);
        this.referenceStorageFileByRefId = new Map<Guid, Guid>([]);
        this.referencesByFieldId = new Map<string, PlanReference[]>([]);
        this.referenceNavigationUrls = new Map<Guid, string>([]);
        this.referenceTooltips = new Map<Guid, string>([]);

        if(!this.plan?.planReferences?.length){ return; }
        const tempStorageFileIdSet = new Set([]);

        this.plan.planReferences.forEach((ref) => {
            const fieldId = ref.data?.blueprintFieldId?.toString();
            if(fieldId){
                const list = this.referencesByFieldId.get(fieldId) ?? [];
                list.push(ref);
                this.referencesByFieldId.set(fieldId, list);
            }
            const sourceDefinition = ref.reference?.type?.definition?.sources?.find(x => x.key == ref.reference.source);
            if(!sourceDefinition){ return; }
            const referenceId = ref.reference?.id;
            this.referenceNavigationUrls.set(referenceId, sourceDefinition?.navigationUrl);
            this.referenceTooltips.set(referenceId, sourceDefinition?.tooltipMessage);
            const storageFileId = sourceDefinition?.storageFile?.id;
            if(storageFileId){
                this.referenceStorageFileByRefId.set(referenceId, storageFileId);
                if(!tempStorageFileIdSet.has(storageFileId)){
                    tempStorageFileIdSet.add(storageFileId);
                    this.storageFileService.download(storageFileId).pipe(takeUntil(this._destroyed))
                    .subscribe(response => {
                        const url = URL.createObjectURL(response.body);
                        this.referenceObjectUrls.push(url);
                        this.referenceLogos.set(storageFileId, this.sanitizer.bypassSecurityTrustUrl(url));
                    });
                }
            }
        })
        this.referencesByFieldId.forEach((list) => list.sort((a, b) => (a.data?.ordinal ?? 0) - (b.data?.ordinal ?? 0)));
    }

    private readonly emptyRefs: PlanReference[] = [];
    private readonly emptyDescriptions: Description[] = [];
    private descriptionsBySectionId: Map<string, Description[]> | null = null;

    getFieldReferences(fieldId: Guid): PlanReference[] {
        return this.referencesByFieldId?.get(fieldId?.toString()) ?? this.emptyRefs;
    }
    hasRenderableValue(values: string[]): boolean {
        return values?.some(v => v != null && v !== '') ?? false;
    }
    getReferenceLogo(referenceId: Guid): SafeResourceUrl {
        const storageFileId = this.referenceStorageFileByRefId?.get(referenceId);
        return storageFileId ? this.referenceLogos?.get(storageFileId) : null;
    }
    getReferenceTooltip(referenceId: Guid): string {
        return this.referenceTooltips?.get(referenceId);
    }

	private lookupFields(): string[] {
		return [
			nameof<BasePlan>(x => x.isActive),
			nameof<Plan>(x => x.id),
			nameof<Plan>(x => x.label),
			nameof<Plan>(x => x.description),
			nameof<Plan>(x => x.accessType),
			nameof<Plan>(x => x.language),
			[nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.contacts),nameof<PlanContact>(x => x.firstName),].join('.'),
			[nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.contacts),nameof<PlanContact>(x => x.lastName),].join('.'),
			[nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.comment)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.ordinal)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.textValue)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.textListValue)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.dateValue)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.booleanValue)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.numberValue)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.externalIdentifier), nameof<DescriptionExternalIdentifier>(x => x.identifier)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.externalIdentifier), nameof<DescriptionExternalIdentifier>(x => x.type)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.references), nameof<Reference>(x => x.id)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.references), nameof<Reference>(x => x.label)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.references), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.id)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.references), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.name)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.references), nameof<Reference>(x => x.reference)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.references), nameof<Reference>(x => x.isActive)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.tags), nameof<Tag>(x => x.id)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.tags), nameof<Tag>(x => x.label)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.tags), nameof<Tag>(x => x.isActive)].join('.'),
            [nameof<Plan>(x => x.properties), nameof<PlanProperties>(x => x.fieldSets), nameof<PropertyDefinitionFieldSet>(x => x.items), nameof<PropertyDefinitionFieldSetItem>(x => x.fields), nameof<PropertyDefinitionField>(x => x.tags), nameof<Tag>(x => x.hash)].join('.'),
			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.name)].join('.'),
			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.internalStatus)].join('.'),
			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.availableActions)].join('.'),
			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.matIconName)].join('.'),
			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.statusColor)].join('.'),
			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),
			nameof<Plan>(x => x.accessType),
			nameof<Plan>(x => x.version),
			nameof<Plan>(x => x.versionStatus),
			nameof<Plan>(x => x.groupId),
			nameof<Plan>(x => x.version),
			nameof<Plan>(x => x.updatedAt),
			nameof<Plan>(x => x.createdAt),
			nameof<Plan>(x => x.entityDois),
			nameof<Plan>(x => x.belongsToCurrentTenant),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.CreateNewVersionPlan].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.DeletePlan].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.ClonePlan].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.FinalizePlan].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.ExportPlan].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.EvaluatePlan].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.InvitePlanUsers].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.AssignPlanUsers].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.EditPlan].join('.'),
			[nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.DepositPlan].join('.'),
            [nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.authorizationInformationPerSections), nameof<PlanAuthorizationInformationPerSection>(x => x.sectionId)].join('.'),
            [nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.authorizationInformationPerSections), nameof<PlanAuthorizationInformationPerSection>(x => x.authorizationFlags), AppPermission.EditPlan].join('.'),
            [nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.authorizationInformationPerSections), nameof<PlanAuthorizationInformationPerSection>(x => x.authorizationFlags), AppPermission.EditDescription].join('.'),
            [nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.authorizationInformationPerSections), nameof<PlanAuthorizationInformationPerSection>(x => x.authorizationFlags), AppPermission.DeleteDescription].join('.'),
            [nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.authorizationInformationPerSections), nameof<PlanAuthorizationInformationPerSection>(x => x.authorizationFlags), AppPermission.AnnotatePlan].join('.'),
            [nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.authorizationInformationPerSections), nameof<PlanAuthorizationInformationPerSection>(x => x.authorizationFlags), AppPermission.AnnotateDescription].join('.'),
			[nameof<Plan>(x => x.statusAuthorizationFlags), PlanStatusPermission.Edit].join('.'),
			[nameof<Plan>(x => x.entityDois), nameof<EntityDoi>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.entityDois), nameof<EntityDoi>(x => x.repositoryId)].join('.'),
			[nameof<Plan>(x => x.entityDois), nameof<EntityDoi>(x => x.doi)].join('.'),
			[nameof<Plan>(x => x.entityDois), nameof<EntityDoi>(x => x.isActive)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.label)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.groupId)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.name)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.internalStatus)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.statusColor)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.isActive)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.sectionId)].join('.'),
            ...PlanEditorEntityResolver.planEditorDescriptionsLookupFields(),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.createdAt)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.sectionId)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.id)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.name)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.contacts), nameof<UserContactInfo>(x => x.value)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.contacts), nameof<UserContactInfo>(x => x.type)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.role)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.plan.id)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.isActive)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.ordinal)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.id)].join('.'),
            [nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.data), nameof<PlanReferenceData>(x => x.blueprintFieldId)].join('.'),
            [nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.data), nameof<PlanReferenceData>(x => x.ordinal)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.label)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.source)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.navigationUrl)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.tooltipMessage)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.reference)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.isActive)].join('.'),
			[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.sectionId)].join('.'),
			[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.descriptionTemplateGroupId)].join('.'),
			[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.isActive)].join('.'),

			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.label)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.versionStatus)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.isActive)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.title)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.description)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.ordinal)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.sections)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.additionalInformation)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.description)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.extendedDescription)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.hasMultiplicity)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.multiplicity), nameof<Multiplicity>(x => x.max)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.multiplicity), nameof<Multiplicity>(x => x.min)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.multiplicity), nameof<Multiplicity>(x => x.placeholder)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.multiplicity), nameof<Multiplicity>(x => x.tableView)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.multiplicity), nameof<Multiplicity>(x => x.transposedTableView)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.ordinal)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.title)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.id)].join('.'),
			// Visibility rules — required so the overview hides rule-driven sections/fields like the editor.
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.visibilityRules), nameof<FieldRule>(x => x.target)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.visibilityRules), nameof<FieldRule>(x => x.textValue)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.visibilityRules), nameof<FieldRule>(x => x.dateValue)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.visibilityRules), nameof<FieldRule>(x => x.booleanValue)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.visibilityRules), nameof<FieldRule>(x => x.numberValue)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.semantics)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.ordinal)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.defaultValue)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<BaseFieldData>(x => x.label)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<BaseFieldData>(x => x.fieldType)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldSystemData>(x => x.systemFieldType)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldReferenceTypeData>(x => x.referenceType), nameof<ReferenceType>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldReferenceTypeData>(x => x.referenceType), nameof<ReferenceType>(x => x.name)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.hasTemplates)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.groupId)].join('.'),

			[nameof<Plan>(x => x.otherPlanVersions), nameof<Plan>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.otherPlanVersions), nameof<Plan>(x => x.groupId)].join('.'),
			[nameof<Plan>(x => x.otherPlanVersions), nameof<Plan>(x => x.version)].join('.'),
			[nameof<Plan>(x => x.otherPlanVersions), nameof<Plan>(x => x.isActive)].join('.'),

			[nameof<Plan>(x => x.availableStatuses), nameof<PlanStatus>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.availableStatuses), nameof<PlanStatus>(x => x.name)].join('.'),
			[nameof<Plan>(x => x.availableStatuses), nameof<PlanStatus>(x => x.internalStatus)].join('.'),
			[nameof<Plan>(x => x.availableStatuses), nameof<PlanStatus>(x => x.action)].join('.'),
			[nameof<Plan>(x => x.availableStatuses), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.matIconName)].join('.'),
			[nameof<Plan>(x => x.availableStatuses), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),

			nameof<Plan>(x => x.hash),
		]
	}

    // protected getBlueprintValue(fieldId: Guid): PlanBlueprintValue{
    //     return (this.plan as Plan)?.properties?.planBlueprintValues?.find((x) => x.fieldId === fieldId);
    // }

    annotationCount: number;
    getAnnotationCount() {
        const descriptionAnnotations = this.plan.descriptions?.map((desc) => desc.id) ?? [];
        const lookup: AnnotationLookup = new AnnotationLookup();
        lookup.entityIds = [this.plan.id, ...descriptionAnnotations];
        lookup.entityTypes = [AnnotationEntityType.Plan, AnnotationEntityType.Description];
        lookup.isActive = [IsActive.Active];
        lookup.project = {
            fields: [
                nameof<Annotation>(x => x.id),
            ]
        };
        this.annotationService.query(lookup).pipe(
            takeUntil(this._destroyed),
            map(x => x.count)
        ).subscribe((res) => this.annotationCount = res)
    }

    get showExpand(): boolean {
        return this.expansionPanels?.some(panel => !panel.expanded) ?? false;
    }

    toggleExpand(){
        if(this.showExpand){
            this.accordion?.openAll();
            this.descriptionExpansionPanels?.forEach((panel) => panel.open());
        }else{
            this.accordion?.closeAll();
            this.descriptionExpansionPanels?.forEach((panel) => panel.close());
        }
    }

    get planOwner(): string {
        return this.plan.planUsers?.filter(x => x.isActive === IsActive.Active)?.find(x => x.role === PlanUserRole.Owner)?.user?.name;
    }

    getSectionFields(section: BaseSection): BaseField[] {
        return (section?.fieldSets ?? []).flatMap(fs => fs.fields ?? []);
    }

    getSectionDescriptions(section: RootSection): Description[] {
        if(!section?.hasTemplates || !this.plan.descriptions?.length){ return this.emptyDescriptions; }
        // Memoize the section -> descriptions grouping (descriptions are static for a loaded plan).
        // Invalidated on (re)load via loadReferenceData(), so it never returns stale data.
        if(this.descriptionsBySectionId == null){
            this.descriptionsBySectionId = new Map<string, Description[]>();
            this.plan.descriptions.forEach(description => {
                const sectionId = description.planDescriptionTemplate?.sectionId;
                if(!sectionId){ return; }
                const list = this.descriptionsBySectionId.get(sectionId) ?? [];
                list.push(description);
                this.descriptionsBySectionId.set(sectionId, list);
            });
        }
        return this.descriptionsBySectionId.get(section.id) ?? this.emptyDescriptions;
    }
    
    showEvaluationHistory: boolean = true;

    fieldSetValueMap = new Map<string, Map<string, string[]>>([]);

    initSectionFieldSetValues(section: BaseSection)  {
        if(section.sections){
            section.sections.forEach((x) => this.initSectionFieldSetValues(x))
        }
        if(section.fieldSets){
            section.fieldSets.forEach((fieldSet) => {
                this.initFieldSetValues(fieldSet).pipe(takeUntil(this._destroyed))
                .subscribe((fieldsetMap) => this.fieldSetValueMap.set(fieldSet.id, fieldsetMap))
            })
        }
    }

    initFieldSetValues: (fieldSet: BaseFieldSet) => Observable<Map<string, string[]>>  = (fieldSet) => {
        const properties: PropertyDefinitionFieldSetItem[]  = this.plan?.properties?.fieldSets?.[fieldSet.id]?.items ?? [];
        
        const fieldValueMapArray = new Map<string, string[]>([]);

        const callStack: Observable<string[]>[] = fieldSet.fields.map((field) =>
            this.buildFieldMap(field, properties).pipe(tap((res) => fieldValueMapArray.set(field.id, res)))
        )
        return forkJoin(
            callStack
        ).pipe(
            map((res) => {
                return fieldValueMapArray;
            }),
            catchError((error) => of(new Map<string, string[]>([])))
        )
        // if(!properties){ return of([])}
        // const callStack: Observable<Map<string, string>>[] = properties.map((item) => this.buildFieldMap(fieldSet, item.fields));
        // return forkJoin(
        //     callStack
        // ).pipe(
        //     map((res) => {
        //         fieldValueMapArray = [];
        //         res?.forEach((itemMap) => fieldValueMapArray.push(itemMap));
        //         return fieldValueMapArray;
        //     }),
        //     catchError((error) => of([]))
        // )

    }
    buildFieldMap(field: BaseField, items: PropertyDefinitionFieldSetItem[]): Observable<string[]>{
        // Multi-item: keep values position-aligned to items (nulls preserved) so value[j] matches
        // properties.items[j] and its per-item visibility ordinal. Single-item: keep prior "hide empty".
        const fieldValues = (items?.length > 1
            ? items.map(x => x.fields?.[field.id] ?? null)
            : items?.map(x => x.fields?.[field.id])?.filter(x => !!x)) as PropertyDefinitionField[];
        if(fieldValues?.length){
            return forkJoin(
                fieldValues.map((value) => {
                    return this.fieldValuePipe.transform(value, field, this.plan)
                    .pipe(
                        takeUntil(this._destroyed), 
                        catchError(() => of(null))
                    )
                })
            ).pipe(catchError((error) => of([])))
        }else if(field.data?.fieldType == FieldType.SYSTEM) {
            //check for system fields 
            return this.fieldValuePipe.transform(null, field, this.plan).pipe(takeUntil(this._destroyed), map((res) => [res]))
        }
        return of([])

        // const itemMap = new Map<string, string>([]);
        // return forkJoin(
        //     Object.keys(fieldValues)?.map((key) => {
        //         const field = fieldSet.fields.find((x) => x.id === key);
        //         return this.fieldValuePipe.transform(fieldValues[key], field, this.plan)
        //         .pipe(
        //             takeUntil(this._destroyed), 
        //             catchError(() => of(null)),
        //             tap((res) =>{itemMap.set(key,res)})
        //         )
        //     })
        // ).pipe(switchMap(() => of(itemMap)), catchError((error) => of(new Map<string, string>([]))))
    }
}
