import { AfterViewInit, Component, computed, OnDestroy, OnInit, signal, ViewChild } from '@angular/core';
import { FormGroup, UntypedFormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { AnnotationEntityType } from '@app/core/common/enum/annotation-entity-type';
import { DescriptionStatusEnum } from '@app/core/common/enum/description-status';
import { FileTransformerEntityType } from '@app/core/common/enum/file-transformer-entity-type';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { LockTargetType } from '@app/core/common/enum/lock-target-type';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { PlanStatusEnum } from '@app/core/common/enum/plan-status';
import { Description, DescriptionPersist } from '@app/core/model/description/description';
import { PlanBlueprint } from '@app/core/model/plan-blueprint/plan-blueprint';
import { Plan, PlanPersist } from '@app/core/model/plan/plan';
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
import { AnnotationDialogComponent } from '@app/ui/annotations/annotation-dialog-component/annotation-dialog.component';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditor } from '@common/base/base-editor';
import { FormService } from '@common/forms/form-service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { map, take, takeUntil } from 'rxjs/operators';
import { PlanFinalizeDialogComponent, PlanFinalizeDialogOutput } from '../plan-finalize-dialog/plan-finalize-dialog.component';
import { PlanEditorModel, PlanEditorForm } from './plan-editor.model';
import { PlanEditorService } from './plan-editor.service';
import { PlanEditorEntityResolver } from './resolvers/plan-editor-enitity.resolver';
import { FormAnnotationService } from '@app/ui/annotations/annotation-dialog-component/form-annotation.service';
import { PlanStatus } from '@app/core/model/plan-status/plan-status';
import { PlanStatusAvailableActionType } from '@app/core/common/enum/plan-status-available-action-type';
import { PlanVersionStatus } from '@app/core/common/enum/plan-version-status';
import { PlanStatusPermission } from '@app/core/common/enum/plan-status-permission.enum';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { NewDescriptionDialogComponent, NewDescriptionDialogComponentResult } from '@app/ui/description/editor/new-description/new-description.component';
import { PlanDescriptionEditorComponent } from './plan-description-editor/plan-description-editor.component';
import { DescriptionEditorHelper } from './plan-description-editor/plan-description-editor-helper';
import { PlanTableOfContentsComponent } from './plan-table-of-contents/plan-table-of-contents.component';
import { DescriptionEditorForm, DescriptionEditorModel } from '@app/ui/description/editor/description-editor.model';
import { DescriptionInfo, PlanTempStorageService } from './plan-temp-storage.service';
import { forkJoin, interval, Observable, of, Subscription } from 'rxjs';
import { PopupNotificationDialogComponent } from '@app/library/notification/popup/popup-notification.component';
import { isNullOrUndefined } from '@swimlane/ngx-datatable';
import { TenantHandlingService } from '@app/core/services/tenant/tenant-handling.service';

@Component({
    selector: 'app-plan-editor',
    templateUrl: './plan-editor.component.html',
    styleUrls: ['./plan-editor.component.scss'],
    providers: [PlanEditorService, FormAnnotationService],
    standalone: false
})
export class PlanEditorComponent extends BaseEditor<PlanEditorModel, Plan> implements OnInit, AfterViewInit, OnDestroy {
    @ViewChild('descriptionEditor') descriptionEditor: PlanDescriptionEditorComponent;
    @ViewChild('tableOfContent') tableOfContent: PlanTableOfContentsComponent;
    formGroup: FormGroup<PlanEditorForm>;
    isLoading: boolean = false;
	isNew = true;
	isDeleted = false;
    showPlanErrors: boolean = false;
	item: Plan;
	selectedBlueprint: PlanBlueprint;
	defaultBlueprint: PlanBlueprint;
	step = signal<number>(0);
	annotationsPerAnchor: Map<string, number> = new Map<string, number>();

    selectedDescription = signal<Guid>(null);

    newDescriptionIds: string[] = [];
    get generateTempDescriptionId(): string {
        return Guid.EMPTY.toString().replace('0', `${this.newDescriptionIds.length + 1}`);
    }



	//Enums
	fileTransformerEntityTypeEnum = FileTransformerEntityType;

	scrollToField: boolean = false;
	openAnnotation: boolean = false;

	permissionPerSection: Map<Guid, string[]>;
	canAnnotatePerField: Map<string, boolean>;

