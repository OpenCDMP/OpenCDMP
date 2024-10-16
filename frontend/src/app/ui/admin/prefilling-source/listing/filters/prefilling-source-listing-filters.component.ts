import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { PrefillingSourceFilter } from '@app/core/query/prefilling-source.lookup';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { nameof } from 'ts-simple-nameof';

@Component({
	selector: 'app-prefilling-source-listing-filters',
	templateUrl: './prefilling-source-listing-filters.component.html',
	styleUrls: ['./prefilling-source-listing-filters.component.scss']
})
export class PrefillingSourceListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: PrefillingSourceFilter;
	@Output() filterChange = new EventEmitter<PrefillingSourceFilter>();

	// * State
	internalFilters: PrefillingSourceListingFilters = this._getEmptyFilters();

	protected appliedFilterCount: number = 0;
	constructor(
		public enumUtils: EnumUtils,
	) { super(); }

	ngOnInit() {
	}

	ngOnChanges(changes: SimpleChanges): void {
		const filterChange = changes[nameof<PrefillingSourceListingFiltersComponent>(x => x.filter)]?.currentValue as PrefillingSourceFilter;
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


	private _parseToInternalFilters(inputFilter: PrefillingSourceFilter): PrefillingSourceListingFilters {
		if (!inputFilter) {
			return this._getEmptyFilters();
		}

		let { excludedIds, ids, isActive, like } = inputFilter;

		return {
			isActive: (isActive ?? [])?.includes(IsActive.Active) || !isActive?.length,
			like: like
		}

	}

	private _getEmptyFilters(): PrefillingSourceListingFilters {
		return {
			isActive: true,
			like: null,
		}
	}

	private _computeAppliedFilters(filters: PrefillingSourceListingFilters): number {
		let count = 0;
		if (!filters?.isActive) {
			count++;
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

interface PrefillingSourceListingFilters {
	isActive: boolean;
	like: string;
}
