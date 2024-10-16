import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

@Injectable()
export class UiNotificationService {

	private notificationSubject = new Subject<UiNotification>();

	constructor() {
	}

	public snackBarNotification(message: string, level: SnackBarNotificationLevel, duration: number = 5000) {
		const notification: SnackBarNotification = new SnackBarNotification();
		notification.level = level;
		notification.message = message;
		notification.duration = duration;
		this.notificationSubject.next(notification);
	}

	public popupNotification(title: string, message: string) {
		const notification: PopupNotification = new PopupNotification();
		notification.title = title;
		notification.message = message;
		this.notificationSubject.next(notification);
	}

	public getNotificationObservable(): Observable<UiNotification> {
		return this.notificationSubject.asObservable();
	}
}

export enum UiNotificationType {
	SnackBar = 0,
	Popup = 1
}

export interface UiNotification {
	type: UiNotificationType;

}

export enum SnackBarNotificationLevel {
	Info = 0,
	Warning = 1,
	Success = 2,
	Error = 3
}

export class SnackBarNotification implements UiNotification {
	type: UiNotificationType = UiNotificationType.SnackBar;
	message: string;
	level: SnackBarNotificationLevel;
	duration: number;
}

export class PopupNotification implements UiNotification {
	type: UiNotificationType = UiNotificationType.Popup;
	message: string;
	title: string;
}