    protected descTemplatesInUseMap = computed(() => {
        const newMap = new Map<Guid, Guid[]>();
        this.planTempStorage.descriptionIdsBySectionMap()?.forEach((descIds, key) => {
            let descTemplateIds = 
            descIds.map((id) => {
                let planDescTemplateId = this.planTempStorage.descriptions().get(id.toString())?.formGroup?.value?.planDescriptionTemplateId;
                return this.planTempStorage.planDescriptionTemplates?.get(planDescTemplateId)?.descriptionTemplateGroupId;
            })?.filter((x) => x)
            newMap.set(key, descTemplateIds)
        })
        return newMap;
    })

	selectedBlueprintId = null;

    protected get isFinalized(){
        return this.item?.status?.internalStatus === PlanStatusEnum.Finalized;
    }

    protected get belongsToCurrentTenant(){
        return this.item?.belongsToCurrentTenant || this.isNew;
    }

	protected get canDeletePlan(): boolean {
		return this.belongsToCurrentTenant &&!this.isDeleted && !this.isNew && (this.hasPermission(this.authService.permissionEnum.DeletePlan) || this.item?.authorizationFlags?.some(x => x === AppPermission.DeletePlan));
	}
    
    protected get canEditPlan(): boolean {
		return this.belongsToCurrentTenant &&!this.isDeleted && !this.isFinalized && (this.isNew ? this.authService.hasPermission(AppPermission.NewPlan) : (this.item.authorizationFlags?.some(x => x === AppPermission.EditPlan) || this.authService.hasPermission(AppPermission.EditPlan)));
	}

    protected get canEditDescriptions(): boolean {
        return this.belongsToCurrentTenant &&!this.isDeleted && !this.isFinalized && this.item?.blueprint?.definition?.sections?.some((section) => this.canEditSection(section.id));
    }

	protected get canFinalizePlan(): boolean {
		return this.belongsToCurrentTenant &&!this.isDeleted && !this.isNew && this.canEditPlan && this.isLockedByUser && !this.isFinalized && (this.hasPermission(this.authService.permissionEnum.EditPlan) || this.item?.authorizationFlags?.some(x => x === AppPermission.EditPlan));
	}

	protected get canReverseFinalizePlan(): boolean {
		return this.belongsToCurrentTenant &&!this.isDeleted && !this.isNew && this.isLockedByUser && this.item.status?.internalStatus == PlanStatusEnum.Finalized && (this.hasPermission(this.authService.permissionEnum.EditPlan) || this.item?.authorizationFlags?.some(x => x === AppPermission.EditPlan));
	}

	protected get canExport(): boolean {
		return this.item?.status?.definition?.availableActions?.filter(x => x === PlanStatusAvailableActionType.Export).length > 0 && (this.hasPermission(this.authService.permissionEnum.ExportPlan) || this.item?.authorizationFlags?.some(x => x === AppPermission.ExportPlan));
	}

	protected get canEditStatus(): boolean{
        return this.item?.statusAuthorizationFlags?.some(x => x.toLowerCase() === PlanStatusPermission.Edit.toLowerCase())
    }

    protected get displayCustomStatus(): boolean {
        return this.canEditStatus && !this.isNew && 
        this.item.availableStatuses?.length > 0 && 
        this.item.versionStatus != PlanVersionStatus.Previous && !this.isLocked && this.hasNotDoi() && this.belongsToCurrentTenant
    }

    protected get isDirty(): boolean {
		return this.formGroup && this.formGroup.dirty;
	}

    protected get lockedDescriptions(): Guid[]{
        return this.descriptionEditor?.lockedDescriptionIds() ?? [];
    }

	protected canEditSection(id: Guid): boolean {
		return this.belongsToCurrentTenant && !this.isDeleted && !this.isFinalized && (this.hasPermission(this.authService.permissionEnum.EditDescription) || this.item?.authorizationFlags?.some(x => x === AppPermission.EditDescription) || (
			this.permissionPerSection && this.permissionPerSection[id.toString()] && this.permissionPerSection[id.toString()].some(x => x === AppPermission.EditDescription)
		));
	}

	protected canDeleteSection(id: Guid): boolean {
		return this.belongsToCurrentTenant && !this.isDeleted && !this.isFinalized && (this.hasPermission(this.authService.permissionEnum.DeleteDescription) || this.item?.authorizationFlags?.some(x => x === AppPermission.DeleteDescription) || (
			this.permissionPerSection && this.permissionPerSection[id.toString()] && this.permissionPerSection[id.toString()].some(x => x === AppPermission.DeleteDescription)
		));
	}

