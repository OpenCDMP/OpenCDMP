import { NgModule } from "@angular/core";
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { PlanStatusRoutingModule } from "./plan-status.routing";
import { PlanStatusListingComponent } from "./listing/plan-status-listing/plan-status-listing.component";
import { CommonFormattingModule } from "@common/formatting/common-formatting.module";
import { ConfirmationDialogModule } from "@common/modules/confirmation-dialog/confirmation-dialog.module";
import { HybridListingModule } from "@common/modules/hybrid-listing/hybrid-listing.module";
import { UserSettingsModule } from "@common/modules/user-settings/user-settings.module";
import { PlanStatusListingFiltersComponent } from "./listing/plan-status-listing/plan-status-listing-filters/plan-status-listing-filters.component";
import { PlanStatusEditorComponent } from "./editor/plan-status-editor/plan-status-editor.component";
import { TextFilterModule } from "@common/modules/text-filter/text-filter.module";
import { RichTextEditorModule } from "@app/library/rich-text-editor/rich-text-editor.module";

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
        PlanStatusRoutingModule,
        ConfirmationDialogModule,
        HybridListingModule,
        UserSettingsModule,
        CommonFormattingModule,
        TextFilterModule,
        RichTextEditorModule
	],
	declarations: [
        PlanStatusListingComponent,
        PlanStatusListingFiltersComponent,
        PlanStatusEditorComponent
	]
})
export class PlanStatusModule { }
