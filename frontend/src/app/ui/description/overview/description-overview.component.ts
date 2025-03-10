import { Location } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { UntypedFormBuilder, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { ActivatedRoute, Params, Router } from '@angular/router';
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
import { BaseDescription, Description, DescriptionStatusPersist, PublicDescription } from '@app/core/model/description/description';
import { RankModel } from '@app/core/model/evaluator/evaluator-plan-model.model';
import { PlanBlueprint, PlanBlueprintDefinition, PlanBlueprintDefinitionSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { PlanStatus } from '@app/core/model/plan-status/plan-status';
import { Plan, PlanDescriptionTemplate, PlanUser, PlanUserRemovePersist } from '@app/core/model/plan/plan';
import { PlanReference } from '@app/core/model/plan/plan-reference';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
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
import { PlanInvitationDialogComponent } from '@app/ui/plan/invitation/dialog/plan-invitation-dialog.component';
import { DescriptionValidationOutput } from '@app/ui/plan/plan-finalize-dialog/plan-finalize-dialog.component';
import { BaseComponent } from '@common/base/base.component';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { map, takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { CopyDialogInputParams, CopyDialogReturnParams, DescriptionCopyDialogComponent } from '../description-copy-dialog/description-copy-dialog.component';
import { EvaluateDescriptionDialogComponent } from './../evaluate-description-dialog/evaluate-description-dialog.component';
import { EvaluatorConfiguration } from '@app/core/model/evaluator/evaluator-configuration';
import { EvaluatorHttpService } from '@app/core/services/evaluator/evaluator.http.service';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { StorageFileService } from '@app/core/services/storage-file/storage-file.service';
import { EntityType } from '@app/core/common/enum/entity-type';
import { Evaluation } from '@app/core/model/evaluation/evaluation';
import { EvaluationService } from '@app/core/services/evaluation/evaluation.service';


@Component({
    selector: 'app-description-overview',
    templateUrl: './description-overview.component.html',
    styleUrls: ['./description-overview.component.scss'],
    standalone: false
})
export class DescriptionOverviewComponent extends BaseComponent implements OnInit {

	description: Description | PublicDescription;
	researchers: PlanReference[] = [];
	isNew = true;
	isFinalized = false;
	isPublicView = true;
	hasPublishButton: boolean = true;
	expand = false;
	isLocked: Boolean;
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

	get canAssignPlanUsers(): boolean {
		const authorizationFlags = !this.isPublicView ? (this.description?.plan as Plan)?.authorizationFlags : [];
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
    
    private DESCRIPTION_PAGE_SIZE = 100;
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
		private evaluatorHttpService: EvaluatorHttpService,
		private logger: LoggingService,
		private sanitizer: DomSanitizer,
		private storageFileService: StorageFileService,
        private evaluationService: EvaluationService
	) {
		super();
	}

	ngOnInit() {
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
									this.description.plan.planUsers = this.isActive || this.description.plan.isActive === IsActive.Active ? data.plan.planUsers.filter(x => x.isActive === IsActive.Active) : data.plan.planUsers;
									this.researchers = this.referenceService.getReferencesForTypes(this.isActive ? this.description?.plan?.planReferences?.filter(x => x.isActive === IsActive.Active): this.description?.plan?.planReferences, [this.referenceTypeService.getResearcherReferenceType()]);
									this.checkLockStatus(this.description.id);
									this.canDelete = this.isActive && (this.authService.hasPermission(AppPermission.DeleteDescription) ||
										this.description.authorizationFlags?.some(x => x === AppPermission.DeleteDescription)) && this.description.belongsToCurrentTenant != false;

									this.canEdit = this.isActive && (this.authService.hasPermission(AppPermission.EditDescription) ||
										this.description.authorizationFlags?.some(x => x === AppPermission.EditDescription)) && this.description.belongsToCurrentTenant != false;

									this.canCopy = this.isActive && (this.canEdit || (this.authService.hasPermission(AppPermission.PublicCloneDescription) && this.isPublicView));

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
										return this.onFetchingDeletedCallbackError('/descriptions/');
									}
									if (error.status === 403) {
										return this.onFetchingForbiddenCallbackError('/descriptions/');
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
								this.researchers = this.referenceService.getReferencesForTypes(this.description?.plan?.planReferences?.filter(x => x.isActive === IsActive.Active), [this.referenceTypeService.getResearcherReferenceType()]);

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
									return this.onFetchingDeletedCallbackError('/explore-descriptions');
								}
								if (error.status === 403) {
									return this.onFetchingForbiddenCallbackError('/explore-descriptions');
								}
							}
						});
				}
			});

		if (this.isAuthenticated()) {

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
		return this.description && this.description.isActive != IsActive.Inactive;
	}

	get unauthorizedTootipText(): string {
		return this.language.instant('DESCRIPTION-OVERVIEW.INFOS.UNAUTHORIZED-ORCID');
	}

	get canExport(): boolean {
		if (!this.isAuthenticated()) return false;
		return this.isActive && this.description?.status?.definition?.availableActions?.filter(x => x === DescriptionStatusAvailableActionType.Export).length > 0;
	}

	get canEditStatus(): boolean {
		return this.isActive && (this.description as Description).statusAuthorizationFlags?.some(x => x.toLowerCase() === DescriptionStatusPermission.Edit.toLowerCase())
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

	onEvaluateDescription(descriptionId: Guid, evaluatorId: string, format: string, isPublicView: boolean) {
		this.evaluatorService.rankDescription(descriptionId, evaluatorId, format).subscribe(
			(response: RankModel) => {
				const dialogRef = this.dialog.open(EvaluateDescriptionDialogComponent, {
					data: {
						rankData: response,
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
					this.router.navigate([this.routerUtils.generateUrl('/descriptions')]);
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
		const path = this.location.path();
		this.router.navigateByUrl('/reload', { skipLocationChange: true }).then(() => this.router.navigate([this.routerUtils.generateUrl(path)]));
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

	openShareDialog() {
		const dialogRef = this.dialog.open(PlanInvitationDialogComponent, {
			restoreFocus: false,
			data: {
				planId: this.description.plan.id,
				planName: this.description.plan.label,
				blueprint: this.isPublicView ? null : (this.description?.plan as Plan)?.blueprint,
			},
            minWidth: 'min(65rem, 90vw)'
		});
	}

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
		this.router.navigate([this.routerUtils.generateUrl('/descriptions')]);
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

		const blueprint = this.isPublicView ? null : (this.description?.plan as Plan)?.blueprint;

		let sections: PlanBlueprintDefinitionSection[] = blueprint?.definition?.sections?.filter((section: PlanBlueprintDefinitionSection) => sectionId === section.id);

		return sections == null ? '' : sections[0].label;
	}

	openCopyToPlanDialog() {
		const formGroup = this.fb.group({
			planId: this.fb.control(null, Validators.required),
			sectionId: this.fb.control(null, Validators.required),
		})
		const dialogRef = this.dialog.open<DescriptionCopyDialogComponent, any, CopyDialogReturnParams>(DescriptionCopyDialogComponent, {
			width: '500px',
			restoreFocus: false,
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
					planId: this.description.plan.id,
					role: planUser.role
				};
				this.planService.removeUser(planUserRemovePersist).pipe(takeUntil(this._destroyed))
					.subscribe({
						next: () => this.reloadPage(),
						error: (error: any) => this.httpErrorHandlingService.handleBackedRequestError(error)
					})
			}
		});
	}

	persistStatus(status: DescriptionStatus, description: Description) {
		if (status.internalStatus != null && status.internalStatus === DescriptionStatusEnum.Finalized) {
			this.finalize(description, status.id);
		} else if (status.internalStatus != null && description.status.internalStatus === DescriptionStatusEnum.Finalized) {
			this.reverseFinalization(description, status.id);
		} else {
			// other statuses
			const descriptionStatusPersist: DescriptionStatusPersist = {
				id: description.id,
				statusId: status.id,
				hash: description.hash
			};
			this.descriptionService.persistStatus(descriptionStatusPersist).pipe(takeUntil(this._destroyed))
				.subscribe({
					next: () => {
						this.reloadPage();
						this.onUpdateCallbackSuccess()
					},
					error: (error: any) => this.onUpdateCallbackError(error)
				})
		}
	}

	finalize(description: Description, statusId: Guid) {

		this.descriptionService.validate([description.id]).pipe(takeUntil(this._destroyed))
			.subscribe({
				next: (result) => {
					if (result[0].result == DescriptionValidationOutput.Invalid) {
						this.router.navigate([this.routerUtils.generateUrl(['plans/edit', description.plan.id.toString(), 'd', description.id.toString(), 'finalize'], '/')]);
					} else {
						const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
							restoreFocus: false,
							data: {
								message: this.language.instant('DESCRIPTION-OVERVIEW.FINALIZE-DIALOG.TITLE'),
								confirmButton: this.language.instant('DESCRIPTION-OVERVIEW.FINALIZE-DIALOG.CONFIRM'),
								cancelButton: this.language.instant('DESCRIPTION-OVERVIEW.FINALIZE-DIALOG.NEGATIVE'),
								isDeleteConfirmation: false
							}
						});
						dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe({
							next: (result) => {
								if (result) {
									const descriptionStatusPersist: DescriptionStatusPersist = {
										id: description.id,
										statusId: statusId,
										hash: description.hash
									};
									this.descriptionService.persistStatus(descriptionStatusPersist).pipe(takeUntil(this._destroyed))
										.subscribe({
											next: () => {
												this.reloadPage();
												this.onUpdateCallbackSuccess()
											},
											error: (error: any) => this.onUpdateCallbackError(error)

										})
								}
							},
							error: (error) => this.httpErrorHandlingService.handleBackedRequestError(error)
						})
					}
				},
				error: (error) => this.httpErrorHandlingService.handleBackedRequestError(error)
			});

	}

	hasReversableStatus(description: Description): boolean {
		return description.plan.status.internalStatus == PlanStatusEnum.Draft && description?.status?.internalStatus == DescriptionStatusEnum.Finalized && this.canFinalize && (this.description as Description).availableStatuses?.find(x => x.internalStatus === DescriptionStatusEnum.Draft) != null
	}

	reverseFinalization(description: Description, statusId: Guid) {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			restoreFocus: false,
			data: {
				message: this.language.instant('DESCRIPTION-OVERVIEW.UNDO-FINALIZATION-DIALOG.TITLE'),
				confirmButton: this.language.instant('DESCRIPTION-OVERVIEW.UNDO-FINALIZATION-DIALOG.CONFIRM'),
				cancelButton: this.language.instant('DESCRIPTION-OVERVIEW.UNDO-FINALIZATION-DIALOG.NEGATIVE'),
				isDeleteConfirmation: false
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result) {
				const planUserRemovePersist: DescriptionStatusPersist = {
					id: description.id,
					statusId: statusId,
					hash: description.hash
				};
				this.descriptionService.persistStatus(planUserRemovePersist).pipe(takeUntil(this._destroyed))
					.subscribe({
						next: (data) => {
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

	private loadStatusLogo(){
		const availableStatus = [...(this.description as Description).availableStatuses, this.description.status];
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
		return 	
	}

	private lookupFields(): string[] {
		return [
			nameof<BaseDescription>(x => x.isActive),
			nameof<Description>(x => x.id),
			nameof<Description>(x => x.label),
			nameof<Description>(x => x.description),
			[nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.id)].join('.'),
			[nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.name)].join('.'),
			[nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.internalStatus)].join('.'),
			[nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.availableActions)].join('.'),
			[nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.matIconName)].join('.'),
			[nameof<Description>(x => x.status), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),

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

			[nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.id)].join('.'),
			[nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.label)].join('.'),
			[nameof<Description>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.groupId)].join('.'),
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
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.authorizationFlags), AppPermission.InvitePlanUsers].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.label)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.label)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.hasTemplates)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.sectionId)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.user.name)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.role)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planUsers), nameof<PlanUser>(x => x.isActive)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.label)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.id)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.source)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.reference), nameof<Reference>(x => x.reference)].join('.'),
			[nameof<Description>(x => x.plan), nameof<Plan>(x => x.planReferences), nameof<PlanReference>(x => x.isActive)].join('.'),
            [nameof<Description>(x => x.plan), nameof<Plan>(x => x.status), nameof<PlanStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.statusColor)].join('.'),

			[nameof<Description>(x => x.availableStatuses), nameof<DescriptionStatus>(x => x.id)].join('.'),
			[nameof<Description>(x => x.availableStatuses), nameof<DescriptionStatus>(x => x.name)].join('.'),
			[nameof<Description>(x => x.availableStatuses), nameof<DescriptionStatus>(x => x.internalStatus)].join('.'),
			[nameof<Description>(x => x.availableStatuses), nameof<DescriptionStatus>(x => x.action)].join('.'),
			[nameof<Description>(x => x.availableStatuses), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.matIconName)].join('.'),
			[nameof<Description>(x => x.availableStatuses), nameof<DescriptionStatus>(x => x.definition), nameof<DescriptionStatusDefinition>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),

		]
	}

}
