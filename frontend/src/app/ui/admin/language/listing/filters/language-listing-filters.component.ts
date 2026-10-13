import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ChangeDetectionStrategy } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { LanguageFilter } from '@app/core/query/language.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { nameof } from 'ts-simple-nameof';

@Component({
    selector: 'app-language-listing-filters',
    templateUrl: './language-listing-filters.component.html',
    styleUrls: ['./language-listing-filters.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager
})
export class LanguageListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: LanguageFilter;
	@Input() hasSelectedTenant: boolean = false;
	@Output() filterChange = new EventEmitter<LanguageFilter>();

	// * State
	internalFilters: FormGroup<LanguageListingFilters> = new FormGroup({
        showDeleted: new FormControl<boolean>(false, { nonNullable: true }),
        like: new FormControl(null),
    })

    get formIsDirty(){
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
		const filterChange = changes[nameof<LanguageListingFiltersComponent>(x => x.filter)]?.currentValue as LanguageFilter;
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
		const { showDeleted, like } = this.internalFilters.value ?? {}
		this.filterChange.emit({
			// search, deleted state and tenant scope live in the listing toolbar and arrive through `filter`
			...this.filter,
			like,
		})
        this.internalFilters.markAsPristine();
	}


	private _parseToInternalFilters(inputFilter: LanguageFilter) {
		if (!inputFilter) {
			return this._getEmptyFilters();
		}

		const {  isActive, like } = inputFilter;

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

	private _computeAppliedFilters(form: FormGroup<LanguageListingFilters>): number {
		let count = 0;
        const filters = form.value;
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

interface LanguageListingFilters {
	showDeleted: FormControl<boolean>;
	like: FormControl<string>;
}
