import { DragDropModule } from '@angular/cdk/drag-drop';
import { NgModule } from '@angular/core';
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
import { FormFocusDirective } from '@common/forms/form-focus.directive';
import { PlanDescriptionEditorComponent } from './plan-description-editor/plan-description-editor.component';
import { TableOfContentsModule } from '@app/ui/description/editor/table-of-contents/table-of-contents.module';
import { DescriptionFormModule } from '@app/ui/description/editor/description-form/description-form.module';
import { DescriptionEditorModule } from '@app/ui/description/editor/description-editor.module';
import { DescriptionFormProgressIndicationModule } from '@app/ui/description/editor/form-progress-indication/form-progress-indication.module';
import { InitialLetterPipe } from '@app/core/pipes/initial-letter.pipe';
import { PlanTableOfContentsComponent } from './plan-table-of-contents/plan-table-of-contents.component';
import { PlanTempStorageService } from './plan-temp-storage.service';
import { PlanUploadFieldComponent } from './plan-upload-field/plan-upload-field.component';
import { FinalizeDescriptionDialogComponent } from './plan-description-editor/finalize-description-dialog/finalize-description-dialog.component';
import { DragAndDropAccessibilityService } from '@app/core/services/accessibility/drag-and-drop-accessibility.service';
import { FileUploadComponent } from '@app/library/file-uploader/file-uploader.component';
import {PlanBlueprintsPreviewModule} from "@app/ui/plan/plan-editor-blueprint/plan-blueprints-preview/plan-blueprint-preview.module";
import { DescriptionTemplateTableSelectModule } from './descriptions-template-table-select/description-template-table-select.module';
import { PlanInternalBlueprintEditorModule } from './internal-blueprint-editor/internal-blueprint-editor.module';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
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
		AnnotationDialogModule,
        FormFocusDirective,
		PlanBlueprintsPreviewModule,
        DescriptionEditorModule,
        DescriptionFormModule,
        DescriptionFormProgressIndicationModule,
        TableOfContentsModule,
        InitialLetterPipe,
		FileUploadComponent,
		DescriptionTemplateTableSelectModule,
        PlanInternalBlueprintEditorModule,
        PlanUploadFieldComponent
	],
	declarations: [
		PlanEditorComponent,
        PlanDescriptionEditorComponent,
        PlanTableOfContentsComponent,
        FinalizeDescriptionDialogComponent,
        PlanTableOfContentsComponent
	],
	exports: [
	],
	providers: [
        InitialLetterPipe,
        PlanTempStorageService,
        DragAndDropAccessibilityService
	]
})
export class PlanEditorModule { }
