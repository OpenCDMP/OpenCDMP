import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { RichTextEditorModule } from "@app/library/rich-text-editor/rich-text-editor.module";
import { CommonUiModule } from '@common/ui/common-ui.module';
import { PlanFinalizeDialogComponent } from './plan-finalize-dialog.component';

@NgModule({
	imports: [CommonUiModule, FormsModule, ReactiveFormsModule, AutoCompleteModule, RichTextEditorModule],
	declarations: [PlanFinalizeDialogComponent],
	exports: [PlanFinalizeDialogComponent]
})
export class PlanFinalizeDialogModule {
	constructor() { }
}
