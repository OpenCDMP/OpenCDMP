import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { HybridListingModule } from '@common/modules/hybrid-listing/hybrid-listing.module';
import { TextFilterModule } from '@common/modules/text-filter/text-filter.module';
import { UserSettingsModule } from '@common/modules/user-settings/user-settings.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { PlanBlueprintTypeListingFiltersComponent } from './listing/filters/plan-blueprint-type-listing-filters.component';
import { FormattingModule } from '@app/core/formatting.module';
import { CommonFormattingModule } from '@common/formatting/common-formatting.module';
import { PlanBlueprintTypeListingComponent } from './listing/plan-blueprint-type-listing.component';
import { PlanBlueprintTypeEditorComponent } from './editor/plan-blueprint-type-editor.component';
import { PlanBlueprintTypesRoutingModule } from './plan-blueprint-type.routing';

@NgModule({
	declarations: [
		PlanBlueprintTypeListingComponent,
		PlanBlueprintTypeEditorComponent,
		PlanBlueprintTypeListingFiltersComponent
	],
	imports: [
		CommonModule,
		CommonUiModule,
		CommonFormsModule,
		CommonFormattingModule,
		PlanBlueprintTypesRoutingModule,
		HybridListingModule,
		TextFilterModule,
		UserSettingsModule
	]
})
export class PlanBlueprintTypesModule { }
