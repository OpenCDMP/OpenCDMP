import { Injectable } from '@angular/core';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { Tenant, TenantPersist } from '@app/core/model/tenant/tenant';
import { TenantLookup } from '@app/core/query/tenant.lookup';
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
export class TenantService {

	constructor(private http: BaseHttpV2Service, private configurationService: ConfigurationService, private filterService: FilterService) {
	}

	private get apiBase(): string { return `${this.configurationService.server}tenant`; }

	query(q: TenantLookup): Observable<QueryResult<Tenant>> {
		const url = `${this.apiBase}/query`;
		return this.http.post<QueryResult<Tenant>>(url, q).pipe(catchError((error: any) => throwError(error)));
	}

	getSingle(id: Guid, reqFields: string[] = []): Observable<Tenant> {
		const url = `${this.apiBase}/${id}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<Tenant>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	persist(item: TenantPersist): Observable<Tenant> {
		const url = `${this.apiBase}/persist`;

		return this.http
			.post<Tenant>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	delete(id: Guid): Observable<Tenant> {
		const url = `${this.apiBase}/${id}`;

		return this.http
			.delete<Tenant>(url).pipe(
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
		displayFn: (item: Tenant) => item.name,
		titleFn: (item: Tenant) => item.name,
		valueAssign: (item: Tenant) => item.id,
	};

	// tslint:disable-next-line: member-ordering
	multipleAutocompleteConfiguration: MultipleAutoCompleteConfiguration = {
		initialItems: (excludedItems: any[], data?: any) => this.query(this.buildAutocompleteLookup(null, excludedItems ? excludedItems : null)).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, excludedItems: any[]) => this.query(this.buildAutocompleteLookup(searchQuery, excludedItems)).pipe(map(x => x.items)),
		getSelectedItems: (selectedItems: any[]) => this.query(this.buildAutocompleteLookup(null, null, selectedItems)).pipe(map(x => x.items)),
		displayFn: (item: Tenant) => item.name,
		titleFn: (item: Tenant) => item.name,
		valueAssign: (item: Tenant) => item.id,
	};

	private buildAutocompleteLookup(like?: string, excludedIds?: Guid[], ids?: Guid[]): TenantLookup {
		const lookup: TenantLookup = new TenantLookup();
		lookup.page = { size: 100, offset: 0 };
		if (excludedIds && excludedIds.length > 0) { lookup.excludedIds = excludedIds; }
		if (ids && ids.length > 0) { lookup.ids = ids; }
		lookup.isActive = [IsActive.Active];
		lookup.project = {
			fields: [
				nameof<Tenant>(x => x.id),
				nameof<Tenant>(x => x.name)
			]
		};
		lookup.order = { items: [nameof<Tenant>(x => x.name)] };
		if (like) { lookup.like = this.filterService.transformLike(like); }
		return lookup;
	}
}