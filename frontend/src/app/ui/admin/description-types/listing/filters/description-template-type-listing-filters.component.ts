import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { DescriptionTemplateTypeStatus } from '@app/core/common/enum/description-template-type-status';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { DescriptionTemplateTypeFilter } from '@app/core/query/description-template-type.lookup';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { nameof } from 'ts-simple-nameof';

@Component({
    selector: 'app-description-template-type-listing-filters',
    templateUrl: './description-template-type-listing-filters.component.html',
    styleUrls: ['./description-template-type-listing-filters.component.scss'],
    standalone: false
})
export class DescriptionTemplateTypeListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: DescriptionTemplateTypeFilter;
	@Output() filterChange = new EventEmitter<DescriptionTemplateTypeFilter>();

	descriptionTemplateTypeStatusEnumValues = this.enumUtils.getEnumValues<DescriptionTemplateTypeStatus>(DescriptionTemplateTypeStatus);

	// * State
	internalFilters: FormGroup<DescriptionTemplateTypeListingFilters> = new FormGroup({
        isActive: new FormControl<boolean>(true),
        like: new FormControl<string>(null),
        statuses: new FormControl<DescriptionTemplateTypeStatus[]>(null)
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
		const filterChange = changes[nameof<DescriptionTemplateTypeListingFiltersComponent>(x => x.filter)]?.currentValue as DescriptionTemplateTypeFilter;
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


	private _parseToInternalFilters(inputFilter: DescriptionTemplateTypeFilter) {
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

	private _computeAppliedFilters(form: FormGroup<DescriptionTemplateTypeListingFilters>): number {
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

interface DescriptionTemplateTypeListingFilters {
	isActive: FormControl<boolean>;
	like: FormControl<string>;
	statuses: FormControl<DescriptionTemplateTypeStatus[]>;
}
