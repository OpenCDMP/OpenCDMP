import { Injectable } from '@angular/core';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { ReferenceSourceType } from '@app/core/common/enum/reference-source-type';
import { PlanReference } from '@app/core/model/plan/plan-reference';
import { Definition, Field, Reference, ReferencePersist } from '@app/core/model/reference/reference';
import { ReferenceSearchDefinitionLookup, ReferenceSearchLookup } from '@app/core/query/reference-search.lookup';
import { ReferenceLookup } from '@app/core/query/reference.lookup';
import { MultipleAutoCompleteConfiguration } from '@app/library/auto-complete/multiple/multiple-auto-complete-configuration';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { QueryResult } from '@common/model/query-result';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { Observable, throwError } from 'rxjs';
import { catchError, filter, map } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { ConfigurationService } from '../configuration/configuration.service';
import { BaseHttpV2Service } from '../http/base-http-v2.service';
import { TranslateService } from '@ngx-translate/core';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';

@Injectable()
export class ReferenceService {

	constructor(
		private http: BaseHttpV2Service,
		private configurationService: ConfigurationService,
		private filterService: FilterService,
		private language: TranslateService,
	) {
	}

	private get apiBase(): string { return `${this.configurationService.server}reference`; }

	query(q: ReferenceLookup): Observable<QueryResult<Reference>> {
		const url = `${this.apiBase}/query`;
		return this.http.post<QueryResult<Reference>>(url, q).pipe(catchError((error: any) => throwError(error)));
	}

	searchWithDefinition(q: ReferenceSearchDefinitionLookup): Observable<Reference[]> {
		const url = `${this.apiBase}/search`;
		return this.http.post<Reference[]>(url, q).pipe(catchError((error: any) => throwError(error)));
	}

	findReference(reference: string, referenceTypeId: Guid): Observable<Boolean> {

		const url = `${this.apiBase}/find/${referenceTypeId}`;
		const options = { params: { reference: reference } };

		return this.http.get<Boolean>(url, options).pipe(catchError((error: any) => throwError(error)));
	}

