import { HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ConfigurationService } from '../configuration/configuration.service';
import { BaseHttpV2Service } from '../http/base-http-v2.service';
import { EvaluationLookup } from '@app/core/model/evaluation/evaluation.lookup';
import { Evaluation } from '@app/core/model/evaluation/evaluation';
import { QueryResult } from '@common/model/query-result';
import { Observable, catchError, throwError } from 'rxjs';
import { Guid } from '@common/types/guid';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { nameof } from 'ts-simple-nameof';
import { EvaluationData } from '@app/core/model/evaluation/evaluation-data';
import { RankConfig } from '@app/core/model/evaluator/rank-config';
import { EvaluationResultMessageModel, EvaluationResultMetricModel, EvaluationResultModel, RankResultModel } from '@app/core/model/evaluator/evaluator-plan-model.model';
import { SelectionConfiguration } from '@app/core/model/evaluator/evaluator-selection';
import { ValueSet } from '@app/core/model/evaluator/evaluator-value-set';

@Injectable({
  providedIn: 'root'
})
export class EvaluationService {
    private headers = new HttpHeaders();

    constructor(
        private http: BaseHttpV2Service,
        private configurationService: ConfigurationService,
    ) {
    }

    private get apiBase(): string { return `${this.configurationService.server}evaluation`; }

    query(q: EvaluationLookup): Observable<QueryResult<Evaluation>> {
        const url = `${this.apiBase}/query`;
        return this.http.post<QueryResult<Evaluation>>(url, q).pipe(catchError((error: any) => throwError(error)));
    }

    getSingle(id: Guid, reqFields: string[] = []): Observable<Evaluation> {
        const url = `${this.apiBase}/${id}`;
        const options = { params: { f: reqFields } };

        return this.http
            .get<Evaluation>(url, options).pipe(
                catchError((error: any) => throwError(error)));
    }

    public buildEvaluationLookup(params: Partial<EvaluationLookup>): EvaluationLookup {
        const lookup: EvaluationLookup = new EvaluationLookup();
        lookup.page = { size: 100, offset: 0 };
        lookup.metadata = { countAll: true };
        if(params?.excludedIds) { lookup.excludedIds = params.excludedIds }
        if(params?.entityIds) { lookup.entityIds = params.entityIds }
        if(params?.ids) { lookup.ids = params.ids }
        if(params?.status) { lookup.status = params.status }
        if(params?.createdByIds) { lookup.createdByIds = params.createdByIds }
        if(params?.entityTypes) { lookup.entityTypes = params.entityTypes }
        lookup.isActive = params?.isActive ?? [IsActive.Active];
        lookup.project = params?.project ?? {
            fields: [
                nameof<Evaluation>(x => x.id),
                nameof<Evaluation>(x => x.evaluatedAt),
                nameof<Evaluation>(x => x.status),
                [nameof<Evaluation>(x => x.data), nameof<EvaluationData>(x => x.evaluatorId)].join('.'),
                [nameof<Evaluation>(x => x.data), nameof<EvaluationData>(x => x.rankConfig), nameof<RankConfig>(x => x.rankType)].join('.'),
                [nameof<Evaluation>(x => x.data), nameof<EvaluationData>(x => x.rankConfig), nameof<RankConfig>(x => x.selectionConfiguration)].join('.'),
                [nameof<Evaluation>(x => x.data), nameof<EvaluationData>(x => x.rankConfig), nameof<RankConfig>(x => x.selectionConfiguration), nameof<SelectionConfiguration>(x => x.valueSetList)].join('.'),
                [nameof<Evaluation>(x => x.data), nameof<EvaluationData>(x => x.rankConfig), nameof<RankConfig>(x => x.selectionConfiguration), nameof<SelectionConfiguration>(x => x.valueSetList), nameof<ValueSet>(x => x.key)].join('.'),
                [nameof<Evaluation>(x => x.data), nameof<EvaluationData>(x => x.rankConfig), nameof<RankConfig>(x => x.selectionConfiguration), nameof<SelectionConfiguration>(x => x.valueSetList), nameof<ValueSet>(x => x.successStatus)].join('.'),
                [nameof<Evaluation>(x => x.data), nameof<EvaluationData>(x => x.rankResult), nameof<RankResultModel>(x => x.details)].join('.'),
                [nameof<Evaluation>(x => x.data), nameof<EvaluationData>(x => x.rankResult), nameof<RankResultModel>(x => x.rank)].join('.'),
                [nameof<Evaluation>(x => x.data), nameof<EvaluationData>(x => x.rankResult), nameof<RankResultModel>(x => x.results), nameof<EvaluationResultModel>(x => x.rank)].join('.'),
                [nameof<Evaluation>(x => x.data), nameof<EvaluationData>(x => x.rankResult), nameof<RankResultModel>(x => x.results), nameof<EvaluationResultModel>(x => x.benchmarkTitle)].join('.'),
                [nameof<Evaluation>(x => x.data), nameof<EvaluationData>(x => x.rankResult), nameof<RankResultModel>(x => x.results), nameof<EvaluationResultModel>(x => x.benchmarkDetails)].join('.'),
                [nameof<Evaluation>(x => x.data), nameof<EvaluationData>(x => x.rankResult), nameof<RankResultModel>(x => x.results), nameof<EvaluationResultModel>(x => x.metrics), nameof<EvaluationResultMetricModel>(x => x.rank)].join('.'),
                [nameof<Evaluation>(x => x.data), nameof<EvaluationData>(x => x.rankResult), nameof<RankResultModel>(x => x.results), nameof<EvaluationResultModel>(x => x.metrics), nameof<EvaluationResultMetricModel>(x => x.metricTitle)].join('.'),
                [nameof<Evaluation>(x => x.data), nameof<EvaluationData>(x => x.rankResult), nameof<RankResultModel>(x => x.results), nameof<EvaluationResultModel>(x => x.metrics), nameof<EvaluationResultMetricModel>(x => x.metricDetails)].join('.'),
                [nameof<Evaluation>(x => x.data), nameof<EvaluationData>(x => x.rankResult), nameof<RankResultModel>(x => x.results), nameof<EvaluationResultModel>(x => x.metrics), nameof<EvaluationResultMetricModel>(x => x.messages), nameof<EvaluationResultMessageModel>(x => x.title)].join('.'),
                [nameof<Evaluation>(x => x.data), nameof<EvaluationData>(x => x.rankResult), nameof<RankResultModel>(x => x.results), nameof<EvaluationResultModel>(x => x.metrics), nameof<EvaluationResultMetricModel>(x => x.messages), nameof<EvaluationResultMessageModel>(x => x.message)].join('.'),
            ]
        };
        lookup.order = params?.order ??  { items: [nameof<Evaluation>(x => x.createdAt)] };
        return lookup;
    }
}
