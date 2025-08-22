import { NgModule } from '@angular/core';
import { FormattingModule } from '@app/core/formatting.module';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { PluginEditorComponent } from './plugin-editor.component';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { FileUploadComponent } from '@app/library/file-uploader/file-uploader.component';
import { RichTextEditorModule } from '@app/library/rich-text-editor/rich-text-editor.module';
import { CommonFormattingModule } from '@common/formatting/common-formatting.module';
import { ConfirmationDialogModule } from '@common/modules/confirmation-dialog/confirmation-dialog.module';
import { HybridListingModule } from '@common/modules/hybrid-listing/hybrid-listing.module';
import { TextFilterModule } from '@common/modules/text-filter/text-filter.module';
import { UserSettingsModule } from '@common/modules/user-settings/user-settings.module';
import { NotifierListModule } from '@notification-service/ui/admin/tenant-configuration/notifier-list/notifier-list-editor.module';
import { NgxColorsModule } from 'ngx-colors';
import { TenantConfigurationRoutingModule } from '../admin/tenant-configuration/tenant-configuration.routing';
import { CommonFormsModule } from '@common/forms/common-forms.module';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		ConfirmationDialogModule,
		TenantConfigurationRoutingModule,
		DragDropModule,
		FormattingModule,
		AutoCompleteModule,
		HybridListingModule,
		TextFilterModule,
		UserSettingsModule,
		CommonFormattingModule,
		RichTextEditorModule,
		NgxColorsModule,
		NotifierListModule,
		FileUploadComponent,
	],
	declarations: [
		PluginEditorComponent,
	],
	exports: [
		PluginEditorComponent,
	]
})
export class PluginEditorModule { }
