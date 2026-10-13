import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogTitle, MatDialogContent, MatDialogActions } from '@angular/material/dialog';
import { PopupNotification } from '../../../core/services/notification/ui-notification-service';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { MatButton } from '@angular/material/button';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-popup-notification-dialog',
    templateUrl: './popup-notification.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatDialogTitle, CdkScrollable, MatDialogContent, MatDialogActions, MatButton, TranslatePipe]
})
export class PopupNotificationDialogComponent {

	constructor(
		public dialogRef: MatDialogRef<PopupNotificationDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public notification: PopupNotification
	) {
	}

	close() {
		this.dialogRef.close();
	}
}
