import { DragDropModule } from '@angular/cdk/drag-drop';
import { NgModule } from '@angular/core';
import { FormattingModule } from '@app/core/formatting.module';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { RichTextEditorModule } from '@app/library/rich-text-editor/rich-text-editor.module';
import { ReferenceFieldModule } from '@app/ui/reference/reference-field/reference-field.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { ConfirmationDialogModule } from '@common/modules/confirmation-dialog/confirmation-dialog.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { PlanUserFieldModule } from '../plan-user-field/plan-user-field.module';
import { PlanEditorComponent } from './plan-editor.component';
import { PlanEditorRoutingModule } from './plan-editor.routing';
import { PlanFormProgressIndicationModule } from './form-progress-indication/plan-form-progress-indication.module';
import { PlanDeleteDialogModule } from '../plan-delete-dialog/plan-delete-dialog.module';
import { PlanContactPrefillDialogModule } from '../plan-contact-prefill-dialog/plan-contact-prefill-dialog.module';
import { AnnotationDialogModule } from '@app/ui/annotations/annotation-dialog-component/annotation-dialog.module';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		FormattingModule,
		ConfirmationDialogModule,
		PlanDeleteDialogModule,
		PlanEditorRoutingModule,
		RichTextEditorModule,
		AutoCompleteModule,
		ReferenceFieldModule,
		DragDropModule,
		PlanUserFieldModule,
		PlanFormProgressIndicationModule,
		PlanContactPrefillDialogModule,
		AnnotationDialogModule
	],
	declarations: [
		PlanEditorComponent,
	],
	exports: [
	],
	providers: [
	]
})
export class PlanEditorModule { }
