import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { AuthService } from '@app/core/services/auth/auth.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { BaseComponent } from '@common/base/base.component';
import { FormService } from '@common/forms/form-service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { TranslateService } from '@ngx-translate/core';
import { NotificationContactType } from '@notification-service/core/enum/notification-contact-type';
import { NotificationTrackingProcess } from '@notification-service/core/enum/notification-tracking-process.enum';
import { NotificationType } from '@notification-service/core/enum/notification-type.enum';
import { NotificationServiceEnumUtils } from '@notification-service/core/formatting/enum-utils.service';
import { UserNotificationPreference } from '@notification-service/core/model/user-notification-preference.model';
import { NotifierListLookup } from '@notification-service/core/query/notifier-list.lookup';
import { UserNotificationPreferenceService } from '@notification-service/services/http/user-notification-preference.service';
import { takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Component({
	selector: 'app-user-profile-notifier-list-editor',
	templateUrl: './user-profile-notifier-list-editor.component.html',
	styleUrls: ['./user-profile-notifier-list-editor.component.scss']
})
export class UserProfileNotifierListEditorComponent extends BaseComponent implements OnInit {

	availableNotifiers: { [key: string]: NotificationContactType[] } = {};
	availableNotifiersKeys: NotificationType[];

	notificationTrackingProcess: NotificationTrackingProcess = NotificationTrackingProcess.PENDING;

	constructor(
		private userNotificationPreferenceService: UserNotificationPreferenceService,
		private uiNotificationService: UiNotificationService,
		private language: TranslateService,
		private httpErrorHandlingService: HttpErrorHandlingService,
		private authService: AuthService,
		private formService: FormService,
		private logger: LoggingService,
		public notificationServiceEnumUtils: NotificationServiceEnumUtils,
	) {
		super();
	}

	ngOnInit(): void {
		this.getConfiguration();
	}

	getConfiguration() {
		this.userNotificationPreferenceService.getNotifierList(new NotifierListLookup())
			.pipe(takeUntil(this._destroyed))
			.subscribe(
				data => {
					try {
						this.availableNotifiers = data.notifiers;
						this.availableNotifiersKeys = Object.keys(this.availableNotifiers) as NotificationType[];
						this.getExistingSelections();
					} catch {
						this.notificationTrackingProcess = NotificationTrackingProcess.ERROR;
						this.logger.error('Could not parse Description: ' + data);
						this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
					}
				},
				error => this.onCallbackError(error)
			);
	}

	getExistingSelections() {
		this.userNotificationPreferenceService.current(this.authService.userId(), [
			nameof<UserNotificationPreference>(x => x.userId),
			nameof<UserNotificationPreference>(x => x.type),
			nameof<UserNotificationPreference>(x => x.channel),
			nameof<UserNotificationPreference>(x => x.ordinal),
		])
			.pipe(takeUntil(this._destroyed)).subscribe(
				data => {
					try {
						if (data.length > 0) {
							this.orderAvailableItemsbasedOnExistingSelections(data);
						}
						this.notificationTrackingProcess = NotificationTrackingProcess.SUCCESSFUL;
					} catch {
						this.notificationTrackingProcess = NotificationTrackingProcess.ERROR;
						this.logger.error('Could not parse Description: ' + data);
						this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
					}
				},
				error => this.onCallbackError(error)
			);
	}

	orderAvailableItemsbasedOnExistingSelections(notificationPreferences: UserNotificationPreference[]) {
		if (!notificationPreferences || notificationPreferences.length === 0) { return; }
		this.availableNotifiersKeys.forEach(key => {
			const orderedList = [];
			orderedList.push(...(notificationPreferences.filter(x => x.type === key && this.availableNotifiers[key].includes(x.channel)).sort((n1, n2) => n1.ordinal - n2.ordinal).map(x => x.channel))); // First push the selected ordered values.
			orderedList.push(...this.availableNotifiers[key].filter(x => !orderedList.includes(x))); //Then push the rest items.
			this.availableNotifiers[key] = orderedList;
		});
	}

	formSubmit(): void {
		this.persist();
	}

	private persist() {
		const persistValue = { notificationPreferences: this.availableNotifiers, userId: this.authService.userId() };

		this.userNotificationPreferenceService.persist(persistValue)
			.pipe(takeUntil(this._destroyed))
			.subscribe(
				response => this.onCallbackSuccess(),
				error => this.onCallbackError(error)
			);
	}

	onCallbackSuccess(): void {
		this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
		this.getConfiguration();
	}

	onCallbackError(errorResponse: HttpErrorResponse) {
		this.notificationTrackingProcess = NotificationTrackingProcess.ERROR;

		this.httpErrorHandlingService.handleBackedRequestError(errorResponse);
	}

	dropped(event: CdkDragDrop<string[]>, type: NotificationType) {
		moveItemInArray(this.availableNotifiers[type], event.previousIndex, event.currentIndex);
	}

	preferencesNotPending(): boolean {
		return ! (this.notificationTrackingProcess === NotificationTrackingProcess.PENDING);
	}
	preferencesNotCompleted(): boolean {
		return ! (this.notificationTrackingProcess === NotificationTrackingProcess.SUCCESSFUL);
	}
	preferencesNotWithErrors(): boolean {
		return ! (this.notificationTrackingProcess === NotificationTrackingProcess.ERROR);
	}

	hasMoreThanOneNotifiers(notificationType: NotificationType): boolean {
		return this.availableNotifiers[notificationType].length > 1;
	}
}
