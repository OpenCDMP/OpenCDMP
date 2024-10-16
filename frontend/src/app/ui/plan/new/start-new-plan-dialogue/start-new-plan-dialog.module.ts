import { NgModule } from '@angular/core';
import { FormattingModule } from '@app/core/formatting.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { PlanUploadDialogModule } from '../upload-dialogue/plan-upload-dialog.module';
import { StartNewPlanDialogComponent } from './start-new-plan-dialog.component';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		FormattingModule,
		PlanUploadDialogModule
	],
	declarations: [
		StartNewPlanDialogComponent,
	],
	exports: [
		StartNewPlanDialogComponent,
	]
})
export class StartNewPlanDialogModule { }
