import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { UsageLimitTargetMetric } from '@app/core/common/enum/usage-limit-target-metric';
import { UsageLimitFilter } from '@app/core/query/usage-limit.lookup';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { nameof } from 'ts-simple-nameof';

@Component({
	selector: 'app-usage-limit-listing-filters',
	templateUrl: './usage-limit-listing-filters.component.html',
	styleUrls: ['./usage-limit-listing-filters.component.scss']
})
export class UsageLimitListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: UsageLimitFilter;
	@Output() filterChange = new EventEmitter<UsageLimitFilter>();
	targetMetricEnumValues = this.enumUtils.getEnumValues<UsageLimitTargetMetric>(UsageLimitTargetMetric);


	// * State
	internalFilters: UsageLimitListingFilters = this._getEmptyFilters();

	protected appliedFilterCount: number = 0;
	constructor(
		public enumUtils: EnumUtils,
	) { super(); }

	ngOnInit() {
	}

	ngOnChanges(changes: SimpleChanges): void {
		const filterChange = changes[nameof<UsageLimitListingFiltersComponent>(x => x.filter)]?.currentValue as UsageLimitFilter;
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
		const { isActive, like, usageLimitTargetMetrics} = this.internalFilters ?? {}
		this.filterChange.emit({
			...this.filter,
			like,
			isActive: isActive ? [IsActive.Active] : [IsActive.Inactive],
			usageLimitTargetMetrics: usageLimitTargetMetrics?.length > 0 ? usageLimitTargetMetrics : null,
		})
	}


	private _parseToInternalFilters(inputFilter: UsageLimitFilter): UsageLimitListingFilters {
		if (!inputFilter) {
			return this._getEmptyFilters();
		}

		let { excludedIds, ids, isActive, like, usageLimitTargetMetrics } = inputFilter;

		return {
			isActive: (isActive ?? [])?.includes(IsActive.Active) || !isActive?.length,
			like: like,
			usageLimitTargetMetrics: usageLimitTargetMetrics
		}

	}

	private _getEmptyFilters(): UsageLimitListingFilters {
		return {
			isActive: true,
			like: null,
			usageLimitTargetMetrics: null
		}
	}

	private _computeAppliedFilters(filters: UsageLimitListingFilters): number {
		let count = 0;
		if (!filters?.isActive) {
			count++
		}
		if (filters?.like) {
			count++;
		}
		if (filters?.usageLimitTargetMetrics?.length) {
			count++;
		}

		return count;
	}

	clearFilters() {
		this.internalFilters = this._getEmptyFilters();
	}
}

interface UsageLimitListingFilters {
	isActive: boolean;
	usageLimitTargetMetrics: UsageLimitTargetMetric[];
	like: string;
}
