import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { DescriptionTemplateTypeStatus } from '@app/core/common/enum/description-template-type-status';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { DescriptionTemplateTypeFilter } from '@app/core/query/description-template-type.lookup';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { nameof } from 'ts-simple-nameof';

@Component({
	selector: 'app-description-template-type-listing-filters',
	templateUrl: './description-template-type-listing-filters.component.html',
	styleUrls: ['./description-template-type-listing-filters.component.scss']
})
export class DescriptionTemplateTypeListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: DescriptionTemplateTypeFilter;
	@Output() filterChange = new EventEmitter<DescriptionTemplateTypeFilter>();

	descriptionTemplateTypeStatusEnumValues = this.enumUtils.getEnumValues<DescriptionTemplateTypeStatus>(DescriptionTemplateTypeStatus);

	// * State
	internalFilters: DescriptionTemplateTypeListingFilters = this._getEmptyFilters();

	protected appliedFilterCount: number = 0;
	constructor(
		public enumUtils: EnumUtils,
	) { super(); }

	ngOnInit() {
	}

	ngOnChanges(changes: SimpleChanges): void {
		const filterChange = changes[nameof<DescriptionTemplateTypeListingFiltersComponent>(x => x.filter)]?.currentValue as DescriptionTemplateTypeFilter;
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


	private _parseToInternalFilters(inputFilter: DescriptionTemplateTypeFilter): DescriptionTemplateTypeListingFilters {
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

	private _getEmptyFilters(): DescriptionTemplateTypeListingFilters {
		return {
			isActive: true,
			like: null,
			statuses: null,
		}
	}

	private _computeAppliedFilters(filters: DescriptionTemplateTypeListingFilters): number {
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

interface DescriptionTemplateTypeListingFilters {
	isActive: boolean;
	like: string;
	statuses: DescriptionTemplateTypeStatus[];
}
