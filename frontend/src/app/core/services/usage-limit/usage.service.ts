import { Injectable } from '@angular/core';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { UsageLimit, UsageLimitPersist } from '@app/core/model/usage-limit/usage-limit';
import { UsageLimitLookup } from '@app/core/query/usage-limit.lookup';
import { MultipleAutoCompleteConfiguration } from '@app/library/auto-complete/multiple/multiple-auto-complete-configuration';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { QueryResult } from '@common/model/query-result';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { ConfigurationService } from '../configuration/configuration.service';
import { BaseHttpV2Service } from '../http/base-http-v2.service';

@Injectable()
export class UsageLimitService {

	constructor(private http: BaseHttpV2Service, private configurationService: ConfigurationService, private filterService: FilterService) {
	}

	private get apiBase(): string { return `${this.configurationService.server}usage-limit`; }

	query(q: UsageLimitLookup): Observable<QueryResult<UsageLimit>> {
		const url = `${this.apiBase}/query`;
		return this.http.post<QueryResult<UsageLimit>>(url, q).pipe(catchError((error: any) => throwError(error)));
	}

	getSingle(id: Guid, reqFields: string[] = []): Observable<UsageLimit> {
		const url = `${this.apiBase}/${id}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<UsageLimit>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	persist(item: UsageLimitPersist): Observable<UsageLimit> {
		const url = `${this.apiBase}/persist`;

		return this.http
			.post<UsageLimit>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	delete(id: Guid): Observable<UsageLimit> {
		const url = `${this.apiBase}/${id}`;

		return this.http
			.delete<UsageLimit>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

	//
	// Autocomplete Commons
	//
	// tslint:disable-next-line: member-ordering
	singleAutocompleteConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: (data?: any) => this.query(this.buildAutocompleteLookup()).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, data?: any) => this.query(this.buildAutocompleteLookup(searchQuery)).pipe(map(x => x.items)),
		getSelectedItem: (selectedItem: any) => this.query(this.buildAutocompleteLookup(null, null, [selectedItem])).pipe(map(x => x.items[0])),
		displayFn: (item: UsageLimit) => item.label,
		titleFn: (item: UsageLimit) => item.label,
		valueAssign: (item: UsageLimit) => item.id,
	};

	// tslint:disable-next-line: member-ordering
	multipleAutocompleteConfiguration: MultipleAutoCompleteConfiguration = {
		initialItems: (excludedItems: any[], data?: any) => this.query(this.buildAutocompleteLookup(null, excludedItems ? excludedItems : null)).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, excludedItems: any[]) => this.query(this.buildAutocompleteLookup(searchQuery, excludedItems)).pipe(map(x => x.items)),
		getSelectedItems: (selectedItems: any[]) => this.query(this.buildAutocompleteLookup(null, null, selectedItems)).pipe(map(x => x.items)),
		displayFn: (item: UsageLimit) => item.label,
		titleFn: (item: UsageLimit) => item.label,
		valueAssign: (item: UsageLimit) => item.id,
	};

	private buildAutocompleteLookup(like?: string, excludedIds?: Guid[], ids?: Guid[]): UsageLimitLookup {
		const lookup: UsageLimitLookup = new UsageLimitLookup();
		lookup.page = { size: 100, offset: 0 };
		if (excludedIds && excludedIds.length > 0) { lookup.excludedIds = excludedIds; }
		if (ids && ids.length > 0) { lookup.ids = ids; }
		lookup.isActive = [IsActive.Active];
		lookup.project = {
			fields: [
				nameof<UsageLimit>(x => x.id),
				nameof<UsageLimit>(x => x.label)
			]
		};
		lookup.order = { items: [nameof<UsageLimit>(x => x.label)] };
		if (like) { lookup.like = this.filterService.transformLike(like); }
		return lookup;
	}
}