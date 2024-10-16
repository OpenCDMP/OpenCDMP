import { NgModule } from '@angular/core';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { FormProgressIndicationComponent } from './form-progress-indication.component';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule
	],
	declarations: [
		FormProgressIndicationComponent
	],
	exports: [
		FormProgressIndicationComponent
	]
})
export class DescriptionFormProgressIndicationModule { }
