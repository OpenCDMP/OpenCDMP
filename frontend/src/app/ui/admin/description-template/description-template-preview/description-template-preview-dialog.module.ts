import { NgModule } from '@angular/core';
import { FormattingModule } from '@app/core/formatting.module';
import { DescriptionFormModule } from '@app/ui/description/editor/description-form/description-form.module';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { DescriptionTemplatePreviewDialogComponent } from './description-template-preview-dialog.component';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		FormattingModule,
		DescriptionFormModule
	],
	declarations: [
		DescriptionTemplatePreviewDialogComponent,
	],
	exports: [
		DescriptionTemplatePreviewDialogComponent,
	],
	providers: [
		VisibilityRulesService
	]
})
export class DescriptionTemplatePreviewDialogModule { }
