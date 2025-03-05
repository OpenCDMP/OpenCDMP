import { Component, computed, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { SupportiveMaterialFieldType } from '@app/core/common/enum/supportive-material-field-type';
import { SupportiveMaterialFilter } from '@app/core/query/supportive-material.lookup';
import { TenantLookup } from '@app/core/query/tenant.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { nameof } from 'ts-simple-nameof';

@Component({
    selector: 'app-supportive-material-listing-filters',
    templateUrl: './supportive-material-listing-filters.component.html',
    styleUrls: ['./supportive-material-listing-filters.component.scss'],
    standalone: false
})
export class SupportiveMaterialListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: SupportiveMaterialFilter;
	@Input() hasSelectedTenant: boolean = false;
	@Output() filterChange = new EventEmitter<SupportiveMaterialFilter>();

	fieldTypeEnumValues = this.enumUtils.getEnumValues<SupportiveMaterialFieldType>(SupportiveMaterialFieldType)
	// * State
	internalFilters: FormGroup<SupportiveMaterialListingFilters> = new FormGroup({
        isActive: new FormControl(true),
        like: new FormControl(null),
        languageCodes: new FormControl(null),
        types: new FormControl(null),
        viewOnlyTenant: new FormControl(false)
    });
    
	protected appliedFilterCount: number = 0;
	constructor(
		public enumUtils: EnumUtils,
		private authService: AuthService,
	) { super(); }

	ngOnInit() {
	}

	ngOnChanges(changes: SimpleChanges): void {
		const filterChange = changes[nameof<SupportiveMaterialListingFiltersComponent>(x => x.filter)]?.currentValue as SupportiveMaterialFilter;
		if (filterChange) {
			this.updateFilters()
		}
	}


	onSearchTermChange(): void {
		this.applyFilters();
	}


	protected updateFilters(): void {
		this._parseToInternalFilters(this.filter);
		this.appliedFilterCount = this._computeAppliedFilters(this.internalFilters);
	}

	protected applyFilters(): void {
		const { isActive, like, languageCodes, types, viewOnlyTenant } = this.internalFilters?.value ?? {}
		let tenantSubQuery = null;
		if (viewOnlyTenant && this.authService.getSelectedTenantId()) {
			tenantSubQuery = new TenantLookup();
			tenantSubQuery.ids = [this.authService.getSelectedTenantId()];
		}
		this.filterChange.emit({
			...this.filter,
			like,
			isActive: isActive ? [IsActive.Active] : [IsActive.Inactive],
			languageCodes: languageCodes?.length > 0 ? languageCodes : null,
			types: types?.length > 0 ? types : null,
			viewOnlyTenant: viewOnlyTenant,
			tenantSubQuery: tenantSubQuery
		});
        this.internalFilters.markAsPristine();
	}


	private _parseToInternalFilters(inputFilter: SupportiveMaterialFilter) {
		if (!inputFilter) {
			this._getEmptyFilters();
		}

		let { isActive, like, languageCodes, types, viewOnlyTenant } = inputFilter;

		this.internalFilters.setValue({
			isActive: (isActive ?? [])?.includes(IsActive.Active) || !isActive?.length,
			like: like ?? null,
			languageCodes: languageCodes ?? null,
			types: types ?? null,
			viewOnlyTenant: viewOnlyTenant ?? null,
		});

	}

	private _getEmptyFilters() {
		this.internalFilters.setValue({
			isActive: true,
			like: null,
			languageCodes: null,
			types: null,
			viewOnlyTenant: false
		});
	}

	private _computeAppliedFilters(formGroup: FormGroup<SupportiveMaterialListingFilters>): number {
		const filters = formGroup?.value;
        let count = 0;
		if (!filters?.isActive) {
			count++
		}
		if (filters?.like) {
			count++;
		}
		if (filters?.languageCodes) {
			count++;
		}
		if (filters?.types?.length) {
			count++;
		}
		if (filters?.viewOnlyTenant) {
			count++;
		}

		return count;
	}

	clearFilters() {
		this._getEmptyFilters();
        this.internalFilters.markAsDirty();
	}
}

interface SupportiveMaterialListingFilters {
	isActive: FormControl<boolean>;
	like: FormControl<string>;
	languageCodes: FormControl<string[]>;
	types: FormControl<SupportiveMaterialFieldType[]>;
	viewOnlyTenant: FormControl<boolean>;
}
