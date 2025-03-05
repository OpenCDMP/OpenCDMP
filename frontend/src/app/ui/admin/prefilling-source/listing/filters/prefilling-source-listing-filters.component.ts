import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { PrefillingSourceFilter } from '@app/core/query/prefilling-source.lookup';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { nameof } from 'ts-simple-nameof';

@Component({
    selector: 'app-prefilling-source-listing-filters',
    templateUrl: './prefilling-source-listing-filters.component.html',
    styleUrls: ['./prefilling-source-listing-filters.component.scss'],
    standalone: false
})
export class PrefillingSourceListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: PrefillingSourceFilter;
	@Output() filterChange = new EventEmitter<PrefillingSourceFilter>();

	// * State
	internalFilters: FormGroup<PrefillingSourceListingFilters> = new FormGroup({
        isActive: new FormControl(true),
        like: new FormControl(null),
    });
    
	protected appliedFilterCount: number = 0;
    
	constructor(
		public enumUtils: EnumUtils,
	) { super(); }

	ngOnInit() {
	}

	ngOnChanges(changes: SimpleChanges): void {
		const filterChange = changes[nameof<PrefillingSourceListingFiltersComponent>(x => x.filter)]?.currentValue as PrefillingSourceFilter;
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


	private _parseToInternalFilters(inputFilter: PrefillingSourceFilter) {
		if (!inputFilter) {
			this._getEmptyFilters();
		}

		let { isActive, like } = inputFilter;

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

	private _computeAppliedFilters(formGroup: FormGroup<PrefillingSourceListingFilters>): number {
		const filters = formGroup?.value;
        let count = 0;
		if (!filters?.isActive) {
			count++;
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

interface PrefillingSourceListingFilters {
	isActive: FormControl<boolean>;
	like: FormControl<string>;
}
