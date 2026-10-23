import {AfterViewInit, Component, computed, effect, inject, model, OnDestroy, OnInit, ViewChild, ChangeDetectionStrategy} from '@angular/core';
import { TourStateService } from '@app/library/guided-tour/tour-state.service';
import { planAssetEditorPageTour, planEditorPageTour } from './plan-editor.tour';
import { planNewPageTour } from './plan-new.tour';
import { FormGroup, UntypedFormArray, UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { DomSanitizer, SafeResourceUrl, SafeUrl, Title } from '@angular/platform-browser';
// FE: ported plan actions (evaluate / clone / new version) — see overview component
import { EvaluatorService } from '@app/core/services/evaluator/evaluator.service';
import { EvaluatorConfiguration, BenchmarkConfiguration } from '@app/core/model/evaluator/evaluator-configuration';
import { RankConfig } from '@app/core/model/evaluator/rank-config';
import { RankResultModel } from '@app/core/model/evaluator/evaluator-plan-model.model';
import { EvaluatorEntityType } from '@app/core/common/enum/evaluator-entity-type';
import { PluginEntityType } from '@app/core/common/enum/plugin-entity-type';
import { BenchmarkDialogComponent, BenchmarkDialogResult } from '@app/ui/evaluation/benchmark-dialog/benchmark-dialog.component';
import { EvaluateDialogComponent } from '@app/ui/evaluation/evaluate-dialog/evaluate-dialog.component';
import { ClonePlanDialogComponent } from '../clone-dialog/plan-clone-dialog.component';
import { NewVersionPlanDialogComponent } from '../new-version-dialog/plan-new-version-dialog.component';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { AnnotationEntityType } from '@app/core/common/enum/annotation-entity-type';
import { DescriptionStatusEnum } from '@app/core/common/enum/description-status';
import { DescriptionTemplateVersionStatus } from '@app/core/common/enum/description-template-version-status';
import { FileTransformerEntityType } from '@app/core/common/enum/file-transformer-entity-type';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { PlanStatusEnum } from '@app/core/common/enum/plan-status';
import { LockTargetType } from '@app/core/common/enum/lock-target-type';
import { Description, DescriptionPersist } from '@app/core/model/description/description';
import { DescriptionTemplatesInSection, PlanBlueprint, PlanBlueprintDefinition, RootSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { Plan, PlanPersist, PlanUser, PlanWithDescriptionsPersist } from '@app/core/model/plan/plan';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { DescriptionService } from '@app/core/services/description/description.service';
import { FileTransformerService } from '@app/core/services/file-transformer/file-transformer.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { PlanBlueprintService } from '@app/core/services/plan/plan-blueprint.service';
import { PlanService } from '@app/core/services/plan/plan.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { UserService } from '@app/core/services/user/user.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditor } from '@common/base/base-editor';
import { FormService } from '@common/forms/form-service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { catchError, filter, finalize, map, switchMap, take, takeUntil, tap } from 'rxjs/operators';
import { toObservable } from '@angular/core/rxjs-interop';
import { PlanFinalizeDialogComponent, PlanFinalizeDialogOutput } from '../plan-finalize-dialog/plan-finalize-dialog.component';
import { PlanEditorModel, PlanEditorForm } from './plan-editor.model';
import { PlanEditorEntityResolver } from './resolvers/plan-editor-enitity.resolver';
import { PlanStatus } from '@app/core/model/plan-status/plan-status';
import { PlanStatusAvailableActionType } from '@app/core/common/enum/plan-status-available-action-type';
import { PlanVersionStatus } from '@app/core/common/enum/plan-version-status';
import { PlanStatusPermission } from '@app/core/common/enum/plan-status-permission.enum';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { PlanDescriptionEditorComponent } from './plan-description-editor/plan-description-editor.component';
import { DescriptionEditorHelper } from './plan-description-editor/plan-description-editor-helper';
import { PlanTableOfContentsComponent, RootToCEntry } from './plan-table-of-contents/plan-table-of-contents.component';
import { DescriptionEditorForm, DescriptionEditorModel, FieldEditorModel, PropertyDefinitionFieldSetItemEditorModel } from '@app/ui/description/editor/description-editor.model';
import { DescriptionInfo, PlanTempStorageService } from './plan-temp-storage.service';
import { combineLatest, EMPTY, forkJoin, interval, Observable, of } from 'rxjs';
import { nameof } from 'ts-simple-nameof';
import { DescriptionTemplate, BaseFieldSet, BaseSection } from '@app/core/model/description-template/description-template';
import { PlanUpdatePreview } from '@app/core/model/plan-update-request/plan-update-preview';
import { EnqueueService } from '@app/core/services/enqueue.service';
import { UserActionPayload } from '@app/core/model/websocket/ws-message.model';
import { FormValidationErrorsDialogComponent } from '@common/forms/form-validation-errors-dialog/form-validation-errors-dialog.component';
import { PlanCreationConsentDialogComponent } from './plan-creation-consent-dialog/plan-creation-consent-dialog.component';
import { PlanReviewService } from './plan-review.service';
import { AcceptedDeleteDescription, AcceptedNewDescription, PlanReviewActionBarComponent, RevertedNewDescription } from './plan-review-action-bar/plan-review-action-bar.component';
import { PlanUpdateRequestService } from '@app/core/services/plan/plan-update-request.service';
import { PlanUpdateRequestApply } from '@app/core/model/plan-update-request/plan-update-request';
import { FormAnnotationService, MULTI_FORM_ANNOTATION_SERVICE_TOKEN } from '@app/ui/annotations/annotation-dialog-component/form-annotation.service';
import { DescriptionTemplateEditorResolver } from '@app/ui/admin/description-template/editor/description-template-editor.resolver';
import { DescriptionTemplateType } from '@app/core/model/description-template-type/description-template-type';
import { StorageFileService } from '@app/core/services/storage-file/storage-file.service';
import { PlanAnnotationService } from './plan-annotation.service';
import { ToCEntry } from '@app/ui/description/editor/table-of-contents/models/toc-entry';
import { UpdatePlanDescriptionTemplate } from '@app/core/model/plan/update-plan-description-template';
import { PlanLockService } from './plan-lock.service';
import { EditorSessionStateService, EntityLockInfo, EntityStatusInfo, statusContrastColor } from './editor-session-state.service';
import { PlanAuthorsEditorDialogComponent, PlanAuthorsEditorDialogParams, PlanAuthorsEditorDialogReturn } from '../plan-authors/plan-authors-editor-dialog/plan-authors-editor-dialog.component';
import { DepositService } from '@app/core/services/deposit/deposit.service';
import { DepositConfiguration } from '@app/core/model/deposit/deposit-configuration';
import { EntityDoi } from '@app/core/model/entity-doi/entity-doi';
import { NgClass, NgTemplateOutlet, DatePipe } from '@angular/common';
import { MatIconButton, MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatTooltip } from '@angular/material/tooltip';
import { PlanActiveUsersComponent } from '../plan-active-users/plan-active-users.component';
import { MatMenuTrigger, MatMenu, MatMenuItem } from '@angular/material/menu';
import { PlanDepositDropdown } from '../editor/plan-deposit-dropdown/plan-deposit-dropdown.component';
import { MatBadge } from '@angular/material/badge';
import { PlanBlueprintsPreviewComponent } from './plan-blueprints-preview/plan-blueprints-preview.component';
import { PlanInternalBlueprintEditorComponent } from './internal-blueprint-editor/plan-internal-blueprint-editor.component';
import { PlanNewDescriptionEditorComponent } from './internal-blueprint-editor/plan-new-description-editor/plan-new-description-editor.component';
import { PlanAnnotationsSideDialogComponent } from './plan-annotations-side-dialog/plan-annotations-side-dialog.component';
import { DateTimeFormatPipe } from '../../../core/pipes/date-time-format.pipe';

// FE-2: view-model for the header context band (what the user is currently editing + its state).
interface ContextBandView {
    kind: 'plan' | 'description';
    label: string;
    numbering?: string;
    status?: EntityStatusInfo;
    dirty: boolean;
    lock: EntityLockInfo;
}

@Component({
    selector: 'app-plan-editor',
    templateUrl: './plan-editor.component.html',
    styleUrls: ['./plan-editor.component.scss'],
    providers: [PlanReviewService, PlanUpdateRequestService, { provide: MULTI_FORM_ANNOTATION_SERVICE_TOKEN, useClass: FormAnnotationService, multi: true }, EnqueueService, PlanAnnotationService, PlanLockService, PlanTempStorageService, EditorSessionStateService],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [NgClass, MatIconButton, MatIcon, MatTooltip, PlanActiveUsersComponent, NgTemplateOutlet, MatButton, MatMenuTrigger, PlanDepositDropdown, MatMenu, MatMenuItem, MatBadge, PlanReviewActionBarComponent, PlanBlueprintsPreviewComponent, PlanTableOfContentsComponent, FormsModule, ReactiveFormsModule, PlanInternalBlueprintEditorComponent, PlanDescriptionEditorComponent, PlanNewDescriptionEditorComponent, PlanAnnotationsSideDialogComponent, DatePipe, TranslatePipe, DateTimeFormatPipe]
})
export class PlanEditorComponent extends BaseEditor<PlanEditorModel, Plan> implements OnInit, AfterViewInit, OnDestroy {
    @ViewChild('descriptionEditor') descriptionEditor: PlanDescriptionEditorComponent;
    @ViewChild('tableOfContent') tableOfContent: PlanTableOfContentsComponent;
    @ViewChild('reviewBar') reviewBar: PlanReviewActionBarComponent;
    formGroup: FormGroup<PlanEditorForm>;
    visibilityRules: VisibilityRulesService;
	isNew = true;
	isDeleted = false;
    showPlanErrors: boolean = false;
	item: Plan;
	selectedBlueprint: PlanBlueprint;
	defaultBlueprint: PlanBlueprint;
    tocEntries = model<RootToCEntry[]>([]);
	// step = signal<number>(0);
	// annotationsPerAnchor: Map<string, number> = new Map<string, number>();

    selectedDescription = model<Guid>();

    showInfo = false;

    newDescriptionIds: string[] = [];
    get generateTempDescriptionId(): string {
        return Guid.EMPTY.toString().replace('0', `${this.newDescriptionIds.length + 1}`);
    }

	//Enums
	fileTransformerEntityTypeEnum = FileTransformerEntityType;


	permissionPerSection: {[key: string]: AppPermission[]} = {};
	canAnnotatePerFieldSet: Map<string, boolean>;
	canAnnotatePerDescription: Map<string, boolean>;

    planStatusEnum = PlanStatusEnum;

    protected descTemplatesInUseMap = computed(() => {
        const newMap = new Map<string, Guid[]>();
        this.planTempStorage.descriptionIdsBySectionMap()?.forEach((descIds, key) => {
            const descTemplateIds =
            descIds.map((id) => {
                const planDescTemplateId = this.planTempStorage.descriptions().get(id.toString())?.formGroup?.getRawValue()?.planDescriptionTemplateId;
                return this.planTempStorage.getPlanDescriptionTemplate(planDescTemplateId)?.descriptionTemplateGroupId;
            })?.filter((x) => x)
            newMap.set(key, descTemplateIds)
        })
        return newMap;
    })

	selectedBlueprintId = null;

    protected get changeStatusEnabled(): boolean {
        return this.canEditStatus && !this.isNew && 
        this.item.availableStatuses?.length > 0 && 
        this.item.versionStatus != PlanVersionStatus.Previous &&
        !this.planLockService?.externalLockIds()?.length &&
        this.hasNotDoi() &&
        this.belongsToCurrentTenant
    }
    protected get disabledStatusTooltip(): string {
        // FE-3: one place computes the specific reason a status change is blocked, shared by the tooltip
        // and the snackbar shown when the user actually clicks (persistStatus).
        return this.statusChangeBlockedReason() ?? '';
    }

    protected statusChangeBlockedReason(): string | null {
        if(this.isLocked){
            return 'PLAN-EDITOR.LOCKED-DIALOG.PLAN-LOCKED';
        }
        if(this.planLockService?.externalLockIds()?.length){
            return 'PLAN-EDITOR.LOCKED-DIALOG.DESCRIPTION-LOCKED';
        }
        if(this.newDescriptionIds?.length){
            return 'PLAN-EDITOR.STATUS-DISABLED.UNSAVED-NEW-DESCRIPTIONS';
        }
        if(this.isDirty){
            return 'PLAN-EDITOR.STATUS-DISABLED.UNSAVED-CHANGES';
        }
        return null;
    }

    protected get isDirty(): boolean {
		// FE-0b: reflect the WHOLE editor session (plan form + every description form + new
		// descriptions), not just the plan form. Previously this only saw `formGroup.dirty`, so the
		// header under-reported description edits and the status/export gates could disagree with the
		// save button. Derived from form.dirty (not editLockIds) via the unified selector.
		return this.editorSessionState.isAnythingDirty();
	}

    protected get isSelectedDescriptionLocked(): boolean {
        const descId = this.selectedDescription()?.toString();
        return !!descId && this.planLockService.externalLockIds()?.includes(descId);
    }

    // FE-1: lock state of whatever the user is currently looking at (the selected description, else
    // the plan). Drives the locked/lost banner, including provenance ("locked by {name}").
    protected get currentEntityLockInfo(): EntityLockInfo {
        const id = this.selectedDescription()?.toString() ?? this.item?.id?.toString();
        return this.editorSessionState.lockInfoFor(id);
    }

    protected get isViewingDescription(): boolean {
        return !!this.selectedDescription();
    }

    // FE-2: readable text color for a status chip given the admin-configured background color.
    protected statusContrastColor = statusContrastColor;

    // FE-2: plan sections (id/label/numbering) that still have pending required / invalid fields, for
    // the finalize dialog's "sections needing input" list.
    protected get incompletePlanSections(): { id: string; label: string; numbering: string }[] {
        return (this.tocEntries() ?? [])
            .filter(e => this.editorSessionState.planSectionIncomplete(e.id))
            .map(e => ({ id: e.id, label: e.label, numbering: e.numbering }));
    }

    // FE-2: delete moved out of the TOC into the context band (for the active description).
    protected get canDeleteSelectedDescription(): boolean {
        const id = this.selectedDescription()?.toString();
        if(!id){ return false; }
        const info = this.planTempStorage.descriptions().get(id);
        const sectionId = info?.lastPersist?.planDescriptionTemplate?.sectionId;
        if(!sectionId){ return false; }
        return this.canDeleteSectionDescriptions(sectionId as unknown as Guid) && !this.isSelectedDescriptionLocked;
    }
    protected deleteSelectedDescription(){
        const id = this.selectedDescription()?.toString();
        const info = id ? this.planTempStorage.descriptions().get(id) : null;
        if(info){ this.removeDescription(info); }
    }

    // FE-2 (two-level header — context band): a small view-model describing what the user is currently
    // editing (the selected description, else the current plan section) plus its status / unsaved /
    // lock state, so the editor always names the active entity and its state above the form.
    protected get contextBand(): ContextBandView | null {
        if(this.isNew || !this.formGroup){ return null; }
        const descId = this.selectedDescription()?.toString();
        if(descId){
            const state = this.editorSessionState.descriptionState(descId);
            if(!state){ return null; }
            const info = this.planTempStorage.descriptions()?.get(descId);
            return {
                kind: 'description',
                label: info?.formGroup?.getRawValue()?.label,
                numbering: this.planTempStorage.descriptionNumbering()?.get(descId),
                status: state.status,
                dirty: state.dirty,
                lock: state.lock,
            };
        }
        const planState = this.editorSessionState.planState();
        // Show the SECTION the user is in (consistent with how a description is named at description-level,
        // not at the fieldset within it). The scroll-spy moves `selected` down to the active fieldset, so
        // we resolve back up to its root section — this also removes the "1. Project → 1.1 …" header flicker.
        const sel = this.tableOfContent?.selected?.entry;
        const section = sel
            ? (this.tocEntries()?.find(e => e.id === sel.id || sel.pathToEntry?.includes(e.id)) ?? sel)
            : null;
        return {
            kind: 'plan',
            label: section?.label,
            numbering: section?.numbering,
            status: planState?.status,
            // FE-2: dirty for THIS section only (not the whole plan form), so navigating sections no
            // longer shows "unsaved changes" on every section after a single edit.
            dirty: section?.id ? this.editorSessionState.planSectionDirty(section.id) : false,
            lock: planState?.lock ?? { state: 'none' },
        };
    }

    protected get planUserCount(): number{
        return this.item?.planUsers?.filter(x => x.isActive === IsActive.Active)?.length ?? 0;
    }

    //** PERMISSIONS  */
    protected get isFinalized(){
        return this.item?.status?.internalStatus === PlanStatusEnum.Finalized;
    }

    protected get belongsToCurrentTenant(){
        return this.item?.belongsToCurrentTenant || this.isNew;
    }

	protected get canDeletePlan(): boolean {
		return this.belongsToCurrentTenant &&!this.isDeleted && !this.isNew && (this.hasPermission(this.authService.permissionEnum.DeletePlan) 
        || this.item?.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.DeletePlan));
	}

    protected get canSavePlan(): boolean {
        if(this.isNew){ return true; }
        const anySectionEditable =  this.item?.blueprint?.definition?.sections?.some((section) => this.canEditPlanInSection(section.id))
        return this.planIsEditable && ( this.canEditEntirePlan || anySectionEditable);
    }
    protected get canSaveDescriptions(): boolean {
        const anySectionDescEditable =  this.item?.blueprint?.definition?.sections?.some((section) => this.canEditSectionDescriptions(section.id))
        return this.planIsEditable && ( this.canEditEntirePlan || anySectionDescEditable || this.canEditEntirePlanDescriptions);
    }
    
    protected get canEditAnySection(): boolean {
        return this.canSavePlan || this.canSaveDescriptions;
	}

    protected get canEditDescriptions(): boolean {
        return this.belongsToCurrentTenant &&!this.isDeleted && !this.isFinalized && this.item?.blueprint?.definition?.sections?.some((section) => this.canEditSectionDescriptions(section.id));
    }

	protected get canFinalizePlan(): boolean {
		return this.belongsToCurrentTenant &&!this.isDeleted && !this.isNew && !this.isLockedByUser && !this.isFinalized && this.canEditEntirePlan;
	}

	protected get canReverseFinalizePlan(): boolean {
		return this.belongsToCurrentTenant &&!this.isDeleted && !this.isNew && !this.isLockedByUser && this.item.status?.internalStatus == PlanStatusEnum.Finalized && this.canEditEntirePlan;
	}

	protected get canExport(): boolean {
		return this.item?.status?.definition?.availableActions?.filter(x => x === PlanStatusAvailableActionType.Export).length > 0 &&
         (this.hasPermission(this.authService.permissionEnum.ExportPlan) || 
         this.item?.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.ExportPlan));
	}

    protected get showDepositButton(): boolean {
		if (!this.item || !this.item.belongsToCurrentTenant) { return false; }
		const authorizationFlags = this.item?.authorizationInformation?.globalAuthorizationFlags;
		return !this.isNew && (authorizationFlags?.some(x => x === AppPermission.DepositPlan) || this.hasPermission(AppPermission.DepositPlan));
	}

	protected get canDeposit(): boolean {
		if (!this.item || !this.item.belongsToCurrentTenant) { return false; }
		return this.showDepositButton && (this.item?.status?.definition?.availableActions?.filter(x => x === PlanStatusAvailableActionType.Deposit).length > 0);
	}

	protected get depositRepos(): DepositConfiguration[] {
		return this.depositService.availableDeposits();
	}

	protected get inputRepos(): DepositConfiguration[] {
		return this.depositRepos?.filter(repo => !this.item?.entityDois?.find(doi => doi.repositoryId === repo.repositoryId));
	}

	protected get hasDoi(): boolean {
		return (this.item?.entityDois?.length > 0);
	}

	protected get moreDeposit(): boolean {
		return (this.item?.entityDois?.length < this.depositRepos?.length);
	}

	protected afterDeposit(result: EntityDoi[]): void {
		if (result.length > 0) {
            this.router.navigate([this.routerUtils.generateUrl(['/plans/overview', this.item?.id.toString()], '/')]);
		}
	}

	// ===== FE: ported plan actions — Evaluate / Clone / New version (see plan overview) =====
	protected availableEvaluators: EvaluatorConfiguration[] = [];
	protected evaluatorLogos = new Map<string, SafeResourceUrl>();

	private loadEvaluators(){
		if(!this.authService.currentAccountIsAuthenticated()){ this.availableEvaluators = []; return; }
		this.availableEvaluators = this.evaluatorService.availableEvaluatorsFor(EvaluatorEntityType.Plan) ?? [];
		this.availableEvaluators.forEach(ev => {
			if(ev.hasLogo && !this.evaluatorLogos.has(ev.evaluatorId)){
				this.evaluatorService.getLogo(ev.evaluatorId).pipe(takeUntil(this._destroyed)).subscribe({
					next: (res) => this.evaluatorLogos.set(ev.evaluatorId, this.sanitizer.bypassSecurityTrustResourceUrl('data:image/png;base64, ' + res.body)),
					error: (err) => this.logger.error('Error fetching evaluator logo:', err)
				});
			}
		});
	}

	protected get canEvaluate(): boolean {
		return !this.isNew && this.item?.status?.definition?.availableActions?.some(x => x === PlanStatusAvailableActionType.Evaluate) &&
			(this.hasPermission(AppPermission.EvaluatePlan) ||
			 this.item?.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.EvaluatePlan));
	}

	// FE: "permission vs state" split for the More-actions menu — permission controls VISIBILITY
	// (hide if you can never do it), the *StatusAllowed flags control DISABLED (+ a 'why' tooltip).
	protected get hasExportPermission(): boolean {
		return !this.isNew && (this.hasPermission(AppPermission.ExportPlan) ||
			this.item?.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.ExportPlan));
	}
	protected get exportStatusAllowed(): boolean {
		return this.item?.status?.definition?.availableActions?.some(x => x === PlanStatusAvailableActionType.Export);
	}
	protected get hasEvaluatePermission(): boolean {
		return !this.isNew && (this.hasPermission(AppPermission.EvaluatePlan) ||
			this.item?.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.EvaluatePlan));
	}
	protected get evaluateStatusAllowed(): boolean {
		return this.item?.status?.definition?.availableActions?.some(x => x === PlanStatusAvailableActionType.Evaluate);
	}
	protected get hasNewVersionPermission(): boolean {
		return !this.isNew && this.belongsToCurrentTenant && this.item?.isActive != IsActive.Inactive &&
			(this.hasPermission(AppPermission.CreateNewVersionPlan) ||
			 this.item?.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.CreateNewVersionPlan));
	}
	protected get isCurrentVersion(): boolean {
		return this.item?.versionStatus === PlanVersionStatus.Current;
	}

	protected onEvaluatePlan(planId: Guid, evaluatorId: string, format: string, rankConfig: RankConfig, availableBenchmarks: BenchmarkConfiguration[] = [], availableTests: BenchmarkConfiguration[] = []){
		availableBenchmarks = availableBenchmarks?.filter(x => x.appliesTo.includes(PluginEntityType.Plan)) || [];
		availableTests = availableTests?.filter(x => x.appliesTo.includes(PluginEntityType.Plan)) || [];

		if (availableBenchmarks?.length > 1 || availableTests?.length > 1) {
			const dialogRef = this.dialog.open(BenchmarkDialogComponent, {
				width: '500px',
				data: { availableBenchmarks, availableTests, evaluatorId }
			});
			dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe((result: BenchmarkDialogResult) => {
				if (result?.values?.length > 0) {
					const benchmarkIds = result.type === 'benchmarks' ? result.values : null;
					const testIds = result.type === 'tests' ? result.values : null;
					this.rankPlan(planId, evaluatorId, format, rankConfig, benchmarkIds, testIds);
				}
			});
		} else if (availableBenchmarks?.length == 1 && !(availableTests?.length > 0)){
			this.rankPlan(planId, evaluatorId, format, rankConfig, [availableBenchmarks[0].id], null);
		} else if (availableTests?.length == 1 && !(availableBenchmarks?.length > 0)){
			this.rankPlan(planId, evaluatorId, format, rankConfig, null, [availableTests[0].id]);
		} else {
			this.rankPlan(planId, evaluatorId, format, rankConfig, null, null);
		}
	}

	private rankPlan(planId: Guid, evaluatorId: string, format: string, rankConfig: RankConfig, benchmarkIds: string[] | null, testIds: string[] | null){
		this.evaluatorService.rankPlan(planId, evaluatorId, format, benchmarkIds ?? undefined, testIds ?? undefined)
			.pipe(takeUntil(this._destroyed))
			.subscribe({
				next: (response: RankResultModel[]) => {
					this.dialog.open(EvaluateDialogComponent, {
						maxWidth: '90vw',
						// planId lets the dialog link each result to the asset it covers.
						data: { rankData: response, rankConfig, evaluatorId, planId }
					});
				},
				error: (err) => this.logger.error('Error ranking plan:', err)
			});
	}

	protected get canClonePlan(): boolean {
		return !this.isNew && (this.hasPermission(AppPermission.ClonePlan) ||
			this.item?.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.ClonePlan));
	}

	protected cloneClicked(){
		const dialogRef = this.dialog.open(ClonePlanDialogComponent, {
			maxWidth: '700px',
			maxHeight: '80vh',
			data: { plan: this.item, isPublic: false }
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe((result: Plan) => {
			if (result) {
				this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
				this.router.navigate([this.routerUtils.generateUrl(['/plans/edit', result.id.toString()], '/')]);
			}
		});
	}

	protected get canCreateNewVersion(): boolean {
		return !this.isNew && this.item?.isActive != IsActive.Inactive &&
			(this.hasPermission(AppPermission.CreateNewVersionPlan) ||
			 this.item?.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.CreateNewVersionPlan)) &&
			this.item?.versionStatus === PlanVersionStatus.Current &&
			this.belongsToCurrentTenant;
	}

	protected newVersionClicked(){
		const dialogRef = this.dialog.open(NewVersionPlanDialogComponent, {
			maxWidth: '700px',
			maxHeight: '80vh',
			data: { plan: { ...this.item, descriptions: this.item?.descriptions } }
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe((result: Plan) => {
			if (result) {
				this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
				this.router.navigate([this.routerUtils.generateUrl(['/plans/edit', result.id.toString()], '/')]);
			}
		});
	}
	// ===== end ported plan actions =====

	protected get canEditStatus(): boolean{
        return this.item?.statusAuthorizationFlags?.some(x => x.toLowerCase() === PlanStatusPermission.Edit.toLowerCase())
    }

    protected get planIsEditable(): boolean{
        return this.isNew || (this.belongsToCurrentTenant && !this.isDeleted && !this.isFinalized);
    }

    protected get canEditEntirePlan(): boolean{
        return this.planIsEditable && this.hasPermission(AppPermission.EditPlan);
    }

    protected get canEditEntirePlanDescriptions(): boolean {
        return this.planIsEditable && this.hasPermission(AppPermission.EditDescription);
    }

    protected canEditPlanInSection(id: string){
        return this.planIsEditable && (this.canEditEntirePlan || this.permissionPerSection?.[id.toString()]?.some(x => x === AppPermission.EditPlan));
    }

	protected canEditSectionDescriptions(id: string): boolean {
		return this.planIsEditable && (
            this.hasPermission(this.authService.permissionEnum.EditDescription) || 
            this.item?.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.EditDescription) || (
			this.permissionPerSection?.[id.toString()]?.some(x => x === AppPermission.EditDescription)
		));
	}

	protected canDeleteSectionDescriptions(id: Guid): boolean {
		return this.planIsEditable && (
            this.hasPermission(this.authService.permissionEnum.DeleteDescription) || 
            this.item?.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.DeleteDescription) || (
			this.permissionPerSection && this.permissionPerSection[id.toString()] && this.permissionPerSection[id.toString()].some(x => x === AppPermission.DeleteDescription)
		));
	}

	protected canAnnotateSection(id: string): boolean {
		return this.belongsToCurrentTenant && !this.isDeleted && this.isNew || (
                this.item.authorizationInformation?.globalAuthorizationFlags?.some(x => x == AppPermission.AnnotatePlan) || 
                this.permissionPerSection?.[id.toString()]?.some(x => x === AppPermission.AnnotatePlan)
            );
	}

	private hasPermission(permission: AppPermission): boolean {
		return this.authService.hasPermission(permission) || 
            this.item?.authorizationInformation?.globalAuthorizationFlags?.some(x => x === permission)
        }

    //** PERMISSIONS END */


    // "What's new" tour for the 2.0.0 editor: registered so Support > Take a tour can replay it, and
    // auto-started once per user on the first open of an existing plan (see ngAfterViewInit).
    private readonly tourState = inject(TourStateService);
    private readonly pageTour = planEditorPageTour(inject(TranslateService));
    /** "Start a new plan" tour over the blueprint wizard, registered instead of the editor tour while isNew. */
    private readonly newPlanTour = planNewPageTour(inject(TranslateService));
    // A second tour for the asset (description) view of the same page: registered while a description is
    // selected so "Take a tour" matches what is on screen, and auto-started the first time an asset is opened.
    private readonly assetTour = planAssetEditorPageTour(inject(TranslateService));
    tocEntryObservable: Observable<RootToCEntry[]>;
    lockInitObservable: Observable<number>;

	constructor(
		// BaseFormEditor injected dependencies
		protected dialog: MatDialog,
		protected language: TranslateService,
		protected formService: FormService,
		protected router: Router,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected filterService: FilterService,
		protected route: ActivatedRoute,
		protected queryParamsService: QueryParamsService,
		protected lockService: LockService,
		protected authService: AuthService,
		protected configurationService: ConfigurationService,
		// Rest dependencies. Inject any other needed deps here:
		private planService: PlanService,
		private logger: LoggingService,
		public planBlueprintService: PlanBlueprintService,
		public routerUtils: RouterUtilsService,
		public enumUtils: EnumUtils,
		public descriptionTemplateService: DescriptionTemplateService,
		public userService: UserService,
		public descriptionService: DescriptionService,
		public titleService: Title,
		private analyticsService: AnalyticsService,
		private breadcrumbService: BreadcrumbService,
		public fileTransformerService: FileTransformerService,
        private planTempStorage: PlanTempStorageService,
        protected planReviewService: PlanReviewService,
        private planUpdateRequestService: PlanUpdateRequestService,
        private storageFileService: StorageFileService,
        private sanitizer: DomSanitizer,
        protected planAnnotationService: PlanAnnotationService,
        private planLockService: PlanLockService,
        private depositService: DepositService,
        private editorSessionState: EditorSessionStateService,
        private evaluatorService: EvaluatorService
	) {
		super(dialog, language, formService, router, uiNotificationService, httpErrorHandlingService, filterService, route, queryParamsService, lockService, authService, configurationService);
        this.visibilityRules = new VisibilityRulesService(this.formService);
        effect(() => {
            const planId = this.item?.id?.toString();
            this.isLockedByUser = this.planLockService.editLockIds()?.includes(planId) || this.planLockService.transientLockId() == planId;
            this.isLocked = this.planLockService.externalLockIds()?.includes(planId);
        });
        effect(() => {
            const descId = this.selectedDescription();
            if(!descId){ return; }
            const description = this.planTempStorage.descriptions()?.get(descId.toString())?.lastPersist;
            if(!description){ return; }
            if(description.descriptionTemplate?.versionStatus === DescriptionTemplateVersionStatus.Previous
                && description.status?.internalStatus === DescriptionStatusEnum.Draft){
                this.promptDeprecatedTemplateUpdate(description);
            }
        });
        // FE: load available FAIR evaluators (ported from overview) whenever the shared list changes.
        effect(() => {
            if(this.evaluatorService.availableEvaluators()){
                this.loadEvaluators();
            }
        });
        this.tocEntryObservable = toObservable(this.tocEntries);
        effect(() => {
            if (this.selectedDescription()) {
                this.tourState.registerPageTour(this.assetTour);
                if (this.canEditAnySection) { this.tourState.startIfUnseen(this.assetTour); }
            } else if (!this.isNew) {
                this.tourState.registerPageTour(this.pageTour);
            }
        });
        this.lockInitObservable = toObservable(this.planLockService.initializeVersion);
        // effect(() => {
        //     let planId = this.item?.id?.toString();
        //     if(this.planLockService.transientLockId() == planId?.toString() && this.canEditAnySection){
        //         this.formGroup.enable({onlySelf: true});
        //         this.lockOnFormChange(this.item.id);
        //     }
        // })
    }

    ngOnInit(): void {
		this.analyticsService.trackPageView(AnalyticsService.PlanEditor);

        super.ngOnInit();
        // isNew is settled by the base editor above: the blueprint wizard gets the "start a plan" tour,
        // an existing plan the editor tour. Creating the plan navigates to /plans/edit/:id, which
        // rebuilds this component, so the wizard tour is unregistered on the way out.
        this.tourState.registerPageTour(this.isNew ? this.newPlanTour : this.pageTour);
        // The blueprint wizard reads it on init; parseQueryParams runs too late and waits for ToC entries a new plan doesn't have.
        if (this.isNew) this.selectedBlueprintId = this.route.snapshot.queryParamMap.get('blueprintId');
		// if (this.isNew === false && this.step() === 0) this.nextStep();

        // if(this.item?.id){
			// this.formAnnotationServices[0].init(this.item.id, AnnotationEntityType.Plan);
            // this.formAnnotationServices[0].getAnnotationCountObservable().pipe(takeUntil(this._destroyed)).subscribe((annotationsPerAnchor: Map<string, number>) => {
            //     this.annotationsPerAnchor = annotationsPerAnchor;
            // });

            // this.formAnnotationServices[0].getOpenAnnotationSubjectObservable().pipe(takeUntil(this._destroyed)).subscribe((anchorId: string) => {
            //     if (anchorId && anchorId == this.item.id?.toString()) this.showAnnotations(anchorId);
            // });
        // }

        this.tryActivateReviewMode();
	}

    private tryActivateReviewMode(): void {
        if (this.isNew || !this.item?.id) return;
        const planUpdateRequestId = this.route.snapshot.queryParamMap.get('planUpdateRequestId');
        if (!planUpdateRequestId) return;

        this.planUpdateRequestService.preview(Guid.parse(planUpdateRequestId))
            .pipe(takeUntil(this._destroyed))
            .subscribe({
                next: preview => {
                    this.planReviewService.activate(preview);
                    this.ensureReviewOrdinals(preview);
                },
                error: error => this.httpErrorHandlingService.handleBackedRequestError(error),
            });
    }

    private ensurePlanReviewOrdinals(preview: PlanUpdatePreview): void {
        if (!preview?.planFieldChanges?.length) return;

        // Flatten all blueprint fieldSets (description-template style) from the plan's blueprint
        const blueprintSections = this.item?.blueprint?.definition?.sections ?? [];
        const allBlueprintFieldSets: BaseFieldSet[] = [];
        const collectBlueprintFieldSets = (sections: any[]): void => {
            for (const section of sections ?? []) {
                allBlueprintFieldSets.push(...(section.fieldSets ?? []));
                collectBlueprintFieldSets(section.sections ?? []);
            }
        };
        collectBlueprintFieldSets(blueprintSections);

        // Find max ordinal per fieldSet from plan field changes
        const maxOrdinalPerFieldSet = new Map<string, number>();
        for (const fc of preview.planFieldChanges) {
            const match = fc.fieldId?.match(/^([^[]+)\[(\d+)\]\./);
            if (!match) continue;
            const [, fieldSetId, ordinalStr] = match;
            const ordinal = parseInt(ordinalStr, 10);
            const current = maxOrdinalPerFieldSet.get(fieldSetId) ?? -1;
            if (ordinal > current) maxOrdinalPerFieldSet.set(fieldSetId, ordinal);
        }

        const validationErrorModel = this.editorModel.validationErrorModel;

        for (const [fieldSetId, maxOrdinal] of maxOrdinalPerFieldSet) {
            const fieldSetDef = allBlueprintFieldSets.find(fs => fs.id?.toString() === fieldSetId);
            if (!fieldSetDef) continue;

            const fieldSetsGroup = this.formGroup.get('properties.fieldSets') as UntypedFormGroup;
            const fieldSetControl = fieldSetsGroup?.get(fieldSetId.toString()) as UntypedFormGroup;
            const itemsArray = fieldSetControl?.get('items') as UntypedFormArray;
            if (!itemsArray) continue;

            while (itemsArray.length <= maxOrdinal) {
                const nextOrdinal = itemsArray.length;
                const rootPath = `properties.fieldSets[${fieldSetId}].`;
                const newItemModel = new PropertyDefinitionFieldSetItemEditorModel(validationErrorModel);
                newItemModel.ordinal = nextOrdinal;
                for (const fieldDef of fieldSetDef.fields ?? []) {
                    newItemModel.fields.set(fieldDef.id, new FieldEditorModel(validationErrorModel).fromModel({ item: null, ordinal: nextOrdinal, templateField: fieldDef }));
                }
                const newItemForm = newItemModel.buildForm({
                    rootPath: `${rootPath}items[${nextOrdinal}].`,
                    visibilityRulesService: this.visibilityRules,
                });
                itemsArray.push(newItemForm);
            }
        }

        // Pre-register display meta for plan field changes
        const planLabel = this.item?.label ?? null;
        for (const fc of preview.planFieldChanges) {
            if (!fc.changeId) continue;
            const match = fc.fieldId?.match(/^([^[]+)\[(\d+)\]\.(.*)/);
            if (match) {
                const [, fieldSetId, , fieldId] = match;
                const fieldSetDef = allBlueprintFieldSets.find(fs => fs.id?.toString() === fieldSetId);
                const fieldDef = fieldSetDef?.fields?.find(f => f.id?.toString() === fieldId);
                const label = (fieldDef as any)?.data?.label ?? (fieldDef as any)?.label ?? fc.fieldId;
                if (!label) continue;
                this.planReviewService.registerDisplayMeta(fc.changeId, { ordinal: null, label, containerLabel: planLabel });
            } else {
                // System field: lookup by field ID in blueprint definition
                let systemLabel: string | null = null;
                for (const fs of allBlueprintFieldSets) {
                    const f = fs.fields?.find(field => field.id?.toString() === fc.fieldId);
                    if (f) {
                        systemLabel = (f as any)?.data?.label ?? (f as any)?.label ?? null;
                        break;
                    }
                }
                if (systemLabel) {
                    this.planReviewService.registerDisplayMeta(fc.changeId, { ordinal: null, label: systemLabel, containerLabel: planLabel });
                }
            }
        }
    }

    // For each description change in the preview, parse fieldIds of the form
    // "fieldSetId[ordinal].fieldId" and ensure the description's FormArray has
    // enough item rows. Missing rows are appended with empty defaults so the
    // review bar can resolve anchor elements and display field changes.
    private ensureReviewOrdinals(preview: PlanUpdatePreview): void {
        this.ensurePlanReviewOrdinals(preview);

        for (const dc of preview?.descriptionChanges ?? []) {
            if (!dc.descriptionId || !dc.fieldChanges?.length) continue;

            const descInfo = this.planTempStorage.descriptions().get(dc.descriptionId.toString());
            if (!descInfo) continue;

            const { formGroup, visibilityRulesService, validationErrorModel } = descInfo;
            const definition = descInfo.lastPersist?.descriptionTemplate?.definition;
            if (!definition) continue;

            // Flatten all fieldSets from the nested pages→sections→fieldSets tree
            const allFieldSets: BaseFieldSet[] = [];
            const collectFieldSets = (sections: BaseSection[]): void => {
                for (const section of sections ?? []) {
                    allFieldSets.push(...(section.fieldSets ?? []));
                    collectFieldSets(section.sections ?? []);
                }
            };
            collectFieldSets(definition.sections ?? []);

            // Parse all referenced fieldSet ordinals: "fieldSetId[ordinal].fieldId"
            const maxOrdinalPerFieldSet = new Map<string, number>();
            for (const fc of dc.fieldChanges) {
                const match = fc.fieldId?.match(/^([^[]+)\[(\d+)\]\./);
                if (!match) continue;
                const [, fieldSetId, ordinalStr] = match;
                const ordinal = parseInt(ordinalStr, 10);
                const current = maxOrdinalPerFieldSet.get(fieldSetId) ?? -1;
                if (ordinal > current) maxOrdinalPerFieldSet.set(fieldSetId, ordinal);
            }

            for (const [fieldSetId, maxOrdinal] of maxOrdinalPerFieldSet) {
                const fieldSetDef = allFieldSets.find(fs => fs.id === fieldSetId);
                if (!fieldSetDef) continue;

                const fieldSetsGroup = formGroup.get('properties.fieldSets') as UntypedFormGroup;
                const fieldSetControl = fieldSetsGroup?.get(fieldSetId) as UntypedFormGroup;
                const itemsArray = fieldSetControl?.get('items') as UntypedFormArray;
                if (!itemsArray) continue;

                // Add rows until itemsArray covers maxOrdinal
                while (itemsArray.length <= maxOrdinal) {
                    const nextOrdinal = itemsArray.length;
                    const rootPath = `properties.fieldSets[${fieldSetId}].`;
                    const newItemModel = new PropertyDefinitionFieldSetItemEditorModel(validationErrorModel);
                    newItemModel.ordinal = nextOrdinal;
                    for (const fieldDef of fieldSetDef.fields ?? []) {
                        newItemModel.fields.set(fieldDef.id, new FieldEditorModel(validationErrorModel).fromModel({ item: null, ordinal: nextOrdinal, templateField: fieldDef }));
                    }
                    const newItemForm = newItemModel.buildForm({
                        rootPath: `${rootPath}items[${nextOrdinal}].`,
                        visibilityRulesService,
                    });
                    itemsArray.push(newItemForm);
                }
            }

            // Pre-register display meta for all description field changes using the
            // template definition. This covers tableView fieldsets (which never render
            // individual form-field components) and new ordinal rows that may not have
            // mounted yet. The form-field component's effect will overwrite with richer
            // data (including the display ordinal) once it mounts.
            for (const fc of dc.fieldChanges) {
                if (!fc.changeId) continue;
                const match = fc.fieldId?.match(/^([^[]+)\[(\d+)\]\.(.*)/);
                if (!match) continue;
                const [, fieldSetId, , fieldId] = match;
                const fieldSetDef = allFieldSets.find(fs => fs.id === fieldSetId);
                const fieldDef = fieldSetDef?.fields?.find(f => f.id === fieldId);
                const label = fieldDef?.data?.label ?? fc.fieldId;
                if (!label) continue;
                this.planReviewService.registerDisplayMeta(fc.changeId, {
                    ordinal: null,
                    label,
                    containerLabel: descInfo.lastPersist?.label ?? null,
                });
            }
        }
    }

    totalAnnotationCount = computed(() => {
        let annotationCount = 0;
        this.planAnnotationService?.annotationCountAnchorMap()?.forEach((val, key) => annotationCount += val);
        return annotationCount;
    })

	ngAfterViewInit(): void {
        // Initial-load gate: wait for params, queryParams, the first completed planLockService.initialize()
        // (version > 0), and a populated ToC. Fires exactly once via take(1) — subsequent re-initializes
        // are handled by the secondary subscription set up inside this handler.
		combineLatest([
            this.route.params,
            this.route.queryParams,
            (!this.canEditAnySection ? of(1) : this.lockInitObservable),
            this.tocEntryObservable,
        ])
        .pipe(
            filter(([a, b, version, entries]) => !!entries?.length && version > 0),
            take(1),
            takeUntil(this._destroyed),
        )
        .subscribe(([params, queryParams]: [Params, Params, number, RootToCEntry[]]) => {
            const queryParamsApplied = this.parseQueryParams(queryParams);
            const paramsApplied = this.parseParams(params);
            if(!paramsApplied && !queryParamsApplied){
                this.tableOfContent.goToFirstStep();
            }
            // Only for an existing plan that the user can edit: a first-time tour over a read-only
            // preview or the new-plan wizard would point at controls that aren't there.
            if (!this.isNew && this.canEditAnySection) {
                this.tourState.startIfUnseen(this.pageTour);
            }
        });
	}

    private parseQueryParams(params: Params): boolean {
        const sectionId = params['sectionId'];
        const descriptionCopyId = params['descriptionCopyId'];
        const descriptionId = params['descriptionId'];
        this.selectedBlueprintId = params['blueprintId'];
        if (descriptionCopyId && sectionId) {
            this.copyDescriptionToPlan(Guid.parse(descriptionCopyId), sectionId);
            return true;
        } else if (descriptionId) {
            const description = this.item?.descriptions?.find((x) => x.id === descriptionId);
            if(!description){return false;}
            this.tableOfContent.goToDescription(descriptionId)
            return true;
        }
        return false;
    }

    private parseParams(params: Params): boolean {
        const fieldId = params['fieldId'];
        const descriptionId = params['descriptionId'];
        if(descriptionId){
            const description = this.item?.descriptions?.find((x) => x.id === descriptionId);
            if(!description){return false;}
            if(fieldId){
                this.tableOfContent.goToEntry(fieldId, descriptionId);
            }else{
                this.tableOfContent.goToDescription(description.id);
            }

            if(fieldId && this.route.snapshot.data['openDescriptionAnnotation']){
                this.planAnnotationService.setContext({
                    anchor: fieldId,
                    entityId: descriptionId,
                    entityType: AnnotationEntityType.Description
                });
                // this.getFormAnnotationServiceById(descriptionId).openAnnotationDialog(fieldId);
            }
            return true;
        } else if(fieldId){
            const scrollToField = this.route.snapshot.data['scrollToField'] ?? false
            const openAnnotation = this.route.snapshot.data['openAnnotation'] ?? false;

            if(scrollToField || openAnnotation){
                this.tableOfContent.goToEntry(fieldId);
            }
            
            if (openAnnotation) {
                this.showAnnotations(fieldId)
            }
            return true;
        }
        return false;
    }

    setStep(params?: {descriptionId?: string, fieldId: string}){
        const {descriptionId, fieldId} = params;
        this.tableOfContent.goToEntry(fieldId, descriptionId);
        // if(descriptionId){
        //     const description = this.item?.descriptions?.find((x) => x.id === descriptionId);
        //     if(!description){return;}
        //     let fieldStep = this.item?.blueprint.definition.sections.find(s => s.id === description.planDescriptionTemplate?.sectionId)?.ordinal;
        //     // this.tableOfContent.changePlanStep({section: fieldStep, descriptionId, descriptionFieldId: fieldId.toString() ?? null});
        // } else if(fieldId){
        //     //TODO akis refactor with new structure
        //     // let fieldStep = this.item?.blueprint.definition.sections.find(s => s.fields?.some(f => f.id == fieldId))?.ordinal;
        //     // this.tableOfContent?.changePlanStep({section: fieldStep, fieldId: fieldId});
        // }
    }

	getItem(itemId: Guid, successFunction: (item: Plan) => void) {
		this.planService.getSingle(itemId, PlanEditorEntityResolver.lookupFields())
			.pipe(map(data => data as Plan), takeUntil(this._destroyed))
			.subscribe(
                {
                    next: (data) => successFunction(data),
                    error: (error) => this.onCallbackError(error)
                }
			);
	}

    private firstRun = true;
	prepareForm(data: Plan, includeConsent?: boolean) {
		try {
            this.resetMetadata();
            this.getPermissions(data);
			if (data?.blueprint?.definition?.sections != null) {
				data.blueprint.definition.sections = data.blueprint.definition.sections.sort((s1, s2) => s1.ordinal - s2.ordinal);
				for (let i = 0; i < data.blueprint.definition.sections.length; i++) {
					const section = data.blueprint.definition.sections[i];
					section.fieldSets = section.fieldSets?.sort((a, b) => a.ordinal - b.ordinal)
						.map(fs => ({ ...fs, fields: fs.fields?.sort((f1, f2) => f1.ordinal - f2.ordinal) }));
				}
			}
            this.isDeleted = data ? data.isActive === IsActive.Inactive : false;
			if (data) {
				if (data.descriptions && !this.isDeleted) {
					if (data.status?.internalStatus == PlanStatusEnum.Finalized) {
						data.descriptions = data.descriptions.filter(x => x.isActive === IsActive.Active && x.status.internalStatus === DescriptionStatusEnum.Finalized);
					} else {
						data.descriptions = data.descriptions.filter(x => x.isActive === IsActive.Active && x.status.internalStatus !== DescriptionStatusEnum.Canceled);
					}
				} else {
                    data.descriptions = [];
                }
				if (data.planDescriptionTemplates && !this.isDeleted) {
					data.planDescriptionTemplates = data.planDescriptionTemplates.filter(x => x.isActive === IsActive.Active);
				}
				if (data.entityDois && data.entityDois.length > 0) data.entityDois = data.entityDois.filter(x => x.isActive === IsActive.Active);
                if (data.planReferences && data.planReferences.length > 0 && !this.isDeleted) data.planReferences = data.planReferences?.filter(x => x.isActive == IsActive.Active);
                this.loadStatusLogos(data);

			}
            this.editorModel = data ? new PlanEditorModel().fromModel(data, includeConsent) : new PlanEditorModel();
			this.item = data;
			this.selectedBlueprint = data?.blueprint;

            //add items to temp storage 
            this.item?.planDescriptionTemplates?.forEach((planDescriptionTemplate) => {
				this.planTempStorage.setPlanDescriptionTemplate(planDescriptionTemplate);
				//create
			});

            this.setAnnotationPermissions(data);
			// this.item?.descriptions?.forEach((planDescription) => {
			// 	const formAnnotationService = new FormAnnotationService(this.annotationService, this.uiNotificationService, this.language );
			// 	formAnnotationService.init(planDescription.id,AnnotationEntityType.Description);
			// 	this.formAnnotationServices.push( formAnnotationService);
            // })

			// if (data && data.id) {
			// 	const descriptionSectionPermissionResolverModel: DescriptionSectionPermissionResolver = {
			// 		planId: data.id,
			// 		sectionIds: data?.blueprint?.definition?.sections?.map(x => x.id),
			// 		permissions: [AppPermission.EditDescription, AppPermission.DeleteDescription]
			// 	}
			// }

			this.buildForm();

			if (!this.canEditAnySection) {
				this.formGroup.disable();
			}else if(!this.isNew){
				this.planLockService.initialize(this.item).pipe(takeUntil(this._destroyed)).subscribe()
            }
            if(this.firstRun && !this.isDeleted && !this.belongsToCurrentTenant){
                this.tenantWarning();
            }
            this.firstRun = false;

            //consent
            if (this.needConsent()) {
                this.consentWarning();
            }

            this.planAnnotationService.init(this.item, this.planTempStorage)
            // FE: ensure the FAIR evaluators list is populated for this plan (the constructor effect
            // covers the async-load case; this covers the case where the service loaded earlier).
            this.loadEvaluators();

            if(data?.blueprint?.definition){
                this.visibilityRules.setContext(data.blueprint.definition, this.formGroup?.controls?.properties);
            }

		} catch (error) {
			this.logger.error('Could not parse Plan item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

    setAnnotationPermissions(item: Plan){
        this.canAnnotatePerFieldSet = new Map<string, boolean>([]);
        this.canAnnotatePerDescription = new Map<string, boolean>([]);
        if(this.permissionPerSection){
            item?.blueprint?.definition?.sections?.forEach((section) => {
                (section?.fieldSets ?? []).forEach((fieldSet) => this.canAnnotatePerFieldSet.set(fieldSet.id, this.canAnnotateSection(section.id)))
            });
            item?.descriptions?.forEach((desc) => {
                const hasAnnotatePermission = desc?.authorizationFlags?.some(x => x === AppPermission.AnnotateDescription);
                this.canAnnotatePerDescription.set(desc.id.toString(), desc.belongsToCurrentTenant && desc.isActive === IsActive.Active && hasAnnotatePermission);
            })
        }
    }

	buildForm() {
		this.formGroup = this.editorModel.buildForm({
            visibilityRules: this.visibilityRules,
            disabled: !this.canEditAnySection
        });
        // FE-0a: wire the per-entity EditorSessionState selector to live component state. Additive —
        // no consumer is rewired onto it yet (FE-0b does that). Thunks keep it reading current state.
        this.editorSessionState.attach({
            getPlanForm: () => this.formGroup,
            getItem: () => this.item,
            getIsNew: () => this.isNew,
            getNewDescriptionIds: () => this.newDescriptionIds,
        });
        const descriptions = this.item?.isActive === IsActive.Inactive ? this.item?.descriptions : this.item?.descriptions?.filter((x) => x.isActive === IsActive.Active);
        descriptions?.forEach((description) => {
            this.buildDescriptionForm({description});
        })
	}


    //For destroy observable purposes
    // fieldValueChangeMap = new Map<Guid, Subscription[]>([]);
    // checkSystemFields(){
        
    // }



    //** LOCKS */
    // formChange$: Subscription;
    // protected lockOnFormChange(targetId: Guid){
    //     this.formChange$?.unsubscribe();

    //     this.formChange$ = this.formGroup.valueChanges
    //     .pipe(take(1)).subscribe((change) => {
    //         this.planLockService.lockOnEdit(this.item.id, LockTargetType.Plan)
    //     })
    // }
    //

	refreshData(): void {
        this.planLockService.reset()
        .subscribe({
            complete: () => {
                
                this.getItem(this.editorModel.id, (data: Plan) => {
                    this.breadcrumbService.addIdResolvedValue(data.id.toString(), data.label);
                    const planCopy: Plan = {...data, descriptions: []}; //add plan info to desc w/o requesting it from the lookup twice
                    data.descriptions?.forEach((desc) => desc.plan = planCopy);
                    this.prepareForm(data);

                    // if (this.isNew === false && this.step() === 0) this.nextStep();
                });
            },
            error: (error) => this.onCallbackError(error)
        })
	}


	refreshOnNavigateToData(id?: Guid): void {
		this.formGroup.markAsPristine();
        this.newDescriptionIds = [];
        this.showPlanErrors = false

		if (this.isNew) {
			const route = [];
			route.push(this.routerUtils.generateUrl('/plans/edit/' + id));
			this.router.navigate(route, { queryParams: { 'lookup': this.queryParamsService.serializeLookup(this.lookupParams), 'lv': ++this.lv }, replaceUrl: true, relativeTo: this.route });
		} else {
			this.refreshData();
		}
	}

    get enableSave(): boolean {
        // FE-0b: enable Save whenever anything in the session is dirty/new (form.dirty-based), instead
        // of keying off editLockIds. Removes the "dirty form but disabled Save dropdown" disagreement.
        if (this.planReviewService.active()) {
            // Block save until all review decisions are resolved.
            if (this.planReviewService.pendingCount() > 0) return false;
            // All decisions resolved — always allow save so the user can submit a reject-only review
            // (which doesn't dirty the form but still needs to reach the backend).
            return true;
        }
        return this.isNew || this.editorSessionState.isAnythingDirty();
    }

	persistEntity(onSuccess?: (response) => void): void {
		const formData = JSON.parse(JSON.stringify(this.formGroup.getRawValue())) as PlanPersist;

		//Transform to persist
		//Transform descriptionTemplates
		formData.descriptionTemplates = [];
		for (const sectionId in (this.formGroup.get('descriptionTemplates') as UntypedFormGroup).controls) {
			if (this.formGroup.get('descriptionTemplates').get(sectionId).value != undefined) {
				formData.descriptionTemplates.push(
					...(this.formGroup.get('descriptionTemplates').get(sectionId).value as Guid[]).map(x => { return { sectionId, descriptionTemplateGroupId: x } })
				);
			}
		}

        // FE-V (clear-on-save): strip values of plan blueprint fields hidden by visibility rules from
        // the payload (formData is a deep copy, so the live form is untouched).
        this.visibilityRules?.stripHiddenFieldValues((formData as any)?.properties);

        const resetStep = !!this.selectedDescription() && this.newDescriptionIds?.includes(this.selectedDescription().toString());

        // FE-4: build the dirty-description payloads once, then decide the save shape.
        const dirtyDescriptions = this.buildDirtyDescriptionPersists();
        const descriptionCount = dirtyDescriptions.length;
        // The plan is persisted only when the user can save it AND holds its lock (or it's new) — same
        // gate as before. When the plan IS being persisted we use the atomic endpoint so the plan and
        // its descriptions commit in one backend transaction (no partial-save window). When only
        // descriptions changed, we keep the descriptions-only persist-multiple call.
        const planShouldPersist = this.canSavePlan && (this.isLockedByUser || this.isNew);

        const save$: Observable<Plan> = planShouldPersist
            ? this.planService.persistWithDescriptions(
                { plan: formData, descriptions: dirtyDescriptions } as PlanWithDescriptionsPersist,
                PlanEditorEntityResolver.lookupFields()
              ).pipe(takeUntil(this._destroyed))
            : this.persistDescriptions().pipe(map(() => null as Plan));

        // If the editor is in review mode, attach the apply payload so the backend can close
        // the plan update request row. Both persist paths receive the payload; the second call
        // is a safe no-op because closePlanUpdateRequestIfPresent checks for Pending status.
        let reviewApply: PlanUpdateRequestApply | undefined;
        if (this.planReviewService.active()) {
            const apply = this.planReviewService.buildApply();
            if (apply) {
                apply.previewPlanVersion = undefined;
                reviewApply = apply;
                formData.planUpdateRequestApply = apply;
            }
        }

        this.enqueueService.enqueueExhaustChannel(
            save$.pipe(
                switchMap((value: Plan) => this.deleteReviewDescriptions().pipe(map(() => value))),
				switchMap((value: Plan) => {
                    // If the editor was in review mode, clear it once the save succeeds.
                    if (reviewApply) this.planReviewService.clear();
                    if (planShouldPersist) {
                        // value is the persisted Plan. Preserve the existing "kicked from the plan" guard.
                        if ((value?.planUsers?.find(x => x?.user?.id == this.authService.userId() && x.isActive === IsActive.Active)) || (
                                value?.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.AssignPlanUsers) ||
                                this.authService.hasPermission(AppPermission.AssignPlanUsers)
                            )
                        ) {
                            if(resetStep){ this.tableOfContent.goToFirstStep(); }
                            if(onSuccess){ onSuccess(value); }
                            else { this.onSaveSuccess(value, true, descriptionCount); }
                        } else {
                            this.closeSafe();
                        }
                    } else {
                        if(resetStep){ this.tableOfContent.goToFirstStep(); }
                        if(onSuccess){ onSuccess(value); }
                        else { this.onSaveSuccess(value, false, descriptionCount); }
                    }
                    return EMPTY;
                }),
                catchError((error) => of(this.onCallbackError(error)))
            )
        )
	}

    checkDraftValidity() {
        if (this.isNew) return true;

		this.formService.touchAllFormFields(this.formGroup);
		const errorMessages = [];
		
		if (this.formGroup?.get('users')?.hasError('planOwnerRequired')) {
			errorMessages.push(this.language.instant('PLAN-EDITOR.FORM-VALIDATION-DISPLAY-DIALOG.PLAN-OWNER-NOT-FOUND'));
		}
		
		if (errorMessages.length > 0) {
			this.showValidationErrorsDialog(errorMessages);
			return false;
		}
		return true;
	}


    private showValidationErrorsDialog(errmess?: string[]) {

        this.dialog.open(FormValidationErrorsDialogComponent, {
            disableClose: true,
            data: {
                errorMessages: errmess
            },
        });

    }


	formSubmit(onSuccess?: (response) => void): void {
		this.formService.removeAllBackEndErrors(this.formGroup);
        if(this.isNew){
            this.persistEntity(onSuccess);
            return; 
        }
        if(this.canEditAnySection && (this.formGroup.controls?.label?.valid || this.formGroup.controls?.label?.disabled) 
            && this.descriptionBaseValid() && this.checkDraftValidity()
        ){
            const oldUser = this.editorModel?.existingPlanUsers?.find(y => y.user?.id == this.authService.userId());
            if (!this.isNew && oldUser) {
                
                const formData = JSON.parse(JSON.stringify(this.formGroup.value)) as PlanPersist;
                const stillIncluded = formData?.users?.find(x => x.user == oldUser?.user?.id) != null || false;
                if (!stillIncluded) {
                    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
                        data: {
                            message: this.language.instant('PLAN-EDITOR.DELETE-YOURSELF-FROM-ENTIRE-PLAN'),
                            confirmButton: this.language.instant('PLAN-EDITOR.ACTIONS.SAVE'),
                            cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
                            isDeleteConfirmation: false
                        }
                    });
                    dialogRef?.afterClosed()?.subscribe(result => {
                        if (result) {
                            this.persistEntity(onSuccess);
                        }
                    });
                } else {
                    this.persistEntity(onSuccess);
                }
                
            } else {
                this.persistEntity(onSuccess);
            }
        } else {
            this.showPlanErrors = true;
            this.formGroup.controls.descriptions?.controls?.forEach(desc => {
                if(!desc.valid){
                    this.showDescErrors.add(desc.value.id.toString())
                }
            });
        }
	}

	discardChanges() {
		if (!this.isNew && !this.planReviewService.active() && !this.editorSessionState.isAnythingDirty()) {
			this.closeSafe({emitSuccessNotification: false});
			return;
		}
		let messageText: string;
		let confirmButtonText: string;
		let cancelButtonText: string;

		if (this.isNew) {

			messageText = this.language.instant('PLAN-EDITOR.ACTIONS.DISCARD.DISCARD-NEW-MESSAGE');
			confirmButtonText = this.language.instant('PLAN-EDITOR.ACTIONS.DISCARD.DISCARD-NEW-CONFIRM');
			cancelButtonText = this.language.instant('PLAN-EDITOR.ACTIONS.DISCARD.DISCARD-NEW-DENY');
		} else {
			messageText = this.language.instant('PLAN-EDITOR.ACTIONS.DISCARD.DISCARD-EDITED-MESSAGE');
			confirmButtonText = this.language.instant('PLAN-EDITOR.ACTIONS.DISCARD.DISCARD-EDITED-CONFIRM');
			cancelButtonText = this.language.instant('PLAN-EDITOR.ACTIONS.DISCARD.DISCARD-EDITED-DENY');
		}


		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: messageText,
				confirmButton: confirmButtonText,
				cancelButton: cancelButtonText,
				isDeleteConfirmation: true
			},
			maxWidth: '40em'
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result) {
				setTimeout(x => {
					// if (this.isNew){ 
                    //     this.step.set(0);
                    // }
					// else {
                    //     this.step.set(this.step() > 0 ? 1 : 0);
                    // }
					this.closeSafe({emitSuccessNotification: false});
				});
			}
		});

	}

	delete() {
		const value = this.formGroup.value;
		if (value.id) {
			const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
				maxWidth: '300px',
				data: {
					message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-ITEM'),
					confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
					cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
				}
			});
			dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
				if (result) {
					this.planService.delete(value.id).pipe(takeUntil(this._destroyed))
						.subscribe({
							complete: () => this.onCallbackDeleteSuccess(),
							error: (error) => this.onCallbackError(error)
                        });
				}
			});
		}
	}

	saveAndClose(): void {
		this.formSubmit((data) => {
            this.closeSafe();
		});
	}

    private closeSafe(params?: {emitSuccessNotification?: boolean}) {
        const {emitSuccessNotification = true} = params ?? {};
        this.formGroup.markAsPristine();
        // The navigation below is guarded by canDeactivate -> editorSessionState.isAnythingDirty(),
        // which also checks each description's own state. Save & Close never refetches the plan (unlike
        // Save & Continue's refreshData(), which rebuilds descriptions from fresh server data), so a
        // just-saved description would otherwise still report itself dirty — either because its form
        // hadn't been marked pristine, or because it's a brand-new description (isNew descriptions are
        // always considered dirty, regardless of form state) whose isNew flag was never cleared.
        this.planTempStorage.markAllDescriptionsPersisted();
        this.newDescriptionIds = [];
        if(emitSuccessNotification){
            this.successNotification();
        }
        this.planLockService.reset().subscribe(() => {
            this.router.navigate([this.routerUtils.generateUrl('/plans/')]);
        })
    }

	// nextStep() {
	// 	this.step.set(this.step() < this.selectedBlueprint.definition.sections.length ? this.step() + 1 : this.step());
	// }
    
	hasNotDoi() {
		return (this.item.entityDois == null || this.item.entityDois.length == 0);
	}

    needConsent() {
        return this.formGroup?.get('properties')?.get('includeConsent')?.value != true && !this.isDeleted && this.belongsToCurrentTenant && (this.item?.properties?.consent == null) && this.item?.blueprint?.definition?.planCreationConsent?.enabled
    }

	persistStatus(status: PlanStatus) {
        // FE-3: instead of silently doing nothing, tell the user why a status change can't proceed.
        const blockedReason = this.statusChangeBlockedReason();
        if(this.isLoading()){ return; }
        if(blockedReason){
            this.uiNotificationService.snackBarNotification(this.language.instant(blockedReason), SnackBarNotificationLevel.Warning);
            return;
        }
		if (status.internalStatus != null && status.internalStatus === PlanStatusEnum.Finalized) {
			this.finalize(status.id);
		} else if (this.item.status.internalStatus === PlanStatusEnum.Finalized){
			this.reverseFinalization(status.id);
		} else {
            this.enqueueService.enqueueExhaustChannel(
                this.planService.setStatus(this.item.id, status.id)
                .pipe(
                    finalize(() => {
                        this.onCallbackSuccess();
                    }), 
                    catchError((error: any) =>
                        of(this.onCallbackError(error))
                    )
                )
            )
		}
	}

	finalize(newStatusId) {
		const dialogRef = this.dialog.open(PlanFinalizeDialogComponent, {
			maxWidth: 'min(500px, 90vw)',
			data: {
				plan: this.item,
                descriptionMetadata: this.item.descriptions?.map((desc) => this.planTempStorage.descriptions().get(desc.id.toString())).filter((x) => !!x && !x.isNew) ?? [],
                // FE-2: plan sections with pending required/invalid fields, for the dialog's "sections
                // needing input" guidance + jump-to.
                incompleteSections: this.incompletePlanSections
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe((result: PlanFinalizeDialogOutput) => {
			if (result) {
                // FE-2: the user asked to fix a specific plan section — navigate there and show errors.
                if(result.navigateToSectionId){
                    this.showPlanErrors = true;
                    this.tableOfContent.goToEntry(result.navigateToSectionId);
                    return;
                }
                // FE-3: the user asked to fix a specific description's errors — navigate there and show
                // its errors instead of finalizing.
                if(result.navigateToDescriptionId){
                    this.showDescErrors.add(result.navigateToDescriptionId.toString());
                    this.tableOfContent.goToDescription(result.navigateToDescriptionId);
                    return;
                }
                if(!result.cancelled && result.planValid){
                    this.showPlanErrors = false;
                    this.enqueueService.enqueueExhaustChannel(
                        this.planService.setStatus(this.item.id, newStatusId, result.descriptionsToBeFinalized)
                        .pipe(
                            takeUntil(this._destroyed),
                            tap(() => {
                                this.onCallbackSuccess();
                                this.router.navigate([this.routerUtils.generateUrl(['/plans/overview', this.item.id.toString()], '/')]);
                            }),
                            catchError((error) => of(this.onCallbackError(error)))
                        )
                    )
                }else {
                    this.formService.touchAllFormFields(this.formGroup);
                    this.showPlanErrors = true;
                    Array.from(this.planTempStorage.descriptions())?.forEach(([key, metadata]) => {if(!metadata.isNew){ 
                        this.showDescErrors.add(metadata.lastPersist.id.toString())}
                    })
                }
			}
		});

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
                this.enqueueService.enqueueExhaustChannel(
                    this.planService.setStatus(this.item.id, newStatusId)
                    .pipe(
                        takeUntil(this._destroyed),
                        finalize(() => this.onCallbackSuccess()),
                        catchError((error) => of(this.onCallbackError(error)))
                    )
                )
			}
		});
	}

	clearErrorModel() {
		this.editorModel.validationErrorModel.clear();
		this.formService.validateAllFormFields(this.formGroup);
	}

	//
	//
	// Steps
	//
	//

	get maxSteps(): number {
		return this.item?.blueprint?.definition?.sections?.length ?? 0;
	}

	isFormControlValid(controlName: string): boolean {
		if (!this.formGroup?.get(controlName)) return true;
		if (!this.formGroup.get(controlName).touched) return true;
        //todo mchouliara check touched description forms
		return this.formGroup.get(controlName).valid;
	}

	//
	//
	// Blueprint
	//
	//
	/** The blueprint wizard has laid out: first-visit "start a plan" tour, once per user. */
	onNewPlanWizardReady(): void {
		if (this.isNew) { this.tourState.startIfUnseen(this.newPlanTour); }
	}

	createPlanWithBlueprint(blueprint: PlanBlueprint) {
		if(!blueprint){
			this.formGroup.markAllAsTouched();
			return;
		}
		this.formGroup.controls.blueprint.setValue(blueprint.id);
        this.formGroup.controls.label.setValue(`${blueprint.label} ${this.language.instant('PLAN-LISTING.PLAN')}`);
        this.selectedBlueprint = blueprint;
        this.buildFormAfterBlueprintSelection();
        this.formSubmit();
		// this.planBlueprintService.getSingle(this.formGroup.get('blueprint').value, PlanEditorEntityResolver.blueprintLookupFields()).pipe(takeUntil(this._destroyed))
		// 	.subscribe({
        //         next: (data) => {
        //             this.selectedBlueprint = data;
        //             this.buildFormAfterBlueprintSelection();
        //             this.nextStep();
        //         },
		// 		error: (error) => this.httpErrorHandlingService.handleBackedRequestError(error)
        //     });
	}
	private buildFormAfterBlueprintSelection() {
		const plan: Plan = {
			label: this.formGroup.get('label').value,
			description: this.formGroup.get('description').value,
			blueprint: this.selectedBlueprint,
		}

        if (this.selectedBlueprint?.definition?.planCreationConsent?.enabled) {
            // add includeConsent to true because this action is only called from plan blueprint preview component ts for creating a plan
            this.prepareForm(plan, true);
        } else {
            this.prepareForm(plan);
        }
		
	}

	//
	//
	// Descriptions
	//
	//

    showDescErrors = new Set<string>([]);

    protected buildDescriptionForm(params:{description: Description, isNew?: boolean}): FormGroup<DescriptionEditorForm>{
        const {description, isNew = false} = params;
        if(this.showDescErrors?.size) { this.showDescErrors.delete(description?.id?.toString())};

        const visibilityRules = new VisibilityRulesService(this.formService);

        const editorModel = new DescriptionEditorModel()
        const formGroup = editorModel.fromModel(description, description.descriptionTemplate)
            .buildForm(null, DescriptionEditorModel.isViewOnlyDescription(description), visibilityRules);
	
        if(!isNew){
            formGroup.controls.descriptionTemplateId.disable();
        }

        if(description?.descriptionTemplate?.definition){
            visibilityRules.setContext(description.descriptionTemplate.definition, formGroup?.controls?.properties);
        }
        // const section = this.item.blueprint.definition.sections?.find((x) => x.id === description.planDescriptionTemplate.sectionId);
        const descriptionInfo: DescriptionInfo = {
            lastPersist: description, 
            formGroup,
            visibilityRulesService: visibilityRules,
            validationErrorModel: editorModel.validationErrorModel,
            isNew
        }
        this.planTempStorage.setDescription(descriptionInfo);
        return formGroup;
    }

    private deleteReviewDescriptions(): Observable<void> {
        if (!this.planReviewService.active()) return of(null);
        const toDelete = this.planReviewService.deleteDescriptionChanges()
            .filter(dc => this.planReviewService.decisionFor(dc.changeId) === 'accepted' && dc.descriptionId)
            .map(dc => dc.descriptionId as Guid);
        if (!toDelete.length) return of(null);
        const apply = this.planReviewService.buildApply()!;
        return forkJoin(toDelete.map(id => this.descriptionService.deleteWithApply(id, apply).pipe(catchError(() => of(null)))))
            .pipe(map(() => null), takeUntil(this._destroyed));
    }

    private _promptedDeprecatedTemplateIds = new Set<string>();

    private promptDeprecatedTemplateUpdate(description: Description): void {
        const descId = description.id?.toString();
        if(this._promptedDeprecatedTemplateIds.has(descId)){ return; }
        this._promptedDeprecatedTemplateIds.add(descId);

        const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
            data: {
                message: this.language.instant('DESCRIPTION-EDITOR.DEPRECATED-DESCRIPTION-TEMPLATE.MESSAGE'),
                confirmButton: this.language.instant('DESCRIPTION-EDITOR.DEPRECATED-DESCRIPTION-TEMPLATE.ACTIONS.UPDATE'),
                cancelButton: this.language.instant('DESCRIPTION-EDITOR.DEPRECATED-DESCRIPTION-TEMPLATE.ACTIONS.CANCEL'),
                isDeleteConfirmation: false
            },
            maxWidth: '40em'
        });
        dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
            if(!result){ return; }
            this.descriptionService.updateDescriptionTemplate({ id: description.id, hash: description.hash })
            .pipe(
                takeUntil(this._destroyed),
                switchMap(() => this.descriptionService.getSingle(description.id, [...DescriptionEditorHelper.BaseDescriptionLookupFields(), ...DescriptionEditorHelper.DescriptionTemplateInDescriptionLookupFields()]))
            )
            .subscribe({
                next: (updated) => {
                    updated.plan = this.item;
                    this.buildDescriptionForm({description: updated});
                },
                error: (error) => this.onCallbackError(error)
            });
        });
    }

    lockedIds = computed(() => this.planLockService.editLockIds());

    /**
     * FE-0b/FE-V: build the DescriptionPersist payloads for the descriptions that need saving. The
     * save-set is derived from form.dirty (via the unified selector), not editLockIds — closing the
     * silent-exclusion bug. New descriptions are sent with id:null. Hidden field values are stripped
     * here (clear-on-save). Shared by the atomic save path (FE-4) and the descriptions-only path.
     */
    private buildDirtyDescriptionPersists(): DescriptionPersist[] {
        const dirtyIds = this.editorSessionState.dirtyDescriptionIds();
        if(!dirtyIds?.length){ return []; }
        const editedDescriptions: DescriptionPersist[] = [];
        const newDescriptions: DescriptionPersist[] = [];
        dirtyIds.forEach((id) => {
            const info = this.planTempStorage.descriptions()?.get(id);
            const raw = info?.formGroup?.getRawValue() as unknown as DescriptionPersist;
            if(!raw){ return; }
            // FE-V (clear-on-save): strip values of fields hidden by this description's visibility rules.
            info.visibilityRulesService?.stripHiddenFieldValues((raw as any)?.properties);
            if(this.newDescriptionIds?.includes(id)){
                newDescriptions.push({ ...(raw as unknown as Partial<DescriptionPersist>), id: null } as DescriptionPersist);
            } else {
                editedDescriptions.push(raw);
            }
        });
        return [...editedDescriptions, ...newDescriptions].filter((x) => x);
    }

    private persistDescriptions(): Observable<Description[]>{
        const descriptions = this.buildDirtyDescriptionPersists();
        if(!descriptions.length){
            return of(null);
        }
        return this.descriptionService.persistMultiple({descriptions})
        .pipe(takeUntil(this._destroyed));
    }

    // FE-0b: a method (not a computed) because it reads form.dirty/valid which are not signals; it is
    // evaluated on demand in formSubmit. Validates exactly the descriptions that will be saved.
    protected descriptionBaseValid(): boolean {
        const forms = this.editorSessionState.dirtyDescriptionIds()
            ?.map((id) => this.planTempStorage.descriptions().get(id)?.formGroup)
            ?.filter((f): f is FormGroup<DescriptionEditorForm> => !!f);
        return !forms?.some((form) => form.disabled || DescriptionEditorModel.baseFieldsAreInvalid((form)));
    }


    // protected addDescriptionToSection(section: RootSection){
    //     let res$: Observable<Description>;
    //     const descriptionTemplates = this.item.planDescriptionTemplates?.filter((x) => x.sectionId === section.id);
    //     if(descriptionTemplates?.length > 1 || section.prefillingSourcesEnabled){
    //         res$ = this.dialog.open(NewDescriptionDialogComponent, {
    //             width: '590px',
    //             minHeight: '200px',
    //             restoreFocus: false,
    //             data: {
    //                 plan: this.item,
    //                 planSectionId: section.id,
    //             },
    //             panelClass: 'custom-modalbox'
    //         }).afterClosed().pipe((map((x) => x?.description)))
    //     } else {
    //         const descriptionTemplate = this.item.planDescriptionTemplates?.find((x) => x.sectionId === section.id && x.isActive == IsActive.Active)?.currentDescriptionTemplate;
    //         res$ = of(NewDescriptionDialog.createDescription({sectionId: section.id, descriptionTemplate, plan: this.item}))
    //     }
    //     res$.subscribe((newDesc: Description) => {
    //         if(!newDesc?.descriptionTemplate?.id){ return; }
    //         const description = newDesc;    
    //         const newId = this.generateTempDescriptionId;
    //         this.newDescriptionIds.push(newId);
    //         description.id = Guid.parse(newId);

    //         const descriptionTemplate = this.planTempStorage.getDescriptionTemplate(description.descriptionTemplate?.id);
    //         (descriptionTemplate ? of(descriptionTemplate) : this.descriptionTemplateService.getSingle(description.descriptionTemplate.id, [
    //             ...DescriptionTemplateEditorResolver.baseLookupFields(), 
    //             ...DescriptionTemplateEditorResolver.definitionLookupFields(),
    //             nameof<DescriptionTemplate>(x => x.language),
    //             [nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>(x => x.id)].join('.'),
    //             [nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>(x => x.name)].join('.'),
    //         ]).pipe(takeUntil(this._destroyed), catchError(() => of(null)))).subscribe((descriptionTemplate) => {
    //             this.planTempStorage.setDescriptionTemplate(descriptionTemplate);
    //             description.descriptionTemplate = descriptionTemplate;
    //             this.onNewDescription(description);
    //         })
    //     })

    // }

    protected onNewDescription(description: Description, sectionId: string){
        if(!description){ return; }
        this.item.descriptions.push(description);
        this.buildDescriptionForm({description, isNew: true});
        // this.tableOfContent?.addDescriptionToToc(description.id, sectionId);
        const hasAnnotatePermission = description?.authorizationFlags?.some(x => x === AppPermission.AnnotateDescription);
        this.canAnnotatePerDescription.set(description.id.toString(), description.belongsToCurrentTenant && description.isActive === IsActive.Active && hasAnnotatePermission);
        this.selectedDescription.set(description.id);
        setTimeout(() => {
            this.tableOfContent.goToDescription(description.id);
        })
    }

    protected onReviewNewDescription(event: AcceptedNewDescription): void {
        const pdt = event.planDescriptionTemplate;
        if (!pdt?.id || !pdt.sectionId) return;

        const planUpdateRequestId = this.planReviewService.preview()?.planUpdateRequestId;
        if (!planUpdateRequestId) return;

        const descriptionTemplate$ = (() => {
            const cached = this.planTempStorage.getDescriptionTemplate(pdt.currentDescriptionTemplate.id);
            return cached ? of(cached) : this.descriptionTemplateService.getSingle(pdt.currentDescriptionTemplate.id, [
                ...DescriptionTemplateEditorResolver.baseLookupFields(),
                ...DescriptionTemplateEditorResolver.definitionLookupFields(),
                nameof<DescriptionTemplate>(x => x.language),
                [nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>(x => x.id)].join('.'),
                [nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>(x => x.name)].join('.'),
            ]).pipe(catchError(() => of(null)));
        })();

        const description$ = this.planUpdateRequestService.descriptionFromPreview(
            planUpdateRequestId, event.datasetId, pdt.id
        ).pipe(catchError(() => of(null)));

        forkJoin({ descriptionTemplate: descriptionTemplate$, description: description$ })
            .pipe(takeUntil(this._destroyed))
            .subscribe(({ descriptionTemplate, description }) => {
                if (!descriptionTemplate) return;
                if (!description) return;

                description.plan = this.item;
                description.isActive = IsActive.Active;
                description.belongsToCurrentTenant = true;
                description.authorizationFlags = [AppPermission.EditDescription, AppPermission.DeleteDescription, AppPermission.FinalizeDescription, AppPermission.AnnotateDescription];
                description.descriptionTemplate = descriptionTemplate;
                description.planDescriptionTemplate = event.planDescriptionTemplate;
                if (description.descriptionTemplate) {
                    this.planTempStorage.setDescriptionTemplate(description.descriptionTemplate);
                }

                const newId = this.generateTempDescriptionId;
                this.newDescriptionIds.push(newId);
                description.id = Guid.parse(newId);
                this.onNewDescription(description, pdt.sectionId);
                this.reviewBar?.registerAddedDescription(event.changeId, description.id);
            });

        // this.planUpdateRequestService.descriptionFromPreview(planUpdateRequestId, event.datasetId, pdt.id)
        //     .pipe(takeUntil(this._destroyed), catchError(() => of(null)))
        //     .subscribe((description: Description) => {
        //         if (!description) return;
        //         description.plan = this.item;
        //         description.isActive = IsActive.Active;
        //         description.belongsToCurrentTenant = true;
        //         description.authorizationFlags = [AppPermission.EditDescription, AppPermission.DeleteDescription, AppPermission.FinalizeDescription, AppPermission.AnnotateDescription];
        //         if (description.descriptionTemplate) {
        //             this.planTempStorage.setDescriptionTemplate(description.descriptionTemplate);
        //         }
        //         const newId = this.generateTempDescriptionId;
        //         this.newDescriptionIds.push(newId);
        //         description.id = Guid.parse(newId);
        //         this.onNewDescription(description);
        //         this.reviewBar?.registerAddedDescription(event.changeId, description.id);
        //     });
    }

    protected onRevertReviewDescription(event: RevertedNewDescription): void {
        const descriptionId = event.descriptionId;
        if (!descriptionId) return;
        const index = this.newDescriptionIds.indexOf(descriptionId.toString());
        if (index >= 0) this.newDescriptionIds.splice(index, 1);
        this.onRemoveSuccess(descriptionId);
    }

    protected onReviewDeleteDescription(event: AcceptedDeleteDescription): void {
        if (!event.descriptionId) return;
        this.onRemoveSuccess(event.descriptionId);
    }

    protected onUndoReviewDeleteDescription(event: AcceptedDeleteDescription): void {
        const descriptionId = event.descriptionId;
        if (!descriptionId) return;
        const description = this.item?.descriptions?.find(d => d.id?.toString() === descriptionId.toString());
        if (!description) return;
        const descriptionTemplate = this.planTempStorage.getDescriptionTemplate(description.descriptionTemplate?.id);
        (descriptionTemplate ? of(descriptionTemplate) : this.descriptionTemplateService.getSingle(description.descriptionTemplate.id, [
            ...DescriptionTemplateEditorResolver.baseLookupFields(),
            ...DescriptionTemplateEditorResolver.definitionLookupFields(),
            nameof<DescriptionTemplate>(x => x.language),
            [nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>(x => x.id)].join('.'),
            [nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>(x => x.name)].join('.'),
        ]).pipe(takeUntil(this._destroyed), catchError(() => of(null)))).subscribe(dt => {
            if (!dt) return;
            this.planTempStorage.setDescriptionTemplate(dt);
            description.descriptionTemplate = dt;
            this.onNewDescription(description, description.planDescriptionTemplate?.sectionId);
        });
    }

    protected onShowDescriptionErrors(id: string){
        this.showDescErrors.add(id);
    }

    protected removeDescription(descriptionInfo: DescriptionInfo) {
        const descriptionId = descriptionInfo?.lastPersist?.id;
        if(!descriptionId){ return; }
        if(descriptionInfo.isNew){
            const index = this.newDescriptionIds.indexOf(descriptionId.toString());
            if(index >= 0){
                this.newDescriptionIds.splice(index, 1);
            }
            this.onRemoveSuccess(descriptionId);
            return;
        }
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			maxWidth: '300px',
			data: {
				message: `${this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-ITEM')} ${this.language.instant('GENERAL.CONFIRMATION-DIALOG.CANNOT-BE-UNDONE')}`,
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.DELETE'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
				isDeleteConfirmation: true
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result) {
				if (descriptionId) {
                    this.planLockService.unlock(descriptionId)
                    .pipe(catchError(() => of(null)))
                    .subscribe(() => {
                        this.enqueueService.enqueueExhaustChannel(
                            this.descriptionService.delete(descriptionId)
                            .pipe(
                                takeUntil(this._destroyed),
                                finalize(() => this.onRemoveSuccess(descriptionId)),
                                catchError((error) => of(this.onCallbackError(error)))
                            )
                        )
                    })
				}
			}
		});
	}

    private onRemoveSuccess(id: Guid){
        // this.descriptionEditor?.unlock(id, false);
        this.successNotification();
        // drop the deleted description's lock so editLockIds length-based gates
        // (enableSave, persistStatus, canDeactivate, persistDescriptions) don't stay blocked
        this.planTempStorage.removeDescription(id);
        // this.tableOfContent.removeDescriptionFromToC(id);

        // this.fieldValueChangeMap?.get(id)?.forEach((sub) => sub.unsubscribe());
        // this.fieldValueChangeMap?.delete(id);

        if(this.planAnnotationService.descriptionsAnnotationMap()?.has(id?.toString())){
            this.planAnnotationService.refreshAnnotations();
        }
        if(this.planAnnotationService.context()?.entityId.equals(id)){
            this.planAnnotationService.setContext(null);
        }

        setTimeout(() => {
            if(this.selectedDescription()?.toString() == id.toString()){
                this.tableOfContent.goToEntry(null);
            }
        })

    }

    private successNotification(){
        this.uiNotificationService.snackBarNotification(this.isNew ? this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-CREATION') : this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
    }

    /**
     * FE-0b: honest save reporting. Replaces the blanket onCallbackSuccess success toast on the save
     * path. Only claims an update when something was actually persisted (plan and/or descriptions),
     * so clicking Save with nothing dirty no longer shows a false "Saved". Then refreshes as before.
     *
     * NOTE: richer per-entity messaging ("plan + N descriptions saved", "Dataset B failed") needs new
     * i18n keys and per-description failure granularity (the latter arrives with the atomic backend
     * endpoint, FE-4); deliberately deferred to avoid shipping untranslated keys here.
     */
    private onSaveSuccess(data: any, planSaved: boolean, descriptionCount: number): void {
        if (this.isNew) {
            this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-CREATION'), SnackBarNotificationLevel.Success);
        } else if (planSaved || descriptionCount > 0) {
            this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
        }
        // else: nothing was sent — suppress the misleading "Saved" toast.
        this.refreshOnNavigateToData(data ? data.id : null);
    }

    private resetMetadata(){
        this.planTempStorage.reset();
        this.newDescriptionIds = [];
    }
    private getPermissions(data: Plan){
        this.permissionPerSection = {};
        data?.authorizationInformation?.authorizationInformationPerSections?.forEach((item) => {
            this.permissionPerSection[item.sectionId] = item.authorizationFlags;
        })
    }

	//
	//
	// Annotations
	//
	//
	showAnnotations(anchorId: string) {
        this.planAnnotationService.setContext({
            anchor: anchorId,
            entityId: this.item.id,
            entityType: AnnotationEntityType.Plan
        });
		// const dialogRef = this.dialog.open(AnnotationDialogComponent, {
		// 	width: '40rem',
		// 	maxWidth: '90vw',
		// 	maxHeight: '90vh',
		// 	data: {
		// 		entityId: this.item.id,
		// 		anchor: anchorId,
		// 		entityType: AnnotationEntityType.Plan,
		// 		planUsers: this.item?.planUsers ?? [],
		// 		canAnnotate: this.canAnnotatePerField.get(anchorId),
        //         generateLink: (anchor: string, entityId: Guid) => {
        //             let currentPath = window.location.pathname;
        //             const rgx = /\/f\/|\/d\/|\/annotation/;
        //             currentPath = currentPath.split(rgx)?.[0]; //get base of route, remove any descriptionid, fieldId, or annotation it might contain
        //             return this.routerUtils.generateUrl([currentPath, 'f', anchor, 'annotation'].join('/'));
        //         }
		// 	}
		// });
		// dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(changesMade => {
		// 	if (changesMade) {
		// 		// this.getFormAnnotationServiceById(this.item.id).refreshAnnotations();
		// 	}
		// });
	}

    canDeactivate(){
        // FE-0b: the navigation guard is the ONLY data-loss safety net (no client-side draft
        // persistence — see target-design §2). Route it through the unified, form.dirty-based session
        // state so description edits are guarded even if their edit-lock never registered (the
        // documented lock-acquisition-failure edge). Decoupled from the lock subsystem entirely.
        return !this.editorSessionState.isAnythingDirty();
    }

    ngOnDestroy(): void {
        // the creation-consent lock is taken outside PlanLockService, so nothing else releases it; if PlanLockService
        // released the same lock already, this unlock is a no-op on the backend
        if (this.touchLock$ && !this.touchLock$.closed) this.lockService.unlockTarget(this.item.id).subscribe();
        super.ngOnDestroy();
        this.tourState.unregisterPageTour(this.pageTour);
        this.tourState.unregisterPageTour(this.assetTour);
        this.tourState.unregisterPageTour(this.newPlanTour);
        // this.formChange$?.unsubscribe();
        this.planTempStorage.reset();
        // this.fieldValueChangeMap?.forEach((value, key) => {
        //     value?.forEach((sub) => {sub.unsubscribe(); sub = null});
        //     value = null;
        // })
        // this.fieldValueChangeMap.clear();
        // Intentionally NOT calling planLockService.reset() here: PlanLockService is provided at
        // this component's level, so its own ngOnDestroy runs reset() and releases held locks when
        // the editor is destroyed. Keep that in sync — if the service's provider scope changes,
        // re-instate this call (see PlanLockService.ngOnDestroy).
        // this.planLockService.reset().subscribe();
        // if (this.item?.id) {
        //     this.planWebSocketService.getClient$().pipe(takeUntil(this._destroyed)).subscribe(stompClient => {
        //         if (stompClient && stompClient.connected && this.stompSubscription) {
        //             this.stompSubscription.unsubscribe();

        //             stompClient.publish({
        //                 destination: `/app/plan/${this.item.id}/leave`,
        //                 body: ''
        //             });

        //         }
        //     })
        // }
    }

	// getFormAnnotationServiceById(id: Guid): FormAnnotationService | undefined {
	// 	return this.formAnnotationServices.find(service => service.getEntityId() === id);
	// }

    private tenantWarning(){
        this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('PLAN-EDITOR.TENANT-LIMITED-ACCESS'),
				noActions: true
			},
			maxWidth: '40em'
		});
    }

    private consentWarning(){

        if (this.item == null || this.item?.id == null) return;

        if (this.item?.blueprint?.definition?.planCreationConsent?.enabled) {
            this.formGroup.disable();
            const dialogRef = this.dialog.open(PlanCreationConsentDialogComponent, {
                restoreFocus: false,
                disableClose: true,
                data: {
                    canEditPlan: this.canEditEntirePlan,
                    selectedBlueprint: this.item?.blueprint
                },
                maxWidth: '60em'
            });

            dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
                if (result) {
                    this.prepareForm(this.item, true);
                    // locked plan to permit form sumbit
                    this.lockService.checkAndLock(this.item.id, LockTargetType.Plan).pipe(takeUntil(this._destroyed))
                        .subscribe(async lockedByUser => { //TODO HANDLE-ERRORS
                            this.isLockedByUser = lockedByUser;
                            this.isLocked = !this.isLockedByUser;
                            if (this.isLocked) {
                                this.formGroup.disable();
                            } else {
                                this.touchLock$ = interval(this.configurationService.lockInterval).pipe(takeUntil(this._destroyed)).subscribe(() => this.touchLock(this.item.id));
                            }
                            this.formSubmit();
                        })
                } else {
                    this.router.navigate([this.routerUtils.generateUrl(['/plans/overview/', this.item?.id?.toString()])]);
                }
            });
        }
    
    }

    protected navigateToPos(event: UserActionPayload){
        if(event.descriptionId){
            const description = this.item?.descriptions?.find((x) => x.id === event.descriptionId);
            if(!description){return;}
            this.tableOfContent.goToEntry(event.fieldSetId ?? event.sectionId, event.descriptionId.toString())
            return;
        }
        this.tableOfContent.goToEntry(event.fieldSetId?.toString() ?? event.sectionId?.toString())

    }

    /** Uploaded status images by storage file id: the current status and the ones the plan can move to. */
    protected statusLogos = new Map<string, SafeUrl>();
    private loadStatusLogos(plan: Plan): void {
        [plan.status, ...(plan.availableStatuses ?? [])]
            .map(status => status?.definition?.iconConfiguration?.storageFile?.id)
            .filter(id => !!id && !this.statusLogos.has(id.toString()))
            .forEach(id => {
                this.storageFileService.download(id).pipe(takeUntil(this._destroyed))
                .subscribe({
                    next: response => this.statusLogos.set(id.toString(), this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(response.body))),
                    error: error => this.onCallbackError(error)
                });
            });
    }

    //*** TABLE OF CONTENTS */
    onNavigateToEntry(entry: ToCEntry){
        this.closeNewDescriptionPanel()
    }

    // ***   PLAN COLLABORATORS EDITOR    ***

    planUsersDialogOpen = false;

    /** Distinct people with an active membership on the plan, for the toolbar badge (`planUserCount` counts rows). */
    get distinctPlanUserCount(): number {
        const users = this.item?.planUsers?.filter(x => x.user?.id && x.isActive !== IsActive.Inactive) ?? [];
        return new Set(users.map(x => x.user.id.toString())).size;
    }

    // getPlanUserDistinctCount(plan: Plan){
    //     const planUsers = plan?.planUsers?.filter(x => x.isActive === IsActive.Active)
    //     if(!planUsers?.length){ return []; }
    //     let groupedPlanUsers = planUsers.reduce((prev, curr) => {
    //         let existing = prev.findIndex(x => x.user.id == curr.user.id && x.role == curr.role);
    //         if(existing >= 0){
    //             prev[existing] = {
    //                 ...prev[existing],
    //                 sectionIds: [
    //                     ...prev[existing].sectionIds,
    //                     curr.sectionId ?? ''
    //                 ]
    //             }
    //             return prev;
    //         }else {
    //             return [...prev, {
    //                 user: curr.user,
    //                 role: curr.role,
    //                 plan: curr.plan,
    //                 sectionIds: [curr.sectionId ?? '']
    //             }] as GroupedPlanUser[]
    //         }
    //     }, [] as GroupedPlanUser[]);
    //     return new Set([...groupedPlanUsers.map(x => x.user.id)]).size;
    // }
    // distinctUserCount: number = 0;

    get canInvitePlanUsers(): boolean {
        const authorizationFlags = (this.item as Plan)?.authorizationInformation?.globalAuthorizationFlags;
        return (
            this.item?.isActive == IsActive.Active &&
            (authorizationFlags?.some(x => x === AppPermission.InvitePlanUsers) || this.authService.hasPermission(AppPermission.InvitePlanUsers)));
    }

    get canAssignPlanUsers(): boolean {
        const authorizationFlags = (this.item as Plan)?.authorizationInformation?.globalAuthorizationFlags;
        return this.item?.isActive == IsActive.Active && (authorizationFlags?.some(x => x === AppPermission.AssignPlanUsers) || this.authService.hasPermission(AppPermission.AssignPlanUsers));
    }
    
    /**
     * Collaborators is a self-contained task (nothing on the canvas is read while inviting or
     * assigning people), so it opens as an overlay side sheet — the same dialog the plan overview
     * uses — rather than pushing the editor content aside like the Comments panel does.
     */
    openPlanUsersDialog(){
        if(this.planUsersDialogOpen){ return; }
        this.planUsersDialogOpen = true;
        this.dialog.open<PlanAuthorsEditorDialogComponent, PlanAuthorsEditorDialogParams, PlanAuthorsEditorDialogReturn>(PlanAuthorsEditorDialogComponent, {
            width: '440px',
            maxWidth: '100vw',
            height: '100%',
            id: 'author-editor',
            position: { right: '0px' },
            panelClass: 'dialog-side-panel',
            ariaLabel: this.language.instant('PLAN-OVERVIEW.COLLABORATORS'),
            disableClose: true,
            data: {
                plan: this.item,
                canAssignUsers: this.canAssignPlanUsers,
                canInviteUsers: this.canInvitePlanUsers,
            }
        }).afterClosed().pipe(takeUntil(this._destroyed)).subscribe((result) => {
            this.planUsersDialogOpen = false;
            this.onPlanUsersChanged(result);
        });
    }

    onPlanUsersChanged(params?: {users?: PlanUser[]}){
        if(params?.users){
            this.item.planUsers = params.users;
        }
    }

    // *** ANOTATIONS **
    annotationsDialogOpen = this.planAnnotationService.annotationDialogOpen;

    toggleAnnotationDialog(){
        this.planAnnotationService.toggleAnnotationDialog();
    }

    //** NEW DESCRIPTION  */
    newDescriptionPanelOpen: boolean = false;
    newDescSection: RootSection;

    openNewDescriptionPanel(sectionId: string){
        // this.closeDialogs();
        
        this.newDescSection = this.item.blueprint?.definition?.sections?.find(x => x.id == sectionId);
        if(this.newDescSection){
            this.newDescriptionPanelOpen = true;
        }
    }

    closeNewDescriptionPanel(){
        this.newDescriptionPanelOpen = false;
        this.newDescSection = null;
    }

    persistTemplates(){
        const persist: UpdatePlanDescriptionTemplate = {
            id: this.item.id,
            hash: this.item.hash,
            descriptionTemplates: []
        };
		for (const sectionId in (this.formGroup.get('descriptionTemplates') as UntypedFormGroup).controls) {
			if (this.formGroup.get('descriptionTemplates').get(sectionId).value != undefined) {
				persist.descriptionTemplates.push(
					...(this.formGroup.get('descriptionTemplates').get(sectionId).value as Guid[]).map(x => (
                        { sectionId, descriptionTemplateGroupId: x } 
                    ))
				);
			}
		}
        this.enqueueService.enqueueExhaustChannel(
            this.planService.updateDescriptionTemplates(persist, [
                nameof<Plan>(x => x.id),
                nameof<Plan>(x => x.hash),
                nameof<Plan>(x => x.isActive),
                ...PlanEditorEntityResolver.planDescriptionTemplateFields(),
                [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.id)].join('.'),
                [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition)].join('.'),
                [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.id)].join('.'),
                [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.sections)].join('.'),
                [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.hasTemplates)].join('.'),
                [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.groupId)].join('.'),
                [nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<RootSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.id)].join('.'),
            ])
            .pipe(
                takeUntil(this._destroyed),
                tap((res => {
                    this.formGroup.controls.hash.patchValue(res.hash);
                    this.item.hash = res.hash;
                    this.editorModel.setPlanDescriptionTemplates(res);
                    this.item.planDescriptionTemplates = res.planDescriptionTemplates ?? [];
                    this.item.planDescriptionTemplates?.forEach((template) => {
                        this.planTempStorage.setPlanDescriptionTemplate(template);
                    })
                    const descriptionTemplatesFormGroup = this.formGroup.get('descriptionTemplates') as UntypedFormGroup;
                    for (const sectionId in descriptionTemplatesFormGroup.controls) {
                        descriptionTemplatesFormGroup.get(sectionId).markAsPristine();
                    }
                }))
            )
        )
    }

    createDescription(desc: Description, sectionId: string){
        const planDescriptionTemplate = this.item?.planDescriptionTemplates?.find(
            (x) => x.sectionId === sectionId && x.descriptionTemplateGroupId === desc.descriptionTemplate?.groupId
        );
        const newId = this.generateTempDescriptionId;
        const description: Description = {
            ...desc,
            id: Guid.parse(newId),
            hash: null,
            plan: this.item,
            planDescriptionTemplate,
            belongsToCurrentTenant: true,
            isActive: IsActive.Active,
            status: null,
            authorizationFlags: [AppPermission.EditDescription, AppPermission.DeleteDescription, AppPermission.FinalizeDescription, AppPermission.AnnotateDescription]
        }
        this.newDescriptionIds.push(newId);

        this.onNewDescription(description, sectionId);
    }

    copyDescriptionToPlan(descriptionId: Guid, sectionId: string){
        if (descriptionId && sectionId) {
            this.descriptionService.clone(descriptionId, 
                [...DescriptionEditorHelper.BaseDescriptionLookupFields(), ...DescriptionEditorHelper.DescriptionTemplateInDescriptionLookupFields()]
            )
            .pipe(takeUntil(this._destroyed))
            .subscribe((desc) => {
                if(desc){
                    this.createDescription(desc, sectionId)
                }
            })
        }
    }

    blueprintSections() {
        return this.selectedBlueprint?.definition?.sections?.filter(s => s.hasTemplates) ?? [];
    }
    
}
