import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { DescriptionTemplatesInSection, PlanBlueprint, PlanBlueprintDefinition, PlanBlueprintDefinitionSection, PlanBlueprintPersist, NewVersionPlanBlueprintPersist } from '@app/core/model/plan-blueprint/plan-blueprint';
import { PlanBlueprintLookup } from '@app/core/query/plan-blueprint.lookup';
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
import { PlanBlueprintStatus } from '@app/core/common/enum/plan-blueprint-status';
import { error } from 'console';
import { PlanBlueprintVersionStatus } from '@app/core/common/enum/plan-blueprint-version-status';
import { TranslateService } from '@ngx-translate/core';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';

@Injectable()
export class PlanBlueprintService {

	private headers = new HttpHeaders();

	constructor(private http: BaseHttpV2Service, private httpClient: HttpClient, private configurationService: ConfigurationService, private filterService: FilterService, private language: TranslateService,
	) {
	}

	private get apiBase(): string { return `${this.configurationService.server}plan-blueprint`; }

	query(q: PlanBlueprintLookup): Observable<QueryResult<PlanBlueprint>> {
		const url = `${this.apiBase}/query`;
		return this.http.post<QueryResult<PlanBlueprint>>(url, q).pipe(catchError((error: any) => throwError(error)));
	}

	getSingle(id: Guid, reqFields: string[] = []): Observable<PlanBlueprint> {
		const url = `${this.apiBase}/${id}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<PlanBlueprint>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	persist(item: PlanBlueprintPersist): Observable<PlanBlueprint> {
		const url = `${this.apiBase}/persist`;

		return this.http
			.post<PlanBlueprint>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	delete(id: Guid): Observable<PlanBlueprint> {
		const url = `${this.apiBase}/${id}`;

		return this.http
			.delete<PlanBlueprint>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

	clone(id: Guid, reqFields: string[] = []): Observable<PlanBlueprint> {
		const url = `${this.apiBase}/clone/${id}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<PlanBlueprint>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	newVersion(item: NewVersionPlanBlueprintPersist, reqFields: string[] = []): Observable<PlanBlueprint> {
		const url = `${this.apiBase}/new-version`;
		const options = { params: { f: reqFields } };

		return this.http
			.post<PlanBlueprint>(url, item).pipe(
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

	uploadFile(file: FileList, labelSent: string, reqFields: string[] = []): Observable<PlanBlueprint> {
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
		displayFn: (item: PlanBlueprint) => item.label,
		titleFn: (item: PlanBlueprint) => item.label,
		valueAssign: (item: PlanBlueprint) => item.id,
	};

	// tslint:disable-next-line: member-ordering
	multipleAutocompleteConfiguration: MultipleAutoCompleteConfiguration = {
		initialItems: (excludedItems: any[], data?: any) => this.query(this.buildAutocompleteLookup(null, excludedItems ? excludedItems : null)).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, excludedItems: any[]) => this.query(this.buildAutocompleteLookup(searchQuery, excludedItems)).pipe(map(x => x.items)),
		getSelectedItems: (selectedItems: any[]) => this.query(this.buildAutocompleteLookup(null, null, selectedItems)).pipe(map(x => x.items)),
		displayFn: (item: PlanBlueprint) => item.label,
		titleFn: (item: PlanBlueprint) => item.label,
		valueAssign: (item: PlanBlueprint) => item.id,
	};

	public buildAutocompleteLookup(like?: string, excludedIds?: Guid[], ids?: Guid[], statuses?: PlanBlueprintStatus[]): PlanBlueprintLookup {
		const lookup: PlanBlueprintLookup = new PlanBlueprintLookup();
		lookup.page = { size: 100, offset: 0 };
		if (excludedIds && excludedIds.length > 0) { lookup.excludedIds = excludedIds; }
		if (ids && ids.length > 0) { lookup.ids = ids; }
		lookup.isActive = [IsActive.Active];
		lookup.statuses = statuses;
		lookup.project = {
			fields: [
				nameof<PlanBlueprint>(x => x.id),
				nameof<PlanBlueprint>(x => x.label),
				nameof<PlanBlueprint>(x => x.version)
			]
		};
		lookup.order = { items: [nameof<PlanBlueprint>(x => x.label)] };
		lookup.versionStatuses = [PlanBlueprintVersionStatus.Current];
		if (like) { lookup.like = this.filterService.transformLike(like); }
		return lookup;
	}

	// Finalized Blueprint with definitions Single AutoComplete 
	singleAutocompleteBlueprintConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: (data?: any) => this.query(this.buildAutocompleteWithDefinitonLookup(null, null, null, [PlanBlueprintStatus.Finalized])).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, data?: any) => this.query(this.buildAutocompleteWithDefinitonLookup(searchQuery, null, null, [PlanBlueprintStatus.Finalized])).pipe(map(x => x.items)),
		getSelectedItem: (selectedItem: any) => this.query(this.buildAutocompleteWithDefinitonLookup(null, null, [selectedItem])).pipe(map(x => x.items[0])),
		displayFn: (item: PlanBlueprint) => item.label,
		subtitleFn: (item: PlanBlueprint) => this.language.instant('PLAN-EDITOR.FIELDS.PLAN-BLUEPRINT-VERSION') + ' '+ item.version,
		titleFn: (item: PlanBlueprint) => item.label,
		valueAssign: (item: PlanBlueprint) => item.id,
	};

