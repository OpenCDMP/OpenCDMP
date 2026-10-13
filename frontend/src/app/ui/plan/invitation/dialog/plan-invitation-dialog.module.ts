import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { RichTextEditorModule } from "@app/library/rich-text-editor/rich-text-editor.module";
import { CommonUiModule } from '@common/ui/common-ui.module';
import { PlanInvitationDialogComponent } from './plan-invitation-dialog.component';
import { PlanUserFieldModule } from '../../plan-user-field/plan-user-field.module';
/** @deprecated  use planAuthorsModule */
@NgModule({
    imports: [CommonUiModule, FormsModule, ReactiveFormsModule, AutoCompleteModule, RichTextEditorModule, PlanUserFieldModule, PlanInvitationDialogComponent],
    exports: [PlanInvitationDialogComponent]
})
export class PlanInvitationDialogModule {
	constructor() { }
}
