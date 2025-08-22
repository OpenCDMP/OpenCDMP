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
import { ReferenceRoutingModule } from './reference.routing';
import { ReferenceEditorComponent } from './editor/reference-editor.component';
import { ReferenceListingComponent } from './listing/reference-listing.component';
import { ReferenceListingFiltersComponent } from './listing/filters/reference-listing-filters.component';
import { RichTextEditorModule } from '@app/library/rich-text-editor/rich-text-editor.module';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		ConfirmationDialogModule,
		ReferenceRoutingModule,
		DragDropModule,
		AutoCompleteModule,
		HybridListingModule,
		TextFilterModule,
		UserSettingsModule,
		CommonFormattingModule,
		RichTextEditorModule
	],
	declarations: [
		ReferenceEditorComponent,
		ReferenceListingComponent,
		ReferenceListingFiltersComponent
	]
})
export class ReferenceModule { }
