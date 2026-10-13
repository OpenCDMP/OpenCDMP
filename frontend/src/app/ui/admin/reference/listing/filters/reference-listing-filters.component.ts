import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ChangeDetectionStrategy } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { ReferenceSourceType } from '@app/core/common/enum/reference-source-type';
import { ReferenceFilter } from '@app/core/query/reference.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { Guid } from '@common/types/guid';
import { nameof } from 'ts-simple-nameof';
import { ListingFilterChipComponent } from '../../../../../../common/modules/admin-listing/listing-filter-chip.component';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { ReactiveAsteriskDirective } from '../../../../../../common/forms/reactive-asterisk-directive';
import { MultipleAutoCompleteComponent } from '../../../../../library/auto-complete/multiple/multiple-auto-complete.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-reference-listing-filters',
    templateUrl: './reference-listing-filters.component.html',
    styleUrls: ['./reference-listing-filters.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [ListingFilterChipComponent, MatFormField, MatLabel, ReactiveAsteriskDirective, MultipleAutoCompleteComponent, FormsModule, ReactiveFormsModule, TranslatePipe]
})
export class ReferenceListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: ReferenceFilter;
	@Input() hasSelectedTenant: boolean = false;
	@Output() filterChange = new EventEmitter<ReferenceFilter>();

	referenceSourceTypeEnumValues = this.enumUtils.getEnumValues<ReferenceSourceType>(ReferenceSourceType)

	// * State
	protected readonly labelOfSourceTypes = (value: any): string => this.enumUtils.toReferenceSourceTypeString(value);

	/** The "More filters" chip lights up from this control. */
	get moreFiltersControl() { return this.internalFilters.controls.typeIds; }
	get moreFiltersSummary(): string | null { const v = this.internalFilters.controls.typeIds.value; return Array.isArray(v) && v.length ? String(v.length) : (v ? '1' : null); }

	internalFilters: FormGroup<ReferenceListingFilters> = new FormGroup({
        showDeleted: new FormControl<boolean>(false, { nonNullable: true }),
        like: new FormControl<string | null>(null),
        typeIds: new FormControl<Guid[] | null>(null),
        sourceTypes: new FormControl<ReferenceSourceType[] | null>(null),
    })

    get formIsDirty(): boolean {
        return this.internalFilters.controls.showDeleted.dirty || this.internalFilters.controls.sourceTypes.dirty || this.internalFilters.controls.typeIds.dirty;
    }

	protected appliedFilterCount: number = 0;
	constructor(
		public enumUtils: EnumUtils,
		public referenceTypeService: ReferenceTypeService,
		private authService: AuthService,
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
		const { showDeleted, like, typeIds, sourceTypes } = this.internalFilters?.value ?? {}
		this.filterChange.emit({
			// search, deleted state and tenant scope live in the listing toolbar and arrive through `filter`
			...this.filter,
			typeIds: typeIds?.length > 0 ? typeIds : null,
			sourceTypes: sourceTypes?.length > 0 ? sourceTypes : null,
		});
        this.internalFilters.markAsPristine();
	}


	private _parseToInternalFilters(inputFilter: ReferenceFilter) {
		if (!inputFilter) {
			return this._getEmptyFilters();
		}

		const { isActive, like, typeIds, sourceTypes } = inputFilter;

		this.internalFilters.setValue({
			showDeleted: (isActive ?? []).includes(IsActive.Inactive),
			like: like ?? null,
			typeIds: typeIds ?? null,
			sourceTypes: sourceTypes ?? null,
		});

	}

	private _getEmptyFilters() {
		this.internalFilters.setValue({
			showDeleted: false,
			like: null,
			typeIds: null,
			sourceTypes: null,
		});
	}

	private _computeAppliedFilters(formGroup: FormGroup<ReferenceListingFilters>): number {
		const filters = formGroup?.value;
        let count = 0;
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
        this.internalFilters.controls.showDeleted.markAsDirty();
	}
}

interface ReferenceListingFilters {
	showDeleted: FormControl<boolean>;
	like: FormControl<string | null>;
	typeIds: FormControl<Guid[] | null>;
	sourceTypes: FormControl<ReferenceSourceType[] | null>;
}
