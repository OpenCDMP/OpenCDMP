import { Injectable } from '@angular/core';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { Language, LanguagePersist } from '@app/core/model/language/language';
import { LanguageLookup } from '@app/core/query/language.lookup';
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
import { InterceptorType } from '@common/http/interceptors/interceptor-type';
import { BaseHttpParams } from '@common/http/base-http-params';
import { HttpParamsOptions } from '@angular/common/http';

@Injectable()
export class LanguageHttpService {

	constructor(private http: BaseHttpV2Service, private configurationService: ConfigurationService, private filterService: FilterService) {
	}

	private get apiBase(): string { return `${this.configurationService.server}language`; }

	query(q: LanguageLookup): Observable<QueryResult<Language>> {
		const url = `${this.apiBase}/query`;
		return this.http.post<QueryResult<Language>>(url, q).pipe(catchError((error: any) => throwError(error)));
	}

	getSingle(id: Guid, reqFields: string[] = []): Observable<Language> {
		const url = `${this.apiBase}/${id}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<Language>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	getSingleWithCode(code: string, tenantCode: string | null, reqFields: string[] = []): Observable<Language> {
		let url = `${this.apiBase}/public/code/${code}`;
		if (tenantCode) url += `/${tenantCode}`;
		const options: HttpParamsOptions = { fromObject: { f: reqFields } };

		let params: BaseHttpParams = new BaseHttpParams(options);
		params.interceptorContext = {
			excludedInterceptors: [InterceptorType.AuthToken,
			]
		};

		return this.http
			.get<Language>(url, { params: params }).pipe(
				catchError((error: any) => throwError(error)));
	}

	queryAvailableCodes(q: LanguageLookup): Observable<QueryResult<string>> {
		const url = `${this.apiBase}/public/available-languages`;
		const params = new BaseHttpParams();
		params.interceptorContext = {
			excludedInterceptors: [InterceptorType.AuthToken,
				InterceptorType.TenantHeaderInterceptor]
		};
		return this.http.post<QueryResult<string>>(url, q, { params: params }).pipe(catchError((error: any) => throwError(error)));
	}

	persist(item: LanguagePersist): Observable<Language> {
		const url = `${this.apiBase}/persist`;

		return this.http
			.post<Language>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	delete(id: Guid): Observable<Language> {
		const url = `${this.apiBase}/${id}`;

		return this.http
			.delete<Language>(url).pipe(
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
		displayFn: (item: Language) => item.code,
		titleFn: (item: Language) => item.code,
		valueAssign: (item: Language) => item.id,
	};

	// tslint:disable-next-line: member-ordering
	multipleAutocompleteConfiguration: MultipleAutoCompleteConfiguration = {
		initialItems: (excludedItems: any[], data?: any) => this.query(this.buildAutocompleteLookup(null, excludedItems ? excludedItems : null)).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, excludedItems: any[]) => this.query(this.buildAutocompleteLookup(searchQuery, excludedItems)).pipe(map(x => x.items)),
		getSelectedItems: (selectedItems: any[]) => this.query(this.buildAutocompleteLookup(null, null, selectedItems)).pipe(map(x => x.items)),
		displayFn: (item: Language) => item.code,
		titleFn: (item: Language) => item.code,
		valueAssign: (item: Language) => item.id,
	};

	public buildAutocompleteLookup(like?: string, excludedIds?: Guid[], ids?: Guid[]): LanguageLookup {
		const lookup: LanguageLookup = new LanguageLookup();
		lookup.page = { size: 100, offset: 0 };
		if (excludedIds && excludedIds.length > 0) { lookup.excludedIds = excludedIds; }
		if (ids && ids.length > 0) { lookup.ids = ids; }
		lookup.isActive = [IsActive.Active];
		lookup.project = {
			fields: [
				nameof<Language>(x => x.id),
				nameof<Language>(x => x.code)
			]
		};
		lookup.order = { items: [nameof<Language>(x => x.ordinal)] };
		if (like) { lookup.like = this.filterService.transformLike(like); }
		return lookup;
	}
}