	public buildAutocompleteWithDefinitonLookup(like?: string, excludedIds?: Guid[], ids?: Guid[], statuses?: PlanBlueprintStatus[]): PlanBlueprintLookup {
		const lookup: PlanBlueprintLookup = new PlanBlueprintLookup();
		lookup.page = { size: 100, offset: 0 };
		if (excludedIds && excludedIds.length > 0) { lookup.excludedIds = excludedIds; }
		if (ids && ids.length > 0) { lookup.ids = ids; }
		lookup.isActive = [IsActive.Active];
		lookup.statuses = statuses;
		lookup.project = {
			fields: [
				nameof<PlanBlueprint>(x => x.id),
				nameof<PlanBlueprint>(x => x.label),
				nameof<PlanBlueprint>(x => x.version),
				[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.id)].join('.'),
				[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.label)].join('.'),
				[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.hasTemplates)].join('.'),
				[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.groupId)].join('.'),
			]
		};
		lookup.order = { items: [nameof<PlanBlueprint>(x => x.label)] };
		lookup.versionStatuses = [PlanBlueprintVersionStatus.Previous, PlanBlueprintVersionStatus.Current];
		if (like) { lookup.like = this.filterService.transformLike(like); }
		return lookup;
	}

	//
	// Plan Blueprint Group Autocomplete Commons
	//
	// tslint:disable-next-line: member-ordering
	planBlueprintGroupSingleAutocompleteConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: (data?: any) => this.query(this.buildPlanBlueprintGroupAutocompleteLookup({isActive: [IsActive.Active]})).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, data?: any) => this.query(this.buildPlanBlueprintGroupAutocompleteLookup({
            isActive: [IsActive.Active], 
            like: searchQuery
        })).pipe(map(x => x.items)),
		getSelectedItem: (selectedItem: any) => this.query(this.buildPlanBlueprintGroupAutocompleteLookup({
            isActive: [IsActive.Active, IsActive.Inactive], 
            groupIds: [selectedItem]
        })).pipe(map(x => x.items[0])),
		displayFn: (item: PlanBlueprint) => item.label,
		titleFn: (item: PlanBlueprint) => item.label,
		subtitleFn: (item: PlanBlueprint) => this.language.instant('PLAN-EDITOR.FIELDS.PLAN-BLUEPRINT-VERSION') + ' '+ item.version,
		valueAssign: (item: PlanBlueprint) => item.groupId,
	};

	// tslint:disable-next-line: member-ordering
	planBlueprintGroupMultipleAutocompleteConfiguration: MultipleAutoCompleteConfiguration = {
		initialItems: (excludedItems: any[], data?: any) => this.query(this.buildPlanBlueprintGroupAutocompleteLookup({
            isActive: [IsActive.Active], 
            excludedIds: excludedItems ? excludedItems : null
        })).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, excludedItems: any[]) => this.query(this.buildPlanBlueprintGroupAutocompleteLookup({
            isActive: [IsActive.Active], 
            like: searchQuery, 
            excludedIds: excludedItems
        })).pipe(map(x => x.items)),
		getSelectedItems: (selectedItems: any[]) => this.query(this.buildPlanBlueprintGroupAutocompleteLookup({
            isActive: [IsActive.Active, IsActive.Inactive],
            groupIds: selectedItems
        })).pipe(map(x => x.items)),
		displayFn: (item: PlanBlueprint) => item.label,
		titleFn: (item: PlanBlueprint) => item.label,
		subtitleFn: (item: PlanBlueprint) => this.language.instant('PLAN-EDITOR.FIELDS.PLAN-BLUEPRINT-VERSION') + ' '+ item.version,
		valueAssign: (item: PlanBlueprint) => item.groupId,
	};

	public buildPlanBlueprintGroupAutocompleteLookup(params?: {
        isActive: IsActive[], 
        like?: string, 
        excludedIds?: Guid[], 
        groupIds?: Guid[],
        excludedGroupIds?: Guid[]
    }): PlanBlueprintLookup {
        const {isActive, like, excludedIds, groupIds, excludedGroupIds} = params ?? {};
		const lookup: PlanBlueprintLookup = new PlanBlueprintLookup();
		lookup.page = { size: 100, offset: 0 };
		if (excludedIds && excludedIds.length > 0) { lookup.excludedIds = excludedIds; }
		if (excludedGroupIds && excludedGroupIds.length > 0) { lookup.excludedGroupIds = excludedGroupIds; }
		if (groupIds && groupIds.length > 0) { lookup.groupIds = groupIds; }

		lookup.isActive = isActive;
		lookup.versionStatuses = [PlanBlueprintVersionStatus.Current];
		lookup.statuses = [PlanBlueprintStatus.Finalized];
		lookup.project = {
			fields: [
				nameof<PlanBlueprint>(x => x.id),
				nameof<PlanBlueprint>(x => x.label),
				nameof<PlanBlueprint>(x => x.groupId),
				nameof<PlanBlueprint>(x => x.version),
			]
		};
		lookup.order = { items: [nameof<PlanBlueprint>(x => x.label)] };
		if (like) { lookup.like = this.filterService.transformLike(like); }
		return lookup;
	}

	//
	//
	// UI Helpers
	//
	//

	getSection(PlanBlueprint: PlanBlueprint, sectionId: Guid): PlanBlueprintDefinitionSection {
		return PlanBlueprint?.definition?.sections?.find(x => x.id === sectionId);
	}
}
