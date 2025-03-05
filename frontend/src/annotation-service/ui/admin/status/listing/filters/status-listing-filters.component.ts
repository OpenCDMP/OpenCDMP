import { Component, computed, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { BaseComponent } from '@common/base/base.component';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { nameof } from 'ts-simple-nameof';
import { StatusFilter } from '@annotation-service/core/query/status.lookup';
import { InternalStatus } from '@annotation-service/core/enum/internal-status.enum';
import { AnnotationServiceEnumUtils } from '@annotation-service/core/formatting/enum-utils.service';
import { FormControl, FormGroup } from '@angular/forms';
import { FormService } from '@common/forms/form-service';

@Component({
    selector: 'app-status-listing-filters',
    templateUrl: './status-listing-filters.component.html',
    styleUrls: ['./status-listing-filters.component.scss'],
    standalone: false
})
export class StatusListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {
    
	@Input() readonly filter: StatusFilter;
	@Output() filterChange = new EventEmitter<StatusFilter>();
	internalStatusEnumValues = this.enumUtils.getEnumValues<InternalStatus>(InternalStatus)

	// * State
    internalFilters = new FormGroup<StatusListingFilters>({
        internalStatuses: new FormControl<InternalStatus[]>(null),
        isActive: new FormControl<boolean>(true),
        like: new FormControl<string>(null)
    })

    like = ''
	protected appliedFilterCount: number = 0;
	constructor(
		public enumUtils: AnnotationServiceEnumUtils	) { super(); }

	ngOnInit() {
	}

	ngOnChanges(changes: SimpleChanges): void {
		const filterChange = changes[nameof<StatusListingFiltersComponent>(x => x.filter)]?.currentValue as StatusFilter;
		if (filterChange) {
			this.updateFilters()
		}
	}

    get formIsDirty(): boolean {
        return this.internalFilters.controls.isActive.dirty || this.internalFilters.controls.internalStatuses.dirty;
    }


	onSearchTermChange(): void {
		this.applyFilters()
	}


	protected updateFilters(): void {
        this._parseToInternalFilters(this.filter);
		this.appliedFilterCount = this._computeAppliedFilters(this.internalFilters);
	}

	protected applyFilters(): void {
		const { isActive, internalStatuses, like } = this.internalFilters?.value ?? {}
		this.filterChange.emit({
			...this.filter,
			like: like ?? null,
			isActive: isActive ? [IsActive.Active] : [IsActive.Inactive],
			internalStatuses: internalStatuses?.length > 0 ? internalStatuses : null,
		})
        this.internalFilters.markAsPristine();
	}


	private _parseToInternalFilters(inputFilter: StatusFilter) {
		if (!inputFilter) {
			this._getEmptyFilters();
		}

		let { isActive, internalStatuses, like } = inputFilter;

		this.internalFilters.setValue({
			isActive: (isActive ?? [])?.includes(IsActive.Active) || !isActive?.length,
			internalStatuses: internalStatuses ?? null,
			like: like ?? null
		});
        this.internalFilters.markAsPristine();
	}

	private _getEmptyFilters() {
		this.internalFilters.setValue({
			isActive: true,
			internalStatuses: null,
			like: null,
		})
	}

	private _computeAppliedFilters(form: FormGroup<StatusListingFilters>): number {
        const filters = form.value;
		let count = 0;
		if (!filters?.isActive) {
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
        this.internalFilters.controls.isActive.markAsDirty();
	}
}

interface StatusListingFilters {
	like: FormControl<string>;
	isActive: FormControl<boolean>;
	internalStatuses: FormControl<InternalStatus[]>;
}
