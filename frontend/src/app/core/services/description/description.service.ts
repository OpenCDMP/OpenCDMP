import { HttpClient, HttpHeaders, HttpParamsOptions, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { Description, DescriptionMultiplePersist, DescriptionPersist, DescriptionSectionPermissionResolver, DescriptionStatusPersist, PublicDescription, UpdateDescriptionTemplatePersist } from '@app/core/model/description/description';
import { DescriptionLookup } from '@app/core/query/description.lookup';
import { MultipleAutoCompleteConfiguration } from '@app/library/auto-complete/multiple/multiple-auto-complete-configuration';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { QueryResult } from '@common/model/query-result';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { Observable, of, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { ConfigurationService } from '../configuration/configuration.service';
import { BaseHttpV2Service } from '../http/base-http-v2.service';
import { BaseHttpParams } from '@common/http/base-http-params';
import { InterceptorType } from '@common/http/interceptors/interceptor-type';
import { DescriptionValidationResult } from '@app/ui/plan/plan-finalize-dialog/plan-finalize-dialog.component';
import { ResolutionContext } from '../auth/auth.service';

@Injectable()
export class DescriptionService {

	private headers = new HttpHeaders();

	constructor(private http: BaseHttpV2Service, private httpClient: HttpClient, private configurationService: ConfigurationService, private filterService: FilterService) {
	}

	private get apiBase(): string { return `${this.configurationService.server}description`; }

	query(q: DescriptionLookup): Observable<QueryResult<Description>> {
		const url = `${this.apiBase}/query`;
		return this.http.post<QueryResult<Description>>(url, q).pipe(catchError((error: any) => throwError(error)));
	}

	getDescriptionSectionPermissions(q: DescriptionSectionPermissionResolver): Observable<Map<Guid, string[]>> {
		const url = `${this.apiBase}/get-description-section-permissions`;
		return this.http.post<Map<Guid, string[]>>(url, q).pipe(catchError((error: any) => throwError(error)));
	}

	publicQuery(q: DescriptionLookup): Observable<QueryResult<PublicDescription>> {
		const url = `${this.apiBase}/public/query`;
		const params = new BaseHttpParams();
		params.interceptorContext = {
			excludedInterceptors: [InterceptorType.AuthToken,
				InterceptorType.TenantHeaderInterceptor]
		};
		return this.http.post<QueryResult<PublicDescription>>(url, q, {params: params}).pipe(catchError((error: any) => throwError(error)));
	}

	getSingle(id: Guid, reqFields: string[] = []): Observable<Description> {
		const url = `${this.apiBase}/${id}`;
		const options: HttpParamsOptions = { fromObject: { f: reqFields } };

		let params: BaseHttpParams = new BaseHttpParams(options);

		return this.http
			.get<Description>(url, { params: params }).pipe(
				catchError((error: any) => throwError(error)));
	}

	getPublicSingle(id: Guid, reqFields: string[] = []): Observable<PublicDescription> {
		const url = `${this.apiBase}/public/${id}`;
		const options: HttpParamsOptions = { fromObject: { f: reqFields } };

		let params: BaseHttpParams = new BaseHttpParams(options);
		params.interceptorContext = {
			excludedInterceptors: [InterceptorType.AuthToken]
		};

		return this.http
			.get<PublicDescription>(url, { params: params }).pipe(
				catchError((error: any) => throwError(error)));
	}

	persist(item: DescriptionPersist, fieldSet: string[] = []): Observable<Description> {
		const url = `${this.apiBase}/persist`;
        const options: HttpParamsOptions = { fromObject: { f: fieldSet } };
        let params: BaseHttpParams = new BaseHttpParams(options);
		return this.http
			.post<Description>(url, item, { params: params }).pipe(
				catchError((error: any) => throwError(error)));
	}

	persistMultiple(item: DescriptionMultiplePersist, fieldSet: string[] = []): Observable<Description[]> {
		const url = `${this.apiBase}/persist-multiple`;
        const options: HttpParamsOptions = { fromObject: { f: fieldSet } };
        let params: BaseHttpParams = new BaseHttpParams(options);
		return this.http
			.post<Description[]>(url, item, { params: params }).pipe(
				catchError((error: any) => throwError(error)));
	}

	persistStatus(item: DescriptionStatusPersist, fieldSet: string[] = []): Observable<Description> {
		const url = `${this.apiBase}/persist-status`;
        const options: HttpParamsOptions = { fromObject: { f: fieldSet } };
        let params: BaseHttpParams = new BaseHttpParams(options);
		return this.http
			.post<Description>(url, item, { params: params }).pipe(
				catchError((error: any) => throwError(error)));
	}

	clone(id: Guid, reqFields: string[] = []): Observable<Description> {
		const url = `${this.apiBase}/clone/${id}`;
		const options: HttpParamsOptions = { fromObject: { f: reqFields } };

		let params: BaseHttpParams = new BaseHttpParams(options);

		return this.http
			.get<Description>(url, { params: params }).pipe(
				catchError((error: any) => throwError(error)));
	}

	delete(id: Guid): Observable<Description> {
		const url = `${this.apiBase}/${id}`;

		return this.http
			.delete<Description>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

	validate(descriptionIds: Guid[]): Observable<DescriptionValidationResult[]> {
		const url = `${this.apiBase}/validate`;
		const options = {params: { descriptionIds: descriptionIds} };

		return this.http
			.get<DescriptionValidationResult[]>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}



	downloadXML(id: Guid): Observable<HttpResponse<Blob>> {
		const url = `${this.apiBase}/xml/export/${id}`;
		let headerXml: HttpHeaders = this.headers.set('Content-Type', 'application/xml');
		const params = new BaseHttpParams();
		params.interceptorContext = {
			excludedInterceptors: [InterceptorType.JSONContentType]
		};
		return this.httpClient.get(url, { params: params, responseType: 'blob', observe: 'response', headers: headerXml });
	}

	downloadPublicXML(id: Guid): Observable<HttpResponse<Blob>> {
		const url = `${this.apiBase}/xml/export-public/${id}`;
		let headerXml: HttpHeaders = this.headers.set('Content-Type', 'application/xml');
		const params = new BaseHttpParams();
		params.interceptorContext = {
			excludedInterceptors: [InterceptorType.JSONContentType]
		};
		return this.httpClient.get(url, { params: params, responseType: 'blob', observe: 'response', headers: headerXml });
	}


	public updateDescriptionTemplate(item: UpdateDescriptionTemplatePersist): Observable<boolean> {
		const url = `${this.apiBase}/update-description-template`;

		return this.http.post<boolean>(url, item).pipe(
			catchError((error: any) => throwError(error)));
	}

	//
	// Autocomplete Commons
	//
	// tslint:disable-next-line: member-ordering
	singleAutocompleteConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: (data?: any) => this.query(this.buildAutocompleteLookup([IsActive.Active])).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, data?: any) => this.query(this.buildAutocompleteLookup([IsActive.Active], searchQuery)).pipe(map(x => x.items)),
		getSelectedItem: (selectedItem: any) => this.query(this.buildAutocompleteLookup([IsActive.Active, IsActive.Inactive], null, null, [selectedItem])).pipe(map(x => x.items[0])),
		displayFn: (item: Description) => item.label,
		titleFn: (item: Description) => item.label,
		valueAssign: (item: Description) => item.id,
	};

	// tslint:disable-next-line: member-ordering
	multipleAutocompleteConfiguration: MultipleAutoCompleteConfiguration = {
		initialItems: (excludedItems: any[], data?: any) => this.query(this.buildAutocompleteLookup([IsActive.Active], null, excludedItems ? excludedItems : null)).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, excludedItems: any[]) => this.query(this.buildAutocompleteLookup([IsActive.Active], searchQuery, excludedItems)).pipe(map(x => x.items)),
		getSelectedItems: (selectedItems: any[]) => this.query(this.buildAutocompleteLookup([IsActive.Active, IsActive.Inactive], null, null, selectedItems)).pipe(map(x => x.items)),
		displayFn: (item: Description) => item.label,
		titleFn: (item: Description) => item.label,
		valueAssign: (item: Description) => item.id,
	};

	public buildAutocompleteLookup(isActive: IsActive[], like?: string, excludedIds?: Guid[], ids?: Guid[]): DescriptionLookup {
		const lookup: DescriptionLookup = new DescriptionLookup();
		lookup.page = { size: 100, offset: 0 };
		if (excludedIds && excludedIds.length > 0) { lookup.excludedIds = excludedIds; }
		if (ids && ids.length > 0) { lookup.ids = ids; }
		lookup.isActive = isActive;
		lookup.project = {
			fields: [
				nameof<Description>(x => x.id),
				nameof<Description>(x => x.label)
			]
		};
		lookup.order = { items: [nameof<Description>(x => x.label)] };
		if (like) { lookup.like = this.filterService.transformLike(like); }
		return lookup;
	}
}
