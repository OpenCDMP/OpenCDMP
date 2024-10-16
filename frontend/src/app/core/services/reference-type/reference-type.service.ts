import { Injectable } from '@angular/core';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { ReferenceType, ReferenceTypePersist } from '@app/core/model/reference-type/reference-type';
import { ReferenceTypeLookup } from '@app/core/query/reference-type.lookup';
import { MultipleAutoCompleteConfiguration } from '@app/library/auto-complete/multiple/multiple-auto-complete-configuration';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { ReferenceTypeEditorResolver } from '@app/ui/admin/reference-type/editor/reference-type-editor.resolver';
import { QueryResult } from '@common/model/query-result';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { ConfigurationService } from '../configuration/configuration.service';
import { BaseHttpV2Service } from '../http/base-http-v2.service';

@Injectable()
export class ReferenceTypeService {

	constructor(
		private http: BaseHttpV2Service,
		private configurationService: ConfigurationService,
		private filterService: FilterService
	) {
	}

	private get apiBase(): string { return `${this.configurationService.server}reference-type`; }

	query(q: ReferenceTypeLookup): Observable<QueryResult<ReferenceType>> {
		const url = `${this.apiBase}/query`;
		return this.http.post<QueryResult<ReferenceType>>(url, q).pipe(catchError((error: any) => throwError(error)));
	}

	getSingle(id: Guid, reqFields: string[] = []): Observable<ReferenceType> {
		const url = `${this.apiBase}/${id}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<ReferenceType>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	getSingleWithCode(code: string, reqFields: string[] = []): Observable<ReferenceType> {
		const url = `${this.apiBase}/code/${code}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<ReferenceType>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	persist(item: ReferenceTypePersist): Observable<ReferenceType> {
		const url = `${this.apiBase}/persist`;

		return this.http
			.post<ReferenceType>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	delete(id: Guid): Observable<ReferenceType> {
		const url = `${this.apiBase}/${id}`;

		return this.http
			.delete<ReferenceType>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

	// LOOKUP

	public static DefaultReferenceTypeLookup(): ReferenceTypeLookup {
		const lookup = new ReferenceTypeLookup();

		lookup.project = {
			fields: [
				...ReferenceTypeEditorResolver.lookupFields()
			]
		};
		lookup.order = { items: [nameof<ReferenceType>(x => x.code)] };
		lookup.page = { offset: 0, size: 100 };
		lookup.isActive = [IsActive.Active];
		return lookup;
	}

	//
	// Autocomplete Commons
	//
	//
	public singleAutocompleteConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: (data?: any) => this.query(this.buildAutocompleteLookup()).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, data?: any) => this.query(this.buildAutocompleteLookup(searchQuery)).pipe(map(x => x.items)),
		getSelectedItem: (selectedItem: any) => this.query(this.buildAutocompleteLookup(null, null, [selectedItem])).pipe(map(x => x.items[0])),
		displayFn: (item: ReferenceType) => item.name,
		titleFn: (item: ReferenceType) => item.name,
		valueAssign: (item: ReferenceType) => item.id,
	};

	public multipleAutocompleteConfiguration: MultipleAutoCompleteConfiguration = {
		initialItems: (excludedItems: any[], data?: any) => this.query(this.buildAutocompleteLookup(null, excludedItems ? excludedItems : null)).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, excludedItems: any[]) => this.query(this.buildAutocompleteLookup(searchQuery, excludedItems)).pipe(map(x => x.items)),
		getSelectedItems: (selectedItems: any[]) => this.query(this.buildAutocompleteLookup(null, null, selectedItems)).pipe(map(x => x.items)),
		displayFn: (item: ReferenceType) => item.name,
		titleFn: (item: ReferenceType) => item.name,
		valueAssign: (item: ReferenceType) => item.id,
	};

	public getSingleAutocompleteConfiguration(excludedIds: Guid[] = null): SingleAutoCompleteConfiguration {
		return {	
			initialItems: (data?: any) => this.query(this.buildAutocompleteLookup(null, excludedIds)).pipe(map(x => x.items)),
			filterFn: (searchQuery: string, data?: any) => this.query(this.buildAutocompleteLookup(searchQuery, excludedIds)).pipe(map(x => x.items)),
			getSelectedItem: (selectedItem: any) => this.query(this.buildAutocompleteLookup(null, null, [selectedItem])).pipe(map(x => x.items[0])),
			displayFn: (item: ReferenceType) => item.name,
			titleFn: (item: ReferenceType) => item.name,
			valueAssign: (item: ReferenceType) => item.id,
		};
	}

	private buildAutocompleteLookup(like?: string, excludedIds?: Guid[], ids?: Guid[]): ReferenceTypeLookup {
		const lookup: ReferenceTypeLookup = new ReferenceTypeLookup();
		lookup.page = { size: 100, offset: 0 };
		if (excludedIds && excludedIds.length > 0) { lookup.excludedIds = excludedIds; }
		if (ids && ids.length > 0) { lookup.ids = ids; }
		lookup.isActive = [IsActive.Active];
		lookup.project = {
			fields: [
				nameof<ReferenceType>(x => x.id),
				nameof<ReferenceType>(x => x.name)
			]
		};
		lookup.order = { items: [nameof<ReferenceType>(x => x.name)] };
		if (like) { lookup.like = this.filterService.transformLike(like); }
		return lookup;
	}


	///system fields 
	getSystemFields(fields: string[]): string[] {
		fields.push('reference_id');
		fields.push('label');
		fields.push('description');

		return fields;
	}

	public getResearcherReferenceType(): any {
		return this.configurationService.researcherId;
	}

	public getGrantReferenceType(): any {
		return this.configurationService.grantId;
	}

	public getOrganizationReferenceType(): any {
		return this.configurationService.organizationId;
	}
}