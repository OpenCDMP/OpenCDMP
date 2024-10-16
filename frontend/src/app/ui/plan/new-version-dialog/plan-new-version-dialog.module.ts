import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { RichTextEditorModule } from "@app/library/rich-text-editor/rich-text-editor.module";
import { CommonUiModule } from '@common/ui/common-ui.module';
import { NewVersionPlanDialogComponent } from './plan-new-version-dialog.component';

@NgModule({
	imports: [CommonUiModule, FormsModule, ReactiveFormsModule, AutoCompleteModule, RichTextEditorModule],
	declarations: [NewVersionPlanDialogComponent],
	exports: [NewVersionPlanDialogComponent]
})
export class NewVersionPlanDialogModule {
	constructor() { }
}
