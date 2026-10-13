import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ChangeDetectionStrategy } from '@angular/core';
import { BaseComponent } from '@common/base/base.component';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { nameof } from 'ts-simple-nameof';
import { StatusFilter } from '@annotation-service/core/query/status.lookup';
import { InternalStatus } from '@annotation-service/core/enum/internal-status.enum';
import { AnnotationServiceEnumUtils } from '@annotation-service/core/formatting/enum-utils.service';
import { FormControl, FormGroup } from '@angular/forms';
import { ListingFilterChipComponent } from '../../../../../../common/modules/admin-listing/listing-filter-chip.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-status-listing-filters',
    templateUrl: './status-listing-filters.component.html',
    styleUrls: ['./status-listing-filters.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [ListingFilterChipComponent, TranslatePipe]
})
export class StatusListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: StatusFilter;
	@Output() filterChange = new EventEmitter<StatusFilter>();
	internalStatusEnumValues = this.enumUtils.getEnumValues<InternalStatus>(InternalStatus)

	// * State
    protected readonly labelOfInternalStatuses = (value: any): string => this.enumUtils.toInternalStatusString(value);

    internalFilters = new FormGroup<StatusListingFilters>({
        internalStatuses: new FormControl<InternalStatus[] | null>(null),
        showDeleted: new FormControl<boolean>(false, { nonNullable: true }),
        like: new FormControl<string | null>(null),
    })

    like = ''
	protected appliedFilterCount: number = 0;
	constructor(
		public enumUtils: AnnotationServiceEnumUtils,
	) { super(); }

	ngOnInit() {
	}

	ngOnChanges(changes: SimpleChanges): void {
		const filterChange = changes[nameof<StatusListingFiltersComponent>(x => x.filter)]?.currentValue as StatusFilter;
		if (filterChange) {
			this.updateFilters()
		}
	}

    get formIsDirty(): boolean {
        return this.internalFilters.controls.showDeleted.dirty || this.internalFilters.controls.internalStatuses.dirty;
    }


	onSearchTermChange(): void {
		this.applyFilters()
	}


	protected updateFilters(): void {
        this._parseToInternalFilters(this.filter);
		this.appliedFilterCount = this._computeAppliedFilters(this.internalFilters);
	}

	protected applyFilters(): void {
		const { showDeleted, internalStatuses, like } = this.internalFilters?.value ?? {}
		this.filterChange.emit({
			...this.filter,
			internalStatuses: internalStatuses?.length > 0 ? internalStatuses : null,
		})
        this.internalFilters.markAsPristine();
	}


	private _parseToInternalFilters(inputFilter: StatusFilter) {
		if (!inputFilter) {
			return this._getEmptyFilters();
		}

		const { isActive, internalStatuses, like } = inputFilter;

		this.internalFilters.setValue({
			showDeleted: (isActive ?? []).includes(IsActive.Inactive),
			internalStatuses: internalStatuses ?? null,
			like: like ?? null,
		});
        this.internalFilters.markAsPristine();
	}

	private _getEmptyFilters() {
		this.internalFilters.setValue({
			showDeleted: false,
			internalStatuses: null,
			like: null,
		})
	}

	private _computeAppliedFilters(form: FormGroup<StatusListingFilters>): number {
        const filters = form.value;
		let count = 0;
		if (filters?.showDeleted) {
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

	clearFilters() {
		this._getEmptyFilters();
        this.internalFilters.controls.internalStatuses.markAsDirty();
        this.internalFilters.controls.showDeleted.markAsDirty();
	}
}

interface StatusListingFilters {
	like: FormControl<string | null>;
	showDeleted: FormControl<boolean>;
	internalStatuses: FormControl<InternalStatus[] | null>;
}