	getSingle(id: Guid, reqFields: string[] = []): Observable<Reference> {
		const url = `${this.apiBase}/${id}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<Reference>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	persist(item: ReferencePersist): Observable<Reference> {
		const url = `${this.apiBase}/persist`;

		return this.http
			.post<Reference>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	delete(id: Guid): Observable<Reference> {
		const url = `${this.apiBase}/${id}`;

		return this.http
			.delete<Reference>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

	//
	// Autocomplete Commons - Query
	//
	public getSingleAutocompleteQueryConfiguration(referenceTypeIds?: Guid[], sourceTypes?: ReferenceSourceType[]): SingleAutoCompleteConfiguration {
		return {
			initialItems: (data?: any) => this.query(this.buildAutocompleteQueryLookup(referenceTypeIds, sourceTypes)).pipe(map(x => x.items)),
			filterFn: (searchQuery: string, data?: any) => this.query(this.buildAutocompleteQueryLookup(referenceTypeIds, sourceTypes, searchQuery)).pipe(map(x => x.items)),
			getSelectedItem: (selectedItem: any) => this.query(this.buildAutocompleteQueryLookup(referenceTypeIds, sourceTypes, null, null, [selectedItem])).pipe(map(x => x.items[0])),
			displayFn: (item: Reference) => item.label,
			titleFn: (item: Reference) => item.label,
			valueAssign: (item: Reference) => item.id,
		};
	};

	public getMultipleAutoCompleteQueryConfiguration(referenceTypeIds?: Guid[], sourceTypes?: ReferenceSourceType[]): MultipleAutoCompleteConfiguration {
		return {
			initialItems: (excludedItems: any[], data?: any) => this.query(this.buildAutocompleteQueryLookup(referenceTypeIds, sourceTypes, null, excludedItems ? excludedItems : null)).pipe(map(x => x.items)),
			filterFn: (searchQuery: string, excludedItems: any[]) => this.query(this.buildAutocompleteQueryLookup(referenceTypeIds, sourceTypes, searchQuery, excludedItems)).pipe(map(x => x.items)),
			getSelectedItems: (selectedItems: any[]) => this.query(this.buildAutocompleteQueryLookup(referenceTypeIds, sourceTypes, null, null, selectedItems)).pipe(map(x => x.items)),
			displayFn: (item: Reference) => item.label,
			titleFn: (item: Reference) => item.label,
			valueAssign: (item: Reference) => item.id,
		};
	}

	private buildAutocompleteQueryLookup(referenceTypeIds?: Guid[], sourceTypes?: ReferenceSourceType[], like?: string, excludedIds?: Guid[], ids?: Guid[]): ReferenceLookup {
		const lookup: ReferenceLookup = new ReferenceLookup();
		lookup.page = { size: 100, offset: 0 };
		if (excludedIds && excludedIds.length > 0) { lookup.excludedIds = excludedIds; }
		if (ids && ids.length > 0) { lookup.ids = ids; }
		lookup.isActive = [IsActive.Active];
		lookup.project = {
			fields: [
				nameof<Reference>(x => x.id),
				nameof<Reference>(x => x.label),
			]
		};
		if (referenceTypeIds && referenceTypeIds.length > 0) { lookup.typeIds = referenceTypeIds; }
		if (sourceTypes && sourceTypes.length > 0) { lookup.sourceTypes = sourceTypes; }
		lookup.order = { items: [nameof<Reference>(x => x.label)] };
		if (like) { lookup.like = this.filterService.transformLike(like); }
		return lookup;
	}

	//
	// Autocomplete Commons - Search
	//
	public getSingleAutocompleteSearchConfiguration(typeId: Guid, dependencyReferences: Reference[]): SingleAutoCompleteConfiguration {
		return {
			initialItems: (data?: any) => this.searchWithDefinition(this.buildAutocompleteSearchLookup(typeId, dependencyReferences)).pipe(map(x => x)),
			filterFn: (searchQuery: string, data?: any) => this.searchWithDefinition(this.buildAutocompleteSearchLookup(typeId, dependencyReferences, searchQuery)).pipe(map(x => x)),
			displayFn: (item: Reference) => item.label,
			subtitleFn: (item: Reference) => item?.sourceType === ReferenceSourceType.External ? this.language.instant('REFERENCE-FIELD-COMPONENT.EXTERNAL-SOURCE') + ': ' + item.source : this.language.instant('REFERENCE-FIELD-COMPONENT.INTERNAL-SOURCE'),
			titleFn: (item: Reference) => item.label,
			valueAssign: (item: Reference) => item,
			uniqueAssign: (item: Reference) => item.source + '_' + item.reference,
		};
	};

	public getMultipleAutoCompleteSearchConfiguration(typeId: Guid, dependencyReferences: Reference[]): MultipleAutoCompleteConfiguration {
		return {
			initialItems: (excludedItems: any[], data?: any) => this.searchWithDefinition(this.buildAutocompleteSearchLookup(typeId, dependencyReferences, null)).pipe(map(x => x)),
			filterFn: (searchQuery: string, excludedItems: any[]) => this.searchWithDefinition(this.buildAutocompleteSearchLookup(typeId, dependencyReferences, searchQuery)).pipe(map(x => x)),
			displayFn: (item: Reference) => item.label,
			titleFn: (item: Reference) => item.label,
			subtitleFn: (item: Reference) => item?.sourceType === ReferenceSourceType.External ? this.language.instant('REFERENCE-FIELD-COMPONENT.EXTERNAL-SOURCE') + ': ' + item.source : this.language.instant('REFERENCE-FIELD-COMPONENT.INTERNAL-SOURCE'),
			valueAssign: (item: Reference) => item,
			uniqueAssign: (item: Reference) => item.source + '_' + item.reference,
		};
	}

	private buildAutocompleteSearchLookup(typeId: Guid, dependencyReferences: Reference[], like?: string): ReferenceSearchLookup {
		const lookup: ReferenceSearchLookup = new ReferenceSearchLookup();
		lookup.page = { size: 100, offset: 0 };
		lookup.project = {
			fields: [
				nameof<Reference>(x => x.id),
				nameof<Reference>(x => x.hash),
				nameof<Reference>(x => x.label),
				nameof<Reference>(x => x.type),
				[nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.id)].join('.'),
				nameof<Reference>(x => x.description),
				[nameof<Reference>(x => x.definition), nameof<Definition>(x => x.fields), nameof<Field>(x => x.code)].join('.'),
				[nameof<Reference>(x => x.definition), nameof<Definition>(x => x.fields), nameof<Field>(x => x.dataType)].join('.'),
				[nameof<Reference>(x => x.definition), nameof<Definition>(x => x.fields), nameof<Field>(x => x.value)].join('.'),
				nameof<Reference>(x => x.reference),
				nameof<Reference>(x => x.abbreviation),
				nameof<Reference>(x => x.source),
				nameof<Reference>(x => x.sourceType),
			]
		};
		lookup.typeId = typeId;
		lookup.dependencyReferences = dependencyReferences;
		lookup.order = { items: [nameof<Reference>(x => x.label)] };
		if (like) { lookup.like = this.filterService.transformLike(like); }
		return lookup;
	}


	//
	//
	// UI Helpers
	//
	//

	hasRerefenceOfTypes(planReferences: PlanReference[], referenceTypeIds?: Guid[]): boolean {
		return this.getReferencesForTypes(planReferences, referenceTypeIds)?.length > 0;
	}

	getReferencesForTypes(planReferences: PlanReference[], referenceTypeIds?: Guid[]): PlanReference[] {
		return planReferences?.filter(x => referenceTypeIds?.includes(x?.reference?.type?.id)).filter(x=> x.isActive === IsActive.Active);;
	}

	getReferencesForTypesFirstSafe(planReferences: PlanReference[], referenceTypeIds?: Guid[]): PlanReference {
		return this.getReferencesForTypes(planReferences, referenceTypeIds)?.find(Boolean);
	}
}
