import { NgModule } from '@angular/core';
import { FormattingModule } from '@app/core/formatting.module';
import { DescriptionRoutingModule } from '@app/ui/description/description.routing';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { TagsComponent } from './tags-field.component';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		FormattingModule
	],
	declarations: [
		TagsComponent
	],
	exports: [
		TagsComponent
	]
})
export class TagsFieldModule { }
