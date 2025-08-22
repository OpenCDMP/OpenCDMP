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
import { NotificationTemplateListingComponent } from './listing/notification-template-listing.component';
import { RichTextEditorModule } from '@app/library/rich-text-editor/rich-text-editor.module';
import { NotificationTemplateRoutingModule } from './notification-template.routing';
import { NotificationTemplateEditorComponent } from './editor/notification-template-editor.component';
import { NotificationTemplateListingFiltersComponent } from './listing/filters/notification-template-listing-filters.component';
import { MatIconModule } from '@angular/material/icon';
import { EditorModule } from '@tinymce/tinymce-angular';
import { NotificationTemplateFieldOptionsComponent } from './editor/field-options/notification-template-field-options.component';
import { NotificationServiceFormattingModule } from '@notification-service/core/formatting/formatting.module';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		ConfirmationDialogModule,
		NotificationTemplateRoutingModule,
		DragDropModule,
		AutoCompleteModule,
		HybridListingModule,
		TextFilterModule,
		UserSettingsModule,
		CommonFormattingModule,
		RichTextEditorModule,
		MatIconModule,
		EditorModule,
		NotificationServiceFormattingModule
	],
	declarations: [
		NotificationTemplateEditorComponent,
		NotificationTemplateListingComponent,
		NotificationTemplateListingFiltersComponent,
		NotificationTemplateFieldOptionsComponent
	]
})
export class NotificationTemplateModule { }
