import { Component, effect, input, output } from '@angular/core';
import { PlanBlueprint } from '@app/core/model/plan-blueprint/plan-blueprint';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { TranslateService } from '@ngx-translate/core';
import { MatDialog } from '@angular/material/dialog';
import { DragAndDropAccessibilityService } from '@app/core/services/accessibility/drag-and-drop-accessibility.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { Guid } from '@common/types/guid';
import { FormGroup } from '@angular/forms';
import { PlanEditorForm, PlanEditorModel } from '../plan-editor.model';
import { PlanBlueprintFieldCategory } from '@app/core/common/enum/plan-blueprint-field-category';
import { UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { DescriptionTemplateType } from '@app/core/model/description-template-type/description-template-type';
import { QueryResult } from '@common/model/query-result';
import { catchError, forkJoin, of, takeUntil } from 'rxjs';
import { DescriptionTemplateTypeService } from '@app/core/services/description-template-type/description-template-type.service';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { types } from 'util';

@Component({
  selector: 'app-plan-internal-blueprint-editor',
  providers: [
    DragAndDropAccessibilityService
  ],
  templateUrl: './plan-internal-blueprint-editor.component.html',
  styleUrl: './plan-internal-blueprint-editor.component.scss',
  standalone: false,
})
export class PlanInternalBlueprintEditorComponent extends BaseComponent{
    editorModel = input.required<PlanEditorModel>();
    blueprint = input.required<PlanBlueprint>();
    formGroup = input<FormGroup<PlanEditorForm>>();
    canEditPlan = input<boolean>(true);
    isNew = input<boolean>(false);
    isDeleted = input<boolean>(false);
    hideEditor = input<boolean>(false);
    step = input<number>(1);
    showAllSections = input<boolean>(false);
    annotationsPerAnchor =  input<Map<string, number>>();
    descTemplatesInUseMap = input<Map<Guid, Guid[]>>(new Map([]));

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

    reApplyPropertiesValidators(){
        PlanEditorModel.reApplyPropertiesValidators(
            {
                formGroup: this.formGroup(),
                validationErrorModel: this.editorModel().validationErrorModel,
                blueprint: this.blueprint()
            }
        );
    }

}
