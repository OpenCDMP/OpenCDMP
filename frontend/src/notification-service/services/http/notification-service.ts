import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Guid } from '@common/types/guid';
import { QueryResult } from '@common/model/query-result';
import { NotificationLookup } from '@notification-service/core/query/notification.lookup';
import { Notification } from '@notification-service/core/model/notification.model';
import { BaseHttpV2Service } from '@app/core/services/http/base-http-v2.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';

@Injectable()
export class NotificationService {

	constructor(private http: BaseHttpV2Service, private configurationService: ConfigurationService) { 
    }

    private get apiBase(): string { return `${this.configurationService.notificationServiceAddress}api/notification`; }

	query(q: NotificationLookup): Observable<QueryResult<Notification>> {
		const url = `${this.apiBase}/query`;

		return this.http
			.post<QueryResult<Notification>>(url, q).pipe(
				catchError((error: any) => throwError(error)));
	}

	delete(id: Guid): Observable<Notification> {
		const url = `${this.apiBase}/${id}`;

		return this.http
			.delete<Notification>(url).pipe(
				catchError((error: any) => throwError(error)));
	}
}