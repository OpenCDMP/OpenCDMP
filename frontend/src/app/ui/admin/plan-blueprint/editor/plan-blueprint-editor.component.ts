
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { Component, OnInit, signal, ViewChild } from '@angular/core';
import { FormArray, FormGroup, UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { MatDialog } from '@angular/material/dialog';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { PlanBlueprintFieldCategory } from '@app/core/common/enum/plan-blueprint-field-category';
import { PlanBlueprintExtraFieldDataType } from '@app/core/common/enum/plan-blueprint-field-type';
import { PlanBlueprintStatus } from '@app/core/common/enum/plan-blueprint-status';
import { PlanBlueprintSystemFieldType } from '@app/core/common/enum/plan-blueprint-system-field-type';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { LockTargetType } from '@app/core/common/enum/lock-target-type';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { PlanBlueprint, PlanBlueprintPersist, NewVersionPlanBlueprintPersist, SystemFieldInSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { PlanBlueprintService } from '@app/core/services/plan/plan-blueprint.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { PrefillingSourceService } from '@app/core/services/prefilling-source/prefilling-source.service';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { FileUtils } from '@app/core/services/utilities/file-utils.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { BaseEditor } from '@common/base/base-editor';
import { FormService } from '@common/forms/form-service';
import { FormValidationErrorsDialogComponent } from '@common/forms/form-validation-errors-dialog/form-validation-errors-dialog.component';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import * as FileSaver from 'file-saver';
import { map, takeUntil } from 'rxjs/operators';
import { DescriptionTemplatePreviewDialogComponent } from '../../description-template/description-template-preview/description-template-preview-dialog.component';
import { PlanBlueprintEditorModel, FieldInSectionEditorModel, PlanBlueprintForm } from './plan-blueprint-editor.model';
import { PlanBlueprintEditorResolver } from './plan-blueprint-editor.resolver';
import { PlanBlueprintEditorService } from './plan-blueprint-editor.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { StepperSelectionEvent } from '@angular/cdk/stepper';
import { MatStepper } from '@angular/material/stepper';
import { GENERAL_ANIMATIONS, STEPPER_ANIMATIONS } from '@app/library/animations/animations';
import { PlanBlueprintTocComponent } from './table-of-content/plan-blueprint-toc.component';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { PlanEditorForm, PlanEditorModel } from '@app/ui/plan/plan-editor-blueprint/plan-editor.model';
import { Plan } from '@app/core/model/plan/plan';


@Component({
    selector: 'app-plan-blueprint-editor-component',
    templateUrl: 'plan-blueprint-editor.component.html',
    styleUrls: ['./plan-blueprint-editor.component.scss'],
    providers: [PlanBlueprintEditorService],
    animations: [...STEPPER_ANIMATIONS, ...GENERAL_ANIMATIONS],
    standalone: false
})
export class PlanBlueprintEditorComponent extends BaseEditor<PlanBlueprintEditorModel, PlanBlueprint> implements OnInit {
    @ViewChild('stepper') stepper: MatStepper;
	formGroup: FormGroup<PlanBlueprintForm>;
	showInactiveDetails = false;

	hoveredSectionIndex: number = -1;
	hoveredDescriptionTemplateIndex: number = -1;

	planBlueprintSectionFieldCategory = PlanBlueprintFieldCategory;
	planBlueprintSystemFieldType = PlanBlueprintSystemFieldType;
	planBlueprintExtraFieldDataType = PlanBlueprintExtraFieldDataType;
	public usedDescriptionTemplateGroupIdsBySection: Map<Guid, Guid[]> = new Map<Guid, Guid[]>;
	public descriptionTemplateGroupIdsConfigBySection: Map<Guid, SingleAutoCompleteConfiguration> = new Map<Guid, SingleAutoCompleteConfiguration>;

	PlanBlueprintStatus = PlanBlueprintStatus;
	isNew = true;
	isClone = false;
	isNewVersion = false;
	isDeleted = false;
	belongsToCurrentTenant = true;

    reorderingMode = signal<boolean>(false);

    selectedSection: number = 0;
    selectedField: number = null;

    previewEditorModel = new PlanEditorModel();
    
	protected get isTransient(): boolean {
		return this.isNew || this.isClone || this.isNewVersion;
	}

	protected get hideEditActions(): boolean {
		return (this.isDeleted || this.isFinalized || !this.authService.hasPermission(AppPermission.EditPlanBlueprint) || !this.belongsToCurrentTenant) && !this.isNewVersion;
	}

	protected get canDownloadXML(): boolean {
		return (this.formGroup.get('status').value === PlanBlueprintStatus.Finalized) && !this.isTransient;
	}

	protected get canDelete(): boolean {
		return !this.isDeleted && !this.isTransient && this.belongsToCurrentTenant && this.hasPermission(this.authService.permissionEnum.DeletePlanBlueprint);
	}

	protected get canFinalize(): boolean {
		return !this.isTransient && !this.isFinalized;
	}

	protected get isFinalized(): boolean {
		return this.editorModel.status == PlanBlueprintStatus.Finalized;
	}

    protected fieldIsActive(sectionIndex: number, fieldIndex: number){
        return this.selectedSection === sectionIndex && this.selectedField === fieldIndex;
    }

	private hasPermission(permission: AppPermission): boolean {
		return this.authService.hasPermission(permission) || this.editorModel?.permissions?.includes(permission);
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
		public enumUtils: EnumUtils,
		private planBlueprintService: PlanBlueprintService,
		private logger: LoggingService,
		private planBlueprintEditorService: PlanBlueprintEditorService,
		private fileUtils: FileUtils,
		public descriptionTemplateService: DescriptionTemplateService,
		public referenceTypeService: ReferenceTypeService,
		public prefillingSourceService: PrefillingSourceService,
		public titleService: Title,
		private analyticsService: AnalyticsService,
		protected routerUtils: RouterUtilsService,
	) {
		const descriptionLabel: string = route.snapshot.data['entity']?.label;
		if (descriptionLabel) {
			titleService.setTitle(descriptionLabel);
		} else {
			titleService.setTitle('PLAN-BLUEPRINT-EDITOR.TITLE-EDIT-BLUEPRINT');
		}
		super(dialog, language, formService, router, uiNotificationService, httpErrorHandlingService, filterService, route, queryParamsService, lockService, authService, configurationService);
	}

	ngOnInit(): void {
		this.analyticsService.trackPageView(AnalyticsService.PlanBlueprintEditor);
		this.initModelFlags(this.route.snapshot.data['action']);
		super.ngOnInit();
		this.route.data.subscribe(d => {
			this.initModelFlags(d['action']);
		});
		if ((this.formGroup.get('definition').get('sections') as FormArray).length == 0) {
			this.addSection();
		}
	}

	private initModelFlags(action: string): void {
		if (action == 'clone') {
			this.isNew = false;
			this.isClone = true;
			this.isNewVersion = false;
		} else if (action == 'new-version') {
			this.isNew = false;
			this.isClone = false;
			this.isNewVersion = true;
		} else {
			this.isClone = false;
			this.isNewVersion = false;
		}
	}

	getItem(itemId: Guid, successFunction: (item: PlanBlueprint) => void) {
		this.planBlueprintService.getSingle(itemId, PlanBlueprintEditorResolver.lookupFields())
			.pipe(map(data => data as PlanBlueprint), takeUntil(this._destroyed))
			.subscribe({
				next: (data) => successFunction(data),
				error: (error) => this.onCallbackError(error)
			});
	}

	prepareForm(data: PlanBlueprint) {
		try {
			this.editorModel = data ? new PlanBlueprintEditorModel().fromModel(data) : new PlanBlueprintEditorModel();
			this.isDeleted = data ? data.isActive === IsActive.Inactive : false;
			this.belongsToCurrentTenant = this.isNew || data.belongsToCurrentTenant;

			this.buildForm();

			if (data && data.id) this.checkLock(data.id, LockTargetType.PlanBlueprint, 'PLAN-BLUEPRINT-EDITOR.LOCKED-DIALOG.TITLE', 'PLAN-BLUEPRINT-EDITOR.LOCKED-DIALOG.MESSAGE');
			if (data && data.definition?.sections) {
				const sectionWithDescriptionTemplates = data.definition.sections.filter(x => x.hasTemplates == true && x.descriptionTemplates != null && x.descriptionTemplates.length > 0) || [];
				if (sectionWithDescriptionTemplates.length > 0) {
					sectionWithDescriptionTemplates.forEach(section => this.descriptionTemplateGroupIdsConfigBySection.set(section.id, this.getdescriptionTemplateGroupSingleAutocompleteConfiguration(section.id)));
				}
			}

		} catch (error) {
			this.logger.error('Could not parse planBlueprint item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

	buildForm() {
		this.formGroup = this.editorModel.buildForm(this._destroyed, null, this.hideEditActions, (this.isNew || this.isClone));
		this.planBlueprintEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);
		if (this.isNewVersion) {
			this.formGroup.get('code').disable();
		}
	}

	refreshData(id?: Guid): void {
		this.getItem(id ?? this.editorModel.id, (data: PlanBlueprint) => this.prepareForm(data));
	}

	refreshOnNavigateToData(id?: Guid): void {
		this.formGroup.markAsPristine();
		if (this.isNew || this.isNewVersion || this.isClone) {
			let route = [];
			route.push(this.routerUtils.generateUrl('/plan-blueprints/' + id));
			this.router.navigate(route, { queryParams: { 'lookup': this.queryParamsService.serializeLookup(this.lookupParams), 'lv': ++this.lv }, replaceUrl: true, relativeTo: this.route });
		} else {
			this.refreshData(id);
		}
	}

	persistEntity(onSuccess?: (response) => void): void {
		if (!this.isNewVersion) {
			const formData = JSON.parse(JSON.stringify(this.formGroup.value)) as PlanBlueprintPersist;
			formData.code = this.formGroup.get('code').getRawValue();

			this.planBlueprintService.persist(formData)
				.pipe(takeUntil(this._destroyed)).subscribe({
					next: (complete) => onSuccess ? onSuccess(complete) : this.onCallbackSuccess(complete),
					error: (error) => {
						this.formGroup.get('status').setValue(PlanBlueprintStatus.Draft);
						this.onCallbackError(error);
					}
				});
		} else if (!this.isNew && !this.isClone) {
			const formData = JSON.parse(JSON.stringify(this.formGroup.value)) as NewVersionPlanBlueprintPersist;

			this.planBlueprintService.newVersion(formData)
				.pipe(takeUntil(this._destroyed)).subscribe({
					next: (complete) => onSuccess ? onSuccess(complete) : this.onCallbackSuccess(complete),
					error: (error) => this.onCallbackError(error)
				});
		}
	}

	formSubmit(): void {
		this.formService.removeAllBackEndErrors(this.formGroup);
		this.formService.touchAllFormFields(this.formGroup);
		if (!this.isFormValid() || !this.hasDescriptionTemplates()) {
			this.checkValidity();
			return;
		}

		this.persistEntity();
	}

	public delete() {
		const value = this.formGroup.getRawValue();
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
					this.planBlueprintService.delete(value.id).pipe(takeUntil(this._destroyed))
						.subscribe({
							complete: () => this.onCallbackDeleteSuccess(),
							error: (error) => this.onCallbackError(error)
						});
				}
			});
		}
	}

	clearErrorModel() {
		this.editorModel.validationErrorModel.clear();
		this.formService.validateAllFormFields(this.formGroup);
	}

    protected changeStep(params: {
        section: number,
        field?: number
    }) {
        const {section, field} = params;
        this.selectedSection = section;
        this.selectedField = field;
        if(field != null){
            this.scrollToSelected({section, field})  
        }else {
            this.scrollOnTop();
        }
    }

    protected scrollToSelected(params:{section: number, field: number}) {
        const {section, field} = params;
        if (section == null || section == undefined) return;
        const id = (field != null && field != undefined) ? `section-${section}-field-${field}` : `section-${section}`
        this.selectedSection = section;
        this.selectedField = field;
        setTimeout(() => {
            const element = document.getElementById(id);
            if (element) {
                element.scrollIntoView({ behavior: 'smooth' });
            }
        });
    }

    protected scrollOnTop() {
		try {
			const topPage = document.getElementById('editor-top');
			topPage.scrollIntoView({ behavior: 'smooth' });
		} catch (e) {
			console.log(e);
			console.log('could not scroll');
		}
	}

	addSection(): void {
		const formArray = this.formGroup.get('definition').get('sections') as FormArray;
		formArray.push(this.editorModel.createChildSection(this._destroyed, formArray.length));
        formArray.markAsDirty();
        setTimeout(() => {
            this.changeStep({section: formArray?.length - 1})
	    });
    }

	removeSection(sectionIndex: number): void {
        const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			restoreFocus: false,
			data: {
				message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-ITEM'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
				isDeleteConfirmation: true
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result) {
                (this.formGroup.get('definition').get('sections') as FormArray).removeAt(sectionIndex);
                (this.formGroup.get('definition').get('sections') as FormArray).controls.forEach((section, index) => {
                    section.get('ordinal').setValue(index + 1);
                });
        
                //Reapply validators
                PlanBlueprintEditorModel.reApplySectionValidators(
                    {
                        formGroup: this.formGroup,
                        validationErrorModel: this.editorModel.validationErrorModel
                    }
                );
                this.formGroup.get('definition').get('sections').markAsDirty();
			}
		});
	}
    

	dropSections(event: CdkDragDrop<string[]>) {
		const sectionsFormArray = (this.formGroup.get('definition').get('sections') as FormArray);

		moveItemInArray(sectionsFormArray.controls, event.previousIndex, event.currentIndex);
        sectionsFormArray.updateValueAndValidity();
        this.reapplySectionOrdinals();
        this.formGroup.get('definition').get('sections').markAsDirty();
        PlanBlueprintEditorModel.reApplySectionValidators(
			{
				formGroup: this.formGroup,
				validationErrorModel: this.editorModel.validationErrorModel
			}
		);
	}

    reapplySectionOrdinals(){
        const sectionsFormArray = (this.formGroup.get('definition').get('sections') as FormArray);
		sectionsFormArray.controls.forEach((section, index) => {
			section.get('ordinal').setValue(index + 1);
		});
    }


	//
	//
	// Fields
	//
	//

	addField(section: number, fieldIndex?: number) {
        const sectionFields = (this.formGroup.get('definition').get('sections') as FormArray).at(section).get('fields') as FormArray;
        const newField = this.editorModel.createChildField(this._destroyed, section, ((this.formGroup.get('definition').get('sections') as FormArray).at(section).get('fields') as FormArray).length);
        const newIndex = fieldIndex != null ? fieldIndex : sectionFields.length
        if(newIndex < sectionFields.length){
            sectionFields.controls.splice(newIndex, 0, newField);
            this.reapplyFieldOrdinals(section);
        }else {
            sectionFields.push(newField);
        }
        sectionFields.markAsDirty();
        setTimeout(() => {
            this.changeStep({section, field: newIndex})
        });
	}

	removeField(params:{section: number, field: number}): void {
        const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			restoreFocus: false,
			data: {
				message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-ITEM'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
				isDeleteConfirmation: true
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result) {
                const {section, field} = params
                const formArray = ((this.formGroup.get('definition').get('sections') as FormArray).at(section).get('fields') as FormArray);
                formArray.removeAt(field);
                formArray.controls.forEach((section, index) => {
                    section.get('ordinal').setValue(index + 1);
                });
        
                //Reapply validators
                PlanBlueprintEditorModel.reApplySectionValidators(
                    {
                        formGroup: this.formGroup,
                        validationErrorModel: this.editorModel.validationErrorModel
                    }
                );
                (this.formGroup.get('definition').get('sections') as FormArray).at(section).get('fields').markAsDirty();
			}
		});
	}

    // systemFieldDisabled(systemField: PlanBlueprintSystemFieldType) {
	// 	return (this.formGroup.get('definition').get('sections') as FormArray)?.controls.some(x => (x.get('fields') as FormArray).controls.some(y => (y as UntypedFormGroup).get('systemFieldType')?.value === systemField));
	// }

    get disabledSystemFields(): Set<PlanBlueprintSystemFieldType>{
        const sectionSystemFields = this.formGroup.controls.definition.controls.sections.controls
            .map((x) => x.controls.fields?.value?.filter((x) => x.category === PlanBlueprintFieldCategory.System)?.map((x) => x.systemFieldType as PlanBlueprintSystemFieldType) ?? []);
        return new Set(sectionSystemFields?.reduce((a,b) => [...a, ...b])?.filter((x) => x != null && x != undefined) ?? [])
    }

	fieldCategoryChanged(sectionIndex: number, fieldIndex: number) {
		//Reapply validators
		// PlanBlueprintEditorModel.reApplySectionValidators(
		// 	{
		// 		formGroup: this.formGroup,
		// 		validationErrorModel: this.editorModel.validationErrorModel
		// 	}
		// );
		(this.formGroup.get('definition').get('sections') as FormArray).at(sectionIndex).get('fields').markAsDirty();
	}


	dropFields(params: {event: CdkDragDrop<string[]>, sectionIndex: number}) {
        const {event, sectionIndex} = params;
		const fieldsFormArray = ((this.formGroup.get('definition').get('sections') as FormArray).at(sectionIndex).get('fields') as FormArray);
		moveItemInArray(fieldsFormArray.controls, event.previousIndex, event.currentIndex);
        fieldsFormArray.updateValueAndValidity();
        this.reapplyFieldOrdinals(sectionIndex);
        PlanBlueprintEditorModel.reApplySectionValidators({
			formGroup: this.formGroup,
			validationErrorModel: this.editorModel.validationErrorModel
		});
        (this.formGroup.get('definition').get('sections') as FormArray).at(sectionIndex).get('fields').markAsDirty();
        this.changeStep({section: sectionIndex, field: event.currentIndex});
        this.reorderingMode.set(false);
	}

    reapplyFieldOrdinals(sectionIndex: number){
        const fieldsFormArray = ((this.formGroup.get('definition').get('sections') as FormArray).at(sectionIndex).get('fields') as FormArray);
        fieldsFormArray.controls.forEach((field, index) => {
			field.get('ordinal').setValue(index + 1);
		});
    }

	//Description Templates

	isDescriptionTemplateSelected(descriptionTemplateId: number): boolean {
		return this.hoveredDescriptionTemplateIndex === descriptionTemplateId;
	}

	onDescriptionTemplateHover(descriptionTemplateId: any): void {
		this.hoveredDescriptionTemplateIndex = descriptionTemplateId;
	}

	clearHoveredDescriptionTemplate(): void {
		this.hoveredDescriptionTemplateIndex = -1;
	}

	removeAllDescriptionTemplates(matCheckBox: MatCheckboxChange, sectionIndex: number) {
		if (matCheckBox.checked == false) {
			const descriptionTemplateSize = ((this.formGroup.get('definition').get('sections') as FormArray).at(sectionIndex).get('descriptionTemplates') as FormArray).length;
			for (let i = 0; i < descriptionTemplateSize; i++) this.removeDescriptionTemplate(sectionIndex, 0);

			const prefillingSourcesEnabledControl = (this.formGroup.get('definition').get('sections') as FormArray).at(sectionIndex).get('prefillingSourcesEnabled');
			prefillingSourcesEnabledControl.reset();

			this.removeAllPrefillingSources(matCheckBox, sectionIndex);
		}
	}

	removeAllPrefillingSources(matCheckBox: MatCheckboxChange, sectionIndex: number) {
		if (matCheckBox.checked == false) {
			const prefillingSourcesSize = ((this.formGroup.get('definition').get('sections') as FormArray).at(sectionIndex).get('prefillingSourcesIds')?.value as []).length;
			for (let i = 0; i < prefillingSourcesSize; i++) this.removePrefillingSources(sectionIndex, 0);
		}
	}

	selectedDescriptionTemplate(descriptionTemplate: DescriptionTemplate, sectionId: Guid) {
		let excludedGroupIds: Guid[] = [];
		if (this.usedDescriptionTemplateGroupIdsBySection.get(sectionId)) {
			excludedGroupIds = this.usedDescriptionTemplateGroupIdsBySection.get(sectionId);
		}
		if (!excludedGroupIds.includes(descriptionTemplate.groupId)) excludedGroupIds.push(descriptionTemplate.groupId);
		this.usedDescriptionTemplateGroupIdsBySection.set(sectionId, excludedGroupIds);
	}

	addDescriptionTemplate(sectionIndex: number): void {
		const descriptionTempaltesArray = (this.formGroup.get('definition').get('sections') as FormArray).at(sectionIndex).get('descriptionTemplates') as FormArray;
		descriptionTempaltesArray.push(this.editorModel.createChildDescriptionTemplate(sectionIndex, ((this.formGroup.get('definition').get('sections') as FormArray).at(sectionIndex).get('descriptionTemplates') as FormArray).length));
		const sectionId = (this.formGroup.get('definition').get('sections') as FormArray).at(sectionIndex).get('id').value;
		if (!this.descriptionTemplateGroupIdsConfigBySection.has(sectionId)) this.descriptionTemplateGroupIdsConfigBySection.set(sectionId, this.getdescriptionTemplateGroupSingleAutocompleteConfiguration(sectionId));
	}


    private descriptionTemplateMap = new Map<Guid, DescriptionTemplate>([]);
	public getdescriptionTemplateGroupSingleAutocompleteConfiguration(sectionId: Guid): SingleAutoCompleteConfiguration {
		return {
			initialItems: (data?: any) => this.descriptionTemplateService.query(this.descriptionTemplateService.buildDescriptionTemplateGroupAutocompleteLookup({
                isActive: [IsActive.Active], 
                excludedGroupIds: this.getUsedDescriptionTemplateGroupIds(sectionId)
            })).pipe(map(x => x.items)),
			filterFn: (searchQuery: string, data?: any) => this.descriptionTemplateService.query(this.descriptionTemplateService.buildDescriptionTemplateGroupAutocompleteLookup({
                isActive: [IsActive.Active], 
                like: searchQuery, 
                excludedGroupIds: this.getUsedDescriptionTemplateGroupIds(sectionId) ? this.getUsedDescriptionTemplateGroupIds(sectionId) : null
            })).pipe(map(x => x.items)),
			getSelectedItem: (selectedItem: any) => this.descriptionTemplateService.query(this.descriptionTemplateService.buildDescriptionTemplateGroupAutocompleteLookup({
                isActive: [IsActive.Active, IsActive.Inactive], 
                groupIds: [selectedItem]
            })).pipe(map(x => x.items[0])),
			displayFn: (item: DescriptionTemplate) => item.label,
			titleFn: (item: DescriptionTemplate) => item.label,
			subtitleFn: (item: DescriptionTemplate) => item.description,
			valueAssign: (item: DescriptionTemplate) => { 
                this.descriptionTemplateMap.set(item.groupId, item);
                return item.groupId;
            },
			popupItemActionIcon: 'visibility'
		};
	}

	getUsedDescriptionTemplateGroupIds(sectionId: Guid): Guid[] {
		let excludedGroupIds: Guid[] = [];
		(this.formGroup.get('definition').get('sections') as FormArray).controls.forEach((section, index) => {
			if (section.get('id').value === sectionId) {
				if (this.usedDescriptionTemplateGroupIdsBySection.get(sectionId)) {
					excludedGroupIds = this.usedDescriptionTemplateGroupIdsBySection.get(sectionId);
				} else {
					this.usedDescriptionTemplateGroupIdsBySection.set(sectionId, excludedGroupIds);
				}
				const descriptionTempaltesArray = (this.formGroup.get('definition').get('sections') as FormArray).at(index).get('descriptionTemplates') as FormArray;
				if (descriptionTempaltesArray.length > 1) {
					descriptionTempaltesArray.controls.forEach((template, index) => {
						if (template.get('descriptionTemplateGroupId').value != undefined && !excludedGroupIds.includes(template.get('descriptionTemplateGroupId').value)) excludedGroupIds.push(template.get('descriptionTemplateGroupId').value as Guid);
					})
					this.usedDescriptionTemplateGroupIdsBySection.set(sectionId, excludedGroupIds);
				}
			}

		});

		return excludedGroupIds;
	}

	removeDescriptionTemplate(sectionIndex: number, descriptionTemplateIndex: number): void {
		const sectionId = (this.formGroup.get('definition').get('sections') as FormArray).at(sectionIndex).get('id').value;
		let groupIds = this.usedDescriptionTemplateGroupIdsBySection.get(sectionId);
		if (groupIds) {
			const descriptionTemplateGroupId = ((this.formGroup.get('definition').get('sections') as FormArray).at(sectionIndex).get('descriptionTemplates') as FormArray).at(descriptionTemplateIndex).get('descriptionTemplateGroupId').value || null;
			if (groupIds.includes(descriptionTemplateGroupId)) {
				const index = groupIds.indexOf(descriptionTemplateGroupId, 0);
				if (index > -1) {
					groupIds.splice(index, 1);
					this.usedDescriptionTemplateGroupIdsBySection.set(sectionId, groupIds);
				}
			}
		}

		const descriptionTempaltesArray = (this.formGroup.get('definition').get('sections') as FormArray).at(sectionIndex).get('descriptionTemplates') as FormArray;
		descriptionTempaltesArray.removeAt(descriptionTemplateIndex);

		PlanBlueprintEditorModel.reApplySectionValidators(
			{
				formGroup: this.formGroup,
				validationErrorModel: this.editorModel.validationErrorModel
			}
		);
		descriptionTempaltesArray.markAsDirty();
	}

	removePrefillingSources(sectionIndex: number, prefillingSourceIndex: number): void {
		const sectionId = (this.formGroup.get('definition').get('sections') as FormArray).at(sectionIndex).get('id').value;

		const prefillingSourcesControl = (this.formGroup.get('definition').get('sections') as FormArray).at(sectionIndex).get('prefillingSourcesIds');
		prefillingSourcesControl.reset();

		PlanBlueprintEditorModel.reApplySectionValidators(
			{
				formGroup: this.formGroup,
				validationErrorModel: this.editorModel.validationErrorModel
			}
		);
		prefillingSourcesControl.markAsDirty();
	}

	dropDescriptionTemplates(event: CdkDragDrop<string[]>, sectionIndex: number) {
		const descriptionTemplatesFormArray = ((this.formGroup.get('definition').get('sections') as FormArray).at(sectionIndex).get('descriptionTemplates') as FormArray);

		moveItemInArray(descriptionTemplatesFormArray.controls, event.previousIndex, event.currentIndex);
		descriptionTemplatesFormArray.updateValueAndValidity();

		PlanBlueprintEditorModel.reApplySectionValidators({
			formGroup: this.formGroup,
			validationErrorModel: this.editorModel.validationErrorModel
		}
		);
		(this.formGroup.get('definition').get('sections') as FormArray).at(sectionIndex).get('descriptionTemplates').markAsDirty();
	}


	onPreviewDescriptionTemplate(event: DescriptionTemplate, sectionIndex: number, descriptionTemplateIndex: number) {
		const dialogRef = this.dialog.open(DescriptionTemplatePreviewDialogComponent, {
			width: 'min(800px, 95vw)',
			minHeight: '200px',
			restoreFocus: false,
			data: {
				descriptionTemplateId: event.id
			},
			panelClass: 'custom-modalbox'
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(descTemplate => {
			if (descTemplate) {
				((this.formGroup.get('definition').get('sections') as UntypedFormArray).at(sectionIndex).get('descriptionTemplates') as UntypedFormArray).at(descriptionTemplateIndex).get('descriptionTemplateGroupId').patchValue(descTemplate.groupId);
			}
		});
	}



	checkValidity() {
		this.formService.touchAllFormFields(this.formGroup);
		let errorMessages = [];
		if (!this.hasTitle()) {
			errorMessages.push(this.language.instant('PLAN-BLUEPRINT-EDITOR.FORM-VALIDATION-DISPLAY-DIALOG.TITLE-REQUIRED'));
		}
		if (!this.hasDescription()) {
			errorMessages.push(this.language.instant('PLAN-BLUEPRINT-EDITOR.FORM-VALIDATION-DISPLAY-DIALOG.DESCRIPTION-REQUIRED'));
		}
		if (!this.hasLanguage()) {
			errorMessages.push(this.language.instant('PLAN-BLUEPRINT-EDITOR.FORM-VALIDATION-DISPLAY-DIALOG.LANGUAGE-REQUIRED'));
		}
		if (!this.hasAccess()) {
			errorMessages.push(this.language.instant('PLAN-BLUEPRINT-EDITOR.FORM-VALIDATION-DISPLAY-DIALOG.ACCESS-REQUIRED'));
		}
		if (!this.hasDescriptionTemplates()) {
			errorMessages.push(this.language.instant('PLAN-BLUEPRINT-EDITOR.FORM-VALIDATION-DISPLAY-DIALOG.DESCRIPTION-TEMPLATES-REQUIRED'));
		}
		if (errorMessages.length > 0) {
			this.showValidationErrorsDialog(undefined, errorMessages);
			return false;
		}
		return true;
	}

	hasTitle(): boolean {
		const planBlueprint = this.formGroup.value as PlanBlueprintPersist;
		return planBlueprint.definition.sections.some(section => section.fields.some(field => (field.category == PlanBlueprintFieldCategory.System) && (field as SystemFieldInSection).systemFieldType === PlanBlueprintSystemFieldType.Title));
	}

	hasDescription(): boolean {
		const planBlueprint = this.formGroup.value as PlanBlueprintPersist;
		return planBlueprint.definition.sections.some(section => section.fields.some(field => (field.category == PlanBlueprintFieldCategory.System) && (field as SystemFieldInSection).systemFieldType === PlanBlueprintSystemFieldType.Description));
	}

	hasLanguage(): boolean {
		const planBlueprint = this.formGroup.value as PlanBlueprintPersist;
		return planBlueprint.definition.sections.some(section => section.fields.some(field => (field.category == PlanBlueprintFieldCategory.System) && (field as SystemFieldInSection).systemFieldType === PlanBlueprintSystemFieldType.Language));
	}

	hasAccess(): boolean {
		const planBlueprint = this.formGroup.value as PlanBlueprintPersist;
		return planBlueprint.definition.sections.some(section => section.fields.some(field => (field.category == PlanBlueprintFieldCategory.System) && (field as SystemFieldInSection).systemFieldType === PlanBlueprintSystemFieldType.AccessRights));
	}

	hasDescriptionTemplates(): boolean {
		const planBlueprint = this.formGroup.value as PlanBlueprintPersist;
		return planBlueprint.definition.sections.some(section => section.hasTemplates == true);
	}

	private showValidationErrorsDialog(projectOnly?: boolean, errmess?: string[]) {

		const dialogRef = this.dialog.open(FormValidationErrorsDialogComponent, {
			disableClose: true,
			restoreFocus: false,
			data: {
				errorMessages: errmess,
				projectOnly: projectOnly
			},
		});

	}

	public cancel(): void {
		this.router.navigate([this.routerUtils.generateUrl('/plan-blueprints')]);
	}

	finalize() {
		if (this.checkValidity() || !this.hasDescriptionTemplates()) {
			this.formGroup.get('status').setValue(PlanBlueprintStatus.Finalized);
			if (this.isNewVersion) { this.isNewVersion = false; }
			this.formSubmit();
		}
	}

	downloadXML(): void {
		const blueprintId = this.formGroup.get('id').value;
		if (blueprintId == null) return;
		this.planBlueprintService.downloadXML(blueprintId)
			.pipe(takeUntil(this._destroyed))
			.subscribe(response => {
				const blob = new Blob([response.body], { type: 'application/xml' });
				const filename = this.fileUtils.getFilenameFromContentDispositionHeader(response.headers.get('Content-Disposition'));

				FileSaver.saveAs(blob, filename);
			},
				error => this.httpErrorHandlingService.handleBackedRequestError(error));
	}

    finalPreviewFormGroup: FormGroup<PlanEditorForm>;
    finalPreviewBlueprint: PlanBlueprint;

    onMatStepperSelectionChange(event: StepperSelectionEvent) {
        if (event.selectedIndex === (this.stepper?.steps.length - 1)) {//preview selected
            const formData = this.formGroup.getRawValue();
			this.finalPreviewBlueprint = {
                ...formData,
                version: 0,
                groupId: null,
                definition: {
                    sections: formData.definition.sections.map((section) => { return {
                        ...section,
                        prefillingSources: [],
						descriptionTemplates: section.descriptionTemplates?.map((item) => { return {
                                descriptionTemplate: this.descriptionTemplateMap.get(item.descriptionTemplateGroupId),
                                maxMultiplicity: item.maxMultiplicity,
                                minMultiplicity: item.minMultiplicity
                            }
                        }) ?? [],
                        fields: section.fields.map((field) => { return {
                            id: field.id,
                            category: field.category,
                            label: field.label,
                            placeholder: field.placeholder,
                            description: field.description,
                            semantics: field.semantics,
                            required: field.required,
                            ordinal: field.ordinal,
                            systemFieldType: field.systemFieldType,
                            dataType: field.dataType,
                            referenceType: field.referenceTypeId ? this.referenceTypeMap.get(field.referenceTypeId) : null,
                            multipleSelect: field.multipleSelect,
                        }})
                    }})
                }
            };
            let finalPreviewPlan: Plan = {
                blueprint: this.finalPreviewBlueprint
            }
            this.previewEditorModel = new PlanEditorModel();
            this.finalPreviewFormGroup = this.previewEditorModel.fromModel(finalPreviewPlan).buildForm();
		}
    }

    protected referenceTypeMap = new Map<Guid, ReferenceType>([]);

    protected enableReordering($event: MouseEvent): void {
		this.reorderingMode.set(true);
        this.selectedField = null; //deselect field
        $event.stopPropagation();
	}
}
