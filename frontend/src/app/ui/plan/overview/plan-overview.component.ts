import { Location } from '@angular/common';
import { Component, ElementRef, OnInit, signal, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { DomSanitizer, SafeResourceUrl, SafeUrl } from '@angular/platform-browser';
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
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { Description, DescriptionSectionPermissionResolver } from '@app/core/model/description/description';
import { EntityDoi } from '@app/core/model/entity-doi/entity-doi';
import { RankModel } from '@app/core/model/evaluator/evaluator-plan-model.model';
import { DescriptionTemplatesInSection, PlanBlueprint, PlanBlueprintDefinition, PlanBlueprintDefinitionSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { PlanStatus, PlanStatusDefinition } from '@app/core/model/plan-status/plan-status';
import { BasePlan, NewVersionPlanPersist, Plan, PlanDescriptionTemplate, PlanPersist, PlanUser, PlanUserRemovePersist, PublicPlan } from '@app/core/model/plan/plan';
import { PlanReference } from '@app/core/model/plan/plan-reference';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { Reference } from '@app/core/model/reference/reference';
import { User } from '@app/core/model/user/user';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { DepositService } from '@app/core/services/deposit/deposit.service';
import { EvaluatorService } from '@app/core/services/evaluator/evaluator.service';
import { FileTransformerService } from '@app/core/services/file-transformer/file-transformer.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { PlanBlueprintService } from '@app/core/services/plan/plan-blueprint.service';
import { PlanService } from '@app/core/services/plan/plan.service';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { ReferenceService } from '@app/core/services/reference/reference.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { UserService } from '@app/core/services/user/user.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { FileUtils } from '@app/core/services/utilities/file-utils.service';
import { PopupNotificationDialogComponent } from '@app/library/notification/popup/popup-notification.component';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseComponent } from '@common/base/base.component';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { catchError, map, switchMap, takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { ClonePlanDialogComponent } from '../clone-dialog/plan-clone-dialog.component';
import { PlanInvitationDialogComponent } from '../invitation/dialog/plan-invitation-dialog.component';
import { NewVersionPlanDialogComponent } from '../new-version-dialog/plan-new-version-dialog.component';
import { PlanDeleteDialogComponent } from '../plan-delete-dialog/plan-delete-dialog.component';
import { PlanEvaluateDialogComponent } from '../plan-evaluate-dialog/plan-evaluate-dialog.component';
import { PlanFinalizeDialogComponent, PlanFinalizeDialogOutput } from '../plan-finalize-dialog/plan-finalize-dialog.component';
import { RankConfig } from '@app/core/model/evaluator/rank-config';
import { EvaluatorConfiguration } from '@app/core/model/evaluator/evaluator-configuration';
import { EvaluatorHttpService } from '@app/core/services/evaluator/evaluator.http.service';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { StorageFileService } from '@app/core/services/storage-file/storage-file.service';
import { DescriptionService } from '@app/core/services/description/description.service';
import { EvaluationService } from '@app/core/services/evaluation/evaluation.service';
import { EntityType } from '@app/core/common/enum/entity-type';
import { Evaluation } from '@app/core/model/evaluation/evaluation';
import { Observable, of } from 'rxjs';
import { PlanEditorEntityResolver } from '../plan-editor-blueprint/resolvers/plan-editor-enitity.resolver';

@Component({
    selector: 'app-plan-overview',
    templateUrl: './plan-overview.component.html',
    styleUrls: ['./plan-overview.component.scss'],
    standalone: false
})
export class PlanOverviewComponent extends BaseComponent implements OnInit {

	plan: Plan | PublicPlan;
	selectedBlueprint: PlanBlueprint;
	researchers: PlanReference[] = [];
	isNew = true;
	isFinalized = false;
	isPublicView = true;
	hasPublishButton: boolean = true;
	isLocked: Boolean;
	textMessage: any;
	fileTransformerEntityTypeEnum = FileTransformerEntityType;
	evaluatorEntityTypeEnum = EvaluatorEntityType
	logos: Map<string, SafeResourceUrl> = new Map<string, SafeResourceUrl>();
	statuslogos: Map<Guid, SafeResourceUrl> = new Map<Guid, SafeResourceUrl>();

    descriptionPermissions: AppPermission[];
	allDescriptions: Description[] = [];

	depositRepos: DepositConfiguration[] = [];
	evaluatorRepos: EvaluatorConfiguration[] = [];

	descriptionStatusEnum = DescriptionStatusEnum;
	planAccessTypeEnum = PlanAccessType;
	planStatusEnum = PlanStatusEnum;
	planUserRoleEnum = PlanUserRole;
	planVersionStatusEnum = PlanVersionStatus;

	authorFocus: string;
	userName: string;

    planEvaluations: Evaluation[];

    RESARCHER_PAGE_SIZE = 8;
    showMoreResearchers = signal<boolean>(false);
    toggleShowMoreResearchers(){
        this.showMoreResearchers.update((value) => !value);
    }
    
    private DESCRIPTION_PAGE_SIZE = 100;
    minimizedDescription: string;
    showLongDescription = signal<boolean>(true);
    toggleShowDescription(){
        this.showLongDescription.update((value) => !value);
    }

	constructor(
		public routerUtils: RouterUtilsService,
		private route: ActivatedRoute,
		private router: Router,
		private planService: PlanService,
        private descriptionService: DescriptionService,
		private depositRepositoriesService: DepositService,
		private authentication: AuthService,
		private dialog: MatDialog,
		private language: TranslateService,
		private uiNotificationService: UiNotificationService,
		private configurationService: ConfigurationService,
		private location: Location,
		private lockService: LockService,
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
		private evaluatorHttpService: EvaluatorHttpService,
		private logger: LoggingService,
		private sanitizer: DomSanitizer,
		private storageFileService: StorageFileService
	) {
		super();
	}

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.PlanOverview);
		// Gets plan data using parameter id
		this.route.params
			.pipe(takeUntil(this._destroyed))
			.subscribe((params: Params) => {
				const itemId = params['id'];
				const publicId = params['publicId'];
				if (itemId != null) {
					this.isNew = false;
					this.isPublicView = false;
					this.planService.getSingle(itemId, this.lookupFields())
                    .pipe(takeUntil(this._destroyed))
                    .subscribe({
                        next: (data) => {
                            this.breadcrumbService.addIdResolvedValue(data.id?.toString(), data.label);

                            this.plan = data;
                            this.plan.planUsers = this.isActive ? data?.planUsers?.filter((x) => x.isActive === IsActive.Active) : data?.planUsers;
                            this.plan.otherPlanVersions = data.otherPlanVersions?.filter(x => x.isActive === IsActive.Active) || null;
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
                            let descriptionSectionPermissionResolverModel: DescriptionSectionPermissionResolver = {
                                planId: this.plan.id,
                                sectionIds: this.plan?.blueprint?.definition?.sections?.map(x => x.id),
                                permissions: [AppPermission.EditDescription]
                            };
                            this.descriptionService.getDescriptionSectionPermissions(descriptionSectionPermissionResolverModel).pipe(takeUntil(this._destroyed))
                            .subscribe((result) => {
                                if(result){
                                    this.descriptionPermissions = Object.values(result).reduce((cur, prev) => [...cur, ...prev]);
                                }
                            })

                            if (data.entityDois && data.entityDois.length > 0) this.plan.entityDois = data.entityDois.filter(x => x.isActive === IsActive.Active);
                            this.selectedBlueprint = data.blueprint;
                            this.researchers = this.referenceService.getReferencesForTypes(this.isActive ? this.plan.planReferences?.filter(x => x.isActive === IsActive.Active): this.plan.planReferences, [this.referenceTypeService.getResearcherReferenceType()]);
                            this.loadStatusLogo();
                            this.checkLockStatus(this.plan.id);
                            this.getEvaluations(this.plan.id);
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
                            this.researchers = this.referenceService.getReferencesForTypes(this.plan?.planReferences?.filter(x => x.isActive === IsActive.Active), [this.referenceTypeService.getResearcherReferenceType()]); //data.planReferences is of wrong type!

                            if(this.plan.description && this.plan.description.split(' ')?.length > this.DESCRIPTION_PAGE_SIZE) {
                                this.minimizedDescription = this.plan.description.split(' ').slice(0, this.DESCRIPTION_PAGE_SIZE).join(' ') + '...';
                                this.showLongDescription.set(false);
                            } else {
                                this.minimizedDescription = null;
                                this.showLongDescription.set(true);
                            }
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
			});
		if (this.isAuthenticated) {
			this.depositRepositoriesService.getAvailableRepos([
				nameof<DepositConfiguration>(x => x.depositType),
				nameof<DepositConfiguration>(x => x.repositoryId),
				nameof<DepositConfiguration>(x => x.repositoryAuthorizationUrl),
				nameof<DepositConfiguration>(x => x.repositoryRecordUrl),
				nameof<DepositConfiguration>(x => x.repositoryClientId),
				nameof<DepositConfiguration>(x => x.hasLogo),
				nameof<DepositConfiguration>(x => x.redirectUri)
			])
				.pipe(takeUntil(this._destroyed))
				.subscribe({
					next: (repos) => this.depositRepos = repos,
					error: () => this.depositRepos = []
				})
			
			this.evaluatorHttpService.getAvailableConfigurations()
				.pipe(takeUntil(this._destroyed))
				.subscribe({
					next: (repos) => {
						this.evaluatorRepos = repos as any;
						if (this.evaluatorRepos?.length > 0) {
							this.evaluatorRepos.forEach(repo => {
							if (repo.hasLogo) {
								this.evaluatorService.getLogo(repo.evaluatorId).subscribe(
									(responseLogo) => {
										this.logos.set(repo.evaluatorId, this.sanitizer.bypassSecurityTrustResourceUrl('data:image/png;base64, ' + responseLogo.body));
									},
									error => {
										this.logger.error("Error fetching evaluator logo:", error);
									}
								);
							}
						})
					}},
					error: () => this.evaluatorRepos = []
				})

			this.userService.getSingle(this.authentication.userId(), [
				nameof<User>(x => x.id),
				nameof<User>(x => x.name)])
				.pipe(map(u => u.name)).subscribe(name => this.userName = name);
		} else {
			this.userName = '';
		}
	}
  

	get isActive(): boolean {
		return this.plan?.isActive != IsActive.Inactive;
	}

	get selectedPlanVersion(): number {
		return this.plan?.version;
	}

	get otherPlanVersions(): (Plan | PublicPlan)[] {
		return this.plan?.otherPlanVersions?.filter((x) => x.version !== this.plan.version);
	}

	get unauthorizedTootipText(): string {
		return this.language.instant('PLAN-OVERVIEW.INFOS.UNAUTHORIZED-ORCID');
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

	canCreateNewVersion(): boolean {
		const authorizationFlags = (this.plan as Plan).authorizationFlags;
		const versionStatus = (this.plan as Plan)?.versionStatus;
		return this.isActive && (authorizationFlags?.some(x => x === AppPermission.CreateNewVersionPlan) || this.authentication.hasPermission(AppPermission.CreateNewVersionPlan)) && versionStatus === PlanVersionStatus.Current && this.isPublicView == false && this.plan.belongsToCurrentTenant != false;
	}

    get canEditPlan(): boolean {
        if(!this.plan){ return; }
		const authorizationFlags = !this.isPublicView ? (this.plan as Plan).authorizationFlags : [];
        const canEditDesc = this.descriptionPermissions?.some((x) => x === AppPermission.EditDescription)
		return this.isActive && (this.isNotFinalizedPlan()) && 
            (authorizationFlags?.some(x => x === AppPermission.EditPlan) || this.authentication.hasPermission(AppPermission.EditPlan) || canEditDesc) && 
            this.isPublicView == false && this.plan.belongsToCurrentTenant != false;
	}

	get canDeletePlan(): boolean {
        if(!this.plan){ return; }
		const authorizationFlags = !this.isPublicView ? (this.plan as Plan).authorizationFlags : [];
		return (
			this.isActive &&
			(authorizationFlags?.some(x => x === AppPermission.DeletePlan) || this.authentication.hasPermission(AppPermission.DeletePlan)) &&
			this.isPublicView == false && this.plan.belongsToCurrentTenant != false
		)
	}

	get canClonePlan(): boolean {
        if(!this.plan){ return; }
		const authorizationFlags = !this.isPublicView ? (this.plan as Plan).authorizationFlags : [];
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
		const authorizationFlags = !this.isPublicView ? (this.plan as Plan).authorizationFlags : [];
		return this.isActive && (authorizationFlags?.some(x => x === AppPermission.ExportPlan) || this.authentication.hasPermission(AppPermission.ExportPlan)) &&
			this.plan?.status?.definition?.availableActions?.filter(x => x === PlanStatusAvailableActionType.Export).length > 0;

	}

    get availableEvaluators(): EvaluatorConfiguration[] {
        return this.evaluatorService.availableEvaluatorsFor(EvaluatorEntityType.Plan);
    }

	canEvaluatePlan(): boolean {
		const authorizationFlags = !this.isPublicView ? (this.plan as Plan).authorizationFlags : [];
		return this.isActive && (authorizationFlags?.some(x => x === AppPermission.EvaluatePlan) || this.authentication.hasPermission(AppPermission.EvaluatePlan)) &&
			this.plan?.status?.definition?.availableActions?.filter(x => x === PlanStatusAvailableActionType.Evaluate).length > 0;
	}

	onEvaluatePlan(planId: Guid, evaluatorId: string, format: string, rankConfig: RankConfig ,isPublicView: boolean) {
		this.evaluatorService.rankPlan(planId, evaluatorId, format).subscribe(
			(response: RankModel) => {
				const dialogRef = this.dialog.open(PlanEvaluateDialogComponent, {
					data: {
						rankData: response,
						rankConfig: rankConfig
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

	canInvitePlanUsers(): boolean {
		const authorizationFlags = !this.isPublicView ? (this.plan as Plan).authorizationFlags : [];
		return (
			this.isActive &&
			(authorizationFlags?.some(x => x === AppPermission.InvitePlanUsers) || this.authentication.hasPermission(AppPermission.InvitePlanUsers))) &&
			this.isPublicView == false && this.plan.belongsToCurrentTenant != false;
	}

	canAssignPlanUsers(): boolean {
		const authorizationFlags = !this.isPublicView ? (this.plan as Plan).authorizationFlags : [];
		return this.isActive && (authorizationFlags?.some(x => x === AppPermission.AssignPlanUsers) || this.authentication.hasPermission(AppPermission.AssignPlanUsers)) && this.isPublicView == false && this.plan.belongsToCurrentTenant != false &&
			(this.plan.status.internalStatus == null || this.plan.status.internalStatus != PlanStatusEnum.Finalized);
	}

	canDepositPlan(): boolean {
		const authorizationFlags = !this.isPublicView ? (this.plan as Plan).authorizationFlags : [];
		return this.isActive && (authorizationFlags?.some(x => x === AppPermission.DepositPlan) || this.authentication.hasPermission(AppPermission.DepositPlan)) && this.isPublicView == false && this.plan.belongsToCurrentTenant != false &&
			this.plan?.status?.definition?.availableActions?.filter(x => x === PlanStatusAvailableActionType.Deposit).length > 0;
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
		this.uiNotificationService.snackBarNotification(error.error.message ? this.language.instant(error.error.message) : this.language.instant('GENERAL.SNACK-BAR.UNSUCCESSFUL-DELETE'), SnackBarNotificationLevel.Error);
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
		return this.depositRepos.filter(repo => !this.plan.entityDois?.find(doi => doi.repositoryId === repo.repositoryId));
	}

	moreDeposit() {
		return (this.plan.entityDois?.length < this.depositRepos?.length);
	}

	persistStatus(status: PlanStatus) {
		if (status.internalStatus != null && status.internalStatus === PlanStatusEnum.Finalized) {
			this.finalize(status.id);
		} else if (this.plan.status.internalStatus === PlanStatusEnum.Finalized) {
			this.reverseFinalization(status.id);
		} else {
			// other statuses
			this.planService.setStatus(this.plan.id, status.id).pipe(takeUntil(this._destroyed))
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
			restoreFocus: false,
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

	openShareDialog(rowId: any, rowName: any) {
		const dialogRef = this.dialog.open(PlanInvitationDialogComponent, {
			restoreFocus: false,
			data: {
				planId: rowId,
				planName: rowName,
				blueprint: this.selectedBlueprint
			},
            minWidth: 'min(65rem, 90vw)'
		});
	}

	createDoiLink(doiModel: any): string {
		//TODO: needs rewriting
		const repository = this.depositRepos.find(r => r.repositoryId == doiModel.repositoryId);
		if (typeof repository !== "undefined") {
			return repository.repositoryRecordUrl.replace(this.configurationService.depositRecordUrlIdPlaceholder, doiModel.doi);
		}
		else {
			return "";
		}
	}

	reverseFinalization(newStatusId: Guid) {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			restoreFocus: false,
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
		const path = this.location.path();
		this.router.navigateByUrl('/reload', { skipLocationChange: true }).then(() => {
			this.router.navigate([this.routerUtils.generateUrl(path)]);
		});
	}

	removeUserFromPlan(planUser: PlanUser) {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-USER'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.REMOVE'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
				isDeleteConfirmation: false
			}
		});
		dialogRef.afterClosed().subscribe(result => {
			if (result) {
				const planUserRemovePersist: PlanUserRemovePersist = {
					id: planUser.id,
					planId: this.plan.id,
					role: planUser.role
				};
				this.planService.removeUser(planUserRemovePersist).pipe(takeUntil(this._destroyed))
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

	copyDoi(doi) {
		const el = document.createElement('textarea');
		el.value = doi;
		el.setAttribute('readonly', '');
		el.style.position = 'absolute';
		el.style.left = '-9999px';
		document.body.appendChild(el);
		el.select();
		document.execCommand('copy');
		document.body.removeChild(el);
		this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-COPY-TO-CLIPBOARD'), SnackBarNotificationLevel.Success);
	}

	public getOrcidPath(): string {
		return this.configurationService.orcidPath;
	}

	isOrcid(reference: Reference) {
		return reference.source === 'orcid';
	}

	getOrcidPathForResearcher(reference: string): string {
		const path = this.getOrcidPath();
		return path + reference;
	}

	getSectionNameById(sectionId: Guid): string {
		if (sectionId == null) return '';
		const blueprint = this.isPublicView ? null : (this.plan as Plan)?.blueprint;
		let sections: PlanBlueprintDefinitionSection[] = blueprint?.definition?.sections?.filter((section: PlanBlueprintDefinitionSection) => sectionId === section.id);

		return sections == null ? '' : sections[0].label;
	}

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
		const availableStatus = [...(this.plan as Plan).availableStatuses, this.plan.status];
		if (availableStatus?.length > 0) {
			availableStatus.forEach(x => {
				if (x.definition?.storageFile?.id) {
					this.storageFileService.download(x.definition?.storageFile?.id).pipe(takeUntil(this._destroyed))
					.subscribe(response => {
						this.statuslogos.set(x.definition?.storageFile?.id,this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(response.body)));
					});		
				}
			})
		}			
	}

	private lookupFields(): string[] {
		return [
			nameof<BasePlan>(x => x.isActive),
			nameof<Plan>(x => x.id),
			nameof<Plan>(x => x.label),
			nameof<Plan>(x => x.description),
			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.name)].join('.'),
			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.internalStatus)].join('.'),
			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.availableActions)].join('.'),
			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.matIconName)].join('.'),
			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.statusColor)].join('.'),
			[nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),
			nameof<Plan>(x => x.accessType),
			nameof<Plan>(x => x.version),
			nameof<Plan>(x => x.versionStatus),
			nameof<Plan>(x => x.groupId),
			nameof<Plan>(x => x.version),
			nameof<Plan>(x => x.updatedAt),
			nameof<Plan>(x => x.entityDois),
			nameof<Plan>(x => x.belongsToCurrentTenant),
			[nameof<Plan>(x => x.authorizationFlags), AppPermission.CreateNewVersionPlan].join('.'),
			[nameof<Plan>(x => x.authorizationFlags), AppPermission.DeletePlan].join('.'),
			[nameof<Plan>(x => x.authorizationFlags), AppPermission.ClonePlan].join('.'),
			[nameof<Plan>(x => x.authorizationFlags), AppPermission.FinalizePlan].join('.'),
			[nameof<Plan>(x => x.authorizationFlags), AppPermission.ExportPlan].join('.'),
			[nameof<Plan>(x => x.authorizationFlags), AppPermission.EvaluatePlan].join('.'),
			[nameof<Plan>(x => x.authorizationFlags), AppPermission.InvitePlanUsers].join('.'),
			[nameof<Plan>(x => x.authorizationFlags), AppPermission.AssignPlanUsers].join('.'),
			[nameof<Plan>(x => x.authorizationFlags), AppPermission.EditPlan].join('.'),
			[nameof<Plan>(x => x.authorizationFlags), AppPermission.DepositPlan].join('.'),
			[nameof<Plan>(x => x.statusAuthorizationFlags), PlanStatusPermission.Edit].join('.'),
			[nameof<Plan>(x => x.entityDois), nameof<EntityDoi>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.entityDois), nameof<EntityDoi>(x => x.repositoryId)].join('.'),
			[nameof<Plan>(x => x.entityDois), nameof<EntityDoi>(x => x.doi)].join('.'),
			[nameof<Plan>(x => x.entityDois), nameof<EntityDoi>(x => x.isActive)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.label)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.name)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.internalStatus)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.statusColor)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.isActive)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.sectionId)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.id)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.name)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.role)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.plan.id)].join('.'),
			[nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.isActive)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.label)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.source)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.reference)].join('.'),
			[nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.isActive)].join('.'),
			[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.sectionId)].join('.'),
			[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.descriptionTemplateGroupId)].join('.'),
			[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.isActive)].join('.'),

			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.label)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.hasTemplates)].join('.'),
			[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.groupId)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.sectionId)].join('.'),
			[nameof<Plan>(x => x.descriptions), nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.groupId)].join('.'),

			[nameof<Plan>(x => x.otherPlanVersions), nameof<Plan>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.otherPlanVersions), nameof<Plan>(x => x.groupId)].join('.'),
			[nameof<Plan>(x => x.otherPlanVersions), nameof<Plan>(x => x.version)].join('.'),
			[nameof<Plan>(x => x.otherPlanVersions), nameof<Plan>(x => x.isActive)].join('.'),

			[nameof<Plan>(x => x.availableStatuses), nameof<PlanStatus>(x => x.id)].join('.'),
			[nameof<Plan>(x => x.availableStatuses), nameof<PlanStatus>(x => x.name)].join('.'),
			[nameof<Plan>(x => x.availableStatuses), nameof<PlanStatus>(x => x.internalStatus)].join('.'),
			[nameof<Plan>(x => x.availableStatuses), nameof<PlanStatus>(x => x.action)].join('.'),
			[nameof<Plan>(x => x.availableStatuses), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.matIconName)].join('.'),
			[nameof<Plan>(x => x.availableStatuses), nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),

			nameof<Plan>(x => x.hash),
		]
	}
}
