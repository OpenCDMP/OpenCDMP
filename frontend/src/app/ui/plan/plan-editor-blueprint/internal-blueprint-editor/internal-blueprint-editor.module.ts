import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PlanInternalBlueprintEditorComponent } from './plan-internal-blueprint-editor.component';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { TranslateModule } from '@ngx-translate/core';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { RichTextEditorModule } from '@app/library/rich-text-editor/rich-text-editor.module';
import { ReferenceFieldModule } from '@app/ui/reference/reference-field/reference-field.module';
import { CommonFormattingModule } from '@common/formatting/common-formatting.module';
import { PlanUserFieldModule } from '../../plan-user-field/plan-user-field.module';
import { PlanInternalBlueprintFieldEditorComponent } from './plan-internal-blueprint-field-editor/plan-internal-blueprint-field-editor.component';
import { PlanUploadFieldComponent } from '../plan-upload-field/plan-upload-field.component';
import { FieldEditorModule } from '@app/ui/description/editor/description-form/field-editor.module';
import { PlanSectionTemplatesEditorComponent } from './plan-section-templates-editor/plan-section-templates-editor.component';
import { MaterialModule } from '@common/material/material.module';
import { PlanNewDescriptionModule } from './plan-new-description-editor/plan-new-description.module';



@NgModule({
    imports: [
    CommonModule,
    CommonFormsModule,
    TranslateModule,
    CommonFormattingModule,
    MaterialModule,
    RichTextEditorModule,
    PlanUserFieldModule,
    DragDropModule,
    ReferenceFieldModule,
    PlanUploadFieldComponent,
    FieldEditorModule,
    PlanNewDescriptionModule,
    PlanInternalBlueprintEditorComponent,
    PlanInternalBlueprintFieldEditorComponent,
    PlanSectionTemplatesEditorComponent
],
    exports: [
        PlanInternalBlueprintEditorComponent,
        PlanInternalBlueprintFieldEditorComponent,
        PlanNewDescriptionModule
    ]
})
export class PlanInternalBlueprintEditorModule { }
