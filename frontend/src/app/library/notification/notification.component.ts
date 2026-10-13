import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PopupNotification, SnackBarNotification, SnackBarNotificationLevel, UiNotificationService, UiNotificationType } from '@app/core/services/notification/ui-notification-service';
import { PopupNotificationDialogComponent } from '@app/library/notification/popup/popup-notification.component';
import { SnackBarNotificationComponent } from '@app/library/notification/snack-bar/snack-bar-notification.component';
import { BaseComponent } from '@common/base/base.component';
import { takeUntil } from 'rxjs/operators';

@Component({
    selector: 'app-notification',
    templateUrl: './notification.component.html',
    styleUrls: ['./notification.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager
})
export class NotificationComponent extends BaseComponent implements OnInit {

	constructor(
		private snackBar: MatSnackBar,
		private dialog: MatDialog,
		private uiNotificationService: UiNotificationService
	) {
		super();
	}

	ngOnInit() {
		this.uiNotificationService.getNotificationObservable().pipe(takeUntil(this._destroyed)).subscribe(notification => {
			switch (notification.type) {
				case UiNotificationType.SnackBar:
					const snackBarNotification: SnackBarNotification = notification as SnackBarNotification;
					this.snackBar.openFromComponent(SnackBarNotificationComponent, {
						data: snackBarNotification,
						duration: snackBarNotification.duration,
						panelClass: [this.getSnackBarLevelClass(snackBarNotification.level)],
						// Announce to screen readers (WCAG 4.1.3). openFromComponent does NOT
						// auto-announce projected content, so pass announcementMessage explicitly.
						// Errors/warnings interrupt (assertive); success/info are polite.
						announcementMessage: snackBarNotification.message,
						politeness: this.getSnackBarPoliteness(snackBarNotification.level)
					});
					break;
				case UiNotificationType.Popup:
					const popupNotification: PopupNotification = notification as PopupNotification;
					this.dialog.open(PopupNotificationDialogComponent, {
						data: popupNotification
					});
					break;
			}
		});
	}

	private getSnackBarLevelClass(level: SnackBarNotificationLevel): string {
		switch (level) {
			case SnackBarNotificationLevel.Warning: return 'snack-bar-notification--warning';
			case SnackBarNotificationLevel.Error: return 'snack-bar-notification--error';
			case SnackBarNotificationLevel.Success: return 'snack-bar-notification--success';
			default: return 'snack-bar-notification--info';
		}

	}

	private getSnackBarPoliteness(level: SnackBarNotificationLevel): 'assertive' | 'polite' {
		switch (level) {
			case SnackBarNotificationLevel.Error:
			case SnackBarNotificationLevel.Warning:
				return 'assertive';
			default:
				return 'polite';
		}
	}
}
