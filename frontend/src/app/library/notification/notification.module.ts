import { NgModule } from '@angular/core';
import { NotificationComponent } from '@app/library/notification/notification.component';
import { PopupNotificationDialogComponent } from '@app/library/notification/popup/popup-notification.component';
import { SnackBarNotificationComponent } from '@app/library/notification/snack-bar/snack-bar-notification.component';
import { CommonUiModule } from '@common/ui/common-ui.module';

@NgModule({
    imports: [
        CommonUiModule
    ],
    declarations: [
        NotificationComponent,
        SnackBarNotificationComponent,
        PopupNotificationDialogComponent,
    ],
    exports: [
        NotificationComponent
    ]
})
export class NotificationModule {
	constructor() { }
}
