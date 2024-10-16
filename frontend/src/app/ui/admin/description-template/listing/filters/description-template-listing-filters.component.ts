import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { DescriptionTemplateStatus } from '@app/core/common/enum/description-template-status';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { DescriptionTemplateFilter } from '@app/core/query/description-template.lookup';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { Guid } from '@common/types/guid';
import { nameof } from 'ts-simple-nameof';

@Component({
	selector: 'app-description-template-listing-filters',
	templateUrl: './description-template-listing-filters.component.html',
	styleUrls: ['./description-template-listing-filters.component.scss']
})
export class DescriptionTemplateListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: DescriptionTemplateFilter;
	@Output() filterChange = new EventEmitter<DescriptionTemplateFilter>();

	descriptionTemplateStatusEnumValues = this.enumUtils.getEnumValues<DescriptionTemplateStatus>(DescriptionTemplateStatus);

	// * State
	internalFilters: DescriptionTemplateListingFilters = this._getEmptyFilters();

	protected appliedFilterCount: number = 0;
	constructor(
		public enumUtils: EnumUtils,
	) { super(); }

	ngOnInit() {
	}

	ngOnChanges(changes: SimpleChanges): void {
		const filterChange = changes[nameof<DescriptionTemplateListingFiltersComponent>(x => x.filter)]?.currentValue as DescriptionTemplateFilter;
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


	private _parseToInternalFilters(inputFilter: DescriptionTemplateFilter): DescriptionTemplateListingFilters {
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

	private _getEmptyFilters(): DescriptionTemplateListingFilters {
		return {
			isActive: true,
			like: null,
			statuses: null,
		}
	}

	private _computeAppliedFilters(filters: DescriptionTemplateListingFilters): number {
		let count = 0;
		if (!filters?.isActive) {
			count++;
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

interface DescriptionTemplateListingFilters {
	isActive: boolean;
	like: string;
	statuses: DescriptionTemplateStatus[];
}
