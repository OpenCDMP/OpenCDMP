import { NgModule } from '@angular/core';
import { PopupNotificationDialogComponent } from '@common/modules/notification/popup/popup-notification.component';
import { SnackBarNotificationComponent } from '@common/modules/notification/snack-bar/snack-bar-notification.component';
import { UiNotificationComponent } from '@common/modules/notification/ui-notification.component';
import { CommonUiModule } from '@common/ui/common-ui.module';

@NgModule({
	imports: [
		CommonUiModule
	],
	declarations: [
		UiNotificationComponent,
		SnackBarNotificationComponent,
		PopupNotificationDialogComponent,
	],
	exports: [
		UiNotificationComponent
	],
	entryComponents: [
		SnackBarNotificationComponent,
		PopupNotificationDialogComponent,
	]
})
export class UiNotificationModule {
	constructor() { }
}
