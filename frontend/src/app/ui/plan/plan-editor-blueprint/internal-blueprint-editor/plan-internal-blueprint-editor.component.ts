import { Component, computed, effect, input, Optional, output, Signal, ChangeDetectionStrategy } from '@angular/core';
import { PlanBlueprint } from '@app/core/model/plan-blueprint/plan-blueprint';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { TranslateService } from '@ngx-translate/core';
import { MatDialog } from '@angular/material/dialog';
import { DragAndDropAccessibilityService } from '@app/core/services/accessibility/drag-and-drop-accessibility.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { Guid } from '@common/types/guid';
import { FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { PlanEditorForm, PlanEditorModel } from '../plan-editor.model';
import { PlanBlueprintFieldCategory } from '@app/core/common/enum/plan-blueprint-field-category';
import { UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { BaseField, BaseSection, DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { DescriptionTemplateType } from '@app/core/model/description-template-type/description-template-type';
import { QueryResult } from '@common/model/query-result';
import { catchError, forkJoin, of, takeUntil } from 'rxjs';
import { DescriptionTemplateTypeService } from '@app/core/services/description-template-type/description-template-type.service';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { SelectedEntry } from '../plan-table-of-contents/plan-table-of-contents.component';
import { DescriptionInfo, PlanTempStorageService } from '../plan-temp-storage.service';
import { AuthService } from '@app/core/services/auth/auth.service';
import { Plan } from '@app/core/model/plan/plan';
import { PlanLockService } from '../plan-lock.service';
import { RootSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { PlanSectionManageTemplatesDialogComponent } from './plan-new-description-editor/plan-section-manage-templates-dialog/plan-section-manage-templates-dialog.component';
import { FormSectionComponent } from '../../../description/editor/description-form/components/form-section/form-section.component';
import { PlanSectionTemplatesEditorComponent } from './plan-section-templates-editor/plan-section-templates-editor.component';

@Component({
    selector: 'app-plan-internal-blueprint-editor',
    providers: [
        DragAndDropAccessibilityService
    ],
    templateUrl: './plan-internal-blueprint-editor.component.html',
    styleUrl: './plan-internal-blueprint-editor.component.scss',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [
        FormSectionComponent,
        FormsModule,
        ReactiveFormsModule,
        PlanSectionTemplatesEditorComponent,
    ],
})
export class PlanInternalBlueprintEditorComponent extends BaseComponent{
    plan = input<Plan>();
    visibilityRulesService = input<VisibilityRulesService>();
    editorModel = input.required<PlanEditorModel>();
    blueprint = input.required<PlanBlueprint>();
    formGroup = input<FormGroup<PlanEditorForm>>();

    //permissions 
    isNew = input<boolean>(false);
    canEditEntirePlan = input<boolean>(false);
    canEditEntirePlanDescriptions = input<boolean>(false);
    planIsEditable = input<boolean>(false);
    permissionPerSection = input<{[key: string]: AppPermission[]}>({});
    //
    hideEditor = input<boolean>(false);
    showAllSections = input<boolean>(false);
    annotationsPerAnchor =  input<Map<string, number>>();
    descTemplatesInUseMap = input<Map<Guid, Guid[]>>(new Map([]));
    selected = input<SelectedEntry>();
    onAddDescription = output<string>();
    onNavigateToDescription = output<Guid>();
    onRemoveDescription = output<DescriptionInfo>();
    onPersistTemplates = output<void>();

    validationErrorModel = computed(() => this.editorModel()?.validationErrorModel )

    descriptionTemplateSelectData:{
        types: QueryResult<DescriptionTemplateType>,
        descTemplates: QueryResult<DescriptionTemplate>
    } = {
        types: {count: 0, items: []},
        descTemplates: {count: 0, items: []},
    }

    showAnnotations = output<Guid>();

    planBlueprintSectionFieldCategoryEnum = PlanBlueprintFieldCategory;

    constructor(
        protected dialog: MatDialog,
        protected language: TranslateService,
        protected uiNotificationService: UiNotificationService,
        public routerUtils: RouterUtilsService,
        public enumUtils: EnumUtils,
        private descriptionTemplateService: DescriptionTemplateService,
        private descriptionTemplateTypeService: DescriptionTemplateTypeService,
        private authService: AuthService,
        @Optional() private planLockService: PlanLockService,
        @Optional() private planTempStorage: PlanTempStorageService
    ){
        super();
        effect(() => {
            const blueprint = this.blueprint();
            if(!blueprint){ return; }
            if(blueprint.definition?.sections?.some((x) => x.hasTemplates)){
                forkJoin([
                    this.descriptionTemplateTypeService.query(this.descriptionTemplateTypeService.buildLookup())
                    .pipe(takeUntil(this._destroyed), catchError(() => of(null))),
                    this.descriptionTemplateService.query(this.descriptionTemplateService.buildLookup({page: {size: 5, offset: 0}}))
                    .pipe(takeUntil(this._destroyed), catchError(() => of(null)))
                ]).subscribe(([types, descTemplates]) => {
                    this.descriptionTemplateSelectData = {
                        types,
                        descTemplates
                    }
                })
            }
        })
    }

    getSectionFields(section: BaseSection): BaseField[] {
        return (section?.fieldSets ?? []).flatMap(fs => fs.fields ?? []);
    }

    protected descriptionsInSection: Signal<Map<string, DescriptionInfo[]>> = computed(() => {
        const descriptionsMap = new Map<string, DescriptionInfo[]>([]);
        this.planTempStorage?.descriptionIdsBySectionMap()?.forEach((value, key) => descriptionsMap.set(key, value?.map((id) => this.planTempStorage.descriptions().get(id.toString()))));
        return descriptionsMap;
    }); 

    protected canAnnotateSection(id: string): boolean {
		return this.planIsEditable() && (
            this.authService.hasPermission(this.authService.permissionEnum.AnnotatePlan) || 
            this.plan()?.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.AnnotatePlan) ||
			this.permissionPerSection()?.[id]?.some(x => x === AppPermission.AnnotatePlan)
		);
	}

    isLocked = computed(() => this.plan()?.id && this.planLockService?.externalLockIds()?.includes(this.plan().id.toString()));
    descriptionLocked = (id: Guid) => id && this.planLockService?.externalLockIds()?.includes(id.toString());

    protected canEditSection(id: string): boolean {
		return this.planIsEditable() && !this.isLocked() && (
            this.canEditEntirePlan() || 
			this.permissionPerSection()?.[id]?.some(x => x === AppPermission.EditPlan)
		);
	}

	protected canEditSectionDescriptions(id: string): boolean {
		return this.planIsEditable() && (
            this.canEditEntirePlan() || 
            this.authService.hasPermission(this.authService.permissionEnum.EditDescription) || 
            this.plan()?.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.EditDescription) ||
			this.permissionPerSection()?.[id]?.some(x => x === AppPermission.EditDescription || x === AppPermission.EditPlan)
		);
	}

	protected canDeleteSectionDescriptions(id: string): boolean {
		return this.planIsEditable() && (
            this.canEditEntirePlan() ||
            this.authService.hasPermission(this.authService.permissionEnum.DeleteDescription) ||
            this.plan()?.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.DeleteDescription) ||
			this.permissionPerSection()?.[id]?.some(x => x === AppPermission.DeleteDescription)
		);
	}

    // Mirrors app-plan-table-of-contents' own add-description guard so the "create new description"
    // action behaves the same whether triggered from the ToC or from this section's templates editor.
    protected descTemplatesTouched(section: RootSection): boolean {
        return !!this.formGroup()?.controls?.descriptionTemplates?.get(section.id?.toString())?.dirty;
    }

    protected hasValidMultiplicity(section: RootSection): boolean {
        if (!section.hasTemplates) {
            return false;
        }
        if (!section.descriptionTemplates?.length) {
            return true;
        }
        const descriptions = this.descriptionsInSection().get(section.id) ?? [];
        const distinctTemplates = new Set(descriptions.map(x => x.formGroup?.value?.planDescriptionTemplateId));

        if (this.plan()?.planDescriptionTemplates?.filter(x => x.sectionId == section.id).length > distinctTemplates.size) {
            return true;
        }

        const descriptionPlanDescriptionTemplates = descriptions.map((x) => this.planTempStorage.getPlanDescriptionTemplate(x.formGroup?.value?.planDescriptionTemplateId)).filter((x) => x);
        const descriptionTemplateGroupIds = descriptionPlanDescriptionTemplates.map((x) => x?.descriptionTemplateGroupId);
        return section.descriptionTemplates.some(sectionDescriptionTemplate => {
            if (sectionDescriptionTemplate.maxMultiplicity != null) {
                const groupId = sectionDescriptionTemplate.descriptionTemplate?.groupId;
                const count = descriptionTemplateGroupIds.filter(x => x === groupId).length;
                return count < sectionDescriptionTemplate.maxMultiplicity;
            } else {
                return true;
            }
        })
    }

    protected canAddDescription(section: RootSection): boolean {
        return this.hasValidMultiplicity(section) && !this.descTemplatesTouched(section);
    }

    reApplyPropertiesValidators(){
        // PlanEditorModel.reApplyPropertiesValidators(
        //     {
        //         formGroup: this.formGroup(),
        //         validationErrorModel: this.editorModel().validationErrorModel,
        //         blueprint: this.blueprint()
        //     }
        // );
    }
    public isOrContainsSelected(entryId: string): boolean {
        if(!this.selected()){ return false; }
        return entryId === this.selected().entry?.id || this.selected().entry?.pathToEntry?.includes(entryId);
    }

    // Owner-only template curation is offered when the blueprint permits it and the user can edit the section.
    protected canManageTemplates(section: RootSection): boolean {
        return !!section?.canEditDescriptionTemplates && this.canEditSection(section.id?.toString());
    }

    protected openManageTemplates(section: RootSection){
        const control = this.formGroup()?.get('descriptionTemplates')?.get(section.id?.toString());
        const dialogRef = this.dialog.open(PlanSectionManageTemplatesDialogComponent, {
            maxWidth: '90vw',
            data: {
                section,
                linkedGroupIds: (control?.value as Guid[]) ?? [],
                // descTemplatesInUseMap is keyed by the section id string at runtime (typed as Guid on the input).
                templatesInUseGroupIds: this.descTemplatesInUseMap()?.get(section.id as unknown as Guid) ?? []
            }
        });
        dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe((result?: Guid[]) => {
            if(result){
                control?.patchValue(result);
                control?.markAsDirty();
                this.onPersistTemplates.emit();
            }
        });
    }

}
