import { NgModule } from '@angular/core';
import { FormattingModule } from '@app/core/formatting.module';
import { MultipleAutoCompleteComponent } from '@app/library/auto-complete/multiple/multiple-auto-complete.component';
import { SingleAutoCompleteComponent } from '@app/library/auto-complete/single/single-auto-complete.component';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';


@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		FormattingModule
	],
	declarations: [
		SingleAutoCompleteComponent,
		MultipleAutoCompleteComponent
	],
	exports: [
		SingleAutoCompleteComponent,
		MultipleAutoCompleteComponent
	]
})
export class AutoCompleteModule {
	constructor() { }
}
