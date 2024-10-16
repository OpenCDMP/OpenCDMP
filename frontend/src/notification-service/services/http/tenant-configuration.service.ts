import { Injectable } from '@angular/core';
import { QueryResult } from '@common/model/query-result';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { BaseHttpV2Service } from '@app/core/services/http/base-http-v2.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { TenantConfigurationLookup } from '@notification-service/core/query/tenant-configuration.lookup';
import { TenantConfiguration, TenantConfigurationPersist } from '@notification-service/core/model/tenant-configuration';
import { TenantConfigurationType } from '@notification-service/core/enum/tenant-configuration-type';
import { NotifierListLookup } from '@notification-service/core/query/notifier-list.lookup';
import { NotifierListConfigurationDataContainer } from '@notification-service/core/model/notifier-configuration.model';

@Injectable()
export class TenantConfigurationService {

	constructor(private http: BaseHttpV2Service, private configurationService: ConfigurationService, private filterService: FilterService) {
	}

	private get apiBase(): string { return `${this.configurationService.notificationServiceAddress}api/notification/tenant-configuration`; }

	query(q: TenantConfigurationLookup): Observable<QueryResult<TenantConfiguration>> {
		const url = `${this.apiBase}/query`;
		return this.http.post<QueryResult<TenantConfiguration>>(url, q).pipe(catchError((error: any) => throwError(error)));
	}

	getSingle(id: Guid, reqFields: string[] = []): Observable<TenantConfiguration> {
		const url = `${this.apiBase}/${id}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<TenantConfiguration>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	getCurrentTenantType(type: TenantConfigurationType, reqFields: string[] = []): Observable<TenantConfiguration> {
		const url = `${this.apiBase}/current-tenant/${type}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<TenantConfiguration>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}


	persist(item: TenantConfigurationPersist): Observable<TenantConfiguration> {
		const url = `${this.apiBase}/persist`;

		return this.http
			.post<TenantConfiguration>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	delete(id: Guid): Observable<void> {
		const url = `${this.apiBase}/${id}`;

		return this.http
			.delete<void>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

	getNotifierList(q: NotifierListLookup): Observable<NotifierListConfigurationDataContainer> {
		const url = `${this.apiBase}/notifier-list/available`;

		return this.http
			.post<NotifierListConfigurationDataContainer>(url, q).pipe(
				catchError((error: any) => throwError(error)));
	}
}
