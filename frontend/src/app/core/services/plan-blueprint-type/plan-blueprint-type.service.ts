import { Injectable } from '@angular/core';
import { IsActive } from '@app/core/common/enum/is-active.enum';
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
import { PlanBlueprintTypeLookup } from '@app/core/query/plan-blueprint-type.lookup';
import { PlanBlueprintType, PlanBlueprintTypePersist } from '@app/core/model/plan-blueprint-type/plan-blueprint-type';
import { PlanBlueprintTypeStatus } from '@app/core/common/enum/plan-blueprint-type-status';

@Injectable()
export class PlanBlueprintTypeService {

	constructor(
		private http: BaseHttpV2Service,
		private installationConfiguration: ConfigurationService,
		private filterService: FilterService
	) { }

	private get apiBase(): string { return `${this.installationConfiguration.server}plan-blueprint-type`; }

	query(q: PlanBlueprintTypeLookup): Observable<QueryResult<PlanBlueprintType>> {
		const url = `${this.apiBase}/query`;
		return this.http
			.post<QueryResult<PlanBlueprintType>>(url, q).pipe(
				catchError((error: any) => throwError(error)));
	}

	getSingle(id: Guid, reqFields: string[] = []): Observable<PlanBlueprintType> {
		const url = `${this.apiBase}/${id}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<PlanBlueprintType>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	persist(item: PlanBlueprintTypePersist): Observable<PlanBlueprintType> {
		const url = `${this.apiBase}/persist`;

		return this.http
			.post<PlanBlueprintType>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	delete(id: Guid): Observable<PlanBlueprintType> {
		const url = `${this.apiBase}/${id}`;

		return this.http
			.delete<PlanBlueprintType>(url).pipe(
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
		displayFn: (item: PlanBlueprintType) => item.name,
		titleFn: (item: PlanBlueprintType) => item.name,
		valueAssign: (item: PlanBlueprintType) => item.id,
	};

	public getSingleAutocompleteConfiguration(statuses?: PlanBlueprintTypeStatus[]): SingleAutoCompleteConfiguration {
		return {
			initialItems: (data?: any) => this.query(this.buildAutocompleteLookup([IsActive.Active], null, null, null, statuses ? statuses: null)).pipe(map(x => x.items)),
			filterFn: (searchQuery: string, data?: any) => this.query(this.buildAutocompleteLookup([IsActive.Active], searchQuery, null, null, statuses ? statuses: null)).pipe(map(x => x.items)),
			getSelectedItem: (selectedItem: any) => this.query(this.buildAutocompleteLookup([IsActive.Active, IsActive.Inactive], null, null, [selectedItem])).pipe(map(x => x.items[0])),
			displayFn: (item: PlanBlueprintType) => item.name,
			titleFn: (item: PlanBlueprintType) => item.name,
			valueAssign: (item: PlanBlueprintType) => item.id,
		};
	}

	// tslint:disable-next-line: member-ordering
	multipleAutocompleteConfiguration: MultipleAutoCompleteConfiguration = {
		initialItems: (excludedItems: any[], data?: any) => this.query(this.buildAutocompleteLookup([IsActive.Active], null, excludedItems ? excludedItems : null)).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, excludedItems: any[]) => this.query(this.buildAutocompleteLookup([IsActive.Active], searchQuery, excludedItems)).pipe(map(x => x.items)),
		getSelectedItems: (selectedItems: any[]) => this.query(this.buildAutocompleteLookup([IsActive.Active, IsActive.Inactive], null, null, selectedItems)).pipe(map(x => x.items)),
		displayFn: (item: PlanBlueprintType) => item.name,
		titleFn: (item: PlanBlueprintType) => item.name,
		valueAssign: (item: PlanBlueprintType) => item.id,
	};

	public getMultipleAutoCompleteSearchConfiguration(statuses?: PlanBlueprintTypeStatus[]): MultipleAutoCompleteConfiguration {
		return {
			initialItems: (excludedItems: any[], data?: any) => this.query(this.buildAutocompleteLookup([IsActive.Active], null, excludedItems ? excludedItems : null, null, statuses ? statuses: null)).pipe(map(x => x.items)),
			filterFn: (searchQuery: string, excludedItems: any[]) => this.query(this.buildAutocompleteLookup([IsActive.Active], searchQuery, excludedItems, null, statuses ? statuses: null)).pipe(map(x => x.items)),
			getSelectedItems: (selectedItems: any[]) => this.query(this.buildAutocompleteLookup([IsActive.Active, IsActive.Inactive], null, null, selectedItems)).pipe(map(x => x.items)),
			displayFn: (item: PlanBlueprintType) => item.name,
			titleFn: (item: PlanBlueprintType) => item.name,
			valueAssign: (item: PlanBlueprintType) => item.id,
		};
	}

	private buildAutocompleteLookup(isActive: IsActive[], like?: string, excludedIds?: Guid[], ids?: Guid[], statuses?: PlanBlueprintTypeStatus[]): PlanBlueprintTypeLookup {
		const lookup: PlanBlueprintTypeLookup = new PlanBlueprintTypeLookup();
		lookup.page = { size: 100, offset: 0 };
		if (excludedIds && excludedIds.length > 0) { lookup.excludedIds = excludedIds; }
		if (ids && ids.length > 0) { lookup.ids = ids; }
		lookup.isActive = isActive;
		lookup.statuses = statuses;
		lookup.project = {
			fields: [
				nameof<PlanBlueprintType>(x => x.id),
				nameof<PlanBlueprintType>(x => x.name)
			]
		};
		lookup.order = { items: [nameof<PlanBlueprintType>(x => x.name)] };
		if (like) { lookup.like = this.filterService.transformLike(like); }
		return lookup;
	}

    buildLookup(params?: Partial<PlanBlueprintTypeLookup>){
        const {metadata, page, isActive, statuses, order, project} = params ?? {};
        const lookup = new PlanBlueprintTypeLookup();

        lookup.metadata = metadata ?? {countAll: true};
		lookup.page = page ??  {offset: 0, size: 100};
		lookup.isActive = isActive ?? [IsActive.Active];
		lookup.statuses = statuses ?? [PlanBlueprintTypeStatus.Finalized];
		lookup.order = order ??  {items: ['-' + (nameof<PlanBlueprintType>(x => x.name))]};

		lookup.project = project ?? {
			fields: [
                nameof<PlanBlueprintType>(x => x.id),
                nameof<PlanBlueprintType>(x => x.name),
                nameof<PlanBlueprintType>(x => x.code)
            ]
		};
        return lookup;
    }
}
