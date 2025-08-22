import { NgModule } from "@angular/core";
import { AutoCompleteModule } from "@app/library/auto-complete/auto-complete.module";
import { CommonFormattingModule } from '@common/formatting/common-formatting.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { ConfirmationDialogModule } from '@common/modules/confirmation-dialog/confirmation-dialog.module';
import { HybridListingModule } from "@common/modules/hybrid-listing/hybrid-listing.module";
import { TextFilterModule } from "@common/modules/text-filter/text-filter.module";
import { UserSettingsModule } from "@common/modules/user-settings/user-settings.module";
import { CommonUiModule } from '@common/ui/common-ui.module';
import { NotificationListingComponent } from './notification-listing.component';
import { NotificationRoutingModule } from './notification.routing';
import { NotificationListingFiltersComponent } from './filters/notification-listing-filters.component';
import { MatIconModule } from '@angular/material/icon';
import { EditorModule } from '@tinymce/tinymce-angular';
import { NotificationServiceFormattingModule } from '@notification-service/core/formatting/formatting.module';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		ConfirmationDialogModule,
		NotificationRoutingModule,
		AutoCompleteModule,
		HybridListingModule,
		TextFilterModule,
		UserSettingsModule,
		CommonFormattingModule,
		MatIconModule,
		EditorModule,
		NotificationServiceFormattingModule
	],
	declarations: [
		NotificationListingComponent,
		NotificationListingFiltersComponent
	]
})
export class NotificationModule { }
