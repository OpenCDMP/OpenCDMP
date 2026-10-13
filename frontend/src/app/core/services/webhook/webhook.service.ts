import { Injectable } from '@angular/core';
import { WebhookEventType } from '@app/core/common/enum/webhook-event-type.enum';
import { WebhookDelivery, WebhookSubscription, WebhookSubscriptionPersist } from '@app/core/model/webhook/webhook';
import { WebhookDeliveryLookup } from '@app/core/query/webhook-delivery.lookup';
import { WebhookSubscriptionLookup } from '@app/core/query/webhook-subscription.lookup';
import { QueryResult } from '@common/model/query-result';
import { Guid } from '@common/types/guid';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ConfigurationService } from '../configuration/configuration.service';
import { BaseHttpV2Service } from '../http/base-http-v2.service';

@Injectable()
export class WebhookService {

	constructor(private http: BaseHttpV2Service, private configurationService: ConfigurationService) {
	}

	private get apiBase(): string { return `${this.configurationService.server}webhook-subscription`; }

	query(q: WebhookSubscriptionLookup): Observable<QueryResult<WebhookSubscription>> {
		const url = `${this.apiBase}/query`;
		return this.http.post<QueryResult<WebhookSubscription>>(url, q).pipe(catchError((error: any) => throwError(error)));
	}

	getSingle(id: Guid, reqFields: string[] = []): Observable<WebhookSubscription> {
		const url = `${this.apiBase}/${id}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<WebhookSubscription>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	persist(item: WebhookSubscriptionPersist): Observable<WebhookSubscription> {
		const url = `${this.apiBase}/persist`;

		return this.http
			.post<WebhookSubscription>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	delete(id: Guid): Observable<WebhookSubscription> {
		const url = `${this.apiBase}/${id}`;

		return this.http
			.delete<WebhookSubscription>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

	ping(id: Guid): Observable<void> {
		const url = `${this.apiBase}/${id}/ping`;

		return this.http
			.post<void>(url, null).pipe(
				catchError((error: any) => throwError(error)));
	}

	eventTypes(): Observable<WebhookEventType[]> {
		const url = `${this.apiBase}/event-types`;

		return this.http
			.get<WebhookEventType[]>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

	queryDeliveries(q: WebhookDeliveryLookup): Observable<QueryResult<WebhookDelivery>> {
		const url = `${this.apiBase}/delivery/query`;
		return this.http.post<QueryResult<WebhookDelivery>>(url, q).pipe(catchError((error: any) => throwError(error)));
	}

	redeliver(deliveryId: Guid): Observable<void> {
		const url = `${this.apiBase}/delivery/${deliveryId}/redeliver`;

		return this.http
			.post<void>(url, null).pipe(
				catchError((error: any) => throwError(error)));
	}
}
