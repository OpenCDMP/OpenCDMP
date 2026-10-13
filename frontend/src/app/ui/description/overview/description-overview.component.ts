import { Location, NgTemplateOutlet, NgClass, UpperCasePipe, LowerCasePipe } from '@angular/common';
import { Component, effect, inject, OnInit, signal, ChangeDetectionStrategy } from '@angular/core';
import { TourStateService } from '@app/library/guided-tour/tour-state.service';
import { descriptionOverviewPageTour } from './description-overview.tour';
import { UntypedFormBuilder, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { ActivatedRoute, Params, Router, RouterLink } from '@angular/router';
import { DescriptionStatusEnum } from '@app/core/common/enum/description-status';
import { DescriptionStatusAvailableActionType } from '@app/core/common/enum/description-status-available-action-type';
import { DescriptionStatusPermission } from '@app/core/common/enum/description-status-permission.enum';
import { EvaluatorEntityType } from '@app/core/common/enum/evaluator-entity-type';
import { FileTransformerEntityType } from '@app/core/common/enum/file-transformer-entity-type';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { PlanAccessType } from '@app/core/common/enum/plan-access-type';
import { PlanStatusEnum } from '@app/core/common/enum/plan-status';
import { PlanUserRole } from '@app/core/common/enum/plan-user-role';
import { DescriptionStatus, DescriptionStatusDefinition } from '@app/core/model/description-status/description-status';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { BaseDescription, Description, DescriptionReference, DescriptionReferenceData, DescriptionTag } from '@app/core/model/description/description';
import { RankResultModel } from '@app/core/model/evaluator/evaluator-plan-model.model';
import { PlanBlueprint, PlanBlueprintDefinition, RootSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import {PlanStatus} from '@app/core/model/plan-status/plan-status';
import { Plan, PlanAuthorizationInformation, PlanDescriptionTemplate, PlanUser, PlanUserRemovePersist } from '@app/core/model/plan/plan';
import { PlanReference } from '@app/core/model/plan/plan-reference';
import { ReferenceType, ReferenceTypeDefinition } from '@app/core/model/reference-type/reference-type';
import { Reference } from '@app/core/model/reference/reference';
import { User } from '@app/core/model/user/user';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { DescriptionService } from '@app/core/services/description/description.service';
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
import { FileUtils } from '@app/core/services/utilities/file-utils.service';
import { PopupNotificationDialogComponent } from '@app/library/notification/popup/popup-notification.component';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseComponent } from '@common/base/base.component';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { map, takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { CopyDialogReturnParams, DescriptionCopyDialogComponent } from '../description-copy-dialog/description-copy-dialog.component';
import { BenchmarkConfiguration, EvaluatorConfiguration } from '@app/core/model/evaluator/evaluator-configuration';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { StorageFileService } from '@app/core/services/storage-file/storage-file.service';
import { EntityType } from '@app/core/common/enum/entity-type';
import { Evaluation } from '@app/core/model/evaluation/evaluation';
import { EvaluationService } from '@app/core/services/evaluation/evaluation.service';
import { PluginEntityType } from '@app/core/common/enum/plugin-entity-type';
import { EvaluateDialogComponent } from '@app/ui/evaluation/evaluate-dialog/evaluate-dialog.component';
import { RankConfig } from '@app/core/model/evaluator/rank-config';
import { BenchmarkDialogComponent, BenchmarkDialogResult } from '@app/ui/evaluation/benchmark-dialog/benchmark-dialog.component';
import { DescriptionEditorEntityResolver } from '../editor/resolvers/description-editor-entity.resolver';
import { PlanEditorEntityResolver } from '@app/ui/plan/plan-editor-blueprint/resolvers/plan-editor-enitity.resolver';
import { IconConfiguration } from '@app/core/model/icon-configuration/icon-configuration';
import { ExternalFetcherBaseSourceConfiguration } from '@app/core/model/external-fetcher/external-fetcher';
import { DescriptionTemplateType } from '@app/core/model/description-template-type/description-template-type';
import { Tag } from '@app/core/model/tag/tag';
import { MatCard, MatCardHeader, MatCardTitle, MatCardContent } from '@angular/material/card';
import { DescriptionPreviewComponent } from './description-preview/description-preview.component';
import { MatDivider } from '@angular/material/list';
import { MatButton, MatIconButton } from '@angular/material/button';
import { PlanEvaluationHistoryComponent } from '../../plan/listing/plan-evaluation-history/plan-evaluation-history.component';
import { MatIcon } from '@angular/material/icon';
import { MatMenuTrigger, MatMenu, MatMenuItem } from '@angular/material/menu';
import { MatTooltip } from '@angular/material/tooltip';
import { DateTimeFormatPipe } from '../../../core/pipes/date-time-format.pipe';


@Component({
    selector: 'app-description-overview',
    templateUrl: './description-overview.component.html',
    styleUrls: ['./description-overview.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [NgTemplateOutlet, MatCard, MatCardHeader, MatCardTitle, MatCardContent, RouterLink, DescriptionPreviewComponent, MatDivider, MatButton, PlanEvaluationHistoryComponent, MatIcon, MatIconButton, MatMenuTrigger, MatMenu, MatMenuItem, NgClass, MatTooltip, UpperCasePipe, LowerCasePipe, TranslatePipe, DateTimeFormatPipe]
})
export class DescriptionOverviewComponent extends BaseComponent implements OnInit {
	isDraft:boolean = false;
	description: Description;
	researchers: PlanReference[] = [];
	isNew = true;
	isFinalized = false;
	isPublicView = true;

	// "What's new" tour for the 2.0.0 asset overview (private view only), see plan-overview for the pattern.
	private readonly tourState = inject(TourStateService);
	private readonly pageTour = descriptionOverviewPageTour(inject(TranslateService));

	override ngOnDestroy(): void {
		this.tourState.unregisterPageTour(this.pageTour);
		super.ngOnDestroy();
	}
	hasPublishButton: boolean = true;
	expand = false;
	isLocked: boolean;
	descriptionStatusEnum = DescriptionStatusEnum;
	planAccessTypeEnum = PlanAccessType;
	planStatusEnum = PlanStatusEnum;
	planUserRoleEnum = PlanUserRole;
	fileTransformerEntityTypeEnum = FileTransformerEntityType;
	evaluatorEntityTypeEnum = EvaluatorEntityType;
	
	evaluatorRepos: EvaluatorConfiguration[] = [];
	logos: Map<string, SafeResourceUrl> = new Map<string, SafeResourceUrl>();
	statuslogos: Map<Guid, SafeResourceUrl> = new Map<Guid, SafeResourceUrl>();


	canEdit = false;
	canCopy = false;
	canDelete = false;
	canFinalize = false;
	canAnnotate = false;
	canInvitePlanUsers = false;
	canEvaluate = false;
    descriptionEvaluations: Evaluation[];
	showEvaluationHistory: boolean = true;

	get canAssignPlanUsers(): boolean {
		const authorizationFlags = !this.isPublicView ? (this.description?.plan as Plan)?.authorizationInformation?.globalAuthorizationFlags : [];
		return (authorizationFlags?.some(x => x === AppPermission.InvitePlanUsers) || this.authentication.hasPermission(AppPermission.InvitePlanUsers)) &&
			!this.isPublicView && this.description?.belongsToCurrentTenant && this.isActive && (this.description?.plan?.status?.internalStatus == null || this.description?.plan?.status?.internalStatus != PlanStatusEnum.Finalized);
	}

	authorFocus: string;
	userName: string;

    RESARCHER_PAGE_SIZE = 8;
    showMoreResearchers = signal<boolean>(false);
    toggleShowMoreResearchers(){
        this.showMoreResearchers.update((value) => !value);
    }
    
    private DESCRIPTION_PAGE_SIZE = 50;
    minimizedDescription: string;
    showLongDescription = signal<boolean>(true);
    toggleShowDescription(){
        this.showLongDescription.update((value) => !value);
    }

	constructor(
		private route: ActivatedRoute,
		private routerUtils: RouterUtilsService,
		private router: Router,
		private descriptionService: DescriptionService,
		private authentication: AuthService,
		private dialog: MatDialog,
		private language: TranslateService,
		private uiNotificationService: UiNotificationService,
		private configurationService: ConfigurationService,
		private planService: PlanService,
		public referenceService: ReferenceService,
		private location: Location,
		public enumUtils: EnumUtils,
		private fileUtils: FileUtils,
		private authService: AuthService,
		public fileTransformerService: FileTransformerService,
		private referenceTypeService: ReferenceTypeService,
		private fb: UntypedFormBuilder,
		private lockService: LockService,
		private analyticsService: AnalyticsService,
		private breadcrumbService: BreadcrumbService,
		private httpErrorHandlingService: HttpErrorHandlingService,
		private userService: UserService,
		private evaluatorService: EvaluatorService,
		private logger: LoggingService,
		private sanitizer: DomSanitizer,
		private storageFileService: StorageFileService,
        private evaluationService: EvaluationService
	) {
		super();
		effect(() => {
			if(this.evaluatorService.availableEvaluators()){
				this.getEvaluators();
			}
		})
	}

	ngOnInit() {
		// No "what's new" tour on the public, unauthenticated view — same rule as the listing pages.
		if (!this.route.snapshot.data['isPublic']) {
			this.tourState.registerPageTour(this.pageTour);
		}
		this.analyticsService.trackPageView(AnalyticsService.DescriptionOverview);

		this.canDelete = false;
		this.canEdit = false;
		this.canCopy = false;
		this.canFinalize = false;
		this.canInvitePlanUsers = false;
		this.canEvaluate = false;
		// Gets description data using parameter id
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
            this.descriptionService.getSingle(itemId, this.lookupFields())
                .pipe(takeUntil(this._destroyed))
                .subscribe(
                    {
                        next: (data) => {
                            this.breadcrumbService.addIdResolvedValue(data.id.toString(), data.label);
                            if(data.plan?.id){
                                this.breadcrumbService.addIdResolvedValue(data.plan.id.toString(), data.plan.label);
                            }
                            this.description = data;
                            this.tourState.startIfUnseen(this.pageTour);
                            if (this.description.status?.internalStatus == DescriptionStatusEnum.Draft) {
                                this.isDraft = true;
                            }
                            if(this.description.plan?.planUsers){
                                this.description.plan.planUsers = this.isActive || this.description.plan.isActive === IsActive.Active ? data.plan.planUsers?.filter(x => x.isActive === IsActive.Active) : data.plan.planUsers;
                            }
                            this.researchers = this.referenceService.getReferencesForTypes(this.description?.plan?.planReferences, this.isActive, [this.referenceTypeService.getResearcherReferenceType()]);
                            this.checkLockStatus(this.description.id);
                            this.canDelete = this.isActive && (this.authService.hasPermission(AppPermission.DeleteDescription) ||
                                this.description.authorizationFlags?.some(x => x === AppPermission.DeleteDescription)) && this.description.belongsToCurrentTenant != false;

                            this.canEdit = this.isActive && (this.authService.hasPermission(AppPermission.EditDescription) ||
                                this.description.authorizationFlags?.some(x => x === AppPermission.EditDescription)) && this.description.belongsToCurrentTenant != false;

                            this.canCopy = this.isActive && (this.authService.hasPermission(AppPermission.CloneDescription) || (this.authService.hasPermission(AppPermission.PublicCloneDescription) && this.isPublicView));

                            this.canAnnotate = this.isActive && (this.authService.hasPermission(AppPermission.AnnotateDescription) ||
                                this.description.authorizationFlags?.some(x => x === AppPermission.AnnotateDescription)) && this.description.belongsToCurrentTenant != false;

                            this.canFinalize = this.isActive && (this.authService.hasPermission(AppPermission.FinalizeDescription) ||
                                this.description.authorizationFlags?.some(x => x === AppPermission.FinalizeDescription)) && this.description.belongsToCurrentTenant != false;

                            this.canInvitePlanUsers = this.isActive && (this.authService.hasPermission(AppPermission.InvitePlanUsers) ||
                                this.description.authorizationFlags?.some(x => x === AppPermission.InvitePlanUsers)) && this.description.belongsToCurrentTenant != false;

                            this.canEvaluate = this.isActive && (this.authService.hasPermission(AppPermission.EvaluateDescription) ||
                                this.description.authorizationFlags?.some(x => x === AppPermission.EvaluateDescription)) && this.description.belongsToCurrentTenant != false;

                            this.loadStatusLogo();
                            this.getEvaluations(this.description.id);
                            // this.loadReferenceData();

                            if(this.description.description && this.description.description.split(' ')?.length > this.DESCRIPTION_PAGE_SIZE) {
                                this.minimizedDescription = this.description.description.split(' ').slice(0, this.DESCRIPTION_PAGE_SIZE).join(' ') + '...';
                                this.showLongDescription.set(false);
                            } else {
                                this.minimizedDescription = null;
                                this.showLongDescription.set(true);
                            }
                        },
                        error: (error: any) => {
                            this.httpErrorHandlingService.handleBackedRequestError(error);

                            if (error.status === 404) {
                                return this.onFetchingDeletedCallbackError('/assets/');
                            }
                            if (error.status === 403) {
                                return this.onFetchingForbiddenCallbackError('/assets/');
                            }
                        }
                    });
        }
        else if (publicId != null) {
            this.isNew = false;
            this.isFinalized = true;
            this.isPublicView = true;
            this.descriptionService.getPublicSingle(publicId, this.lookupFields())
                .pipe(takeUntil(this._destroyed))
                .subscribe({
                    next: (data) => {
                        this.canCopy = this.authService.hasPermission(AppPermission.PublicCloneDescription) && this.isPublicView;

                        this.breadcrumbService.addExcludedParam('public', true);
                        this.breadcrumbService.addIdResolvedValue(data.id.toString(), data.label);

                        this.description = data;
                        if (this.description.status?.internalStatus == DescriptionStatusEnum.Draft) {
                            this.isDraft = true;
                        }
                        this.description.plan.planUsers = this.isActive || this.description.plan.isActive === IsActive.Active ? data.plan.planUsers?.filter(x => x.isActive === IsActive.Active) : data.plan.planUsers;
                        this.researchers = this.referenceService.getReferencesForTypes(this.description?.plan?.planReferences, this.isActive [this.referenceTypeService.getResearcherReferenceType()]);

                        if(this.description.description && this.description.description.split(' ')?.length > this.DESCRIPTION_PAGE_SIZE) {
                            this.minimizedDescription = this.description.description.split(' ').slice(0, this.DESCRIPTION_PAGE_SIZE).join(' ') + '...';
                            this.showLongDescription.set(false);
                        } else {
                            this.minimizedDescription = null;
                            this.showLongDescription.set(true);
                        }
                        // this.loadReferenceData();
                    },
                    error: (error: any) => {
                        this.httpErrorHandlingService.handleBackedRequestError(error);

                        if (error.status === 404) {
                            return this.onFetchingDeletedCallbackError('/explore-assets');
                        }
                        if (error.status === 403) {
                            return this.onFetchingForbiddenCallbackError('/explore-assets');
                        }
                    }
                });
        }
    }

    getEvaluators() {
		if (this.isAuthenticated()) {

			this.evaluatorRepos = this.evaluatorService.availableEvaluatorsFor(EvaluatorEntityType.Description);
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
			}

			this.userService.getSingle(this.authentication.userId(), [
				nameof<User>(x => x.id),
				nameof<User>(x => x.name)])
				.pipe(map(u => u.name)).subscribe(name => this.userName = name);
		} else {
			this.userName = '';
		}
    }

	get isActive(): boolean {
		return this.description && this.description.isActive != IsActive.Inactive;
	}

	get canExport(): boolean {
		if (!this.isAuthenticated()) return false;
		return this.isActive && this.description?.status?.definition?.availableActions?.filter(x => x === DescriptionStatusAvailableActionType.Export).length > 0;
	}

	get canEditStatus(): boolean {
		return this.isActive && (this.description as Description).statusAuthorizationFlags?.some(x => x.toLowerCase() === DescriptionStatusPermission.Edit.toLowerCase())
	}

    get planOwners(): string {
        return this.description.plan?.planUsers?.filter(x => x.isActive === IsActive.Active && x.role === PlanUserRole.Owner)?.map(x => x.user?.name).join(', ');
    }

    get collaboratorCount(): number {
        return this.description.plan?.planUsers?.filter(x => x.isActive === IsActive.Active)?.length;
    }

	hasAvailableFinalizeStatus() {
		return (this.description as Description).availableStatuses?.find(x => x.internalStatus === DescriptionStatusEnum.Finalized) != null;
	}

    getEvaluations(id: Guid) {
        this.evaluationService.query(this.evaluationService.buildEvaluationLookup({
            entityIds: [id],
            entityTypes: [EntityType.Description]
        }))
        .pipe(takeUntil(this._destroyed), map((res) => res.items))
        .subscribe({
            next: (result: Evaluation[]) => {
                this.descriptionEvaluations = result;
            },
            error: (error: any) => this.httpErrorHandlingService.handleBackedRequestError(error)
        })
    }

	onEvaluateDescription(descriptionId: Guid, evaluatorId: string, format: string, rankConfig: RankConfig, availableBenchmarks: BenchmarkConfiguration[] = [], availableTests: BenchmarkConfiguration[] = [], isPublicView: boolean) {
		
		availableBenchmarks = availableBenchmarks?.filter(x => x.appliesTo.includes(PluginEntityType.Description)) || [];
		availableTests = availableTests?.filter(x => x.appliesTo.includes(PluginEntityType.Description)) || [];
		
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
					this.rankDescription(descriptionId, evaluatorId, format, rankConfig, benchmarkIds, testIds);
				}
			});
		} else if (availableBenchmarks?.length == 1 && !(availableTests?.length > 0)){
			this.rankDescription(descriptionId, evaluatorId, format, rankConfig, [availableBenchmarks[0].id], null)
		} else if (availableTests?.length == 1 && !(availableBenchmarks?.length > 0)){
			this.rankDescription(descriptionId, evaluatorId, format, rankConfig, null, [availableTests[0].id])
		} else {
			this.rankDescription(descriptionId, evaluatorId, format, rankConfig, null, null)
		}
		
	}

	private rankDescription(descriptionId: Guid, evaluatorId: string, format: string, rankConfig: RankConfig, benchmarkIds: string[] | null, testIds: string[] | null) {
		this.evaluatorService.rankDescription(descriptionId, evaluatorId, format, benchmarkIds ?? undefined, testIds ?? undefined).subscribe(
			(response: RankResultModel[]) => {
				const dialogRef = this.dialog.open(EvaluateDialogComponent, {
					maxWidth: '90vw',
					data: {
						rankData: response,
						rankConfig: rankConfig,
                        evaluatorId
					}
				});

				dialogRef.afterClosed().subscribe(result => {
					this.logger.debug("Dialog closed with result:", result);
                    this.getEvaluations(descriptionId);
				});
			},
			error => {
				this.logger.error("Error ranking description:", error);
			}
		);
	}

	checkLockStatus(id: Guid) {
		this.lockService.checkLockStatus(id).pipe(takeUntil(this._destroyed))
			.subscribe({
				next: (lockStatus) => {
					this.isLocked = lockStatus.status;
					if (this.isLocked) {
						this.dialog.open(PopupNotificationDialogComponent, {
							data: {
								title: this.language.instant('DESCRIPTION-OVERVIEW.LOCKED-DIALOG.TITLE'),
								message: this.language.instant('DESCRIPTION-OVERVIEW.LOCKED-DIALOG.MESSAGE')
							}, maxWidth: '30em'
						});
					}
				},
				error: (error) => {
					this.router.navigate([this.routerUtils.generateUrl('/assets')]);
					this.httpErrorHandlingService.handleBackedRequestError(error);
				}
			});
	}

	onFetchingDeletedCallbackError(redirectRoot: string) {
		this.router.navigate([this.routerUtils.generateUrl(redirectRoot)]);
	}

	onFetchingForbiddenCallbackError(redirectRoot: string) {
		this.router.navigate([this.routerUtils.generateUrl(redirectRoot)]);
	}

	goBack(): void {
		this.location.back();
	}

	reloadPage(): void {
        this.getItem({
            itemId: this.isPublicView ? null : this.description.id,
            publicId: this.isPublicView ? this.description.id : null,
        });
        this.getEvaluators();
	}


	isUserAuthor(userId: Guid): boolean {
		if (this.isAuthenticated()) {
			const principalId: Guid = this.authentication.userId();
			return this.userName && (userId === principalId);
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

	// openShareDialog() {
	// 	const dialogRef = this.dialog.open(PlanInvitationDialogComponent, {
	// 		restoreFocus: false,
	// 		data: {
	// 			planId: this.description.plan.id,
	// 			planName: this.description.plan.label,
	// 			blueprint: this.isPublicView ? null : (this.description?.plan as Plan)?.blueprint,
	// 		},
    //         minWidth: 'min(65rem, 90vw)'
	// 	});
	// }

	public isAuthenticated(): boolean {
		return this.authentication.currentAccountIsAuthenticated();
	}

	isNotFinalizedDescription(description: Description) {
		return description?.status?.internalStatus != DescriptionStatusEnum.Finalized;
	}

	isNotFinalizedPlan(description: Description) {
		return description?.plan?.status?.internalStatus != PlanStatusEnum.Finalized;
	}

	editClicked() {
        if(!this.description.plan?.id) { return; }
		this.router.navigate([
            this.routerUtils.generateUrl(['/plans/edit/' +  this.description.plan.id.toString()])],
            {queryParams: 
                {'descriptionId': this.description.id}
            }
        );
	}

	deleteClicked() {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			maxWidth: '300px',
			data: {
				message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-ITEM'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.DELETE'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
				isDeleteConfirmation: true
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result) {
				this.descriptionService.delete(this.description.id)
					.pipe(takeUntil(this._destroyed))
					.subscribe({
						complete: () => this.onDeleteCallbackSuccess(),
						error: (error) => this.onDeleteCallbackError(error)
					});
			}
		});
	}

	planRoute(plan: Plan): string {
		if (this.isPublicView) {
			return this.routerUtils.generateUrl(['/explore-plans/overview/public/', plan.id.toString()]);
		} else {
			return this.routerUtils.generateUrl(['/plans/overview/', plan.id.toString()]);
		}
	}

	private onDeleteCallbackSuccess(): void {
		this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-DELETE'), SnackBarNotificationLevel.Success);
		this.router.navigate([this.routerUtils.generateUrl('/assets')]);
	}

	private onDeleteCallbackError(error) {
		this.uiNotificationService.snackBarNotification(error.error.message ? error.error.message : this.language.instant('GENERAL.SNACK-BAR.UNSUCCESSFUL-DELETE'), SnackBarNotificationLevel.Error);
	}

	private onUpdateCallbackSuccess(): void {
		this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
		this.reloadPage();
	}

	private onUpdateCallbackError(error) {
		this.httpErrorHandlingService.handleBackedRequestError(error);
	}

	private tryTranslate(errorMessage: string): string {
		return errorMessage.replace('Field value of', this.language.instant('Field value of'))
			.replace('must be filled', this.language.instant('must be filled'));
	}

	// getSectionNameById(sectionId: Guid): string {
	// 	if (sectionId == null) return '';

	// 	const blueprint = this.isPublicView ? null : (this.description?.plan as Plan)?.blueprint;
        
	// 	let sections: PlanBlueprintDefinitionSection[] = blueprint?.definition?.sections?.filter((section: PlanBlueprintDefinitionSection) => sectionId === section.id);

	// 	return sections == null ? '' : sections[0].label;
	// }

	openCopyToPlanDialog() {
		const formGroup = this.fb.group({
			planId: this.fb.control(null, Validators.required),
			sectionId: this.fb.control(null, Validators.required),
		})
		const dialogRef = this.dialog.open<DescriptionCopyDialogComponent, any, CopyDialogReturnParams>(DescriptionCopyDialogComponent, {
			width: '500px',
			data: {
				formGroup: formGroup,
				description: this.description,
				descriptionTemplate: this.description.descriptionTemplate,
				planDescriptionTemplate: this.description.planDescriptionTemplate,
				descriptionProfileExist: false,
				confirmButton: this.language.instant('DESCRIPTION-OVERVIEW.COPY-DIALOG.COPY'),
				cancelButton: this.language.instant('DESCRIPTION-OVERVIEW.COPY-DIALOG.CANCEL')
			}
		});

		dialogRef.afterClosed().pipe(takeUntil(this._destroyed))
			.subscribe(result => {
				if (result) {
					this.router.navigate(
                        [this.routerUtils.generateUrl(['plans/edit', result.planId.toString()], '/')],
                        {queryParams: {
                            'descriptionCopyId': this.description.id,
                            'sectionId': result.sectionId
                        }}
                    );
				}
			});
	}

	removeUserFromPlanDialog(planUser: PlanUser) {
		let dialogRef;
		if (planUser?.user?.id == this.authentication.userId() && !this.description?.plan?.planUsers?.find(x => x.id != planUser.id && x.user?.id === planUser?.user?.id)) {
			dialogRef = this.dialog.open(ConfirmationDialogComponent, {
				data: {
					message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-YOURSELF-FROM-ENTIRE-PLAN'),
					confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.REMOVE'),
					cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
					isDeleteConfirmation: false
				}
			});
		} else {
			dialogRef = this.dialog.open(ConfirmationDialogComponent, {
				data: {
					message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-USER'),
					confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.REMOVE'),
					cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
					isDeleteConfirmation: false
				}
			});
		}

		dialogRef?.afterClosed()?.subscribe(result => {
			if (result) {
				this.removeUserFromPlan(planUser);
			}
		});
	}

	removeUserFromPlan(planUser: PlanUser) {
		const planUserRemovePersist: PlanUserRemovePersist = {
			id: planUser.id,
			planId: this.description.plan.id,
			role: planUser.role
		};

		this.planService.removeUser(planUserRemovePersist, PlanEditorEntityResolver.lookupFields()).pipe(takeUntil(this._destroyed))
			.subscribe({
				next: (plan) => {
					if ((plan?.planUsers?.find(x => x?.user?.id == planUser?.user?.id && x.isActive === IsActive.Active)) || (plan?.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.AssignPlanUsers) || this.authentication.hasPermission(AppPermission.AssignPlanUsers))) {
						this.onUpdateCallbackSuccess();
					} else {
						this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
						this.router.navigate([this.routerUtils.generateUrl('/assets')]);
					}
				},
				error: (error) => {
					this.onUpdateCallbackError(error);
				}
			});
	}
    
    private loadStatusLogo(){
		const availableStatus = [...(this.description as Description).availableStatuses, this.description.status];
		if (availableStatus?.length > 0) {
			availableStatus.forEach(x => {
				if (x.definition?.iconConfiguration?.storageFile?.id) {
					this.storageFileService.download(x.definition?.iconConfiguration?.storageFile?.id).pipe(takeUntil(this._destroyed))
					.subscribe(response => {
						this.statuslogos.set(x.definition?.iconConfiguration?.storageFile?.id,this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(response.body)));
					});		
				}
			})
		}			
		return 	
	}

    private lookupFields(): string[] {
		return [
            ...DescriptionEditorEntityResolver.descriptionLookupFields(),
            ...DescriptionEditorEntityResolver.descriptionTemplateLookupFieldsForDescription(),
			nameof<BaseDescription>(x => x.isActive),
			nameof<Description>(x => x.id),
			nameof<Description>(x => x.label),
			nameof<Description>(x => x.description),
			[nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.id)].join('.'),
			[nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.name)].join('.'),
			[nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.internalStatus)].join('.'),
			[nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.availableActions)].join('.'),
			[nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.matIconName)].join('.'),
			[nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.statusColor)].join('.'),
			[nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),
			[nameof<Description>(x => x.descriptionTags), nameof<DescriptionTag>(x => x.tag), nameof<Tag>(x => x.id)].join('.'),
			[nameof<Description>(x => x.descriptionTags), nameof<DescriptionTag>(x => x.tag), nameof<Tag>(x => x.label)].join('.'),

			nameof<Description>(x => x.updatedAt),
			nameof<Description>(x => x.belongsToCurrentTenant),
			nameof<Description>(x => x.hash),

			[nameof<Description>(x => x.authorizationFlags), AppPermission.EditDescription].join('.'),
			[nameof<Description>(x => x.authorizationFlags), AppPermission.DeleteDescription].join('.'),
			[nameof<Description>(x => x.authorizationFlags), AppPermission.FinalizeDescription].join('.'),
			[nameof<Description>(x => x.authorizationFlags), AppPermission.InvitePlanUsers].join('.'),
			[nameof<Description>(x => x.authorizationFlags), AppPermission.AnnotateDescription].join('.'),
			[nameof<Description>(x => x.authorizationFlags), AppPermission.EvaluateDescription].join('.'),

			[nameof<Description>(x => x.statusAuthorizationFlags), DescriptionStatusPermission.Edit].join('.'),
			[nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.id)].join('.'),
			[nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.plan), nameof<Plan>(x => x.id)].join('.'),
			[nameof<Description>(x => x.planDescriptionTemplate), nameof<PlanDescriptionTemplate>(x => x.sectionId)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.label)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.accessType)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.isActive)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.name)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.internalStatus)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.authorizationInformation), nameof<PlanAuthorizationInformation>(x => x.globalAuthorizationFlags), AppPermission.InvitePlanUsers].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.label)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.title)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.sections)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.hasTemplates)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.sectionId)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.name)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.role)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.isActive)].join('.'),
            [nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.id)].join('.'),
            [nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.reference), nameof<Reference>(x => x.id)].join('.'),
            [nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.reference), nameof<Reference>(x => x.label)].join('.'),
            [nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.reference), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.id)].join('.'),
            [nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.reference), nameof<Reference>(x => x.reference)].join('.'),
            [nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.isActive)].join('.'),
            [nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.data), nameof<DescriptionReferenceData>(x => x.fieldId)].join('.'),
            [nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.data), nameof<DescriptionReferenceData>(x => x.ordinal)].join('.'),
            [nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.reference), nameof<Reference>(x => x.source)].join('.'),
            [nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.reference), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.key)].join('.'),
            [nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.reference), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),
            [nameof<Description>(x => x.descriptionReferences), nameof<DescriptionReference>(x => x.reference), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.tooltipMessage)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.label)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.source)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.reference)].join('.'),
            // [nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.navigationUrl)].join('.'),
            // [nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),
            // [nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.tooltipMessage)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.isActive)].join('.'),
            [nameof<Description>(x => x.plan), nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.statusColor)].join('.'),

			[nameof<Description>(x => x.availableStatuses), nameof<DescriptionStatus>(x => x.id)].join('.'),
			[nameof<Description>(x => x.availableStatuses), nameof<DescriptionStatus>(x => x.name)].join('.'),
			[nameof<Description>(x => x.availableStatuses), nameof<DescriptionStatus>(x => x.internalStatus)].join('.'),
			[nameof<Description>(x => x.availableStatuses), nameof<DescriptionStatus>(x => x.action)].join('.'),
			[nameof<Description>(x => x.availableStatuses), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.matIconName)].join('.'),
			[nameof<Description>(x => x.availableStatuses), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),
			[nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>(x => x.name)].join('.'),

		]
	}

    /** @deprecated not in use anymore, change description status in editor*/
	// persistStatus(status: DescriptionStatus, description: Description) {
	// 	if (status.internalStatus != null && status.internalStatus === DescriptionStatusEnum.Finalized) {
	// 		this.finalize(description, status.id);
	// 	} else if (status.internalStatus != null && description.status.internalStatus === DescriptionStatusEnum.Finalized) {
	// 		this.reverseFinalization(description, status.id);
	// 	} else {
	// 		// other statuses
	// 		const descriptionStatusPersist: DescriptionStatusPersist = {
	// 			id: description.id,
	// 			statusId: status.id,
	// 			hash: description.hash
	// 		};
	// 		this.descriptionService.persistStatus(descriptionStatusPersist).pipe(takeUntil(this._destroyed))
	// 			.subscribe({
	// 				next: () => {
	// 					this.reloadPage();
	// 					this.onUpdateCallbackSuccess()
	// 				},
	// 				error: (error: any) => this.onUpdateCallbackError(error)
	// 			})
	// 	}
	// }

	// finalize(description: Description, statusId: Guid) {

	// 	this.descriptionService.validate([description.id]).pipe(takeUntil(this._destroyed))
	// 		.subscribe({
	// 			next: (result) => {
	// 				if (result[0].result == DescriptionValidationOutput.Invalid) {
	// 					this.router.navigate([this.routerUtils.generateUrl(['plans/edit', description.plan.id.toString(), 'd', description.id.toString(), 'finalize'], '/')]);
	// 				} else {
	// 					const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
	// 						restoreFocus: false,
	// 						data: {
	// 							message: this.language.instant('DESCRIPTION-OVERVIEW.FINALIZE-DIALOG.TITLE'),
	// 							confirmButton: this.language.instant('DESCRIPTION-OVERVIEW.FINALIZE-DIALOG.CONFIRM'),
	// 							cancelButton: this.language.instant('DESCRIPTION-OVERVIEW.FINALIZE-DIALOG.NEGATIVE'),
	// 							isDeleteConfirmation: false
	// 						}
	// 					});
	// 					dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe({
	// 						next: (result) => {
	// 							if (result) {
	// 								const descriptionStatusPersist: DescriptionStatusPersist = {
	// 									id: description.id,
	// 									statusId: statusId,
	// 									hash: description.hash
	// 								};
	// 								this.descriptionService.persistStatus(descriptionStatusPersist).pipe(takeUntil(this._destroyed))
	// 									.subscribe({
	// 										next: () => {
	// 											this.reloadPage();
	// 											this.onUpdateCallbackSuccess()
	// 										},
	// 										error: (error: any) => this.onUpdateCallbackError(error)

	// 									})
	// 							}
	// 						},
	// 						error: (error) => this.httpErrorHandlingService.handleBackedRequestError(error)
	// 					})
	// 				}
	// 			},
	// 			error: (error) => this.httpErrorHandlingService.handleBackedRequestError(error)
	// 		});

	// }

	// hasReversableStatus(description: Description): boolean {
	// 	return description.plan.status.internalStatus == PlanStatusEnum.Draft && description?.status?.internalStatus == DescriptionStatusEnum.Finalized && this.canFinalize && (this.description as Description).availableStatuses?.find(x => x.internalStatus === DescriptionStatusEnum.Draft) != null
	// }

	// reverseFinalization(description: Description, statusId: Guid) {
	// 	const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
	// 		restoreFocus: false,
	// 		data: {
	// 			message: this.language.instant('DESCRIPTION-OVERVIEW.UNDO-FINALIZATION-DIALOG.TITLE'),
	// 			confirmButton: this.language.instant('DESCRIPTION-OVERVIEW.UNDO-FINALIZATION-DIALOG.CONFIRM'),
	// 			cancelButton: this.language.instant('DESCRIPTION-OVERVIEW.UNDO-FINALIZATION-DIALOG.NEGATIVE'),
	// 			isDeleteConfirmation: false
	// 		}
	// 	});
	// 	dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
	// 		if (result) {
	// 			const planUserRemovePersist: DescriptionStatusPersist = {
	// 				id: description.id,
	// 				statusId: statusId,
	// 				hash: description.hash
	// 			};
	// 			this.descriptionService.persistStatus(planUserRemovePersist).pipe(takeUntil(this._destroyed))
	// 				.subscribe({
	// 					next: (data) => {
	// 						this.reloadPage();
	// 						this.onUpdateCallbackSuccess()
	// 					},
	// 					error: (error: any) => {
	// 						this.onUpdateCallbackError(error)
	// 					}
	// 				});
	// 		}
	// 	});
		
	// }


    
    // referenceLogos:  Map<Guid, SafeResourceUrl>;
    // referenceNavigationUrls: Map<Guid, string>;
    // referenceTooltips: Map<Guid, string>;
    // private loadReferenceData(){
    //     this.referenceLogos = new Map<Guid, SafeResourceUrl>([]);
    //     this.referenceNavigationUrls = new Map<Guid, string>([]);
    //     this.referenceTooltips = new Map<Guid, string>([]);

    //     if(!this.description.plan?.planReferences?.length){ return; }
    //     const tempStorageFileIdSet = new Set([]);

    //     this.description.plan.planReferences.forEach((ref) => {
    //         const sourceDefinition = ref.reference.type.definition?.sources?.find(x => x.key == ref.reference.source);
    //         if(!sourceDefinition){ return; }
    //         const navigationUrl = sourceDefinition?.navigationUrl;
    //         this.referenceNavigationUrls.set(ref.id, navigationUrl);
    //         const tooltip = sourceDefinition?.tooltipMessage;
    //         this.referenceTooltips.set(ref.id, tooltip);
    //         const storageFileId = sourceDefinition?.storageFile?.id;
    //         if(storageFileId && !tempStorageFileIdSet.has(storageFileId)){
    //             tempStorageFileIdSet.add(storageFileId);
    //             this.storageFileService.download(storageFileId).pipe(takeUntil(this._destroyed))
    //             .subscribe(response => {
    //                 this.referenceLogos.set(storageFileId,this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(response.body)));
    //             });
    //         }
    //     })
    // }
}
