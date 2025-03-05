import { Component, computed, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormGroup, FormControl } from '@angular/forms';
import { DescriptionTemplateStatus } from '@app/core/common/enum/description-template-status';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { DescriptionTemplateFilter } from '@app/core/query/description-template.lookup';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { nameof } from 'ts-simple-nameof';

@Component({
    selector: 'app-description-template-listing-filters',
    templateUrl: './description-template-listing-filters.component.html',
    styleUrls: ['./description-template-listing-filters.component.scss'],
    standalone: false
})
export class DescriptionTemplateListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: DescriptionTemplateFilter;
	@Output() filterChange = new EventEmitter<DescriptionTemplateFilter>();

	descriptionTemplateStatusEnumValues = this.enumUtils.getEnumValues<DescriptionTemplateStatus>(DescriptionTemplateStatus);

	// * State
	internalFilters: FormGroup<DescriptionTemplateListingFilters> = new FormGroup({
        isActive: new FormControl<boolean>(true),
        like: new FormControl<string>(null),
        statuses: new FormControl<DescriptionTemplateStatus[]>(null)
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
		const filterChange = changes[nameof<DescriptionTemplateListingFiltersComponent>(x => x.filter)]?.currentValue as DescriptionTemplateFilter;
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
		const { isActive, like, statuses } = this.internalFilters?.value ?? {}
		this.filterChange.emit({
			...this.filter,
			like,
			isActive: isActive ? [IsActive.Active] : [IsActive.Inactive],
			statuses: statuses?.length > 0 ? statuses : null, 
		});
        this.internalFilters.markAsPristine();
	}


	private _parseToInternalFilters(inputFilter: DescriptionTemplateFilter) {
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

	private _computeAppliedFilters(form: FormGroup<DescriptionTemplateListingFilters>): number {
        const filters = form.value;
		let count = 0;
		if (!filters?.isActive) {
			count++;
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

interface DescriptionTemplateListingFilters {
	isActive: FormControl<boolean>;
	like: FormControl<string>;
	statuses: FormControl<DescriptionTemplateStatus[]>;
}