	protected canAnnotateSection(id: Guid): boolean {
		return this.belongsToCurrentTenant && !this.isDeleted && this.permissionPerSection?.[id.toString()]?.some(x => x === AppPermission.AnnotatePlan);
	}

	private hasPermission(permission: AppPermission): boolean {
		return this.authService.hasPermission(permission) || this.item?.authorizationFlags?.some(x => x === permission) || this.editorModel?.permissions?.includes(permission);
	}

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
		public visibilityRulesService: VisibilityRulesService,
		public routerUtils: RouterUtilsService,
		public enumUtils: EnumUtils,
		public descriptionTemplateService: DescriptionTemplateService,
		public userService: UserService,
		public descriptionService: DescriptionService,
		public titleService: Title,
		private analyticsService: AnalyticsService,
		private breadcrumbService: BreadcrumbService,
		public fileTransformerService: FileTransformerService,
		private formAnnotationService: FormAnnotationService,
        private planEditorService: PlanEditorService,
        private planTempStorage: PlanTempStorageService,
        private tenantHandlingService: TenantHandlingService,
	) {
		const descriptionLabel: string = route.snapshot.data['entity']?.label;
		if (descriptionLabel) {
			titleService.setTitle(descriptionLabel);
		} else {
			titleService.setTitle('PLAN-EDITOR.TITLE-EDIT');
		}
		super(dialog, language, formService, router, uiNotificationService, httpErrorHandlingService, filterService, route, queryParamsService, lockService, authService, configurationService);

    }

	ngOnInit(): void {
		this.analyticsService.trackPageView(AnalyticsService.PlanEditor);
		this.permissionPerSection = this.route.snapshot.data['permissions'] as Map<Guid, string[]> ?? new Map<Guid, string[]>();
        
        super.ngOnInit();
        this.parseQueryParams();
		if (this.isNew === false && this.step() === 0) this.nextStep();

        this.canAnnotatePerField = new Map<string, boolean>([]);
        if(!this.isNew && this.permissionPerSection){
            this.item?.blueprint?.definition?.sections?.forEach((section) => {
                section?.fields?.forEach((field) => this.canAnnotatePerField.set(field.id.toString(), this.canAnnotateSection(section.id)))
            })
        }

        if(this.item?.id){
            this.formAnnotationService.init(this.item.id, AnnotationEntityType.Plan);
            this.formAnnotationService.getAnnotationCountObservable().pipe(takeUntil(this._destroyed)).subscribe((annotationsPerAnchor: Map<string, number>) => {
                this.annotationsPerAnchor = annotationsPerAnchor;
            });

            this.formAnnotationService.getOpenAnnotationSubjectObservable().pipe(takeUntil(this._destroyed)).subscribe((anchorId: string) => {
                if (anchorId && anchorId == this.item.id?.toString()) this.showAnnotations(anchorId);
            });
        }
	}

    private parseQueryParams() {
        this.route.queryParams.pipe(takeUntil(this._destroyed))
        .subscribe((params: Params) => {
            const sectionId = params['sectionId'];
            const descriptionCopyId = params['descriptionCopyId'];
            const descriptionId = params['descriptionId'];
			this.selectedBlueprintId = params['blueprintId'];
            if (descriptionCopyId && sectionId) {
                this.descriptionService.clone(Guid.parse(descriptionCopyId), 
                    [...DescriptionEditorHelper.BaseDescriptionLookupFields(), ...DescriptionEditorHelper.DescriptionTemplateInDescriptionLookupFields()]
                )
                .pipe(takeUntil(this._destroyed))
                .subscribe((desc) => {
                    if(desc){
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

                        this.onNewDescription(description);
                    }
                })
            } else if (descriptionId) {
                this.selectedDescription.set(descriptionId);
            }
        });
    }

	ngAfterViewInit(): void {
		this.route.params
			.pipe(takeUntil(this._destroyed))
			.subscribe((params: Params) => {
				const fieldId = params['fieldId'];
                const descriptionId = params['descriptionId'];
                if(descriptionId){
                    const description = this.item?.descriptions?.find((x) => x.id === descriptionId);
                    if(!description){return;}
                    let fieldStep = this.item?.blueprint.definition.sections.find(s => s.id === description.planDescriptionTemplate?.sectionId)?.ordinal;
                    this.tableOfContent.changePlanStep({section: fieldStep, descriptionId, descriptionFieldId: fieldId ?? null});
                    if(fieldId && this.route.snapshot.data['openDescriptionAnnotation']){
                        this.formAnnotationService.οpenAnnotationDialog(fieldId);
                    }
                } else if(fieldId){
                    this.scrollToField = this.route.snapshot.data['scrollToField'] ?? false
                    this.openAnnotation = this.route.snapshot.data['openAnnotation'] ?? false;

                    let fieldStep = this.item?.blueprint.definition.sections.find(s => s.fields?.some(f => f.id.toString() == fieldId))?.ordinal;
                    this.tableOfContent?.changePlanStep({section: fieldStep, fieldId: Guid.parse(fieldId)});
                    
                    if (this.openAnnotation) {
                        this.showAnnotations(fieldId)
                    }
                }

			});
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

	prepareForm(data: Plan) {
		try {
            this.resetMetadata();
			if (data?.blueprint?.definition?.sections != null) {
				data.blueprint.definition.sections = data.blueprint.definition.sections.sort((s1, s2) => s1.ordinal - s2.ordinal);
				for (let i = 0; i < data.blueprint.definition.sections.length; i++) {
					data.blueprint.definition.sections[i].fields = data.blueprint.definition.sections[i]?.fields?.sort((f1, f2) => f1.ordinal - f2.ordinal);
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

			}
            this.editorModel = data ? new PlanEditorModel().fromModel(data) : new PlanEditorModel();
			this.item = data;
			this.selectedBlueprint = data?.blueprint;

            //add items to temp storage 
            this.item?.planDescriptionTemplates?.forEach((planDescriptionTemplate) => {
                this.planTempStorage.setPlanDescriptionTemplate(planDescriptionTemplate);
            })

			// if (data && data.id) {
			// 	const descriptionSectionPermissionResolverModel: DescriptionSectionPermissionResolver = {
			// 		planId: data.id,
			// 		sectionIds: data?.blueprint?.definition?.sections?.map(x => x.id),
			// 		permissions: [AppPermission.EditDescription, AppPermission.DeleteDescription]
			// 	}
			// }

			this.buildForm();

			if (!this.canEditPlan) {
				this.formGroup.disable();
			}else if(!this.isNew){
				this.checkLock(this.item.id);
            }

		} catch (error) {
			this.logger.error('Could not parse Plan item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

	buildForm() {
		this.formGroup = this.editorModel.buildForm(null, !this.canEditPlan);
        const descriptions = this.item?.isActive === IsActive.Inactive ? this.item?.descriptions : this.item?.descriptions?.filter((x) => x.isActive === IsActive.Active);
        descriptions?.forEach((description) => {
            this.buildDescriptionForm({description});
        })
        this.planEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);
	}

    protected checkLock(targetId: Guid){
        if(!targetId){ return; }
        this.isNew = false;
        // check if locked.
        this.lockService.checkLockStatus(targetId).pipe(takeUntil(this._destroyed)).subscribe(lockStatus => { //TODO HANDLE-ERRORS
            this.isLocked = lockStatus.status;
            if (this.isLocked) {
                this.formGroup.disable();
                this.dialog.open(PopupNotificationDialogComponent, {
                    data: {
                        title: this.language.instant('PLAN-EDITOR.LOCKED-DIALOG.TITLE'),
                        message: this.language.instant('PLAN-EDITOR.LOCKED-DIALOG.MESSAGE')
                    }, maxWidth: '30em'
                });
            }

            if (!this.isLocked && !isNullOrUndefined(this.authService.currentAccountIsAuthenticated())) {
                this.lockOnFormChange(targetId);
            }
        });
    }

    private lock$: Subscription;
    protected lockOnFormChange(targetId: Guid){
        this.formGroup.valueChanges
        .pipe(take(1)).subscribe(() => 
            this.lockService.checkAndLock(targetId, LockTargetType.Plan).pipe(takeUntil(this._destroyed))
            .subscribe(async lockedByUser => { //TODO HANDLE-ERRORS
                this.isLockedByUser = lockedByUser;
                this.isLocked = !this.isLockedByUser;
                if (this.isLocked) {
                    this.formGroup.disable();
                } else {
                    this.lock$ = interval(this.configurationService.lockInterval).pipe(takeUntil(this._destroyed)).subscribe(() => this.touchLock(targetId));
                }
            })
        )
    }

    protected touchLock(targetId: Guid){
        this.lockService.touchLock(targetId).pipe(takeUntil(this._destroyed)).subscribe(async result => { }); //TODO HANDLE-ERRORS
    }

	refreshData(): void {
        let planLock$ = this.isLockedByUser ? this.lockService.unlockTarget(this.editorModel.id).pipe(takeUntil(this._destroyed)) : of(null);
        let descLock$ = this.descriptionEditor.unlockAll();
        forkJoin([planLock$, descLock$]).pipe(takeUntil(this._destroyed))
        .subscribe({
            complete: () => {
                this.isLockedByUser = false;
                this.isLocked = false;
                this.lock$?.unsubscribe(); 
                this.getItem(this.editorModel.id, (data: Plan) => {
                    this.breadcrumbService.addIdResolvedValue(data.id.toString(), data.label);
                    const planCopy: Plan = {...data, descriptions: []}; //add plan info to desc w/o requesting it from the lookup twice
                    data.descriptions?.forEach((desc) => desc.plan = planCopy);
                    this.prepareForm(data);
                    if (this.isNew === false && this.step() === 0) this.nextStep();
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
			let route = [];
			route.push(this.routerUtils.generateUrl('/plans/edit/' + id));
			this.router.navigate(route, { queryParams: { 'lookup': this.queryParamsService.serializeLookup(this.lookupParams), 'lv': ++this.lv }, replaceUrl: true, relativeTo: this.route });
		} else {
			this.refreshData();
		}
	}

	persistEntity(onSuccess?: (response) => void): void {
		const formData = JSON.parse(JSON.stringify(this.formGroup.value)) as PlanPersist;

		//Transform to persist
		//Transform descriptionTemplates
		formData.descriptionTemplates = [];
		for (const sectionId in (this.formGroup.get('descriptionTemplates') as UntypedFormGroup).controls) {
			if (this.formGroup.get('descriptionTemplates').get(sectionId).value != undefined) {
				formData.descriptionTemplates.push(
					...(this.formGroup.get('descriptionTemplates').get(sectionId).value as Guid[]).map(x => { return { sectionId: Guid.parse(sectionId), descriptionTemplateGroupId: x } })
				);
			}
		}

        const resetStep = this.selectedDescription() && this.newDescriptionIds?.includes(this.selectedDescription().toString());

        this.isLoading = true;
        this.persistDescriptions().subscribe({
            next: (complete) => {
                if(this.canEditPlan && (this.isLockedByUser || this.isNew)){
                    this.planService.persist(formData)
                    .pipe(takeUntil(this._destroyed)).subscribe({
                        next: (complete) => {
                            this.isLoading = false;
                            if(resetStep){
                                this.tableOfContent.changePlanStep({
                                    section: 1
                                })
                            }
                            if(onSuccess){
                                onSuccess(complete)
                            } else {
                                this.onCallbackSuccess(complete);
                            }
                        },
                        error: (error) => {this.onCallbackError(error); this.isLoading = false;}
                    });
                } else {
                    this.isLoading = false;
                    if(resetStep){
                        this.tableOfContent.changePlanStep({
                            section: 1
                        })
                    }
                    if(onSuccess){
                        onSuccess(complete)
                    } else {
                        this.onCallbackSuccess(complete);
                    }
                }

            },
            error: (error) => {
                this.isLoading = false;
                this.onCallbackError(error);
            }
        })
	}


	formSubmit(onSuccess?: (response) => void): void {
		this.formService.removeAllBackEndErrors(this.formGroup);
        if((!this.canEditPlan || this.formGroup.controls?.label?.valid) && this.descriptionBaseValid()){
            this.persistEntity(onSuccess);
        }
	}

	discardChanges() {
		let messageText = "";
		let confirmButtonText = "";
		let cancelButtonText = "";

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
			restoreFocus: false,
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
					if (this.isNew){ 
                        this.step.set(0);
                    }
					else {
                        this.step.set(this.step() > 0 ? 1 : 0);
                    }
					this.refreshOnNavigateToData();
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
            this.formGroup.markAsPristine();
            this.newDescriptionIds = [];
			this.successNotification();
            this.descriptionEditor.unlockAll().subscribe(() => {
                this.router.navigate([this.routerUtils.generateUrl('/plans/')]);
            })
		});
	}

	nextStep() {
		this.step.set(this.step() < this.selectedBlueprint.definition.sections.length ? this.step() + 1 : this.step());
	}
    
	hasNotDoi() {
		return (this.item.entityDois == null || this.item.entityDois.length == 0);
	}

	persistStatus(status: PlanStatus) {
		if (status.internalStatus != null && status.internalStatus === PlanStatusEnum.Finalized) {
			this.finalize(status.id);
		} else if (this.item.status.internalStatus === PlanStatusEnum.Finalized){
			this.reverseFinalization(status.id);
		} else {
			// other statuses
            this.isLoading = true;
			this.planService.setStatus(this.item.id, status.id)
            .pipe(takeUntil(this._destroyed))
            .subscribe({
                next: (data) => {
                    this.onCallbackSuccess();
                    this.isLoading = false;
                }, 
                error: (error: any) => {
                    this.onCallbackError(error);
                    this.isLoading = false;
                }});
		}
	}

	finalize(newStatusId) {
		const dialogRef = this.dialog.open(PlanFinalizeDialogComponent, {
			maxWidth: 'min(500px, 90vw)',
			restoreFocus: false,
			data: {
				plan: this.item,
                descriptionMetadata: this.item.descriptions?.map((desc) => this.planTempStorage.descriptions().get(desc.id.toString())).filter((x) => !!x && !x.isNew) ?? []
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe((result: PlanFinalizeDialogOutput) => {
			if (result) {
                if(!result.cancelled && result.planValid){
                    this.showPlanErrors = false;
                    this.isLoading = true;
                    this.planService.setStatus(this.item.id, newStatusId, result.descriptionsToBeFinalized)
                        .pipe(takeUntil(this._destroyed))
                        .subscribe({
                            next: (data) => {
                                this.isLoading = false;
                                this.onCallbackSuccess();
                                this.router.navigate([this.routerUtils.generateUrl(['/plans/overview', this.item.id.toString()], '/')]);
                            },
                            error: (error: any) => {
                                this.isLoading = false;
                                this.onCallbackError(error);
                            }
                        });
                }else {
                    this.formService.touchAllFormFields(this.formGroup);
                    this.showPlanErrors = true;
                    Array.from(this.planTempStorage.descriptions())?.forEach(([key, metadata]) => {if(!metadata.isNew){ 
                        this.showDescErrors.add(metadata.lastPersist.id)}
                    })
                }
			}
		});

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
                this.isLoading = true;
				this.planService.setStatus(this.item.id, newStatusId).pipe(takeUntil(this._destroyed))
					.subscribe({
                        complete: () => {
                            this.onCallbackSuccess();
                            this.isLoading = false;
					    }, 
                        error: (error: any) => {
                            this.onCallbackError(error);
                            this.isLoading = false;
					    }
                    });
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
	selectBlueprint(blueprint: PlanBlueprint) {
		if(!blueprint){
			this.formGroup.markAllAsTouched();
			return;
		}
		this.formGroup.controls.blueprint.setValue(blueprint.id);
        this.formGroup.controls.label.setValue(`${blueprint.label} ${this.language.instant('PLAN-LISTING.PLAN')}`);
		this.planBlueprintService.getSingle(this.formGroup.get('blueprint').value, PlanEditorEntityResolver.blueprintLookupFields()).pipe(takeUntil(this._destroyed))
			.subscribe({
                next: (data) => {
                    this.selectedBlueprint = data;
                    this.buildFormAfterBlueprintSelection();
                    this.nextStep();
                },
				error: (error) => this.httpErrorHandlingService.handleBackedRequestError(error)
            });
	}
	private buildFormAfterBlueprintSelection() {
		const plan: Plan = {
			label: this.formGroup.get('label').value,
			description: this.formGroup.get('description').value,
			blueprint: this.selectedBlueprint,
		}

		this.prepareForm(plan);
	}

	//
	//
	// Descriptions
	//
	//

    showDescErrors = new Set<Guid>([]);

    private buildDescriptionForm(params:{description: Description, isNew?: boolean}): FormGroup<DescriptionEditorForm>{
        const {description, isNew = false} = params;
        if(this.showDescErrors?.size) { this.showDescErrors.delete(description?.id)};

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
        const section = this.item.blueprint.definition.sections?.find((x) => x.id === description.planDescriptionTemplate.sectionId);
        this.planTempStorage.setDescription({
            lastPersist: description, 
            formGroup,
            visibilityRulesService: visibilityRules,
            validationErrorModel: editorModel.validationErrorModel,
            isNew
        });
        return formGroup;
    }

        
    private persistDescriptions(): Observable<Description[]>{
        if(!this.lockedDescriptions?.length && !this.newDescriptionIds?.length){
            return of(null);
        }
        const editedDescriptions = this.lockedDescriptions?.map((id) => this.planTempStorage.descriptions()?.get(id.toString())?.formGroup?.getRawValue() as DescriptionPersist) ?? [];
        const newDescriptions = this.newDescriptionIds?.map((id) =>{
            return {
                ...this.planTempStorage.descriptions()?.get(id)?.formGroup?.value,
                id: null
            } as DescriptionPersist
        }) ?? [];
        
        return this.descriptionService.persistMultiple({descriptions: [...editedDescriptions, ...newDescriptions].filter((x) => x)})
        .pipe(takeUntil(this._destroyed));
    }

    protected descriptionBaseValid = computed(() => {
        const forms = this.lockedDescriptions.map((id) => this.planTempStorage.descriptions().get(id.toString())?.formGroup);
        return !forms.some((form) => form.disabled || DescriptionEditorModel.baseFieldsAreInvalid((form)));
    })


    protected addDescriptionToSection(sectionId: Guid){
        const dialogRef = this.dialog.open(NewDescriptionDialogComponent, {
            width: '590px',
            minHeight: '200px',
            restoreFocus: false,
            data: {
                plan: this.item,
                planSectionId: sectionId
            },
            panelClass: 'custom-modalbox'
        });
        dialogRef.afterClosed().subscribe((result: NewDescriptionDialogComponentResult) => {
            if (result?.description) {
                const description = result.description;

                const newId = this.generateTempDescriptionId;
                this.newDescriptionIds.push(newId);

                description.id = Guid.parse(newId);

                const descriptionTemplate = this.planTempStorage.getDescriptionTemplate(description.descriptionTemplate?.id);
                description.descriptionTemplate = descriptionTemplate;
                this.onNewDescription(description);
            }
        })
    }

    protected onNewDescription(description: Description){
        if(!description){ return; }
        this.item.descriptions.push(description);
        this.buildDescriptionForm({description, isNew: true});
        const sectionIndex =  this.item.blueprint?.definition?.sections?.findIndex((x) => x.id === description.planDescriptionTemplate?.sectionId)
        this.tableOfContent.changePlanStep({
            section: (sectionIndex ?? 0) + 1,
            descriptionId: description.id
        })
    }

    protected onShowDescriptionErrors(id: Guid){
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
			restoreFocus: false,
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
                    this.isLoading = true;
					this.descriptionService.delete(descriptionId)
                    .pipe(takeUntil(this._destroyed))
                    .subscribe({
                        complete: () => {
                            this.isLoading = false;
                            this.onRemoveSuccess(descriptionId);
                        },
                        error: (error) =>  {this.isLoading = false; this.onCallbackError(error)}
                    });
				}
			}
		});
	}

    private onRemoveSuccess(id: Guid){
        this.descriptionEditor?.unlock(id, false);
        if(this.selectedDescription() === id) {
            this.tableOfContent.previousPlanStep();
        }
        this.successNotification();
        this.planTempStorage.removeDescription(id);
    }

    private successNotification(){
        this.uiNotificationService.snackBarNotification(this.isNew ? this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-CREATION') : this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
    }

    private resetMetadata(){
        this.planTempStorage.reset();
        this.newDescriptionIds = [];
    }

	//
	//
	// Description Template
	//
	//

	// getDescriptionTemplateMultipleAutoCompleteConfiguration(sectionId: Guid): MultipleAutoCompleteConfiguration {
	// 	return {
	// 		initialItems: (excludedItems: any[], data?: any) => this.descriptionTemplateService.query(
    //             this.descriptionTemplateService.buildDescriptionTemplateGroupAutocompleteLookup({
    //                 isActive: [IsActive.Active], 
    //                 excludedGroupIds: excludedItems ? excludedItems : null,
    //                 lookupFields: [...DescriptionTemplateEditorResolver.baseLookupFields(), ...DescriptionTemplateEditorResolver.definitionLookupFields()]
    //             })
    //         ).pipe(map(x => x.items)),
	// 		filterFn: (searchQuery: string, excludedItems: any[]) => this.descriptionTemplateService.query(
    //             this.descriptionTemplateService.buildDescriptionTemplateGroupAutocompleteLookup({
    //                 isActive: [IsActive.Active], 
    //                 like: searchQuery, 
    //                 excludedGroupIds: excludedItems,
    //                 lookupFields: [...DescriptionTemplateEditorResolver.baseLookupFields(), ...DescriptionTemplateEditorResolver.definitionLookupFields()]
    //             })
    //         ).pipe(map(x => x.items)),
	// 		getSelectedItems: (selectedItems: any[]) => this.descriptionTemplateService.query(
    //             this.descriptionTemplateService.buildDescriptionTemplateGroupAutocompleteLookup({
    //                 isActive: [IsActive.Active, IsActive.Inactive],
    //                 groupIds: selectedItems,
    //                 lookupFields: [...DescriptionTemplateEditorResolver.baseLookupFields(), ...DescriptionTemplateEditorResolver.definitionLookupFields()]
    //             })
    //         ).pipe(map(x => x.items)),
	// 		displayFn: (item: DescriptionTemplate) => item.label,
	// 		titleFn: (item: DescriptionTemplate) => item.label,
	// 		subtitleFn: (item: DescriptionTemplate) => item.description,
	// 		valueAssign: (item: DescriptionTemplate) => {
    //             this.planTempStorage.setDescriptionTemplate(item);
    //             return item.groupId;
    //         },
	// 		canRemoveItem: (item: DescriptionTemplate) => this.canRemoveDescriptionTemplate(item, sectionId),
	// 		popupItemActionIcon: 'visibility'
	// 	}
	// };

	// onPreviewDescriptionTemplate(event, sectionId: Guid) {
	// 	const dialogRef = this.dialog.open(DescriptionTemplatePreviewDialogComponent, {
	// 		width: '590px',
	// 		minHeight: '200px',
	// 		restoreFocus: false,
	// 		data: {
	// 			descriptionTemplateId: event.id
	// 		},
	// 		panelClass: 'custom-modalbox'
	// 	});
	// 	dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(groupId => {
	// 		if (groupId) {
	// 			let data = this.formGroup.get('descriptionTemplates').get(sectionId.toString()).value as Guid[];
	// 			if (data) {
	// 				data.push(groupId);
	// 				this.formGroup.get('descriptionTemplates').get(sectionId.toString()).patchValue(data);
	// 			} else {
	// 				this.formGroup.get('descriptionTemplates').get(sectionId.toString()).patchValue([groupId]);
	// 			}
	// 		}
	// 	});
    // }

	// canRemoveDescriptionTemplate(item: DescriptionTemplate, sectionId): MultipleAutoCompleteCanRemoveItem {
	// 	if (item) {
	// 		const descriptionsInSection = this.descriptionsInSection(sectionId);
    //         const templateInUse = descriptionsInSection?.some((x) => x.formGroup?.value?.descriptionTemplateId === item.id);
    //         return {
    //             canRemove: !templateInUse,
	// 			message: templateInUse ? 'PLAN-EDITOR.UNSUCCESSFUL-REMOVE-TEMPLATE' : null
    //         } as MultipleAutoCompleteCanRemoveItem;
	// 	}
	// 	return;
	// }

	//
	//
	// Annotations
	//
	//
	showAnnotations(anchorId: string) {
		const dialogRef = this.dialog.open(AnnotationDialogComponent, {
			width: '40rem',
			maxWidth: '90vw',
			maxHeight: '90vh',
			data: {
				entityId: this.item.id,
				anchor: anchorId,
				entityType: AnnotationEntityType.Plan,
				planUsers: this.item?.planUsers ?? [],
				canAnnotate: this.canAnnotatePerField.get(anchorId),
                generateLink: (anchor: string, entityId: Guid) => {
                    let currentPath = window.location.pathname;
                    const rgx = /\/f\/|\/d\/|\/annotation/;
                    currentPath = currentPath.split(rgx)?.[0]; //get base of route, remove any descriptionid, fieldId, or annotation it might contain
                    return this.routerUtils.generateUrl([currentPath, 'f', anchor, 'annotation'].join('/'));
                }
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(changesMade => {
			if (changesMade) {
				this.formAnnotationService.refreshAnnotations();
			}
		});
	}

    canDeactivate(){
        return !this.formGroup?.dirty && !this.lockedDescriptions?.length && !this.newDescriptionIds.length;
    }

    ngOnDestroy(): void {
        super.ngOnDestroy();
        this.planTempStorage.reset();
    }
}
