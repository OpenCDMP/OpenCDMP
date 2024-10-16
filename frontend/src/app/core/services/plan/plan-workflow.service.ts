import { HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { BaseHttpV2Service } from "../http/base-http-v2.service";
import { ConfigurationService } from "../configuration/configuration.service";
import { PlanWorkflowPersist } from "@app/core/model/workflow/plan-workflow-persist";
import { PlanWorkflow } from "@app/core/model/workflow/plan-workflow";
import { catchError, map, Observable, of, throwError } from "rxjs";
import { PlanWorkflowLookup } from "@app/core/query/plan-workflow.lookup";
import { QueryResult } from "@common/model/query-result";
import { Guid } from "@common/types/guid";

@Injectable()
export class PlanWorkflowService {
	private headers = new HttpHeaders();

	constructor(
		private http: BaseHttpV2Service,
		private configurationService: ConfigurationService,
	) {
	}

	private get apiBase(): string { return `${this.configurationService.server}plan-workflow`; }

    getCurrent(reqFields: string[] = []): Observable<PlanWorkflow> {
        const url = `${this.apiBase}/current-tenant`;
        const options = { params: { f: reqFields } };
        return this.http
			.get<PlanWorkflow>(url, options).pipe(
				catchError((error: any) => throwError(() => error)));
    }

    query(q: PlanWorkflowLookup): Observable<QueryResult<PlanWorkflow>> {
		const url = `${this.apiBase}/query`;
		return this.http.post<QueryResult<PlanWorkflow>>(url, q).pipe(
            catchError((error: any) => throwError(() => error))
        );
	}


    persist(item: PlanWorkflowPersist): Observable<PlanWorkflow> {
		const url = `${this.apiBase}/persist`;

		return this.http
			.post<PlanWorkflow>(url, item).pipe(
				catchError((error: any) => throwError(() => error)));
	}

    delete(id: Guid): Observable<void> {
		const url = `${this.apiBase}/${id}`;

		return this.http
			.delete<void>(url).pipe(
				catchError((error: any) => throwError(() => error)));
	}
}