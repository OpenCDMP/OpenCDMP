import { HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { BaseHttpV2Service } from "../http/base-http-v2.service";
import { ConfigurationService } from "../configuration/configuration.service";
import { PlanStatusLookup } from "@app/core/query/plan-status.lookup";
import { PlanStatus } from "@app/core/model/plan-status/plan-status";
import { QueryResult } from "@common/model/query-result";
import { catchError, map, Observable, throwError } from "rxjs";
import { PlanStatusPersist } from "@app/core/model/plan-status/plan-status-persist";
import { Guid } from "@common/types/guid";
import { FilterService } from "@common/modules/text-filter/filter-service";
import { IsActive } from "@notification-service/core/enum/is-active.enum";
import { nameof } from "ts-simple-nameof";
import { PlanStatusEnum } from "@app/core/common/enum/plan-status";
import { SingleAutoCompleteConfiguration } from "@app/library/auto-complete/single/single-auto-complete-configuration";
import { MultipleAutoCompleteConfiguration } from "@app/library/auto-complete/multiple/multiple-auto-complete-configuration";

@Injectable()
export class PlanStatusService {
	private headers = new HttpHeaders();

	constructor(
		private http: BaseHttpV2Service,
		private configurationService: ConfigurationService,
        private filterService: FilterService
	) {
	}

	private get apiBase(): string { return `${this.configurationService.server}plan-status`; }

    query(q: PlanStatusLookup): Observable<QueryResult<PlanStatus>> {
		const url = `${this.apiBase}/query`;
		return this.http.post<QueryResult<PlanStatus>>(url, q).pipe(catchError((error: any) => throwError(error)));
	}

    getSingle(id: Guid, reqFields: string[] = []): Observable<PlanStatus> {
		const url = `${this.apiBase}/${id}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<PlanStatus>(url, options).pipe(
				catchError((error: any) => throwError(() => error)));
	}

    persist(item: PlanStatusPersist): Observable<PlanStatus> {
		const url = `${this.apiBase}/persist`;

		return this.http
			.post<PlanStatus>(url, item).pipe(
				catchError((error: any) => throwError(() => error)));
	}

	delete(id: Guid): Observable<PlanStatus> {
		const url = `${this.apiBase}/${id}`;

		return this.http
			.delete<PlanStatus>(url).pipe(
				catchError((error: any) => throwError(() => error)));
	}

	// tslint:disable-next-line: member-ordering
	singleAutocompleteConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: (data?: any) => this.query(this.buildAutocompleteLookup([IsActive.Active])).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, data?: any) => this.query(this.buildAutocompleteLookup([IsActive.Active], searchQuery)).pipe(map(x => x.items)),
		getSelectedItem: (selectedItem: any) => this.query(this.buildAutocompleteLookup([IsActive.Active, IsActive.Inactive], null, null, [selectedItem])).pipe(map(x => x.items[0])),
		displayFn: (item: PlanStatus) => item.name,
		titleFn: (item: PlanStatus) => item.name,
		valueAssign: (item: PlanStatus) => item.id,
	};

	// tslint:disable-next-line: member-ordering
	multipleAutocompleteConfiguration: MultipleAutoCompleteConfiguration = {
		initialItems: (excludedItems: any[], data?: any) => this.query(this.buildAutocompleteLookup([IsActive.Active], null, excludedItems ? excludedItems : null)).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, excludedItems: any[]) => this.query(this.buildAutocompleteLookup([IsActive.Active],searchQuery, excludedItems)).pipe(map(x => x.items)),
		getSelectedItems: (selectedItems: any[]) => this.query(this.buildAutocompleteLookup([IsActive.Active, IsActive.Inactive], null, null, selectedItems)).pipe(map(x => x.items)),
		displayFn: (item: PlanStatus) => item.name,
		titleFn: (item: PlanStatus) => item.name,
		valueAssign: (item: PlanStatus) => item.id,
	};

	public buildAutocompleteLookup(isActive: IsActive[], like?: string, excludedIds?: Guid[], ids?: Guid[]): PlanStatusLookup {
		const lookup: PlanStatusLookup = new PlanStatusLookup();
		lookup.page = { size: 100, offset: 0 };
		if (excludedIds && excludedIds.length > 0) { lookup.excludedIds = excludedIds; }
		if (ids && ids.length > 0) { lookup.ids = ids; }
		lookup.isActive = isActive;
		lookup.project = {
			fields: [
				nameof<PlanStatus>(x => x.id),
				nameof<PlanStatus>(x => x.name)
			]
		};
		lookup.order = { items: [nameof<PlanStatus>(x => x.name)] };
		if (like) { lookup.like = this.filterService.transformLike(like); }
		return lookup;
	}

    buildLookup(params: {
        like?: string, 
        excludedIds?: Guid[], 
        ids?: Guid[],
        lookupFields?: string[],
        size?: number,
        order?: string[],
        internalStatuses?: PlanStatusEnum[]
    }): PlanStatusLookup {
        const {like, excludedIds, ids, lookupFields, size = 100, order, internalStatuses} = params;
        const lookup = new PlanStatusLookup();
        lookup.isActive = [IsActive.Active];

        lookup.order = { items: order ?? [nameof<PlanStatus>(x => x.name)] };
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