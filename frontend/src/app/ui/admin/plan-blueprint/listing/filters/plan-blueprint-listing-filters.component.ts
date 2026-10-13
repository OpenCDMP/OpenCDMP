import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ChangeDetectionStrategy } from '@angular/core';
import { PlanBlueprintStatus } from '@app/core/common/enum/plan-blueprint-status';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { PlanBlueprintType } from '@app/core/model/plan-blueprint-type/plan-blueprint-type';
import { PlanBlueprintFilter } from '@app/core/query/plan-blueprint.lookup';
import { PlanBlueprintTypeLookup } from '@app/core/query/plan-blueprint-type.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { PlanBlueprintTypeService } from '@app/core/services/plan-blueprint-type/plan-blueprint-type.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { Guid } from '@common/types/guid';
import { nameof } from 'ts-simple-nameof';
import { FormControl, FormGroup } from '@angular/forms';
import { takeUntil } from 'rxjs/operators';
import { ListingFilterChipComponent } from '../../../../../../common/modules/admin-listing/listing-filter-chip.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-plan-blueprint-listing-filters',
    templateUrl: './plan-blueprint-listing-filters.component.html',
    styleUrls: ['./plan-blueprint-listing-filters.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [ListingFilterChipComponent, TranslatePipe]
})
export class PlanBlueprintListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: PlanBlueprintFilter;
	@Input() hasSelectedTenant: boolean = false;
	@Output() filterChange = new EventEmitter<PlanBlueprintFilter>();

	planBlueprintStatusEnumValues = this.enumUtils.getEnumValues<PlanBlueprintStatus>(PlanBlueprintStatus);

	// * State
	protected readonly labelOfStatuses = (value: any): string => this.enumUtils.toPlanBlueprintStatusString(value);

	/** Blueprint types offered by the Type chip (active ones, loaded once); values are ids, labels their names. */
	protected typeOptions: Guid[] = [];
	private typeNames = new Map<string, string>();
	protected readonly labelOfTypeIds = (value: Guid): string => this.typeNames.get(value?.toString()) ?? '';

	internalFilters: FormGroup<PlanBlueprintListingFilters> = new FormGroup({
        showDeleted: new FormControl<boolean>(false, { nonNullable: true }),
        like: new FormControl<string | null>(null),
        statuses: new FormControl<PlanBlueprintStatus[] | null>(null),
        typeIds: new FormControl<Guid[] | null>(null),
    })

    get formIsDirty(): boolean {
        return this.internalFilters.controls.showDeleted.dirty || this.internalFilters.controls.statuses.dirty || this.internalFilters.controls.typeIds.dirty;
    }

	protected appliedFilterCount: number = 0;
	constructor(
		public enumUtils: EnumUtils,
		private authService: AuthService,
		private planBlueprintTypeService: PlanBlueprintTypeService,
	) { super(); }

	ngOnInit() {
		const lookup = new PlanBlueprintTypeLookup();
		lookup.page = { offset: 0, size: 100 };
		lookup.isActive = [IsActive.Active];
		lookup.order = { items: [nameof<PlanBlueprintType>(x => x.name)] };
		lookup.project = { fields: [nameof<PlanBlueprintType>(x => x.id), nameof<PlanBlueprintType>(x => x.name)] };
		this.planBlueprintTypeService.query(lookup).pipe(takeUntil(this._destroyed)).subscribe({
			next: result => {
				this.typeNames = new Map((result?.items ?? []).map(t => [t.id.toString(), t.name]));
				this.typeOptions = (result?.items ?? []).map(t => t.id);
			},
			error: () => { this.typeOptions = []; },
		});
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
		const { statuses, typeIds } = this.internalFilters?.value ?? {}
		this.filterChange.emit({
			// search, deleted state and tenant scope live in the listing toolbar and arrive through `filter`
			...this.filter,
			statuses: statuses?.length > 0 ? statuses : null,
			typeIds: typeIds?.length > 0 ? typeIds : null,
		});
        this.internalFilters.markAsPristine();
	}


	private _parseToInternalFilters(inputFilter: PlanBlueprintFilter) {
		if (!inputFilter) {
			return this._getEmptyFilters();
		}

		const { isActive, like, statuses, typeIds } = inputFilter;

		this.internalFilters.setValue({
			showDeleted: (isActive ?? []).includes(IsActive.Inactive),
			like: like ?? null,
			statuses: statuses ?? null,
			typeIds: typeIds ?? null,
		});

	}

	private _getEmptyFilters() {
		this.internalFilters.setValue({
			showDeleted: false,
			like: null,
			statuses: null,
			typeIds: null,
		});
	}

	private _computeAppliedFilters(form: FormGroup<PlanBlueprintListingFilters>): number {
		let count = 0;
        const filters = form.value;
		if (filters?.like) {
			count++;
		}
		if (filters?.statuses?.length) {
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
        this.internalFilters.controls.statuses.markAsDirty();
        this.internalFilters.controls.typeIds.markAsDirty();
	}
}

interface PlanBlueprintListingFilters {
	showDeleted: FormControl<boolean>;
	like: FormControl<string | null>;
	statuses: FormControl<PlanBlueprintStatus[] | null>;
	typeIds: FormControl<Guid[] | null>;
}
