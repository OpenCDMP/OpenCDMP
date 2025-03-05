import { Component, computed, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { ReferenceTypeFilter } from '@app/core/query/reference-type.lookup';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { nameof } from 'ts-simple-nameof';

@Component({
    selector: 'app-reference-type-listing-filters',
    templateUrl: './reference-type-listing-filters.component.html',
    styleUrls: ['./reference-type-listing-filters.component.scss'],
    standalone: false
})
export class ReferenceTypeListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: ReferenceTypeFilter;
	@Output() filterChange = new EventEmitter<ReferenceTypeFilter>();

	// * State
	internalFilters: FormGroup<ReferenceTypeListingFilters> = new FormGroup({
        isActive: new FormControl(true),
        like: new FormControl(null),
    })
    

	protected appliedFilterCount: number = 0;
	constructor(
		public enumUtils: EnumUtils
	) { super(); }

	ngOnInit() {
	}

	ngOnChanges(changes: SimpleChanges): void {
		const filterChange = changes[nameof<ReferenceTypeListingFiltersComponent>(x => x.filter)]?.currentValue as ReferenceTypeFilter;
		if (filterChange) {
			this.updateFilters()
		}
	}


	onSearchTermChange(searchTerm: string): void {
		this.applyFilters()
	}


	protected updateFilters(): void {
		this._parseToInternalFilters(this.filter);
		this.appliedFilterCount = this._computeAppliedFilters(this.internalFilters);
	}

	protected applyFilters(): void {
		const { isActive, like } = this.internalFilters?.value ?? {}
		this.filterChange.emit({
			...this.filter,
			like,
			isActive: isActive ? [IsActive.Active] : [IsActive.Inactive]
		});
        this.internalFilters.markAsPristine();
	}


	private _parseToInternalFilters(inputFilter: ReferenceTypeFilter) {
		if (!inputFilter) {
			return this._getEmptyFilters();
		}

		let { excludedIds, ids, isActive, like } = inputFilter;

		this.internalFilters.setValue({
			isActive: (isActive ?? [])?.includes(IsActive.Active) || !isActive?.length,
			like: like ?? null
		});

	}

	private _getEmptyFilters() {
		this.internalFilters.setValue({
			isActive: true,
			like: null,
		});
	}

	private _computeAppliedFilters(formGroup: FormGroup<ReferenceTypeListingFilters>): number {
		const filters = formGroup?.value;
        let count = 0;
		if (!filters?.isActive) {
			count++
		}
		if (filters?.like) {
			count++;
		}
		
		return count;
	}

	clearFilters() {
		this._getEmptyFilters();
        this.internalFilters.markAsDirty();
	}
}

interface ReferenceTypeListingFilters {
    isActive: FormControl<boolean>;
    like: FormControl<string>;
}
