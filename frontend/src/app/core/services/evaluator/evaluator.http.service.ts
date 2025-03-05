import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BaseService } from '@common/base/base.service';
import { Guid } from '@common/types/guid';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ConfigurationService } from '../configuration/configuration.service';
import { BaseHttpV2Service } from '../http/base-http-v2.service';
import { EvaluatorFormat } from '@app/core/model/evaluator/evaluator-format.model';
import { RankModel } from '@app/core/model/evaluator/evaluator-plan-model.model';


@Injectable()
export class EvaluatorHttpService extends BaseService {

	private headers = new HttpHeaders();

	constructor(
		private http: BaseHttpV2Service,
		private configurationService: ConfigurationService
	) { super(); }

	private get apiBase(): string { return `${this.configurationService.server}evaluator`; }

	getAvailableConfigurations(): Observable<EvaluatorFormat[]> {
		const url = `${this.apiBase}/available`;
		return this.http.get<EvaluatorFormat[]>(url).pipe(catchError((error: any) => throwError(error)));
	}

	rankPlan(id: Guid, evaluatorId: string, format: string): Observable<HttpResponse<RankModel>> {
		const url = `${this.apiBase}/rank-plan`;
		return this.http.post<HttpResponse<RankModel>>(url, {id: id, evaluatorId: evaluatorId, format: format}, {responseType: 'json', observe: 'response'}).pipe(catchError((error: any) => throwError(error)));
	}

	rankDescription(id: Guid, evaluatorId: string, format: string): Observable<HttpResponse<RankModel>> {
		const url = `${this.apiBase}/rank-description`;
		return this.http.post<HttpResponse<RankModel>>(url, {id: id, evaluatorId: evaluatorId, format: format}, {responseType: 'json', observe: 'response'}).pipe(catchError((error: any) => throwError(error)));
	}

	getLogo(evaluatorId: string): Observable<HttpResponse<string>>{
		const url = `${this.apiBase}/${evaluatorId}/logo`;
		return this.http.get(url, { responseType: 'string', observe: 'response' });
	}
}