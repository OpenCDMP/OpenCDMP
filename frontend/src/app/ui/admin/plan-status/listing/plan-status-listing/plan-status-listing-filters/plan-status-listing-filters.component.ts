import { Component, computed, effect, EventEmitter, input, Input, Output } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { PlanStatusEnum } from '@app/core/common/enum/plan-status';
import { PlanStatusFilter } from '@app/core/query/plan-status.lookup';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';

@Component({
    selector: 'app-plan-status-listing-filters',
    templateUrl: './plan-status-listing-filters.component.html',
    styleUrl: './plan-status-listing-filters.component.scss',
    standalone: false
})
export class PlanStatusListingFiltersComponent extends BaseComponent {
	readonly filter = input<PlanStatusFilter>();
	@Output() filterChange = new EventEmitter<PlanStatusFilter>();

	internalFilters: FormGroup<PlanStatusListingFilters> = new FormGroup({
        isActive: new FormControl(true),
        like: new FormControl(null),
        internalStatuses: new FormControl(null)
    })
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


	private _parseToInternalFilters(inputFilter: PlanStatusFilter) {
		if (!inputFilter) {
			this._getEmptyFilters();
		}

		let { isActive, like, internalStatuses } = inputFilter;

		this.internalFilters.setValue({
			isActive: (isActive ?? [])?.includes(IsActive.Active) || !isActive?.length,
			like: like ?? null,
			internalStatuses: internalStatuses?.length ? internalStatuses : null
		});

	}

	private _computeAppliedFilters(form: FormGroup<PlanStatusListingFilters>): number {
		const filters = form.value;
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

	private _getEmptyFilters() {
		this.internalFilters.setValue({
			isActive: true,
			like: null,
			internalStatuses: null
		});
	}

	protected updateFilters(): void {
		this._parseToInternalFilters(this.filter());
		this.appliedFilterCount = this._computeAppliedFilters(this.internalFilters);
	}

	protected applyFilters(): void {
		const { isActive, like, internalStatuses } = this.internalFilters?.value ?? {}
		this.filterChange.emit({
			...this.filter(),
			like,
			isActive: isActive ? [IsActive.Active] : [IsActive.Inactive],
			internalStatuses: internalStatuses?.length ? internalStatuses : null
		});
        this.internalFilters.markAsPristine();
	}

	protected onSearchTermChange(searchTerm: string): void {
		this.applyFilters();
	}

	protected clearFilters() {
		this._getEmptyFilters();
        this.internalFilters.markAsDirty();
	}
}

interface PlanStatusListingFilters {
	isActive: FormControl<boolean>;
	like: FormControl<string>;
	internalStatuses: FormControl<PlanStatusEnum[]>;
}
