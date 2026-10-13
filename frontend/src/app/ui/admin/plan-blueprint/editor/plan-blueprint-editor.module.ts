import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { PlanInternalBlueprintEditorModule } from '@app/ui/plan/plan-editor-blueprint/internal-blueprint-editor/internal-blueprint-editor.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { DescriptionTemplatePreviewDialogModule } from '../../description-template/description-template-preview/description-template-preview-dialog.module';
import { StructureEditorModule } from '../../structure-editor/structure-editor.module';
import { TemplatePickerDialogComponent } from './dialogs/template-picker/template-picker.dialog';
import { PlanBlueprintEditorComponent } from './plan-blueprint-editor.component';
import { Bp2DetailsTabComponent } from './tabs/details-tab/details-tab.component';
import { Bp2ReviewTabComponent } from './tabs/review-tab/review-tab.component';
import { Bp2AssetsBlockComponent } from './tabs/structure-tab/assets-block/assets-block.component';

/**
 * The plan blueprint editor. The structure, frame and dialogs come from `StructureEditorModule`; what is left here
 * is what only a blueprint has.
 */
@NgModule({
    imports: [
        CommonUiModule,
        RouterModule,
        StructureEditorModule,
        DescriptionTemplatePreviewDialogModule,
        PlanInternalBlueprintEditorModule,
        PlanBlueprintEditorComponent,
        Bp2DetailsTabComponent,
        Bp2AssetsBlockComponent,
        Bp2ReviewTabComponent,
        TemplatePickerDialogComponent
    ],
    exports: [PlanBlueprintEditorComponent]
})
export class PlanBlueprintEditorModule { }
