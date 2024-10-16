import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { PlanBlueprintStatus } from '@app/core/common/enum/plan-blueprint-status';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { PlanBlueprintFilter } from '@app/core/query/plan-blueprint.lookup';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { nameof } from 'ts-simple-nameof';

@Component({
	selector: 'app-plan-blueprint-listing-filters',
	templateUrl: './plan-blueprint-listing-filters.component.html',
	styleUrls: ['./plan-blueprint-listing-filters.component.scss']
})
export class PlanBlueprintListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: PlanBlueprintFilter;
	@Output() filterChange = new EventEmitter<PlanBlueprintFilter>();

	planBlueprintStatusEnumValues = this.enumUtils.getEnumValues<PlanBlueprintStatus>(PlanBlueprintStatus);

	// * State
	internalFilters: PlanBlueprintListingFilters = this._getEmptyFilters();

	protected appliedFilterCount: number = 0;
	constructor(
		public enumUtils: EnumUtils,
	) { super(); }

	ngOnInit() {
	}

	ngOnChanges(changes: SimpleChanges): void {
		const filterChange = changes[nameof<PlanBlueprintListingFiltersComponent>(x => x.filter)]?.currentValue as PlanBlueprintFilter;
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
		const { isActive, like, statuses } = this.internalFilters ?? {}
		this.filterChange.emit({
			...this.filter,
			like,
			isActive: isActive ? [IsActive.Active] : [IsActive.Inactive],
			statuses: statuses?.length > 0 ? statuses : null,
		})
	}


	private _parseToInternalFilters(inputFilter: PlanBlueprintFilter): PlanBlueprintListingFilters {
		if (!inputFilter) {
			return this._getEmptyFilters();
		}

		let { excludedIds, ids, isActive, like, statuses } = inputFilter;

		return {
			isActive: (isActive ?? [])?.includes(IsActive.Active) || !isActive?.length,
			like: like,
			statuses: statuses
		}

	}

	private _getEmptyFilters(): PlanBlueprintListingFilters {
		return {
			isActive: true,
			like: null,
			statuses: null,
		}
	}

	private _computeAppliedFilters(filters: PlanBlueprintListingFilters): number {
		let count = 0;
		if (!filters?.isActive) {
			count++
		}
		if (filters?.like) {
			count++;
		}
		if (filters?.statuses?.length) {
			count++;
		}
		return count;
	}

	clearFilters() {
		this.internalFilters = this._getEmptyFilters();
	}
}

interface PlanBlueprintListingFilters {
	isActive: boolean;
	like: string;
	statuses: PlanBlueprintStatus[];
}
