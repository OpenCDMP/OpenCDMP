import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { ReferenceSourceType } from '@app/core/common/enum/reference-source-type';
import { ReferenceFilter } from '@app/core/query/reference.lookup';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { Guid } from '@common/types/guid';
import { nameof } from 'ts-simple-nameof';

@Component({
	selector: 'app-reference-listing-filters',
	templateUrl: './reference-listing-filters.component.html',
	styleUrls: ['./reference-listing-filters.component.scss']
})
export class ReferenceListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: ReferenceFilter;
	@Output() filterChange = new EventEmitter<ReferenceFilter>();

	referenceSourceTypeEnumValues = this.enumUtils.getEnumValues<ReferenceSourceType>(ReferenceSourceType)

	// * State
	internalFilters: ReferenceListingFilters = this._getEmptyFilters();

	protected appliedFilterCount: number = 0;
	constructor(
		public enumUtils: EnumUtils,
		public referenceTypeService: ReferenceTypeService
	) { super(); }

	ngOnInit() {
	}

	ngOnChanges(changes: SimpleChanges): void {
		const filterChange = changes[nameof<ReferenceListingFiltersComponent>(x => x.filter)]?.currentValue as ReferenceFilter;
		if (filterChange) {
			this.updateFilters()
		}
	}


	onSearchTermChange(searchTerm: string): void {
		this.applyFilters()
	}


	protected updateFilters(): void {
		this.internalFilters = this._parseToInternalFilters(this.filter);
		this.appliedFilterCount = this._computeAppliedFilters(this.internalFilters);
	}

	protected applyFilters(): void {
		const { isActive, like, typeIds, sourceTypes } = this.internalFilters ?? {}
		this.filterChange.emit({
			...this.filter,
			like,
			isActive: isActive ? [IsActive.Active] : [IsActive.Inactive],
			typeIds: typeIds?.length > 0 ? typeIds : null,
			sourceTypes: sourceTypes?.length > 0 ? sourceTypes : null,
		})
	}


	private _parseToInternalFilters(inputFilter: ReferenceFilter): ReferenceListingFilters {
		if (!inputFilter) {
			return this._getEmptyFilters();
		}

		let { excludedIds, ids, isActive, like, typeIds, sourceTypes } = inputFilter;

		return {
			isActive: (isActive ?? [])?.includes(IsActive.Active) || !isActive?.length,
			like: like,
			typeIds: typeIds,
			sourceTypes: sourceTypes
		}

	}

	private _getEmptyFilters(): ReferenceListingFilters {
		return {
			isActive: true,
			like: null,
			typeIds: null,
			sourceTypes: null
		}
	}

	private _computeAppliedFilters(filters: ReferenceListingFilters): number {
		let count = 0;
		if (!filters?.isActive) {
			count++
		}
		if (filters?.like) {
			count++;
		}
		if (filters?.sourceTypes?.length) {
			count++;
		}
		if (filters?.typeIds?.length) {
			count++;
		}

		return count;
	}

	clearFilters() {
		this.internalFilters = this._getEmptyFilters();
	}
}

interface ReferenceListingFilters {
	isActive: boolean;
	like: string;
	typeIds: Guid[];
	sourceTypes: ReferenceSourceType[];
}
