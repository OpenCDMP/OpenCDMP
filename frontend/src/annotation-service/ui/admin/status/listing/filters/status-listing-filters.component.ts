import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { BaseComponent } from '@common/base/base.component';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { nameof } from 'ts-simple-nameof';
import { StatusFilter } from '@annotation-service/core/query/status.lookup';
import { InternalStatus } from '@annotation-service/core/enum/internal-status.enum';
import { AnnotationServiceEnumUtils } from '@annotation-service/core/formatting/enum-utils.service';

@Component({
	selector: 'app-status-listing-filters',
	templateUrl: './status-listing-filters.component.html',
	styleUrls: ['./status-listing-filters.component.scss']
})
export class StatusListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: StatusFilter;
	@Output() filterChange = new EventEmitter<StatusFilter>();
	internalStatusEnumValues = this.enumUtils.getEnumValues<InternalStatus>(InternalStatus)

	// * State
	internalFilters: StatusListingFilters = this._getEmptyFilters();

	protected appliedFilterCount: number = 0;
	constructor(
		public enumUtils: AnnotationServiceEnumUtils,
	) { super(); }

	ngOnInit() {
	}

	ngOnChanges(changes: SimpleChanges): void {
		const filterChange = changes[nameof<StatusListingFiltersComponent>(x => x.filter)]?.currentValue as StatusFilter;
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
		const { isActive, internalStatuses, like } = this.internalFilters ?? {}
		this.filterChange.emit({
			...this.filter,
			like: like,
			isActive: isActive ? [IsActive.Active] : [IsActive.Inactive],
			internalStatuses: internalStatuses?.length > 0 ? internalStatuses : null,
		})
	}


	private _parseToInternalFilters(inputFilter: StatusFilter): StatusListingFilters {
		if (!inputFilter) {
			return this._getEmptyFilters();
		}

		let { isActive, internalStatuses, like } = inputFilter;

		return {
			isActive: (isActive ?? [])?.includes(IsActive.Active) || !isActive?.length,
			internalStatuses: internalStatuses,
			like: like
		}

	}

	private _getEmptyFilters(): StatusListingFilters {
		return {
			isActive: true,
			internalStatuses: null,
			like: null,
		}
	}

	private _computeAppliedFilters(filters: StatusListingFilters): number {
		let count = 0;
		if (!filters?.isActive) {
			count++
		}
		if (filters?.like) {
			count++
		}
		if (filters?.internalStatuses?.length) {
			count++
		}

		return count;
	}

	clearFilters() {
		this.internalFilters = this._getEmptyFilters();
	}
}

interface StatusListingFilters {
	like: string;
	isActive: boolean;
	internalStatuses: InternalStatus[];
}
