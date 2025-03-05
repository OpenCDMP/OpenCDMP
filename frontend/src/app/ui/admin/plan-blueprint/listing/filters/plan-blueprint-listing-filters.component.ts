import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { PlanBlueprintStatus } from '@app/core/common/enum/plan-blueprint-status';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { PlanBlueprintFilter } from '@app/core/query/plan-blueprint.lookup';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { nameof } from 'ts-simple-nameof';
import { FormControl, FormGroup } from '@angular/forms';

@Component({
    selector: 'app-plan-blueprint-listing-filters',
    templateUrl: './plan-blueprint-listing-filters.component.html',
    styleUrls: ['./plan-blueprint-listing-filters.component.scss'],
    standalone: false
})
export class PlanBlueprintListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: PlanBlueprintFilter;
	@Output() filterChange = new EventEmitter<PlanBlueprintFilter>();

	planBlueprintStatusEnumValues = this.enumUtils.getEnumValues<PlanBlueprintStatus>(PlanBlueprintStatus);

	// * State
	internalFilters: FormGroup<PlanBlueprintListingFilters> =new FormGroup({
        isActive: new FormControl(true),
        like: new FormControl(null),
        statuses: new FormControl(null)
    })
    
	protected appliedFilterCount: number = 0;
	constructor(
		public enumUtils: EnumUtils,
	) { super(); }

	ngOnInit() {
	}

	ngOnChanges(changes: SimpleChanges): void {
		const filterChange = changes[nameof<PlanBlueprintListingFiltersComponent>(x => x.filter)]?.currentValue as PlanBlueprintFilter;
		if (filterChange) {
			this.updateFilters();
		}
	}


	onSearchTermChange(searchTerm: string): void {
		this.applyFilters();
	}


	protected updateFilters(): void {
		this._parseToInternalFilters(this.filter);
		this.appliedFilterCount = this._computeAppliedFilters(this.internalFilters);
	}

	protected applyFilters(): void {
		const { isActive, like, statuses } = this.internalFilters?.value ?? {}
		this.filterChange.emit({
			...this.filter,
			like,
			isActive: isActive ? [IsActive.Active] : [IsActive.Inactive],
			statuses: statuses?.length > 0 ? statuses : null,
		});
        this.internalFilters.markAsPristine();
	}


	private _parseToInternalFilters(inputFilter: PlanBlueprintFilter) {
		if (!inputFilter) {
			this._getEmptyFilters();
		}

		let { excludedIds, ids, isActive, like, statuses } = inputFilter;

		this.internalFilters.setValue({
			isActive: (isActive ?? [])?.includes(IsActive.Active) || !isActive?.length,
			like: like ?? null,
			statuses: statuses ?? null
		});

	}

	private _getEmptyFilters() {
		this.internalFilters.setValue({
			isActive: true,
			like: null,
			statuses: null,
		});
	}

	private _computeAppliedFilters(form: FormGroup<PlanBlueprintListingFilters>): number {
		let count = 0;
        const filters = form.value;
		if (!filters?.isActive) {
			count++
		}
		if (filters?.like) {
			count++;
		}
		if (filters?.statuses?.length) {
			count++;
		}
		return count;
	}

	clearFilters() {
		this._getEmptyFilters();
        this.internalFilters.markAsDirty();
	}
}

interface PlanBlueprintListingFilters {
	isActive: FormControl<boolean>;
	like: FormControl<string>;
	statuses: FormControl<PlanBlueprintStatus[]>;
}
