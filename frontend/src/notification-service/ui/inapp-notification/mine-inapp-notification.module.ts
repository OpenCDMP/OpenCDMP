import { NgModule } from '@angular/core';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { ConfirmationDialogModule } from '@common/modules/confirmation-dialog/confirmation-dialog.module';
import { TextFilterModule } from '@common/modules/text-filter/text-filter.module';
import { UserSettingsModule } from '@common/modules/user-settings/user-settings.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { MineInAppNotificationRoutingModule } from './mine-inapp-notification-routing.module';
import { MineInAppNotificationListingComponent } from './listing/mine-inapp-notification-listing.component';
import { HybridListingModule } from '@common/modules/hybrid-listing/hybrid-listing.module';
import { InAppNotificationEditorComponent } from './editor/inapp-notification-editor.component';
import { MineInAppNotificationListingFiltersComponent } from './listing/filters/mine-inapp-notification-listing-filters.component';
import { MineInAppNotificationListingDialogComponent } from './listing-dialog/mine-inapp-notification-listing-dialog.component';
import { CommonFormattingModule } from '@common/formatting/common-formatting.module';
import { FormattingModule } from '@app/core/formatting.module';
import { NotificationServiceFormattingModule } from '@notification-service/core/formatting/formatting.module';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		ConfirmationDialogModule,
		HybridListingModule,
		TextFilterModule,
		MineInAppNotificationRoutingModule,
		UserSettingsModule,
		CommonFormattingModule,
		FormattingModule,
		NotificationServiceFormattingModule
	],
	declarations: [
		MineInAppNotificationListingComponent,
		InAppNotificationEditorComponent,
		MineInAppNotificationListingFiltersComponent,
		MineInAppNotificationListingDialogComponent
	],
	exports: [
		MineInAppNotificationListingComponent,
	]
})
export class MineInAppNotificationModule { }
