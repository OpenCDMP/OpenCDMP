import { Component, computed, effect, EventEmitter, HostBinding, input, Output } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { DescriptionStatusEnum } from '@app/core/common/enum/description-status';
import { DescriptionStatusFilter } from '@app/core/query/description-status.lookup';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { FormService } from '@common/forms/form-service';
import { IsActive } from '@notification-service/core/enum/is-active.enum';

@Component({
    selector: 'app-description-status-listing-filters',
    templateUrl: './description-status-listing-filters.component.html',
    styleUrl: './description-status-listing-filters.component.scss',
    standalone: false
})
export class DescriptionStatusListingFiltersComponent extends BaseComponent{
    
    readonly filter = input<DescriptionStatusFilter>();
	@Output() filterChange = new EventEmitter<DescriptionStatusFilter>();

    internalFilters: FormGroup<DescriptionStatusListingFilters> = new FormGroup({
        internalStatuses: new FormControl<DescriptionStatusEnum[]>(null),
        isActive: new FormControl<boolean>(true),
        like: new FormControl<string>(null)
    })
    appliedFilterCount: number = 0;

    get formIsDirty(): boolean {
        return this.internalFilters.controls.isActive.dirty || this.internalFilters.controls.internalStatuses.dirty;
    }


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


	private _parseToInternalFilters(inputFilter: DescriptionStatusFilter) {
		if (!inputFilter) {
			this._getEmptyFilters();
		}

		let { isActive, like, internalStatuses } = inputFilter;

		this.internalFilters.setValue({
			isActive: (isActive ?? [])?.includes(IsActive.Active) || !isActive?.length,
			internalStatuses: internalStatuses ?? null,
			like: like ?? null
		});
        this.internalFilters.markAsPristine();

	}

    private _computeAppliedFilters(form: FormGroup<DescriptionStatusListingFilters>): number {
        const filters = form.value;
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

    private _getEmptyFilters() {
		this.internalFilters.setValue({
			isActive: true,
			internalStatuses: null,
			like: null,
		})
	}

    protected updateFilters(): void {
		this._parseToInternalFilters(this.filter());
		this.appliedFilterCount = this._computeAppliedFilters(this.internalFilters);
	}

    protected applyFilters(): void {
		const { isActive, like, internalStatuses } = this.internalFilters.value ?? {}
		this.filterChange.emit({
			...this.filter(),
			like,
			isActive: isActive ? [IsActive.Active] : [IsActive.Inactive],
            internalStatuses: internalStatuses?.length ? internalStatuses : null
		});
        this.internalFilters.markAsPristine();
	}

    protected onSearchTermChange(): void {
		this.applyFilters();
	}


    protected clearFilters() {
		this._getEmptyFilters();
        this.internalFilters.controls.internalStatuses.markAsDirty();
        this.internalFilters.controls.isActive.markAsDirty();
	}
}

interface DescriptionStatusListingFilters {
	isActive: FormControl<boolean>;
	like: FormControl<string>;
    internalStatuses: FormControl<DescriptionStatusEnum[]>;
}
