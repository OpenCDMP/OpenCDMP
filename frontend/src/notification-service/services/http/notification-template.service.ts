import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { QueryResult } from '@common/model/query-result';
import { Guid } from '@common/types/guid';
import { NotificationTemplateLookup } from '@notification-service/core/query/notification-template.lookup';
import { NotificationTemplate, NotificationTemplateDefault, NotificationTemplatePersist } from '@notification-service/core/model/notification-template.model';
import { NotificationTemplateChannel } from '@notification-service/core/enum/notification-template-channel.enum';
import { NotificationType } from '@notification-service/core/enum/notification-type.enum';
import { BaseHttpV2Service } from '@app/core/services/http/base-http-v2.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';

@Injectable()
export class NotificationTemplateService {

	constructor(private http: BaseHttpV2Service, private configurationService: ConfigurationService) { 
    }

    private get apiBase(): string { return `${this.configurationService.notificationServiceAddress}api/notification/notification-template`; }

	query(q: NotificationTemplateLookup): Observable<QueryResult<NotificationTemplate>> {
		const url = `${this.apiBase}/query`;

		return this.http
			.post<QueryResult<NotificationTemplate>>(url, q).pipe(
				catchError((error: any) => throwError(error)));
	}

	getSingle(id: Guid, reqFields: string[] = []): Observable<NotificationTemplate> {
		const url = `${this.apiBase}/${id}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<NotificationTemplate>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	persist(item: NotificationTemplatePersist): Observable<NotificationTemplate> {
		const url = `${this.apiBase}/persist`;

		return this.http
			.post<NotificationTemplate>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	getDefaults(): Observable<NotificationTemplateDefault[]> {
		const url = `${this.apiBase}/defaults`;

		return this.http
			.get<NotificationTemplateDefault[]>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

	getDefault(type: NotificationType, channel: NotificationTemplateChannel, language: string): Observable<NotificationTemplateDefault> {
		const url = `${this.apiBase}/default`;
		const options = { params: { type, channel: channel.toString(), language } };

		return this.http
			.get<NotificationTemplateDefault>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	sendTest(item: NotificationTemplatePersist): Observable<void> {
		const url = `${this.apiBase}/test`;

		return this.http
			.post<void>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	delete(id: Guid): Observable<NotificationTemplate> {
		const url = `${this.apiBase}/${id}`;

		return this.http
			.delete<NotificationTemplate>(url).pipe(
				catchError((error: any) => throwError(error)));
	}
}