import { Injectable } from '@angular/core';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { DescriptionPrefillingRequest, PrefillingSearchRequest } from '@app/core/model/description-prefilling-request/description-prefilling-request';
import { Description } from '@app/core/model/description/description';
import { Prefilling, PrefillingSource, PrefillingSourcePersist } from '@app/core/model/prefilling-source/prefilling-source';
import { PrefillingSourceLookup } from '@app/core/query/prefilling-source.lookup';
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
export class PrefillingSourceService {

	constructor(
		private http: BaseHttpV2Service,
		private configurationService: ConfigurationService,
		private filterService: FilterService
	) {
	}

	private get apiBase(): string { return `${this.configurationService.server}prefilling-source`; }

	query(q: PrefillingSourceLookup): Observable<QueryResult<PrefillingSource>> {
		const url = `${this.apiBase}/query`;
		return this.http.post<QueryResult<PrefillingSource>>(url, q).pipe(catchError((error: any) => throwError(error)));
	}

	getSingle(id: Guid, reqFields: string[] = []): Observable<PrefillingSource> {
		const url = `${this.apiBase}/${id}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<PrefillingSource>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	persist(item: PrefillingSourcePersist): Observable<PrefillingSource> {
		const url = `${this.apiBase}/persist`;

		return this.http
			.post<PrefillingSource>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	delete(id: Guid): Observable<PrefillingSource> {
		const url = `${this.apiBase}/${id}`;

		return this.http
			.delete<PrefillingSource>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

	search(item: PrefillingSearchRequest): Observable<Prefilling[]> {
		const url = `${this.apiBase}/search`;

		return this.http
			.post<Prefilling[]>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	generate(item: DescriptionPrefillingRequest, reqFields: string[] = []): Observable<Description> {
		const url = `${this.apiBase}/generate`;
		const options = { params: { f: reqFields } };

		return this.http
			.post<Description>(url, item, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	//
	// Autocomplete Commons
	//
	//
	public singleAutocompleteConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: (data?: any) => this.query(this.buildAutocompleteLookup()).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, data?: any) => this.query(this.buildAutocompleteLookup(searchQuery)).pipe(map(x => x.items)),
		getSelectedItem: (selectedItem: any) => this.query(this.buildAutocompleteLookup(null, null, [selectedItem])).pipe(map(x => x.items[0])),
		displayFn: (item: PrefillingSource) => item.label,
		titleFn: (item: PrefillingSource) => item.label,
		valueAssign: (item: PrefillingSource) => item.id,
	};

	public getSingleAutocompleteConfiguration(ids: Guid[]): SingleAutoCompleteConfiguration {
		return {
			initialItems: (data?: any) => this.query(this.buildAutocompleteLookup(null, null, ids && ids.length > 0 ? ids : null)).pipe(map(x => x.items)),
			filterFn: (searchQuery: string, data?: any) => this.query(this.buildAutocompleteLookup(searchQuery, null, ids && ids.length > 0 ? ids : null)).pipe(map(x => x.items)),
			getSelectedItem: (selectedItem: any) => this.query(this.buildAutocompleteLookup(null, null, [selectedItem])).pipe(map(x => x.items[0])),
			displayFn: (item: PrefillingSource) => item.label,
			titleFn: (item: PrefillingSource) => item.label,
			valueAssign: (item: PrefillingSource) => item.id,
		}
	}

	public multipleAutocompleteConfiguration: MultipleAutoCompleteConfiguration = {
		initialItems: (excludedItems: any[], data?: any) => this.query(this.buildAutocompleteLookup(null, excludedItems ? excludedItems : null)).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, excludedItems: any[]) => this.query(this.buildAutocompleteLookup(searchQuery, excludedItems)).pipe(map(x => x.items)),
		getSelectedItems: (selectedItems: any[]) => this.query(this.buildAutocompleteLookup(null, null, selectedItems)).pipe(map(x => x.items)),
		displayFn: (item: PrefillingSource) => item.label,
		titleFn: (item: PrefillingSource) => item.label,
		valueAssign: (item: PrefillingSource) => item.id,
	};

	public getMltipleAutocompleteConfiguration(ids: Guid[]): MultipleAutoCompleteConfiguration {
		return {
			initialItems: (excludedItems: any[], data?: any) => this.query(this.buildAutocompleteLookup(null, excludedItems ? excludedItems : null, ids && ids.length > 0 ? ids : null)).pipe(map(x => x.items)),
			filterFn: (searchQuery: string, excludedItems: any[]) => this.query(this.buildAutocompleteLookup(searchQuery, excludedItems, ids && ids.length > 0 ? ids : null)).pipe(map(x => x.items)),
			getSelectedItems: (selectedItems: any[]) => this.query(this.buildAutocompleteLookup(null, null, selectedItems)).pipe(map(x => x.items)),
			displayFn: (item: PrefillingSource) => item.label,
			titleFn: (item: PrefillingSource) => item.label,
			valueAssign: (item: PrefillingSource) => item.id,
		}
	}

	private buildAutocompleteLookup(like?: string, excludedIds?: Guid[], ids?: Guid[]): PrefillingSourceLookup {
		const lookup: PrefillingSourceLookup = new PrefillingSourceLookup();
		lookup.page = { size: 100, offset: 0 };
		if (excludedIds && excludedIds.length > 0) { lookup.excludedIds = excludedIds; }
		if (ids && ids.length > 0) { lookup.ids = ids; }
		lookup.isActive = [IsActive.Active];
		lookup.project = {
			fields: [
				nameof<PrefillingSource>(x => x.id),
				nameof<PrefillingSource>(x => x.label)
			]
		};
		lookup.order = { items: [nameof<PrefillingSource>(x => x.label)] };
		if (like) { lookup.like = this.filterService.transformLike(like); }
		return lookup;
	}

}
