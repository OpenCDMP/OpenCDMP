import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { DescriptionTemplatePersist, NewVersionDescriptionTemplatePersist } from '@app/core/model/description-template/description-template-persist';
import { DescriptionTemplateLookup } from '@app/core/query/description-template.lookup';
import { MultipleAutoCompleteConfiguration } from '@app/library/auto-complete/multiple/multiple-auto-complete-configuration';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { BaseHttpParams } from '@common/http/base-http-params';
import { InterceptorType } from '@common/http/interceptors/interceptor-type';
import { QueryResult } from '@common/model/query-result';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { ConfigurationService } from '../configuration/configuration.service';
import { BaseHttpV2Service } from '../http/base-http-v2.service';
import { DescriptionTemplateVersionStatus } from '@app/core/common/enum/description-template-version-status';
import { DescriptionTemplateStatus } from '@app/core/common/enum/description-template-status';

@Injectable()
export class DescriptionTemplateService {

	private headers = new HttpHeaders();

	constructor(private http: BaseHttpV2Service, private httpClient: HttpClient, private configurationService: ConfigurationService, private filterService: FilterService) {
	}

	private get apiBase(): string { return `${this.configurationService.server}description-template`; }

	query(q: DescriptionTemplateLookup): Observable<QueryResult<DescriptionTemplate>> {
		const url = `${this.apiBase}/query`;
		return this.http.post<QueryResult<DescriptionTemplate>>(url, q).pipe(catchError((error: any) => throwError(error)));
	}

	getSingle(id: Guid, reqFields: string[] = []): Observable<DescriptionTemplate> {
		const url = `${this.apiBase}/${id}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<DescriptionTemplate>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	persist(item: DescriptionTemplatePersist): Observable<DescriptionTemplate> {
		const url = `${this.apiBase}/persist`;

		return this.http
			.post<DescriptionTemplate>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	delete(id: Guid): Observable<DescriptionTemplate> {
		const url = `${this.apiBase}/${id}`;

		return this.http
			.delete<DescriptionTemplate>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

	clone(id: Guid, reqFields: string[] = []): Observable<DescriptionTemplate> {
		const url = `${this.apiBase}/clone/${id}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<DescriptionTemplate>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	newVersion(item: NewVersionDescriptionTemplatePersist, reqFields: string[] = []): Observable<DescriptionTemplate> {
		const url = `${this.apiBase}/new-version`;
		const options = { params: { f: reqFields } };

		return this.http
			.post<DescriptionTemplate>(url, item).pipe(
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

	uploadFile(file: FileList, labelSent: string, reqFields: string[] = []): Observable<DescriptionTemplate> {
		const url = `${this.apiBase}/xml/import`;
		const params = new BaseHttpParams();
		params.interceptorContext = {
			excludedInterceptors: [InterceptorType.JSONContentType]
		};
		const formData = new FormData();
		formData.append('file', file[0], labelSent);
		return this.http.post(url, formData, { params: params });
	}

	//
	// Autocomplete Commons
	//
	// tslint:disable-next-line: member-ordering
	singleAutocompleteConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: (data?: any) => this.query(this.buildAutocompleteLookup()).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, data?: any) => this.query(this.buildAutocompleteLookup(searchQuery)).pipe(map(x => x.items)),
		getSelectedItem: (selectedItem: any) => this.query(this.buildAutocompleteLookup(null, null, [selectedItem])).pipe(map(x => x.items[0])),
		displayFn: (item: DescriptionTemplate) => item.label,
		titleFn: (item: DescriptionTemplate) => item.label,
		subtitleFn: (item: DescriptionTemplate) => item.description,
		valueAssign: (item: DescriptionTemplate) => item.id,
		popupItemActionIcon: 'visibility'
	};

	// tslint:disable-next-line: member-ordering
	multipleAutocompleteConfiguration: MultipleAutoCompleteConfiguration = {
		initialItems: (excludedItems: any[], data?: any) => this.query(this.buildAutocompleteLookup(null, excludedItems ? excludedItems : null)).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, excludedItems: any[]) => this.query(this.buildAutocompleteLookup(searchQuery, excludedItems)).pipe(map(x => x.items)),
		getSelectedItems: (selectedItems: any[]) => this.query(this.buildAutocompleteLookup(null, null, selectedItems)).pipe(map(x => x.items)),
		displayFn: (item: DescriptionTemplate) => item.label,
		titleFn: (item: DescriptionTemplate) => item.label,
		subtitleFn: (item: DescriptionTemplate) => item.description,
		valueAssign: (item: DescriptionTemplate) => item.id,
		popupItemActionIcon: 'visibility'
	};
	
	public buildMultipleAutocompleteConfiguration(preview: boolean = false): MultipleAutoCompleteConfiguration {
		return {
			initialItems: (excludedItems: any[], data?: any) => this.query(this.buildAutocompleteLookup(null, excludedItems ? excludedItems : null)).pipe(map(x => x.items)),
			filterFn: (searchQuery: string, excludedItems: any[]) => this.query(this.buildAutocompleteLookup(searchQuery, excludedItems)).pipe(map(x => x.items)),
			getSelectedItems: (selectedItems: any[]) => this.query(this.buildAutocompleteLookup(null, null, selectedItems)).pipe(map(x => x.items)),
			displayFn: (item: DescriptionTemplate) => item.label,
			titleFn: (item: DescriptionTemplate) => item.label,
			subtitleFn: (item: DescriptionTemplate) => item.description,
			valueAssign: (item: DescriptionTemplate) => item.id,
			popupItemActionIcon: preview ? 'visibility' : null,
		};
	}

