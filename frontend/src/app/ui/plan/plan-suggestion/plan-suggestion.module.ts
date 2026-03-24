import {NgModule} from '@angular/core';
import {CommonUiModule} from '@common/ui/common-ui.module';
import {TextFilterModule} from "@common/modules/text-filter/text-filter.module";
import {ReactiveFormsModule} from "@angular/forms";
import { PlanSuggestionComponent } from './plan-suggestion.component';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { FormattingModule } from '@app/core/formatting.module';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { DescriptionTemplatePreviewDialogModule } from '@app/ui/admin/description-template/description-template-preview/description-template-preview-dialog.module';


@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		FormattingModule,
		AutoCompleteModule,
		DescriptionTemplatePreviewDialogModule,
		TextFilterModule,
		ReactiveFormsModule
	],
	declarations: [
		PlanSuggestionComponent
	],
	exports: [
		PlanSuggestionComponent
	]
})
export class PlanSuggestionModule {}
