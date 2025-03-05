import { Component, input, output } from '@angular/core';
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

    showAnnotations = output<Guid>();
    descriptionTemplateLoaded = output<DescriptionTemplate>();

    planBlueprintSectionFieldCategoryEnum = PlanBlueprintFieldCategory;

    constructor(
        protected dialog: MatDialog,
        protected language: TranslateService,
        protected uiNotificationService: UiNotificationService,
        public routerUtils: RouterUtilsService,
        public enumUtils: EnumUtils,
    ){
        super();
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
