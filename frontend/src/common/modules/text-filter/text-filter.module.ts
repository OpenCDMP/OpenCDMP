import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { ExpandableSearchFieldComponent } from '@common/modules/text-filter/expandable-search-field/expandable-search-field.component';
import { TextFilterComponent } from '@common/modules/text-filter/text-filter.component';
import { CommonUiModule } from '@common/ui/common-ui.module';

@NgModule({
	imports: [
		CommonUiModule,
		FormsModule,
        CommonFormsModule
	],
	declarations: [
		TextFilterComponent,
		ExpandableSearchFieldComponent
	],
	exports: [
		TextFilterComponent,
		ExpandableSearchFieldComponent
	]
})
export class TextFilterModule {
	constructor() { }
}