	public buildAutocompleteLookup(like?: string, excludedIds?: Guid[], ids?: Guid[]): DescriptionTemplateLookup {
		const lookup: DescriptionTemplateLookup = new DescriptionTemplateLookup();
		lookup.page = { size: 100, offset: 0 };
		if (excludedIds && excludedIds.length > 0) { lookup.excludedIds = excludedIds; }
		if (ids && ids.length > 0) { lookup.ids = ids; }
		lookup.isActive = [IsActive.Active];
		lookup.project = {
			fields: [
				nameof<DescriptionTemplate>(x => x.id),
				nameof<DescriptionTemplate>(x => x.label),
				nameof<DescriptionTemplate>(x => x.description),
			]
		};
		lookup.order = { items: [nameof<DescriptionTemplate>(x => x.label)] };
		if (like) { lookup.like = this.filterService.transformLike(like); }
		return lookup;
	}

	//
	// Description Tempalte Group Autocomplete Commons
	//
	// tslint:disable-next-line: member-ordering
	descriptionTempalteGroupSingleAutocompleteConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: (data?: any) => this.query(this.buildDescriptionTempalteGroupAutocompleteLookup([IsActive.Active])).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, data?: any) => this.query(this.buildDescriptionTempalteGroupAutocompleteLookup([IsActive.Active], searchQuery)).pipe(map(x => x.items)),
		getSelectedItem: (selectedItem: any) => this.query(this.buildDescriptionTempalteGroupAutocompleteLookup([IsActive.Active, IsActive.Inactive], null, null, [selectedItem])).pipe(map(x => x.items[0])),
		displayFn: (item: DescriptionTemplate) => item.label,
		titleFn: (item: DescriptionTemplate) => item.label,
		subtitleFn: (item: DescriptionTemplate) => item.description,
		valueAssign: (item: DescriptionTemplate) => item.groupId,
		popupItemActionIcon: 'visibility'
	};

	// tslint:disable-next-line: member-ordering
	descriptionTempalteGroupMultipleAutocompleteConfiguration: MultipleAutoCompleteConfiguration = {
		initialItems: (excludedItems: any[], data?: any) => this.query(this.buildDescriptionTempalteGroupAutocompleteLookup([IsActive.Active], null, excludedItems ? excludedItems : null)).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, excludedItems: any[]) => this.query(this.buildDescriptionTempalteGroupAutocompleteLookup([IsActive.Active], searchQuery, excludedItems)).pipe(map(x => x.items)),
		getSelectedItems: (selectedItems: any[]) => this.query(this.buildDescriptionTempalteGroupAutocompleteLookup([IsActive.Active, IsActive.Inactive], null, null, selectedItems)).pipe(map(x => x.items)),
		displayFn: (item: DescriptionTemplate) => item.label,
		titleFn: (item: DescriptionTemplate) => item.label,
		subtitleFn: (item: DescriptionTemplate) => item.description,
		valueAssign: (item: DescriptionTemplate) => item.groupId,
		popupItemActionIcon: 'visibility'
	};

	public buildDescriptionTempalteGroupMultipleAutocompleteConfiguration(preview: boolean = false): MultipleAutoCompleteConfiguration {

		return {
			initialItems: (excludedItems: any[], data?: any) => this.query(this.buildDescriptionTempalteGroupAutocompleteLookup([IsActive.Active], null, excludedItems ? excludedItems : null)).pipe(map(x => x.items)),
			filterFn: (searchQuery: string, excludedItems: any[]) => this.query(this.buildDescriptionTempalteGroupAutocompleteLookup([IsActive.Active], searchQuery, excludedItems)).pipe(map(x => x.items)),
			getSelectedItems: (selectedItems: any[]) => this.query(this.buildDescriptionTempalteGroupAutocompleteLookup([IsActive.Active, IsActive.Inactive], null, null, selectedItems)).pipe(map(x => x.items)),
			displayFn: (item: DescriptionTemplate) => item.label,
			titleFn: (item: DescriptionTemplate) => item.label,
			subtitleFn: (item: DescriptionTemplate) => item.description,
			valueAssign: (item: DescriptionTemplate) => item.groupId,
			popupItemActionIcon: preview ? 'visibility' : null,
		};
	}

	public buildDescriptionTempalteGroupAutocompleteLookup(isActive: IsActive[], like?: string, excludedIds?: Guid[], groupIds?: Guid[], excludedGroupIds?: Guid[]): DescriptionTemplateLookup {
		const lookup: DescriptionTemplateLookup = new DescriptionTemplateLookup();
		lookup.page = { size: 100, offset: 0 };
		if (excludedIds && excludedIds.length > 0) { lookup.excludedIds = excludedIds; }
		if (groupIds && groupIds.length > 0) { lookup.groupIds = groupIds; }
		if (excludedGroupIds && excludedGroupIds.length > 0) { lookup.excludedGroupIds = excludedGroupIds; }

		lookup.isActive = isActive;
		lookup.versionStatuses = [DescriptionTemplateVersionStatus.Current, DescriptionTemplateVersionStatus.NotFinalized];
		lookup.statuses = [DescriptionTemplateStatus.Finalized];
		lookup.project = {
			fields: [
				nameof<DescriptionTemplate>(x => x.id),
				nameof<DescriptionTemplate>(x => x.label),
				nameof<DescriptionTemplate>(x => x.groupId),
				nameof<DescriptionTemplate>(x => x.description),
			]
		};
		lookup.order = { items: [nameof<DescriptionTemplate>(x => x.label)] };
		if (like) { lookup.like = this.filterService.transformLike(like); }
		return lookup;
	}

}
