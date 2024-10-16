import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { QueryResult } from '@common/model/query-result';
import { Guid } from '@common/types/guid';
import { NotificationTemplateLookup } from '@notification-service/core/query/notification-template.lookup';
import { NotificationTemplate, NotificationTemplatePersist } from '@notification-service/core/model/notification-template.model';
import { NotificationTemplateKind } from '@notification-service/core/enum/notification-template-kind.enum';
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

	updateKind(id: Guid, kind: NotificationTemplateKind): Observable<NotificationTemplate> {
		const url = `${this.apiBase}/${id}/${kind}`;

		return this.http
			.post<NotificationTemplate>(url, {}).pipe(
				catchError((error: any) => throwError(error)));
	}

	persist(item: NotificationTemplatePersist): Observable<NotificationTemplate> {
		const url = `${this.apiBase}/persist`;

		return this.http
			.post<NotificationTemplate>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	delete(id: Guid): Observable<NotificationTemplate> {
		const url = `${this.apiBase}/${id}`;

		return this.http
			.delete<NotificationTemplate>(url).pipe(
				catchError((error: any) => throwError(error)));
	}
}