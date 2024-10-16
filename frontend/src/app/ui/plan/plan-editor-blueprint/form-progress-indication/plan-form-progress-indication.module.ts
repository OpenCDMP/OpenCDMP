import { NgModule } from '@angular/core';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { PlanFormProgressIndicationComponent } from './plan-form-progress-indication.component';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule
	],
	declarations: [
		PlanFormProgressIndicationComponent
	],
	exports: [
		PlanFormProgressIndicationComponent
	]
})
export class PlanFormProgressIndicationModule { }
