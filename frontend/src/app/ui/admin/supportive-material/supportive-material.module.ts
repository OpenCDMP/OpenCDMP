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
import { SupportiveMaterialRoutingModule } from './supportive-material.routing';
import { SupportiveMaterialEditorComponent } from './editor/supportive-material-editor.component';
import { SupportiveMaterialListingComponent } from './listing/supportive-material-listing.component';
import { SupportiveMaterialListingFiltersComponent } from './listing/filters/supportive-material-listing-filters.component';
import { EditorModule } from '@tinymce/tinymce-angular';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		ConfirmationDialogModule,
		SupportiveMaterialRoutingModule,
		NgxDropzoneModule,
		DragDropModule,
		AutoCompleteModule,
		HybridListingModule,
		TextFilterModule,
		UserSettingsModule,
		CommonFormattingModule,
		EditorModule
	],
	declarations: [
		SupportiveMaterialEditorComponent,
		SupportiveMaterialListingComponent,
		SupportiveMaterialListingFiltersComponent
	]
})
export class SupportiveMaterialModule { }
