import { Injectable } from "@angular/core";
import { AnnotationStatus, AnnotationStatusPersist } from "@annotation-service/core/model/annotation-status.model";
import { Annotation, AnnotationPersist } from "@annotation-service/core/model/annotation.model";
import { AnnotationLookup } from "@annotation-service/core/query/annotation.lookup";
import { ConfigurationService } from "@app/core/services/configuration/configuration.service";
import { BaseHttpV2Service } from "@app/core/services/http/base-http-v2.service";
import { QueryResult } from "@common/model/query-result";
import { Guid } from "@common/types/guid";
import { Observable, throwError } from "rxjs";
import { catchError } from "rxjs/operators";

@Injectable()
export class AnnotationService {
  private get apiBase(): string { return `${this.installationConfiguration.annotationServiceAddress}api/annotation`; }

	constructor(
		private installationConfiguration: ConfigurationService,
		private http: BaseHttpV2Service
	) { }

	query(q: AnnotationLookup): Observable<QueryResult<Annotation>> {
		const url = `${this.apiBase}/query`;

		return this.http
			.post<QueryResult<Annotation>>(url, q).pipe(
				catchError((error: any) => throwError(error)));
	}

	getSingle(id: Guid, reqFields: string[] = []): Observable<Annotation> {
		const url = `${this.apiBase}/${id}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<Annotation>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	persist(item: AnnotationPersist) {
		const url = `${this.apiBase}/persist`;

		return this.http
			.post<Annotation>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	persistStatus(item: AnnotationStatusPersist) {
		const url = `${this.apiBase}/persist-status`;

		return this.http
			.post<AnnotationStatus>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	delete(id: Guid, ): Observable<void> {
		const url = `${this.apiBase}/${id}`;
		return this.http
			.delete<void>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

}