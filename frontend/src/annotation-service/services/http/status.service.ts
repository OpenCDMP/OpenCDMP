import { Injectable } from "@angular/core";
import { Status, StatusPersist } from "@annotation-service/core/model/status.model";
import { StatusLookup } from "@annotation-service/core/query/status.lookup";
import { ConfigurationService } from "@app/core/services/configuration/configuration.service";
import { BaseHttpV2Service } from "@app/core/services/http/base-http-v2.service";
import { QueryResult } from "@common/model/query-result";
import { FilterService } from "@common/modules/text-filter/filter-service";
import { Guid } from "@common/types/guid";
import { Observable, throwError } from "rxjs";
import { catchError } from "rxjs/operators";

@Injectable()
export class StatusService {
  private get apiBase(): string { return `${this.installationConfiguration.annotationServiceAddress}api/annotation/status`; }

	constructor(
		private installationConfiguration: ConfigurationService,
		private http: BaseHttpV2Service,
		private filterService: FilterService
	) { }

	query(q: StatusLookup): Observable<QueryResult<Status>> {
		const url = `${this.apiBase}/query`;

		return this.http
			.post<QueryResult<Status>>(url, q).pipe(
				catchError((error: any) => throwError(error)));
	}

	getSingle(id: Guid, reqFields: string[] = []): Observable<Status> {
		const url = `${this.apiBase}/${id}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<Status>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	persist(item: StatusPersist) {
		const url = `${this.apiBase}/persist`;

		return this.http
			.post<Status>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	delete(id: Guid, ): Observable<void> {
		const url = `${this.apiBase}/${id}`;
		return this.http
			.delete<void>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

}