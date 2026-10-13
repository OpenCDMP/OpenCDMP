import { DragDropModule } from '@angular/cdk/drag-drop';
import { NgModule } from '@angular/core';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { RichTextEditorModule } from '@app/library/rich-text-editor/rich-text-editor.module';
import { ReferenceFieldModule } from '@app/ui/reference/reference-field/reference-field.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { ConfirmationDialogModule } from '@common/modules/confirmation-dialog/confirmation-dialog.module';
import { FormValidationErrorsDialogModule } from '@common/forms/form-validation-errors-dialog/form-validation-errors-dialog.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { PlanUserFieldModule } from '../plan-user-field/plan-user-field.module';
import { PlanEditorComponent } from './plan-editor.component';
import { PlanEditorRoutingModule } from './plan-editor.routing';
import { PlanFormProgressIndicationModule } from './form-progress-indication/plan-form-progress-indication.module';
import { PlanDeleteDialogModule } from '../plan-delete-dialog/plan-delete-dialog.module';
import { PlanFinalizeDialogModule } from '../plan-finalize-dialog/plan-finalize-dialog.module';
import { AnnotationDialogModule } from '@app/ui/annotations/annotation-dialog-component/annotation-dialog.module';
import { FormFocusDirective } from '@common/forms/form-focus.directive';
import { PlanDescriptionEditorComponent } from './plan-description-editor/plan-description-editor.component';
import { TableOfContentsModule } from '@app/ui/description/editor/table-of-contents/table-of-contents.module';
import { DescriptionFormModule } from '@app/ui/description/editor/description-form/description-form.module';
import { DescriptionEditorModule } from '@app/ui/description/editor/description-editor.module';
import { DescriptionFormProgressIndicationModule } from '@app/ui/description/editor/form-progress-indication/form-progress-indication.module';
import { InitialLetterPipe } from '@app/core/pipes/initial-letter.pipe';
import { PlanTableOfContentsComponent } from './plan-table-of-contents/plan-table-of-contents.component';
import { PlanUploadFieldComponent } from './plan-upload-field/plan-upload-field.component';
import { FinalizeDescriptionDialogComponent } from './plan-description-editor/finalize-description-dialog/finalize-description-dialog.component';
import { DragAndDropAccessibilityService } from '@app/core/services/accessibility/drag-and-drop-accessibility.service';
import { FileUploadComponent } from '@app/library/file-uploader/file-uploader.component';
import {PlanBlueprintsPreviewModule} from "@app/ui/plan/plan-editor-blueprint/plan-blueprints-preview/plan-blueprint-preview.module";
import { PlanInternalBlueprintEditorModule } from './internal-blueprint-editor/internal-blueprint-editor.module';
import { PlanSuggestionModule } from '../plan-suggestion/plan-suggestion.module';
import { PlanDepositDropdownModule } from '../editor/plan-deposit-dropdown/plan-deposit-dropdown.module';
import { PlanActiveUsersComponent } from '../plan-active-users/plan-active-users.component';
import { PlanCreationConsentDialogModule } from './plan-creation-consent-dialog/plan-creation-consent-dialog.module';
import { FormattingModule } from '@app/core/formatting.module';
import { PlanAuthorsModule } from '../plan-authors/plan-authors.module';
import { PlanUploadDialogModule } from '../new/upload-dialogue/plan-upload-dialog.module';
import { PlanAnnotationsSideDialogComponent } from './plan-annotations-side-dialog/plan-annotations-side-dialog.component';
import { InvokeMemoizedPipe } from '@common/formatting/pipes/invoke-memoized.pipe';
import { FieldEditorModule } from '@app/ui/description/editor/description-form/field-editor.module';
// FE: ported plan actions (evaluate / clone / new version) dialog modules
import { ClonePlanDialogModule } from '../clone-dialog/plan-clone-dialog.module';
import { NewVersionPlanDialogModule } from '../new-version-dialog/plan-new-version-dialog.module';
import { EvaluateDialogModule } from '@app/ui/evaluation/evaluate-dialog/evaluate-dialog.module';
import { BenchmarkDialogModule } from '@app/ui/evaluation/benchmark-dialog/benchmark-dialog.module';

@NgModule({
    imports: [
    CommonUiModule,
    CommonFormsModule,
    FormattingModule,
    ConfirmationDialogModule,
    FormValidationErrorsDialogModule,
    PlanDeleteDialogModule,
    PlanFinalizeDialogModule,
    PlanEditorRoutingModule,
    RichTextEditorModule,
    AutoCompleteModule,
    ReferenceFieldModule,
    DragDropModule,
    PlanUserFieldModule,
    PlanFormProgressIndicationModule,
    AnnotationDialogModule,
    FormFocusDirective,
    PlanBlueprintsPreviewModule,
    PlanSuggestionModule,
    PlanDepositDropdownModule,
    DescriptionEditorModule,
    DescriptionFormModule,
    DescriptionFormProgressIndicationModule,
    TableOfContentsModule,
    InitialLetterPipe,
    FileUploadComponent,
    PlanInternalBlueprintEditorModule,
    PlanUploadFieldComponent,
    PlanActiveUsersComponent,
    PlanCreationConsentDialogModule,
    PlanAuthorsModule,
    PlanUploadDialogModule,
    InvokeMemoizedPipe,
    FieldEditorModule,
    ClonePlanDialogModule,
    NewVersionPlanDialogModule,
    EvaluateDialogModule,
    BenchmarkDialogModule,
    PlanEditorComponent,
    PlanDescriptionEditorComponent,
    PlanTableOfContentsComponent,
    FinalizeDescriptionDialogComponent,
    PlanTableOfContentsComponent,
    PlanAnnotationsSideDialogComponent
],
    exports: [],
    providers: [
        InitialLetterPipe,
        DragAndDropAccessibilityService,
    ]
})
export class PlanEditorModule { }
