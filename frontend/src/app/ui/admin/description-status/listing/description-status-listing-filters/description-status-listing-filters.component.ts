import { Component, effect, EventEmitter, input, Output } from '@angular/core';
import { DescriptionStatusEnum } from '@app/core/common/enum/description-status';
import { DescriptionStatusFilter } from '@app/core/query/description-status.lookup';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { IsActive } from '@notification-service/core/enum/is-active.enum';

@Component({
  selector: 'app-description-status-listing-filters',
  templateUrl: './description-status-listing-filters.component.html',
  styleUrl: './description-status-listing-filters.component.scss'
})
export class DescriptionStatusListingFiltersComponent extends BaseComponent{
    readonly filter = input<DescriptionStatusFilter>();
	@Output() filterChange = new EventEmitter<DescriptionStatusFilter>();

    internalFilters: DescriptionStatusListingFilters = this._getEmptyFilters();
    appliedFilterCount: number = 0;

    internalStatusEnum = this.enumUtils.getEnumValues<DescriptionStatusEnum>(DescriptionStatusEnum);

    constructor(protected enumUtils: EnumUtils){
        super();
        effect(() => {
            const newFilters = this.filter();
            if(newFilters){
                this.updateFilters();
            }
        })
    }


	private _parseToInternalFilters(inputFilter: DescriptionStatusFilter): DescriptionStatusListingFilters {
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

	private _computeAppliedFilters(filters: DescriptionStatusListingFilters): number {
		let count = 0;
		if (!filters?.isActive) {
			count++
		}
        if(filters?.like){
            count++;
        }
		return count;
	}

    private _getEmptyFilters(): DescriptionStatusListingFilters {
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

interface DescriptionStatusListingFilters {
	isActive: boolean;
	like: string;
    internalStatuses: DescriptionStatusEnum[];
}
