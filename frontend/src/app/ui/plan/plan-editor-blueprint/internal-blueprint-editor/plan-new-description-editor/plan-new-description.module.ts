import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { CommonFormattingModule } from "@common/formatting/common-formatting.module";
import { CommonFormsModule } from "@common/forms/common-forms.module";
import { MaterialModule } from "@common/material/material.module";
import { TextFilterModule } from "@common/modules/text-filter/text-filter.module";
import { TranslateModule } from "@ngx-translate/core";
import { PlanNewDescriptionEditorComponent } from "./plan-new-description-editor.component";
import { PlanSectionAddTemplatesDialogComponent } from "./plan-section-add-templates-dialog/plan-section-add-templates-dialog.component";
import { PlanSectionManageTemplatesDialogComponent } from "./plan-section-manage-templates-dialog/plan-section-manage-templates-dialog.component";

// The "Add a new asset" wizard itself is standalone (see PlanNewDescriptionEditorComponent);
// this module only still declares the template-curation dialogs and re-exports the wizard.
@NgModule({
    imports: [
        CommonModule,
        CommonFormsModule,
        TranslateModule,
        CommonFormattingModule,
        MaterialModule,
        TextFilterModule,
        PlanNewDescriptionEditorComponent,
        PlanSectionAddTemplatesDialogComponent,
        PlanSectionManageTemplatesDialogComponent
    ],
    exports: [
        PlanNewDescriptionEditorComponent,
        PlanSectionManageTemplatesDialogComponent
    ]
})
export class PlanNewDescriptionModule { }
