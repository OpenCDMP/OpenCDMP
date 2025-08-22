import { NgModule } from '@angular/core';
import { FormattingModule } from '@app/core/formatting.module';
import { DescriptionRoutingModule, PublicDescriptionRoutingModule } from '@app/ui/description/description.routing';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { EvaluateDialogModule } from '../evaluation/evaluate-dialog/evaluate-dialog.module';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		FormattingModule,
		DescriptionRoutingModule,
	],
	declarations: [
	],
	exports: [
	]
})
export class DescriptionModule { }

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		FormattingModule,
		PublicDescriptionRoutingModule,
		EvaluateDialogModule,
	],
	declarations: [
	],
	exports: [
	]
})
export class PublicDescriptionModule { }
