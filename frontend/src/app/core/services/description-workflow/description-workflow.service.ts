import { HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { DescriptionWorkflow } from "@app/core/model/workflow/description-workflow";
import { DescriptionWorkflowPersist } from "@app/core/model/workflow/description-workflow-persist";
import { QueryResult } from "@common/model/query-result";
import { Guid } from "@common/types/guid";
import { Observable, catchError, throwError } from "rxjs";
import { ConfigurationService } from "../configuration/configuration.service";
import { BaseHttpV2Service } from "../http/base-http-v2.service";
import { DescriptionWorkflowLookup } from "@app/core/query/description-workflow.lookup";

@Injectable()
export class DescriptionWorkflowService {
	private headers = new HttpHeaders();

	constructor(
		private http: BaseHttpV2Service,
		private configurationService: ConfigurationService,
	) {
	}

	private get apiBase(): string { return `${this.configurationService.server}description-workflow`; }

    getCurrent(reqFields: string[] = []): Observable<DescriptionWorkflow> {
        const url = `${this.apiBase}/current-tenant`;
        const options = { params: { f: reqFields } };
        return this.http
			.get<DescriptionWorkflow>(url, options).pipe(
				catchError((error: any) => throwError(() => error)));
    }

    query(q: DescriptionWorkflowLookup): Observable<QueryResult<DescriptionWorkflow>> {
		const url = `${this.apiBase}/query`;
		return this.http.post<QueryResult<DescriptionWorkflow>>(url, q).pipe(
            catchError((error: any) => throwError(() => error))
        );
	}


    persist(item: DescriptionWorkflowPersist): Observable<DescriptionWorkflow> {
		const url = `${this.apiBase}/persist`;

		return this.http
			.post<DescriptionWorkflow>(url, item).pipe(
				catchError((error: any) => throwError(() => error)));
	}

    delete(id: Guid): Observable<void> {
		const url = `${this.apiBase}/${id}`;

		return this.http
			.delete<void>(url).pipe(
				catchError((error: any) => throwError(() => error)));
	}
}