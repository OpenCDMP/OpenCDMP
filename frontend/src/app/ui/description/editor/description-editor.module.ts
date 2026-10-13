import { NgModule } from '@angular/core';
import { FormattingModule } from '@app/core/formatting.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { ConfirmationDialogModule } from '@common/modules/confirmation-dialog/confirmation-dialog.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { DescriptionBaseFieldsEditorComponent } from './description-base-fields-editor/description-base-fields-editor.component';
import { DescriptionFormModule } from './description-form/description-form.module';
import { VisibilityRulesService } from './description-form/visibility-rules/visibility-rules.service';
import { DescriptionFormProgressIndicationModule } from './form-progress-indication/form-progress-indication.module';
import { RichTextEditorModule } from '@app/library/rich-text-editor/rich-text-editor.module';
import { TagsFieldModule } from '@app/ui/tag/tags-field/tags-field.module';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { DeprecatedDescriptionTemplateDialog } from './description-base-fields-editor/dialog-description-template/deprecated-description-template-dialog.component';
import { FormFocusDirective } from '@common/forms/form-focus.directive';

@NgModule({
    imports: [
    CommonUiModule,
    CommonFormsModule,
    FormattingModule,
    ConfirmationDialogModule,
    DescriptionFormProgressIndicationModule,
    DescriptionFormModule,
    // DescriptionEditorRoutingModule,
    RichTextEditorModule,
    TagsFieldModule,
    AutoCompleteModule,
    FormFocusDirective,
    DescriptionBaseFieldsEditorComponent,
    DeprecatedDescriptionTemplateDialog,
],
    exports: [
        DescriptionBaseFieldsEditorComponent
    ],
    providers: [
        VisibilityRulesService,
    ]
})
export class DescriptionEditorModule { }
