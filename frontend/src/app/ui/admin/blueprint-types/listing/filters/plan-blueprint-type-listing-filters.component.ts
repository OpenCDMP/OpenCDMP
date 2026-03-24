import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { PlanBlueprintTypeStatus } from '@app/core/common/enum/plan-blueprint-type-status';
import { PlanBlueprintTypeFilter } from '@app/core/query/plan-blueprint-type.lookup';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { nameof } from 'ts-simple-nameof';

@Component({
    selector: 'app-plan-blueprint-type-listing-filters',
    templateUrl: './plan-blueprint-type-listing-filters.component.html',
    styleUrls: ['./plan-blueprint-type-listing-filters.component.scss'],
    standalone: false
})
export class PlanBlueprintTypeListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: PlanBlueprintTypeFilter;
	@Output() filterChange = new EventEmitter<PlanBlueprintTypeFilter>();

	planBlueprintTypeStatusEnumValues = this.enumUtils.getEnumValues<PlanBlueprintTypeStatus>(PlanBlueprintTypeStatus);

	// * State
	internalFilters: FormGroup<PlanBlueprintTypeListingFilters> = new FormGroup({
        isActive: new FormControl<boolean>(true),
        like: new FormControl<string>(null),
        statuses: new FormControl<PlanBlueprintTypeStatus[]>(null)
    })
    get formIsDirty(): boolean {
        return this.internalFilters.controls.isActive.dirty || this.internalFilters.controls.statuses.dirty;
    }
    
	protected appliedFilterCount: number = 0;
	constructor(
		public enumUtils: EnumUtils
	) { super(); }

	ngOnInit() {
	}

	ngOnChanges(changes: SimpleChanges): void {
		const filterChange = changes[nameof<PlanBlueprintTypeListingFiltersComponent>(x => x.filter)]?.currentValue as PlanBlueprintTypeFilter;
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
		const { isActive, like, statuses } = this.internalFilters.value ?? {}
		this.filterChange.emit({
			...this.filter,
			like,
			isActive: isActive ? [IsActive.Active] : [IsActive.Inactive],
			statuses: statuses?.length > 0 ? statuses : null,
		});
        this.internalFilters.markAsPristine();
	}


	private _parseToInternalFilters(inputFilter: PlanBlueprintTypeFilter) {
		if (!inputFilter) {
			this._getEmptyFilters();
		}

		let { isActive, like, statuses } = inputFilter;

		this.internalFilters.setValue({
			isActive: (isActive ?? [])?.includes(IsActive.Active) || !isActive?.length,
			like: like ?? null,
			statuses: statuses ?? null
		});

	}

	private _getEmptyFilters(){
		this.internalFilters.setValue({
			isActive: true,
			like: null,
			statuses: null,
		});
	}

	private _computeAppliedFilters(form: FormGroup<PlanBlueprintTypeListingFilters>): number {
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
        this.internalFilters.controls.isActive.markAsDirty();
        this.internalFilters.controls.statuses.markAsDirty();
	}
}

interface PlanBlueprintTypeListingFilters {
	isActive: FormControl<boolean>;
	like: FormControl<string>;
	statuses: FormControl<PlanBlueprintTypeStatus[]>;
}
