import { SpinnerComponent } from './spinner.component';
import { NgModule } from '@angular/core';
import { CommonUiModule } from '@common/ui/common-ui.module';

@NgModule({
	imports: [
		CommonUiModule,
	],
	declarations: [
		SpinnerComponent
	],
	exports: [
		SpinnerComponent,
	]
})

export class SpinnerModule { }
