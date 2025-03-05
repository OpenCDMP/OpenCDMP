import { HttpHeaders, HttpResponse } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { BaseHttpParams } from "@common/http/base-http-params";
import { InterceptorType } from "@common/http/interceptors/interceptor-type";
import { QueryResult } from "@common/model/query-result";
import { AggregationMetricSortField, Guid } from "@citesa/kpi-client/types";
import { AggregateResponseModel, ElasticValuesResponse, IndicatorPointDistinctLookup, IndicatorPointLookup, IndicatorPointReportLookup } from "@citesa/kpi-client/types";
import { Observable, of, throwError } from "rxjs";
import { catchError, tap } from "rxjs/operators";
import { BaseHttpV2Service } from "../../app/core/services/http/base-http-v2.service";
import { ConfigurationService } from "../../app/core/services/configuration/configuration.service";

@Injectable()
export class IndicatorPointService {
    constructor(
		private installationConfiguration: ConfigurationService,
        private http: BaseHttpV2Service,
    ) { }

    private get apiBase(): string { return `${this.installationConfiguration.kpiServiceAddress}api/indicator-point`; }


    public getIndicatorPointReport(id: Guid | string, lookup: IndicatorPointReportLookup, skipLoader?: boolean,): Observable<AggregateResponseModel> {


        let _id = id;

        if (_id === 'inherited') {
            return of({
                items: [],
                total: 0,
            })
        }

        const url = `${this.apiBase}/${_id}/report`;

        const params = new BaseHttpParams();
        if (skipLoader) {
            params.interceptorContext = {
                excludedInterceptors: [InterceptorType.ProgressIndication]
            };
        }
       
        if (lookup?.bucket?.bucketSort?.sortFields) {
            lookup?.bucket?.bucketSort?.sortFields.forEach(x => {
                // TODO: remove this logic that order has value "DESC" and make it "Desc"
                if (x.order == null || x.order.endsWith("DESC") ) {
                    x.order = AggregationMetricSortField.Order.Descending
                } else if (x.order.endsWith("ASC")) {
                    x.order = AggregationMetricSortField.Order.Ascending
                }
            })
        }

        return this.http.post<AggregateResponseModel>(url, lookup, { params })
        .pipe(
            tap((response) =>{
            } ),
            catchError((error: any) => throwError(error))
        );
    }


    public exportXlsx(id: Guid | string, lookup: IndicatorPointReportLookup, skipLoader?: boolean,): Observable<HttpResponse<Blob>> {
        const url = `${this.apiBase}/${id}/export-xlsx`;
        const params = new BaseHttpParams();
        if (skipLoader) {
            params.interceptorContext = {
                excludedInterceptors: [InterceptorType.ProgressIndication],
            };
        }
        return this.http.post<HttpResponse<Blob>>(url, lookup, { params, responseType: 'blob', observe: 'response'   }).pipe(
            catchError((error: any) => throwError(error)));;
    }
    public exportJSON(id: Guid | string, lookup: IndicatorPointReportLookup, skipLoader?: boolean,): Observable<HttpResponse<Blob>> {
        const url = `${this.apiBase}/${id}/export-json`;
        const params = new BaseHttpParams();
        if (skipLoader) {
            params.interceptorContext = {
                excludedInterceptors: [InterceptorType.ProgressIndication],
            };
        }
        return this.http.post<HttpResponse<Blob>>(url, lookup, { params, responseType: 'blob', observe: 'response'   }).pipe(
            catchError((error: any) => throwError(error)));;
    }

    public getIndicatorPointQueryDistinct(lookup: IndicatorPointDistinctLookup): Observable<ElasticValuesResponse<string>> {
        const url = `${this.apiBase}/query-distinct`;
        return this.http
            .post<ElasticValuesResponse<string>>(url, lookup).pipe(
                catchError((error: any) => throwError(error)));

    }

    public query(lookup: IndicatorPointLookup):Observable<QueryResult<Record<string, string>>>{
        const url = `${this.apiBase}/query`;

        return this.http.post<QueryResult<Record<string, string>>>(url, lookup).pipe(
            catchError((error: any) => throwError(error)));;
    }

    public getGlobalSearchConfig(key: string):Observable<string>{
        const url = `${this.apiBase}/global-search-config-by-key/${key}`;

        return this.http.get<string>(url).pipe(
            catchError((error: any) => throwError(error)));;
    }
}