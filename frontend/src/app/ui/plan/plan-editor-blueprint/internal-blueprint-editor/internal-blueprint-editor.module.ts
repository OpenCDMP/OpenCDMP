import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PlanInternalBlueprintEditorComponent } from './plan-internal-blueprint-editor.component';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { TranslateModule } from '@ngx-translate/core';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RichTextEditorModule } from '@app/library/rich-text-editor/rich-text-editor.module';
import { ReferenceFieldModule } from '@app/ui/reference/reference-field/reference-field.module';
import { CommonFormattingModule } from '@common/formatting/common-formatting.module';
import { PlanUserFieldModule } from '../../plan-user-field/plan-user-field.module';
import { DescriptionTemplateTableSelectModule } from '../descriptions-template-table-select/description-template-table-select.module';
import { PlanInternalBlueprintFieldEditorComponent } from './plan-internal-blueprint-field-editor/plan-internal-blueprint-field-editor.component';
import { MatBadgeModule } from '@angular/material/badge';



@NgModule({
  declarations: [
    PlanInternalBlueprintEditorComponent,
    PlanInternalBlueprintFieldEditorComponent
  ],
  imports: [
    CommonModule,
    CommonFormsModule,
    TranslateModule, 
    MatInputModule,
    MatSelectModule, 
    MatDatepickerModule,
    MatTooltipModule,
    MatFormFieldModule, 
    CommonFormattingModule,
    MatButtonModule,
    MatIconModule,
    RichTextEditorModule,
    PlanUserFieldModule,
    DragDropModule,
    DescriptionTemplateTableSelectModule,
    ReferenceFieldModule,
    MatBadgeModule
  ],
  exports: [
    PlanInternalBlueprintEditorComponent,
    PlanInternalBlueprintFieldEditorComponent
  ]
})
export class PlanInternalBlueprintEditorModule { }
