import { NgModule } from "@angular/core";
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { CommonFormattingModule } from "@common/formatting/common-formatting.module";
import { ConfirmationDialogModule } from "@common/modules/confirmation-dialog/confirmation-dialog.module";
import { HybridListingModule } from "@common/modules/hybrid-listing/hybrid-listing.module";
import { UserSettingsModule } from "@common/modules/user-settings/user-settings.module";
import { TextFilterModule } from "@common/modules/text-filter/text-filter.module";
import { RichTextEditorModule } from "@app/library/rich-text-editor/rich-text-editor.module";
import { DescriptionStatusRoutingModule } from "./description-status.routing";
import { DescriptionStatusListingComponent } from "./listing/description-status-listing/description-status-listing.component";
import { DescriptionStatusEditorComponent } from "./editor/description-status-editor/description-status-editor.component";
import { DescriptionStatusListingFiltersComponent } from "./listing/description-status-listing-filters/description-status-listing-filters.component";
import { NgxColorsModule } from "ngx-colors";
import { FormFocusDirective } from "@common/forms/form-focus.directive";
import { FileUploadComponent } from "@app/library/file-uploader/file-uploader.component";

@NgModule({
	imports: [
	CommonUiModule,
	CommonFormsModule,
        DescriptionStatusRoutingModule,
        ConfirmationDialogModule,
        HybridListingModule,
        UserSettingsModule,
        CommonFormattingModule,
        TextFilterModule,
        RichTextEditorModule,
        NgxColorsModule,
        FormFocusDirective,
        FileUploadComponent
	],
	declarations: [
        DescriptionStatusListingComponent,
        DescriptionStatusListingFiltersComponent,
        DescriptionStatusEditorComponent,
	]
})
export class DescriptionStatusModule { }
