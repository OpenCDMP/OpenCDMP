import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ChangeDetectionStrategy } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { PrefillingSourceFilter } from '@app/core/query/prefilling-source.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { nameof } from 'ts-simple-nameof';

@Component({
    selector: 'app-prefilling-source-listing-filters',
    templateUrl: './prefilling-source-listing-filters.component.html',
    styleUrls: ['./prefilling-source-listing-filters.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager
})
export class PrefillingSourceListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: PrefillingSourceFilter;
	@Input() hasSelectedTenant: boolean = false;
	@Output() filterChange = new EventEmitter<PrefillingSourceFilter>();

	// * State
	internalFilters: FormGroup<PrefillingSourceListingFilters> = new FormGroup({
        showDeleted: new FormControl<boolean>(false, { nonNullable: true }),
        like: new FormControl<string | null>(null),
    });

    get formIsDirty(): boolean {
        return this.internalFilters.controls.showDeleted.dirty;
    }

	protected appliedFilterCount: number = 0;

	constructor(
		public enumUtils: EnumUtils,
		private authService: AuthService,
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
		const { showDeleted, like } = this.internalFilters?.value ?? {}
		this.filterChange.emit({
			// search, deleted state and tenant scope live in the listing toolbar and arrive through `filter`
			...this.filter,
		});
        this.internalFilters.markAsPristine();
	}


	private _parseToInternalFilters(inputFilter: PrefillingSourceFilter) {
		if (!inputFilter) {
			return this._getEmptyFilters();
		}

		const { isActive, like } = inputFilter;

		this.internalFilters.setValue({
			showDeleted: (isActive ?? []).includes(IsActive.Inactive),
			like: like ?? null,
		});

	}

	private _getEmptyFilters() {
		this.internalFilters.setValue({
			showDeleted: false,
			like: null,
		});
	}

	private _computeAppliedFilters(formGroup: FormGroup<PrefillingSourceListingFilters>): number {
		const filters = formGroup?.value;
        let count = 0;
		if (filters?.like) {
			count++;
		}

		return count;
	}

	clearFilters() {
		this._getEmptyFilters();
        this.internalFilters.controls.showDeleted.markAsDirty();
	}
}

interface PrefillingSourceListingFilters {
	showDeleted: FormControl<boolean>;
	like: FormControl<string | null>;
}
