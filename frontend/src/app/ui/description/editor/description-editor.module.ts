import { NgModule } from '@angular/core';
import { FormattingModule } from '@app/core/formatting.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { ConfirmationDialogModule } from '@common/modules/confirmation-dialog/confirmation-dialog.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { DescriptionBaseFieldsEditorComponent } from './description-base-fields-editor/description-base-fields-editor.component';
import { DescriptionEditorComponent } from './description-editor.component';
import { DescriptionEditorRoutingModule } from './description-editor.routing';
import { DescriptionFormModule } from './description-form/description-form.module';
import { VisibilityRulesService } from './description-form/visibility-rules/visibility-rules.service';
import { DescriptionFormProgressIndicationModule } from './form-progress-indication/form-progress-indication.module';
import { TableOfContentsModule } from './table-of-contents/table-of-contents.module';
import { RichTextEditorModule } from '@app/library/rich-text-editor/rich-text-editor.module';
import { TagsFieldModule } from '@app/ui/tag/tags-field/tags-field.module';
import { NewDescriptionDialogComponent } from './new-description/new-description.component';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { DeprecatedDescriptionTemplateDialog } from './description-base-fields-editor/dialog-description-template/deprecated-description-template-dialog.component';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		FormattingModule,
		ConfirmationDialogModule,
		TableOfContentsModule,
		DescriptionFormProgressIndicationModule,
		DescriptionFormModule,
		DescriptionEditorRoutingModule,
		RichTextEditorModule,
		TagsFieldModule,
		AutoCompleteModule
	],
	declarations: [
		DescriptionEditorComponent,
		DescriptionBaseFieldsEditorComponent,
		NewDescriptionDialogComponent,
		DeprecatedDescriptionTemplateDialog
	],
	exports: [
	],
	providers: [
		VisibilityRulesService,
	]
})
export class DescriptionEditorModule { }
