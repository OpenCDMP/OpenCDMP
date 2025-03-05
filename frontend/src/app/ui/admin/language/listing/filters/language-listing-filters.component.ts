import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { LanguageFilter } from '@app/core/query/language.lookup';
import { TenantLookup } from '@app/core/query/tenant.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { nameof } from 'ts-simple-nameof';

@Component({
    selector: 'app-language-listing-filters',
    templateUrl: './language-listing-filters.component.html',
    styleUrls: ['./language-listing-filters.component.scss'],
    standalone: false
})
export class LanguageListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: LanguageFilter;
	@Input() hasSelectedTenant: boolean = false;
	@Output() filterChange = new EventEmitter<LanguageFilter>();

	// * State
	internalFilters: FormGroup<LanguageListingFilters> = new FormGroup({
        isActive: new FormControl(true),
        like: new FormControl(null),
        viewOnlyTenant: new FormControl(false)
    })

    get formIsDirty(){
        return this.internalFilters.controls.isActive.dirty || this.internalFilters.controls.viewOnlyTenant.dirty;
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
		const { isActive, like, viewOnlyTenant } = this.internalFilters.value ?? {}
		let tenantSubQuery = null;
		if (viewOnlyTenant && this.authService.getSelectedTenantId()) {
			tenantSubQuery = new TenantLookup();
			tenantSubQuery.ids = [this.authService.getSelectedTenantId()];
		}
		this.filterChange.emit({
			...this.filter,
			like,
			isActive: isActive ? [IsActive.Active] : [IsActive.Inactive],
			viewOnlyTenant: viewOnlyTenant,
			tenantSubQuery: tenantSubQuery
		})
        this.internalFilters.markAsPristine();
	}


	private _parseToInternalFilters(inputFilter: LanguageFilter) {
		if (!inputFilter) {
			this._getEmptyFilters();
		}

		let {  isActive, like, viewOnlyTenant } = inputFilter;

		this.internalFilters.setValue({
			isActive: (isActive ?? [])?.includes(IsActive.Active) || !isActive?.length,
			like: like ?? null,
			viewOnlyTenant: viewOnlyTenant ?? null,
		});

	}

	private _getEmptyFilters() {
		this.internalFilters.setValue({
			isActive: true,
			like: null,
			viewOnlyTenant: false
		});
	}

	private _computeAppliedFilters(form: FormGroup<LanguageListingFilters>): number {
		let count = 0;
        const filters = form.value;
		if (!filters?.isActive) {
			count++
		}
		if (filters?.like) {
			count++;
		}
		if (filters?.viewOnlyTenant) {
			count++;
		}

		return count;
	}

	clearFilters() {
		this._getEmptyFilters();
        this.internalFilters.controls.isActive.markAsDirty();
        this.internalFilters.controls.viewOnlyTenant.markAsDirty();
	}
}

interface LanguageListingFilters {
	isActive: FormControl<boolean>;
	like: FormControl<string>;
	viewOnlyTenant: FormControl<boolean>;
}
