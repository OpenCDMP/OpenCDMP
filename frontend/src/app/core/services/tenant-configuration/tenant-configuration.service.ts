import { Injectable } from '@angular/core';
import { QueryResult } from '@common/model/query-result';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ConfigurationService } from '../configuration/configuration.service';
import { BaseHttpV2Service } from '../http/base-http-v2.service';
import { TenantConfigurationLookup } from '@app/core/query/tenant-configuration.lookup';
import { TenantConfiguration, TenantConfigurationPersist } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';

@Injectable()
export class TenantConfigurationService {

	constructor(private http: BaseHttpV2Service, private configurationService: ConfigurationService, private filterService: FilterService) {
	}

	private get apiBase(): string { return `${this.configurationService.server}tenant-configuration`; }

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
}
