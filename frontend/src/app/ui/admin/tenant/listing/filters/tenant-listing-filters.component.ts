import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ChangeDetectionStrategy } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { TenantFilter } from '@app/core/query/tenant.lookup';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { nameof } from 'ts-simple-nameof';

@Component({
    selector: 'app-tenant-listing-filters',
    templateUrl: './tenant-listing-filters.component.html',
    styleUrls: ['./tenant-listing-filters.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager
})
export class TenantListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: TenantFilter;
	@Output() filterChange = new EventEmitter<TenantFilter>();

	// * State
	internalFilters: FormGroup<TenantListingFilters> = new FormGroup({
        showDeleted: new FormControl<boolean>(false, { nonNullable: true }),
        like: new FormControl(null),
    })
    

	protected appliedFilterCount: number = 0;
	constructor(
		public enumUtils: EnumUtils,
	) { super(); }

	ngOnInit() {
	}

	ngOnChanges(changes: SimpleChanges): void {
		const filterChange = changes[nameof<TenantListingFiltersComponent>(x => x.filter)]?.currentValue as TenantFilter;
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
		const { showDeleted, like } = this.internalFilters?.value ?? {}
		this.filterChange.emit({
			// search, deleted state and tenant scope live in the listing toolbar and arrive through `filter`
			...this.filter,
			like,
		});
        this.internalFilters.markAsPristine();
	}


	private _parseToInternalFilters(inputFilter: TenantFilter) {
		if (!inputFilter) {
			return this._getEmptyFilters();
		}

		const { isActive, like } = inputFilter;

		this.internalFilters.setValue({
			showDeleted: (isActive ?? []).includes(IsActive.Inactive),
			like: like ?? null
		});

	}

	private _getEmptyFilters() {
		this.internalFilters.setValue({
			showDeleted: false,
			like: null,
		});
	}

	private _computeAppliedFilters(formGroup: FormGroup<TenantListingFilters>): number {
		const filters = formGroup?.value;
        let count = 0;
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

interface TenantListingFilters {
    showDeleted: FormControl<boolean>;
    like: FormControl<string>;
}
