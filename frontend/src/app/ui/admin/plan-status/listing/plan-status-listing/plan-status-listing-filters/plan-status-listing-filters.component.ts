import { Component, effect, EventEmitter, input, Input, Output } from '@angular/core';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { PlanStatusEnum } from '@app/core/common/enum/plan-status';
import { PlanStatusFilter } from '@app/core/query/plan-status.lookup';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';

@Component({
	selector: 'app-plan-status-listing-filters',
	templateUrl: './plan-status-listing-filters.component.html',
	styleUrl: './plan-status-listing-filters.component.scss'
})
export class PlanStatusListingFiltersComponent extends BaseComponent {
	readonly filter = input<PlanStatusFilter>();
	@Output() filterChange = new EventEmitter<PlanStatusFilter>();

	internalFilters: PlanStatusListingFilters = this._getEmptyFilters();
	appliedFilterCount: number = 0;
	internalStatusEnum = this.enumUtils.getEnumValues<PlanStatusEnum>(PlanStatusEnum);

	constructor(protected enumUtils: EnumUtils) {
		super();
		effect(() => {
			const newFilters = this.filter();
			if (newFilters) {
				this.updateFilters();
			}
		})
	}


	private _parseToInternalFilters(inputFilter: PlanStatusFilter): PlanStatusListingFilters {
		if (!inputFilter) {
			return this._getEmptyFilters();
		}

		let { isActive, like, internalStatuses } = inputFilter;

		return {
			isActive: (isActive ?? [])?.includes(IsActive.Active) || !isActive?.length,
			like,
			internalStatuses: internalStatuses?.length ? internalStatuses : null
		}

	}

	private _computeAppliedFilters(filters: PlanStatusListingFilters): number {
		let count = 0;
		if (!filters?.isActive) {
			count++
		}
		if (filters?.like) {
			count++;
		}
		if (filters?.internalStatuses?.length) {
			count++;
		}
		return count;
	}

	private _getEmptyFilters(): PlanStatusListingFilters {
		return {
			isActive: true,
			like: null,
			internalStatuses: null
		}
	}

	protected updateFilters(): void {
		this.internalFilters = this._parseToInternalFilters(this.filter());
		this.appliedFilterCount = this._computeAppliedFilters(this.internalFilters);
	}

	protected applyFilters(): void {
		const { isActive, like, internalStatuses } = this.internalFilters ?? {}
		this.filterChange.emit({
			...this.filter(),
			like,
			isActive: isActive ? [IsActive.Active] : [IsActive.Inactive],
			internalStatuses: internalStatuses?.length ? internalStatuses : null
		})
	}

	protected onSearchTermChange(searchTerm: string): void {
		this.applyFilters();
	}


	protected clearFilters() {
		this.internalFilters = this._getEmptyFilters();
	}
}

interface PlanStatusListingFilters {
	isActive: boolean;
	like: string;
	internalStatuses: PlanStatusEnum[];
}
