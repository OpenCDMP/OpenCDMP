import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Guid } from '@common/types/guid';
import { NotifierListLookup } from '@notification-service/core/query/notifier-list.lookup';
import { NotifierListConfigurationDataContainer } from '@notification-service/core/model/notifier-configuration.model';
import { UserNotificationPreference, UserNotificationPreferencePersist } from '@notification-service/core/model/user-notification-preference.model';
import { BaseHttpV2Service } from '@app/core/services/http/base-http-v2.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';

@Injectable()
export class UserNotificationPreferenceService {

	private get apiBase(): string { return `${this.configurationService.notificationServiceAddress}api/notification/notification-preference`; }

	constructor(
		private configurationService: ConfigurationService,
		private http: BaseHttpV2Service
	) { }

	current(id: Guid, reqFields: string[] = []): Observable<UserNotificationPreference[]> {
		const url = `${this.apiBase}/user/${id}/current`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<UserNotificationPreference[]>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	getNotifierList(q: NotifierListLookup): Observable<NotifierListConfigurationDataContainer> {
		const url = `${this.apiBase}/notifier-list/available`;
		
		return this.http
			.post<NotifierListConfigurationDataContainer>(url, q).pipe(
				catchError((error: any) => throwError(error)));
	}

	persist(item: UserNotificationPreferencePersist, totp?: string): Observable<UserNotificationPreference> {
		const url = `${this.apiBase}/persist`;

		return this.http
			.post<UserNotificationPreference>(url, item).pipe(
				catchError((error: any) => throwError(error)));
		return;
	}
}
