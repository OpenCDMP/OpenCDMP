import { Injectable } from '@angular/core';
import { QueryResult } from '@common/model/query-result';
import { Guid } from '@common/types/guid';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ConfigurationService } from '../configuration/configuration.service';
import { BaseHttpV2Service } from '../http/base-http-v2.service';
import { PlanSuggestion, PlanUpdateRequest, PlanUpdateRequestPersist } from '@app/core/model/plan-update-request/plan-update-request';
import { PlanUpdateRequestLookup } from '@app/core/query/plan-update-request.lookup';
import { PreprocessingPlanModel } from '@app/core/model/plan/plan-import';
import { Plan } from '@app/core/model/plan/plan';

@Injectable()
export class PlanUpdateRequestService {

	constructor(
		private http: BaseHttpV2Service,
		private configurationService: ConfigurationService,
	) {
	}

	private get apiBase(): string { return `${this.configurationService.server}plan-update-request`; }

	query(q: PlanUpdateRequestLookup): Observable<QueryResult<PlanUpdateRequest>> {
		const url = `${this.apiBase}/query`;
		return this.http.post<QueryResult<PlanUpdateRequest>>(url, q).pipe(catchError((error: any) => throwError(error)));
	}

	getSingle(id: Guid, reqFields: string[] = []): Observable<PlanUpdateRequest> {
		const url = `${this.apiBase}/${id}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<PlanUpdateRequest>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	persist(item: PlanUpdateRequestPersist): Observable<PlanUpdateRequest> {
		const url = `${this.apiBase}/persist`;

		return this.http
			.post<PlanUpdateRequest>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	delete(id: Guid): Observable<PlanUpdateRequest> {
		const url = `${this.apiBase}/${id}`;

		return this.http
			.delete<PlanUpdateRequest>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

	preprocessing(id: Guid): Observable<PreprocessingPlanModel> {
		const url = `${this.apiBase}/preprocessing/${id}`;

		return this.http
			.get<PreprocessingPlanModel>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

	buildPlan(item: PlanSuggestion): Observable<Plan> {
		const url = `${this.apiBase}/build-plan`;

		return this.http
			.post<Plan>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}
}
