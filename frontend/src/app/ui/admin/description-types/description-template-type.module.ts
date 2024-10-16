import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { HybridListingModule } from '@common/modules/hybrid-listing/hybrid-listing.module';
import { TextFilterModule } from '@common/modules/text-filter/text-filter.module';
import { UserSettingsModule } from '@common/modules/user-settings/user-settings.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { DescriptionTemplateTypesRoutingModule } from './description-template-type.routing';
import { DescriptionTemplateTypeEditorComponent } from './editor/description-template-type-editor.component';
import { DescriptionTemplateTypeListingComponent } from './listing/description-template-type-listing.component';
import { DescriptionTemplateTypeListingFiltersComponent } from './listing/filters/description-template-type-listing-filters.component';
import { FormattingModule } from '@app/core/formatting.module';
import { CommonFormattingModule } from '@common/formatting/common-formatting.module';

@NgModule({
	declarations: [
		DescriptionTemplateTypeListingComponent,
		DescriptionTemplateTypeEditorComponent,
		DescriptionTemplateTypeListingFiltersComponent
	],
	imports: [
		CommonModule,
		CommonUiModule,
		CommonFormsModule,
		CommonFormattingModule,
		DescriptionTemplateTypesRoutingModule,
		HybridListingModule,
		TextFilterModule,
		UserSettingsModule
	]
})
export class DescriptionTemplateTypesModule { }
