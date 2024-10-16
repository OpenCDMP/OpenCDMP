import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { ReferenceTypeFilter } from '@app/core/query/reference-type.lookup';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { nameof } from 'ts-simple-nameof';

@Component({
	selector: 'app-reference-type-listing-filters',
	templateUrl: './reference-type-listing-filters.component.html',
	styleUrls: ['./reference-type-listing-filters.component.scss']
})
export class ReferenceTypeListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: ReferenceTypeFilter;
	@Output() filterChange = new EventEmitter<ReferenceTypeFilter>();

	// * State
	internalFilters: ReferenceTypeListingFilters = this._getEmptyFilters();

	protected appliedFilterCount: number = 0;
	constructor(
		public enumUtils: EnumUtils,
	) { super(); }

	ngOnInit() {
	}

	ngOnChanges(changes: SimpleChanges): void {
		const filterChange = changes[nameof<ReferenceTypeListingFiltersComponent>(x => x.filter)]?.currentValue as ReferenceTypeFilter;
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
		const { isActive, like } = this.internalFilters ?? {}
		this.filterChange.emit({
			...this.filter,
			like,
			isActive: isActive ? [IsActive.Active] : [IsActive.Inactive]
		})
	}


	private _parseToInternalFilters(inputFilter: ReferenceTypeFilter): ReferenceTypeListingFilters {
		if (!inputFilter) {
			return this._getEmptyFilters();
		}

		let { excludedIds, ids, isActive, like } = inputFilter;

		return {
			isActive: (isActive ?? [])?.includes(IsActive.Active) || !isActive?.length,
			like: like
		}

	}

	private _getEmptyFilters(): ReferenceTypeListingFilters {
		return {
			isActive: true,
			like: null,
		}
	}

	private _computeAppliedFilters(filters: ReferenceTypeListingFilters): number {
		let count = 0;
		if (!filters?.isActive) {
			count++
		}
		if (filters?.like) {
			count++;
		}
		
		return count;
	}

	clearFilters() {
		this.internalFilters = this._getEmptyFilters();
	}
}

interface ReferenceTypeListingFilters {
	isActive: boolean;
	like: string;
}
