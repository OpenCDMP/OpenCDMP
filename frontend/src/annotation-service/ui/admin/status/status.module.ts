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
import { StatusListingComponent } from './listing/status-listing.component';
import { RichTextEditorModule } from '@app/library/rich-text-editor/rich-text-editor.module';
import { MatIconModule } from '@angular/material/icon';
import { EditorModule } from '@tinymce/tinymce-angular';
import { StatusRoutingModule } from './status.routing';
import { AnnotationServiceFormattingModule } from '@annotation-service/core/formatting/formatting.module';
import { StatusListingFiltersComponent } from './listing/filters/status-listing-filters.component';
import { StatusEditorComponent } from './editor/status-editor.component';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		ConfirmationDialogModule,
		StatusRoutingModule,
		NgxDropzoneModule,
		DragDropModule,
		AutoCompleteModule,
		HybridListingModule,
		TextFilterModule,
		UserSettingsModule,
		CommonFormattingModule,
		RichTextEditorModule,
		MatIconModule,
		EditorModule,
		AnnotationServiceFormattingModule
	],
	declarations: [
		StatusEditorComponent,
		StatusListingComponent,
		StatusListingFiltersComponent,
	]
})
export class StatusModule { }
