import { Component, computed, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { ReferenceSourceType } from '@app/core/common/enum/reference-source-type';
import { ReferenceFilter } from '@app/core/query/reference.lookup';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { Guid } from '@common/types/guid';
import { nameof } from 'ts-simple-nameof';

@Component({
    selector: 'app-reference-listing-filters',
    templateUrl: './reference-listing-filters.component.html',
    styleUrls: ['./reference-listing-filters.component.scss'],
    standalone: false
})
export class ReferenceListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: ReferenceFilter;
	@Output() filterChange = new EventEmitter<ReferenceFilter>();

	referenceSourceTypeEnumValues = this.enumUtils.getEnumValues<ReferenceSourceType>(ReferenceSourceType)

	// * State
	internalFilters: FormGroup<ReferenceListingFilters> = new FormGroup({
        isActive: new FormControl(true),
        like: new FormControl(null),
        typeIds: new FormControl(null),
        sourceTypes: new FormControl(null)
    })
    
	protected appliedFilterCount: number = 0;
	constructor(
		public enumUtils: EnumUtils,
		public referenceTypeService: ReferenceTypeService
	) { super(); }

	ngOnInit() {
	}

	ngOnChanges(changes: SimpleChanges): void {
		const filterChange = changes[nameof<ReferenceListingFiltersComponent>(x => x.filter)]?.currentValue as ReferenceFilter;
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
		const { isActive, like, typeIds, sourceTypes } = this.internalFilters?.value ?? {}
		this.filterChange.emit({
			...this.filter,
			like,
			isActive: isActive ? [IsActive.Active] : [IsActive.Inactive],
			typeIds: typeIds?.length > 0 ? typeIds : null,
			sourceTypes: sourceTypes?.length > 0 ? sourceTypes : null,
		});
        this.internalFilters.markAsPristine();
	}


	private _parseToInternalFilters(inputFilter: ReferenceFilter) {
		if (!inputFilter) {
			this._getEmptyFilters();
		}

		let { isActive, like, typeIds, sourceTypes } = inputFilter;

		this.internalFilters.setValue({
			isActive: (isActive ?? [])?.includes(IsActive.Active) || !isActive?.length,
			like: like ?? null,
			typeIds: typeIds ?? null,
			sourceTypes: sourceTypes ?? null
		});

	}

	private _getEmptyFilters() {
		this.internalFilters.setValue({
			isActive: true,
			like: null,
			typeIds: null,
			sourceTypes: null
		});
	}

	private _computeAppliedFilters(formGroup: FormGroup<ReferenceListingFilters>): number {
		const filters = formGroup?.value;
        let count = 0;
		if (!filters?.isActive) {
			count++
		}
		if (filters?.like) {
			count++;
		}
		if (filters?.sourceTypes?.length) {
			count++;
		}
		if (filters?.typeIds?.length) {
			count++;
		}

		return count;
	}

	clearFilters() {
		this._getEmptyFilters();
        this.internalFilters.markAsDirty();
	}
}

interface ReferenceListingFilters {
	isActive: FormControl<boolean>;
	like: FormControl<string>;
	typeIds: FormControl<Guid[]>;
	sourceTypes: FormControl<ReferenceSourceType[]>;
}
