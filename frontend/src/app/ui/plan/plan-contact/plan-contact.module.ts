import { NgModule } from '@angular/core';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { PlanContactPrefillDialogComponent } from './plan-contact-prefill-dialog/plan-contact-prefill-dialog.component';
import { PlanContactEditorComponent } from './plan-contact-editor.component';
import { DragDropModule } from '@angular/cdk/drag-drop';



@NgModule({
    imports: [
        CommonUiModule, FormsModule, ReactiveFormsModule, AutoCompleteModule, DragDropModule,
        PlanContactEditorComponent, PlanContactPrefillDialogComponent
    ],
    exports: [
        PlanContactEditorComponent, PlanContactPrefillDialogComponent
    ]
})
export class PlanContactModule { }
