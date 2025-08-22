import { NgModule } from '@angular/core';
import { FormattingModule } from '@app/core/formatting.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { TagsComponent } from './tags-field.component';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		FormattingModule,
        AutoCompleteModule
	],
	declarations: [
		TagsComponent
	],
	exports: [
		TagsComponent
	]
})
export class TagsFieldModule { }
