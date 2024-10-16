import { HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { BaseHttpV2Service } from "../http/base-http-v2.service";
import { ConfigurationService } from "../configuration/configuration.service";
import { QueryResult } from "@common/model/query-result";
import { catchError, Observable, throwError } from "rxjs";
import { Guid } from "@common/types/guid";
import { DescriptionStatusLookup } from "@app/core/query/description-status.lookup";
import { DescriptionStatus } from "@app/core/model/description-status/description-status";
import { DescriptionStatusPersist } from "@app/core/model/description/description";
import { IsActive } from "@app/core/common/enum/is-active.enum";
import { nameof } from "ts-simple-nameof";
import { FilterService } from "@common/modules/text-filter/filter-service";
import { DescriptionStatusEnum } from "@app/core/common/enum/description-status";

@Injectable()
export class DescriptionStatusService {
	private headers = new HttpHeaders();

	constructor(
		private http: BaseHttpV2Service,
		private configurationService: ConfigurationService,
        private filterService: FilterService,
	) {
	}

	private get apiBase(): string { return `${this.configurationService.server}description-status`; }

    query(q: DescriptionStatusLookup): Observable<QueryResult<DescriptionStatus>> {
		const url = `${this.apiBase}/query`;
		return this.http.post<QueryResult<DescriptionStatus>>(url, q).pipe(catchError((error: any) => throwError(() => error)));
	}

    getSingle(id: Guid, reqFields: string[] = []): Observable<DescriptionStatus> {
		const url = `${this.apiBase}/${id}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<DescriptionStatus>(url, options).pipe(
				catchError((error: any) => throwError(() => error)));
	}

    persist(item: DescriptionStatusPersist): Observable<DescriptionStatus> {
		const url = `${this.apiBase}/persist`;

		return this.http
			.post<DescriptionStatus>(url, item).pipe(
				catchError((error: any) => throwError(() => error)));
	}

	delete(id: Guid): Observable<DescriptionStatus> {
		const url = `${this.apiBase}/${id}`;

		return this.http
			.delete<DescriptionStatus>(url).pipe(
				catchError((error: any) => throwError(() => error)));
	}

    buildLookup(params: {
        like?: string, 
        excludedIds?: Guid[], 
        ids?: Guid[],
        lookupFields?: string[],
        size?: number,
        order?: string[],
        internalStatuses?: DescriptionStatusEnum[] 
    }): DescriptionStatusLookup {
        const {like, excludedIds, ids, lookupFields, size = 100, order, internalStatuses} = params;
        const lookup = new DescriptionStatusLookup();
        lookup.isActive = [IsActive.Active];

        lookup.order = { items: order ?? [nameof<DescriptionStatus>(x => x.name)] };
        lookup.page = { size, offset: 0 };
        
        if(internalStatuses?.length){ lookup.internalStatuses = internalStatuses };
		if (excludedIds && excludedIds.length > 0) { lookup.excludedIds = excludedIds; }
		if (ids && ids.length > 0) { lookup.ids = ids; }
        if (like) { lookup.like = this.filterService.transformLike(like); }
        if(lookupFields){
            lookup.project = {
                fields: lookupFields
            }
        };
        return lookup;
    }
}