import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { AfterViewInit, Component, OnInit } from '@angular/core';
import { FormArray, UntypedFormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { AnnotationEntityType } from '@app/core/common/enum/annotation-entity-type';
import { DescriptionStatusEnum } from '@app/core/common/enum/description-status';
import { FileTransformerEntityType } from '@app/core/common/enum/file-transformer-entity-type';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { LockTargetType } from '@app/core/common/enum/lock-target-type';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { PlanAccessType } from '@app/core/common/enum/plan-access-type';
import { PlanBlueprintFieldCategory } from '@app/core/common/enum/plan-blueprint-field-category';
import { PlanBlueprintExtraFieldDataType } from '@app/core/common/enum/plan-blueprint-field-type';
import { PlanBlueprintStatus } from '@app/core/common/enum/plan-blueprint-status';
import { PlanBlueprintSystemFieldType } from '@app/core/common/enum/plan-blueprint-system-field-type';
import { PlanStatusEnum } from '@app/core/common/enum/plan-status';
import { PlanUserRole } from '@app/core/common/enum/plan-user-role';
import { PlanUserType } from '@app/core/common/enum/plan-user-type';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { DescriptionSectionPermissionResolver } from '@app/core/model/description/description';
import { LanguageInfo } from '@app/core/model/language-info';
import { FieldInSection, PlanBlueprint, PlanBlueprintDefinitionSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { Plan, PlanPersist } from '@app/core/model/plan/plan';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { LanguageInfoService } from '@app/core/services/culture/language-info-service';
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
import { MultipleAutoCompleteConfiguration } from '@app/library/auto-complete/multiple/multiple-auto-complete-configuration';
import { MultipleAutoCompleteCanRemoveItem } from '@app/library/auto-complete/multiple/multiple-auto-complete.component';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { DescriptionTemplatePreviewDialogComponent } from '@app/ui/admin/description-template/description-template-preview/description-template-preview-dialog.component';
import { AnnotationDialogComponent } from '@app/ui/annotations/annotation-dialog-component/annotation-dialog.component';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditor } from '@common/base/base-editor';
import { FormService } from '@common/forms/form-service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { map, takeUntil } from 'rxjs/operators';
import { PlanContactPrefillDialogComponent } from '../plan-contact-prefill-dialog/plan-contact-prefill-dialog.component';
import { PlanFinalizeDialogComponent, PlanFinalizeDialogOutput } from '../plan-finalize-dialog/plan-finalize-dialog.component';
import { PlanEditorModel, PlanFieldIndicator } from './plan-editor.model';
import { PlanEditorService } from './plan-editor.service';
import { PlanEditorEntityResolver } from './resolvers/plan-editor-enitity.resolver';
import { FormAnnotationService } from '@app/ui/annotations/annotation-dialog-component/form-annotation.service';
import { PlanAssociatedUser } from '@app/core/model/user/user';

@Component({
	selector: 'app-plan-editor',
	templateUrl: './plan-editor.component.html',
	styleUrls: ['./plan-editor.component.scss'],
	providers: [PlanEditorService]
})
export class PlanEditorComponent extends BaseEditor<PlanEditorModel, Plan> implements OnInit, AfterViewInit {

	isNew = true;
	isDeleted = false;
	isFinalized = false;
	viewOnly = false;
	item: Plan;
	selectedBlueprint: PlanBlueprint;
	step: number = 0;
	annotationsPerAnchor: Map<string, number> = new Map<string, number>();

	//Enums
	descriptionStatusEnum = DescriptionStatusEnum;
	planBlueprintSectionFieldCategoryEnum = PlanBlueprintFieldCategory;
	planBlueprintSystemFieldTypeEnum = PlanBlueprintSystemFieldType;
	planBlueprintExtraFieldDataTypeEnum = PlanBlueprintExtraFieldDataType;
	planAccessTypeEnum = PlanAccessType;
	planAccessTypeEnumValues = this.enumUtils.getEnumValues<PlanAccessType>(PlanAccessType);
	planUserTypeEnum = PlanUserType;
	planUserTypeEnumValues = this.enumUtils.getEnumValues<PlanUserType>(PlanUserType);
	planUserRoleEnumValues = this.enumUtils.getEnumValues<PlanUserRole>(PlanUserRole);
	fileTransformerEntityTypeEnum = FileTransformerEntityType;

	scrollToField: boolean = false;
	openAnnotation: boolean = false;

	permissionPerSection: Map<Guid, string[]>;

	hoveredContact: number = -1;

	singleAutocompleteBlueprintConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: (data?: any) => this.planBlueprintService.query(this.planBlueprintService.buildAutocompleteLookup(null, null, null, [PlanBlueprintStatus.Finalized])).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, data?: any) => this.planBlueprintService.query(this.planBlueprintService.buildAutocompleteLookup(searchQuery, null, null, [PlanBlueprintStatus.Finalized])).pipe(map(x => x.items)),
		getSelectedItem: (selectedItem: any) => this.planBlueprintService.query(this.planBlueprintService.buildAutocompleteLookup(null, null, [selectedItem])).pipe(map(x => x.items[0])),
		displayFn: (item: PlanBlueprint) => item.label,
		subtitleFn: (item: PlanBlueprint) => this.language.instant('PLAN-EDITOR.FIELDS.PLAN-BLUEPRINT-VERSION') + ' ' + item.version,
		titleFn: (item: PlanBlueprint) => item.label,
		valueAssign: (item: PlanBlueprint) => item.id,
	};

	getDescriptionTemplateMultipleAutoCompleteConfiguration(sectionId: Guid): MultipleAutoCompleteConfiguration {
		return {
			initialItems: (excludedItems: any[], data?: any) => this.descriptionTemplateService.query(this.descriptionTemplateService.buildDescriptionTempalteGroupAutocompleteLookup([IsActive.Active], null, excludedItems ? excludedItems : null)).pipe(map(x => x.items)),
			filterFn: (searchQuery: string, excludedItems: any[]) => this.descriptionTemplateService.query(this.descriptionTemplateService.buildDescriptionTempalteGroupAutocompleteLookup([IsActive.Active], searchQuery, excludedItems)).pipe(map(x => x.items)),
			getSelectedItems: (selectedItems: any[]) => this.descriptionTemplateService.query(this.descriptionTemplateService.buildDescriptionTempalteGroupAutocompleteLookup([IsActive.Active, IsActive.Inactive], null, null, selectedItems)).pipe(map(x => x.items)),
			displayFn: (item: DescriptionTemplate) => item.label,
			titleFn: (item: DescriptionTemplate) => item.label,
			subtitleFn: (item: DescriptionTemplate) => item.description,
			valueAssign: (item: DescriptionTemplate) => item.groupId,
			canRemoveItem: (item: DescriptionTemplate) => this.canRemoveDescriptionTemplate(item, sectionId),
			popupItemActionIcon: 'visibility'
		}
	};

	sectionToFieldsMap: Map<string, PlanFieldIndicator> = new Map<string, PlanFieldIndicator>();

	protected get canDelete(): boolean {
		return !this.isDeleted && !this.isNew && (this.hasPermission(this.authService.permissionEnum.DeletePlan) || this.item?.authorizationFlags?.some(x => x === AppPermission.DeletePlan));
	}

	protected get canSave(): boolean {
		return !this.isDeleted && !this.isFinalized && (this.hasPermission(this.authService.permissionEnum.EditPlan) || this.item?.authorizationFlags?.some(x => x === AppPermission.EditPlan));
	}

	protected get canFinalize(): boolean {
		return !this.isDeleted && !this.isNew && this.canEdit && this.isLockedByUser && !this.isFinalized && (this.hasPermission(this.authService.permissionEnum.EditPlan) || this.item?.authorizationFlags?.some(x => x === AppPermission.EditPlan));
	}

	protected get canReverseFinalize(): boolean {
		return !this.isDeleted && !this.isNew && this.isLockedByUser && this.item.status == PlanStatusEnum.Finalized && (this.hasPermission(this.authService.permissionEnum.EditPlan) || this.item?.authorizationFlags?.some(x => x === AppPermission.EditPlan));
	}

	protected canEditSection(id: Guid): boolean {
		return !this.isDeleted && !this.isFinalized && (this.hasPermission(this.authService.permissionEnum.EditDescription) || this.item?.authorizationFlags?.some(x => x === AppPermission.EditDescription) || (
			this.permissionPerSection && this.permissionPerSection[id.toString()] && this.permissionPerSection[id.toString()].some(x => x === AppPermission.EditDescription)
		));
	}

	protected canDeleteSection(id: Guid): boolean {
		return !this.isDeleted && !this.isFinalized && (this.hasPermission(this.authService.permissionEnum.DeleteDescription) || this.item?.authorizationFlags?.some(x => x === AppPermission.DeleteDescription) || (
			this.permissionPerSection && this.permissionPerSection[id.toString()] && this.permissionPerSection[id.toString()].some(x => x === AppPermission.DeleteDescription)
		));
	}

	get canEdit(): boolean {
		return !this.isDeleted && !this.isFinalized &&(this.isNew ? this.authService.hasPermission(AppPermission.NewPlan) : this.item.authorizationFlags?.some(x => x === AppPermission.EditPlan) || this.authService.hasPermission(AppPermission.EditPlan));
	}

	protected canAnnotate(id: Guid): boolean {
		return !this.isDeleted && this.permissionPerSection && this.permissionPerSection[id.toString()] && this.permissionPerSection[id.toString()].some(x => x === AppPermission.AnnotatePlan);
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
		// public visibilityRulesService: VisibilityRulesService,
		private languageInfoService: LanguageInfoService,
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
		if (this.isNew === false && this.step === 0) this.nextStep();

		this.formAnnotationService.init(this.item.id, AnnotationEntityType.Plan);
		this.formAnnotationService.getAnnotationCountObservable().pipe(takeUntil(this._destroyed)).subscribe((annotationsPerAnchor: Map<string, number>) => {
			this.annotationsPerAnchor = annotationsPerAnchor;
		});

		this.formAnnotationService.getOpenAnnotationSubjectObservable().pipe(takeUntil(this._destroyed)).subscribe((anchorId: string) => {
			if (anchorId && anchorId == this.item.id?.toString()) this.showAnnotations(anchorId);
		});
	}

	ngAfterViewInit(): void {

		
		this.route.params
			.pipe(takeUntil(this._destroyed))
			.subscribe((params: Params) => {
				const fieldId = params['fieldId'];

				this.scrollToField = this.route.snapshot.data['scrollToField'] ?? false
				this.openAnnotation = this.route.snapshot.data['openAnnotation'] ?? false;

				if (fieldId) {
					let fieldStep = this.item?.blueprint.definition.sections.find(s => s.fields.filter(f => f.id.toString() == fieldId)?.length > 0)?.ordinal;
					this.changeStep(fieldStep);
					setTimeout(() => this.changeStep1(fieldId), 600);
					
					const openAnnotation = this.route.snapshot.data['openAnnotation'] ?? false;
					if (openAnnotation) this.showAnnotations(fieldId);
				}
			});
	}

	getItem(itemId: Guid, successFunction: (item: Plan) => void) {
		this.planService.getSingle(itemId, PlanEditorEntityResolver.lookupFields())
			.pipe(map(data => data as Plan), takeUntil(this._destroyed))
			.subscribe(
				data => successFunction(data),
				error => this.onCallbackError(error)
			);
	}

	prepareForm(data: Plan) {
		try {
			if (data?.blueprint?.definition?.sections != null) {
				data.blueprint.definition.sections = data.blueprint.definition.sections.sort((s1, s2) => s1.ordinal - s2.ordinal);
				for (let i = 0; i < data.blueprint.definition.sections.length; i++) {
					data.blueprint.definition.sections[i].fields = data.blueprint.definition.sections[i]?.fields?.sort((f1, f2) => f1.ordinal - f2.ordinal);
				}
			}
			this.editorModel = data ? new PlanEditorModel().fromModel(data) : new PlanEditorModel();
			if (data) {
				if (data.descriptions) {
					if (data.status == PlanStatusEnum.Finalized) {
						data.descriptions = data.descriptions.filter(x => x.isActive === IsActive.Active && x.status === DescriptionStatusEnum.Finalized);
					} else {
						data.descriptions = data.descriptions.filter(x => x.isActive === IsActive.Active && x.status !== DescriptionStatusEnum.Canceled);
					}
				}
				if (data.planDescriptionTemplates) {
					data.planDescriptionTemplates = data.planDescriptionTemplates.filter(x => x.isActive === IsActive.Active);
				}
				if (data.entityDois && data.entityDois.length > 0) data.entityDois = data.entityDois.filter(x => x.isActive === IsActive.Active);

			}
			this.item = data;

			this.selectedBlueprint = data?.blueprint;
			this.isDeleted = data ? data.isActive === IsActive.Inactive : false;
			this.isFinalized = data ? data.status === PlanStatusEnum.Finalized : false;

			if (data && data.id) {
				const descriptionSectionPermissionResolverModel: DescriptionSectionPermissionResolver = {
					planId: data.id,
					sectionIds: data?.blueprint?.definition?.sections?.map(x => x.id),
					permissions: [AppPermission.EditDescription, AppPermission.DeleteDescription]
				}
				this.buildForm();
			} else {
				this.buildForm();
			}

			if (this.item && this.item.id != null) {
				this.checkLock(this.item.id, LockTargetType.Plan, 'PLAN-EDITOR.LOCKED-DIALOG.TITLE', 'PLAN-EDITOR.LOCKED-DIALOG.MESSAGE');
			}
		} catch (error) {
			this.logger.error('Could not parse Plan item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

	buildForm() {
		this.formGroup = this.editorModel.buildForm(null, this.isDeleted || !this.canEdit);

		this.sectionToFieldsMap = this.prepareErrorIndication();

		if (this.editorModel.status == PlanStatusEnum.Finalized || this.isDeleted) {
			this.viewOnly = true;
			this.isFinalized = this.editorModel.status == PlanStatusEnum.Finalized;
			this.formGroup.disable();
		} else {
			this.viewOnly = false;
		}
	}

	prepareErrorIndication(): Map<string, PlanFieldIndicator> {
		if (this.selectedBlueprint?.definition == null) return;

		const sectionToFieldsMap: Map<string, PlanFieldIndicator> = new Map<string, PlanFieldIndicator>();
		this.selectedBlueprint.definition.sections.forEach((section: PlanBlueprintDefinitionSection) => {
			let value: PlanFieldIndicator = new PlanFieldIndicator(section);
			sectionToFieldsMap.set(section.id.toString(), value);
		});

		return sectionToFieldsMap;
	}

	refreshData(): void {
		this.getItem(this.editorModel.id, (data: Plan) => {
			this.breadcrumbService.addIdResolvedValue(data.id.toString(), data.label);
			this.prepareForm(data)
			if (this.isNew === false && this.step === 0) this.nextStep();
		});
	}

	refreshOnNavigateToData(id?: Guid): void {
		this.formGroup.markAsPristine();

		if (this.isNew) {
			let route = [];
			route.push(this.routerUtils.generateUrl('/plans/edit/' + id));
			this.router.navigate(route, { queryParams: { 'lookup': this.queryParamsService.serializeLookup(this.lookupParams), 'lv': ++this.lv }, replaceUrl: true, relativeTo: this.route });
		} else {
			this.refreshData();
		}
	}

	persistEntity(onSuccess?: (response) => void): void {
		const formData = this.formService.getValue(this.formGroup.value) as PlanPersist;

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

		this.planService.persist(formData)
			.pipe(takeUntil(this._destroyed)).subscribe(
				complete => onSuccess ? onSuccess(complete) : this.onCallbackSuccess(complete),
				error => this.onCallbackError(error)
			);
	}

	formSubmit(onSuccess?: (response) => void): void {
		this.formService.removeAllBackEndErrors(this.formGroup);
		this.persistEntity(onSuccess);
	}

	discardChanges() {
		let messageText = "";
		let confirmButtonText = "";
		let cancelButtonText = "";
		let isDeleteConfirmation = false;

		if (this.isNew) {

			messageText = this.language.instant('PLAN-EDITOR.ACTIONS.DISCARD.DISCARD-NEW-MESSAGE');
			confirmButtonText = this.language.instant('PLAN-EDITOR.ACTIONS.DISCARD.DISCARD-NEW-CONFIRM');
			cancelButtonText = this.language.instant('PLAN-EDITOR.ACTIONS.DISCARD.DISCARD-NEW-DENY');
			isDeleteConfirmation = true;
		} else {
			messageText = this.language.instant('PLAN-EDITOR.ACTIONS.DISCARD.DISCARD-EDITED-MESSAGE');
			confirmButtonText = this.language.instant('PLAN-EDITOR.ACTIONS.DISCARD.DISCARD-EDITED-CONFIRM');
			cancelButtonText = this.language.instant('PLAN-EDITOR.ACTIONS.DISCARD.DISCARD-EDITED-DENY');
			isDeleteConfirmation = false;
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
					if (this.isNew) this.step = 0;
					else this.step = this.step > 0 ? this.step - 1 : 0;
					this.refreshOnNavigateToData();
				});
			}
		});

	}

	public delete() {
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
						.subscribe(
							complete => this.onCallbackDeleteSuccess(),
							error => this.onCallbackError(error)
						);
				}
			});
		}
	}

	public saveAndClose(): void {
		this.formSubmit((data) => {
			this.uiNotificationService.snackBarNotification(this.isNew ? this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-CREATION') : this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
			this.formGroup = null;
			this.router.navigate([this.routerUtils.generateUrl('/plans/')]);
		});
	}

	hasNotDoi() {
		return (this.item.entityDois == null || this.item.entityDois.length == 0);
	}

	finalize() {
		const dialogRef = this.dialog.open(PlanFinalizeDialogComponent, {
			maxWidth: '500px',
			restoreFocus: false,
			autoFocus: false,
			data: {
				plan: this.item
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe((result: PlanFinalizeDialogOutput) => {
			if (result && !result.cancelled) {

				this.planService.finalize(this.item.id, result.descriptionsToBeFinalized)
					.pipe(takeUntil(this._destroyed))
					.subscribe(data => {
						this.onCallbackSuccess()
						this.router.navigate([this.routerUtils.generateUrl(['/plans/overview', this.item.id.toString()], '/')])
					}, (error: any) => {
						this.onCallbackError(error)
					});
			}
		});

	}

	reverseFinalization() {
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
				this.planService.undoFinalize(this.item.id, PlanEditorEntityResolver.lookupFields()).pipe(takeUntil(this._destroyed))
					.subscribe(data => {
						this.onCallbackSuccess()
					}, (error: any) => {
						this.onCallbackError(error)
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

	changeStep(index: number) {
		this.step = index;
		this.resetScroll();
	}

	public changeStep1(fieldId: string = null) {
		if (!fieldId) return;

		const element = document.getElementById(fieldId);
		if (element) {
			element.scrollIntoView({ behavior: 'smooth' });
		}
	}

	nextStep() {
		this.step = this.step < this.selectedBlueprint.definition.sections.length ? this.step + 1 : this.step;
		this.resetScroll();
	}

	previousStep() {
		this.step = this.step !== 1 ? this.step - 1 : this.step;
		this.resetScroll();
	}

	hasErrors(sectionId: string): boolean {
		const formControlBySection = this.sectionToFieldsMap?.get(sectionId);
		if (formControlBySection == null || this.formGroup == null || ((this.formGroup?.disabled??false)==true || (!this.isNew && !this.canSave))) return false;

		for (let controlName of formControlBySection.fieldControlNames) {
			if (this.isFormControlValid(controlName) === false) {
				return true;
			}
		}

		return false;
	}

	isFormControlValid(controlName: string): boolean {
		if (!this.formGroup?.get(controlName)) return true;
		if (!this.formGroup.get(controlName).touched) return true;

		return this.formGroup.get(controlName).valid;
	}

	private resetScroll() {
		if (document.getElementById('editor-form') != null) document.getElementById('editor-form').scrollTop = 0;
	}

	//
	//
	// Blueprint
	//
	//
	selectBlueprint() {
		this.planBlueprintService.getSingle(this.formGroup.get('blueprint').value, PlanEditorEntityResolver.blueprintLookupFields()).pipe(takeUntil(this._destroyed))
			.subscribe(data => {
				this.selectedBlueprint = data;
				this.buildFormAfterBlueprintSelection();
				this.nextStep();
			},
				error => this.httpErrorHandlingService.handleBackedRequestError(error));
	}

	selectDefaultBlueprint(): void {
		this.planBlueprintService.getSingle(this.configurationService.defaultBlueprintId, PlanEditorEntityResolver.blueprintLookupFields()).pipe(takeUntil(this._destroyed))
			.subscribe(data => {
				this.selectedBlueprint = data;
				this.formGroup.get('blueprint').setValue(this.selectedBlueprint.id);

				const goToNextStep: boolean = this.formGroup.get('label').valid;
				if (goToNextStep) {
					this.buildFormAfterBlueprintSelection();
					this.nextStep();
				}
			},
				error => this.httpErrorHandlingService.handleBackedRequestError(error));
	}

	private buildFormAfterBlueprintSelection() {
		const plan: Plan = {
			label: this.formGroup.get('label').value,
			description: this.formGroup.get('description').value,
			blueprint: this.selectedBlueprint,
			status: PlanStatusEnum.Draft
		}

		this.prepareForm(plan);
	}

	//
	//
	// Contacts
	//
	//

	isContactSelected(contactId: number): boolean {
		return this.hoveredContact === contactId;
	}

	onContactHover(contactIndex: number): void {
		this.hoveredContact = contactIndex;
	}

	clearHoveredContact(): void {
		this.hoveredContact = -1;
	}

	addContact(): void {
		const contactArray = this.formGroup.get('properties').get('contacts') as FormArray;
		(this.formGroup.get('properties').get('contacts') as FormArray).push(this.editorModel.createChildContact(contactArray.length));
	}

	removeContact(contactIndex: number): void {
		(this.formGroup.get('properties').get('contacts') as FormArray).removeAt(contactIndex);

		PlanEditorModel.reApplyPropertiesValidators(
			{
				formGroup: this.formGroup,
				validationErrorModel: this.editorModel.validationErrorModel,
				blueprint: this.item.blueprint
			}
		);
		this.formGroup.get('properties').get('contacts').markAsDirty();
	}

	dropContacts(event: CdkDragDrop<string[]>) {
		const contactsFormArray = (this.formGroup.get('properties').get('contacts') as FormArray);

		moveItemInArray(contactsFormArray.controls, event.previousIndex, event.currentIndex);
		contactsFormArray.updateValueAndValidity();

		PlanEditorModel.reApplyPropertiesValidators(
			{
				formGroup: this.formGroup,
				validationErrorModel: this.editorModel.validationErrorModel,
				blueprint: this.item.blueprint
			}
		);
		this.formGroup.get('properties').get('contacts').markAsDirty();
	}

	searchContact(contactIndex: number, field: FieldInSection): void {
		const dialogRef = this.dialog.open(PlanContactPrefillDialogComponent, {
			maxWidth: '700px',
			maxHeight: '80vh',
			data: {
				label: field.label
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe((result: PlanAssociatedUser) => {
			if (result) {
				const contactFormGroup = (this.formGroup.get('properties').get('contacts') as FormArray).at(contactIndex);
				contactFormGroup.get('firstName').patchValue(result?.name);
				contactFormGroup.get('lastName').patchValue(null);
				contactFormGroup.get('email').patchValue(result?.email);
				
				this.uiNotificationService.snackBarNotification(this.language.instant('PLAN-EDITOR.SNACK-BAR.SUCCESSFUL-PLAN-CONTACT'), SnackBarNotificationLevel.Success);
			}
		});
	}


	//
	//
	// Descriptions
	//
	//
	public descriptionsInSection(sectionId: Guid) {
		return this.item?.descriptions?.filter(x => x.isActive == IsActive.Active && x?.planDescriptionTemplate?.sectionId === sectionId && x.planDescriptionTemplate.isActive == IsActive.Active) || [];
	}

	editDescription(id: string, isNew: boolean) {
		if (!isNew) {
			this.router.navigate([this.routerUtils.generateUrl(['/descriptions', 'edit', id], '/')]);
		}
	}

	public removeDescription(descriptionId: Guid) {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			maxWidth: '300px',
			restoreFocus: false,
			data: {
				message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-ITEM'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.DELETE'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
				isDeleteConfirmation: true
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result) {
				if (descriptionId) {
					this.descriptionService.delete(descriptionId)
						.pipe(takeUntil(this._destroyed))
						.subscribe(
							complete => {
								this.onCallbackSuccess();
							},
							error => this.onCallbackError(error)
						);
				}
				this.step = 0;
			}
		});
	}

	hasDescriptionTemplates(section: PlanBlueprintDefinitionSection): boolean {
		if (this.item.planDescriptionTemplates?.filter(x => x.sectionId == section.id).length > 0) return true;
		return false;
	}

	hasValidMultiplicity(section: PlanBlueprintDefinitionSection): boolean {
		if (section.hasTemplates) {
			if (section.descriptionTemplates?.length > 0) {
				const descriptions = this.descriptionsInSection(section.id)

				if (this.item.planDescriptionTemplates.filter(x => x.sectionId == section.id).length > descriptions.map(x => x.planDescriptionTemplate).length) {
					return true;
				}

				let multiplicityValidResults: boolean[] = [];
				const descriptionTemplatesGroupIds = this.formGroup.get('descriptionTemplates').get(section.id.toString()).value as Guid[] || [];
				let descriptionTemplatesGroupIdsWithMaxMultitplicity: Guid[] = [];
				section.descriptionTemplates.forEach(sectionDescriptionTemplate => {
					if (sectionDescriptionTemplate.maxMultiplicity != null) {
						const count = descriptions.filter(x => x.planDescriptionTemplate.descriptionTemplateGroupId == sectionDescriptionTemplate.descriptionTemplateGroupId).length || 0;
						if (count >= sectionDescriptionTemplate.maxMultiplicity) multiplicityValidResults.push(false);
						else multiplicityValidResults.push(true);
						descriptionTemplatesGroupIdsWithMaxMultitplicity.push(sectionDescriptionTemplate.descriptionTemplateGroupId);
					} else {
						multiplicityValidResults.push(true);
					}
				})

				if (descriptionTemplatesGroupIdsWithMaxMultitplicity.length > 0 && descriptionTemplatesGroupIds.length > 0) {
					const descriptionTemplatesWithoutMaxMultiplicity = descriptionTemplatesGroupIds.filter(x => !descriptionTemplatesGroupIdsWithMaxMultitplicity.map(y => y).includes(x)) || [];
					if (descriptionTemplatesWithoutMaxMultiplicity.length > 0 && this.formGroup.pristine) return true;
				}

				if (multiplicityValidResults.includes(true)) return true
				else return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	//
	//
	// Description Template
	//
	//

	onPreviewDescriptionTemplate(event, sectionId: Guid) {
		const dialogRef = this.dialog.open(DescriptionTemplatePreviewDialogComponent, {
			width: '590px',
			minHeight: '200px',
			restoreFocus: false,
			data: {
				descriptionTemplateId: event.id
			},
			panelClass: 'custom-modalbox'
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(groupId => {
			if (groupId) {
				let data = this.formGroup.get('descriptionTemplates').get(sectionId.toString()).value as Guid[];
				if (data) {
					data.push(groupId);
					this.formGroup.get('descriptionTemplates').get(sectionId.toString()).patchValue(data);
				} else {
					this.formGroup.get('descriptionTemplates').get(sectionId.toString()).patchValue([groupId]);
				}
			}
		});
	}

	onRemoveDescriptionTemplate(event, sectionId: Guid) {
		let foundDescription = false;
		const descriptionsInSection = this.descriptionsInSection(sectionId);
		let descriptionTemplatesInSection = this.formGroup.get('descriptionTemplates').get(sectionId.toString()).value as Guid[];

		if (descriptionsInSection && descriptionsInSection.length > 0) {
			for (let index = 0; index < descriptionsInSection.length; index++) {
				const description = descriptionsInSection[index];
				if (description.planDescriptionTemplate?.descriptionTemplateGroupId === event.groupId) {
					foundDescription = true;
					this.uiNotificationService.snackBarNotification(this.language.instant('PLAN-EDITOR.UNSUCCESSFUL-REMOVE-TEMPLATE'), SnackBarNotificationLevel.Error);
					break;
				}
			}

			if (foundDescription) {
				if (descriptionTemplatesInSection) this.formGroup.get('descriptionTemplates').get(sectionId.toString()).patchValue(descriptionTemplatesInSection);
				else this.formGroup.get('descriptionTemplates').get(sectionId.toString()).patchValue([]);
			}
		}
	}

	canRemoveDescriptionTemplate(item: DescriptionTemplate, sectionId): MultipleAutoCompleteCanRemoveItem {
		if (item) {
			const descriptionsInSection = this.descriptionsInSection(sectionId);

			if (descriptionsInSection && descriptionsInSection.length > 0) {
				for (let index = 0; index < descriptionsInSection.length; index++) {
					const description = descriptionsInSection[index];
					if (description.planDescriptionTemplate?.descriptionTemplateGroupId === item.groupId) {
						return {
							canRemove: false,
							message: 'PLAN-EDITOR.UNSUCCESSFUL-REMOVE-TEMPLATE'
						} as MultipleAutoCompleteCanRemoveItem
					}
				}
			}
		}
		return {
			canRemove: true,
		} as MultipleAutoCompleteCanRemoveItem;
	}

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
				queryIncludesField: this.scrollToField,
				queryIncludesAnnotation: this.openAnnotation,
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(changesMade => {
			if (changesMade) {
				this.formAnnotationService.refreshAnnotations();
			}
		});
	}

	//
	//
	// Misc
	//
	//
	public isDirty(): boolean {
		return this.formGroup && this.formGroup.dirty;
	}

	getLanguageInfos(): LanguageInfo[] {
		return this.languageInfoService.getLanguageInfoValues();
	}

	copyLink(fieldId: string): void {
		const el = document.createElement('textarea');
		let domain = `${window.location.protocol}//${window.location.hostname}`;
		if (window.location.port && window.location.port != '') domain += `:${window.location.port}`
		const descriptionSectionPath = this.routerUtils.generateUrl(['plans/edit', this.item.id.toString(), 'f', fieldId].join('/'));
		el.value = domain + descriptionSectionPath;
		el.setAttribute('readonly', '');
		el.style.position = 'absolute';
		el.style.left = '-9999px';
		document.body.appendChild(el);
		el.select();
		document.execCommand('copy');
		document.body.removeChild(el);
		this.uiNotificationService.snackBarNotification(
			this.language.instant('DESCRIPTION-EDITOR.QUESTION.EXTENDED-DESCRIPTION.COPY-LINK-SUCCESSFUL'), 
			SnackBarNotificationLevel.Success
		);
	}
}
