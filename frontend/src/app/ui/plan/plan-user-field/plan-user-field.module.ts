import { NgModule } from '@angular/core';
import { FormattingModule } from '@app/core/formatting.module';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { PlanUserFieldComponent } from './plan-user-field.component';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		FormattingModule,
		DragDropModule,
		AutoCompleteModule
	],
	declarations: [
		PlanUserFieldComponent
	],
	exports: [
		PlanUserFieldComponent
	]
})
export class PlanUserFieldModule { }
