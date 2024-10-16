import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { ClonePlanDialogComponent } from './plan-clone-dialog.component';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import {RichTextEditorModule} from "@app/library/rich-text-editor/rich-text-editor.module";

@NgModule({
    imports: [CommonUiModule, FormsModule, ReactiveFormsModule, AutoCompleteModule, RichTextEditorModule],
    declarations: [ClonePlanDialogComponent],
    exports: [ClonePlanDialogComponent]
})
export class ClonePlanDialogModule {
	constructor() { }
}
