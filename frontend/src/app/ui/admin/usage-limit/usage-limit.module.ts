import { DragDropModule } from '@angular/cdk/drag-drop';
import { NgModule } from "@angular/core";
import { AutoCompleteModule } from "@app/library/auto-complete/auto-complete.module";
import { CommonFormattingModule } from '@common/formatting/common-formatting.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { ConfirmationDialogModule } from '@common/modules/confirmation-dialog/confirmation-dialog.module';
import { HybridListingModule } from "@common/modules/hybrid-listing/hybrid-listing.module";
import { TextFilterModule } from "@common/modules/text-filter/text-filter.module";
import { UserSettingsModule } from "@common/modules/user-settings/user-settings.module";
import { CommonUiModule } from '@common/ui/common-ui.module';
import { NgxDropzoneModule } from "ngx-dropzone";
import { UsageLimitRoutingModule } from './usage-limit.routing';
import { UsageLimitEditorComponent } from './editor/usage-limit-editor.component';
import { RichTextEditorModule } from '@app/library/rich-text-editor/rich-text-editor.module';
import { UsageLimitListingComponent } from './listing/usage-limit-listing.component';
import { UsageLimitListingFiltersComponent } from './listing/filters/usage-limit-listing-filters.component';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		ConfirmationDialogModule,
		UsageLimitRoutingModule,
		NgxDropzoneModule,
		DragDropModule,
		AutoCompleteModule,
		HybridListingModule,
		TextFilterModule,
		UserSettingsModule,
		CommonFormattingModule,
		RichTextEditorModule
	],
	declarations: [
		UsageLimitEditorComponent,
		UsageLimitListingComponent,
		UsageLimitListingFiltersComponent
	]
})
export class UsageLimitModule { }
