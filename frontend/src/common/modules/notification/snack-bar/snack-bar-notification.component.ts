import { Component, Inject } from '@angular/core';
import { MAT_SNACK_BAR_DATA } from '@angular/material/snack-bar';
import { SnackBarNotification } from '@common/modules/notification/ui-notification-service';

@Component({
	selector: 'app-snack-bar-notification',
	templateUrl: './snack-bar-notification.component.html'
})

export class SnackBarNotificationComponent {
	message: string;
	constructor(@Inject(MAT_SNACK_BAR_DATA) public data: SnackBarNotification) {
		this.message = data.message;
	}
}
