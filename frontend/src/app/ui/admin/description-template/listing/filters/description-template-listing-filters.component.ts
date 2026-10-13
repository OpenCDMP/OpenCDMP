import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ChangeDetectionStrategy } from '@angular/core';
import { FormGroup, FormControl } from '@angular/forms';
import { DescriptionTemplateStatus } from '@app/core/common/enum/description-template-status';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { DescriptionTemplateFilter } from '@app/core/query/description-template.lookup';
import { DescriptionTemplateType } from '@app/core/model/description-template-type/description-template-type';
import { DescriptionTemplateTypeLookup } from '@app/core/query/description-template-type.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { DescriptionTemplateTypeService } from '@app/core/services/description-template-type/description-template-type.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { Guid } from '@common/types/guid';
import { nameof } from 'ts-simple-nameof';
import { takeUntil } from 'rxjs/operators';
import { ListingFilterChipComponent } from '../../../../../../common/modules/admin-listing/listing-filter-chip.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-description-template-listing-filters',
    templateUrl: './description-template-listing-filters.component.html',
    styleUrls: ['./description-template-listing-filters.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [ListingFilterChipComponent, TranslatePipe]
})
export class DescriptionTemplateListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: DescriptionTemplateFilter;
	@Input() hasSelectedTenant: boolean = false;
	@Output() filterChange = new EventEmitter<DescriptionTemplateFilter>();

	descriptionTemplateStatusEnumValues = this.enumUtils.getEnumValues<DescriptionTemplateStatus>(DescriptionTemplateStatus);

	// * State
	protected readonly labelOfStatuses = (value: any): string => this.enumUtils.toDescriptionTemplateTypeStatusString(value);

	/** Template types offered by the Type chip (active ones, loaded once); values are ids, labels their names. */
	protected typeOptions: Guid[] = [];
	private typeNames = new Map<string, string>();
	protected readonly labelOfTypeIds = (value: Guid): string => this.typeNames.get(value?.toString()) ?? '';

	internalFilters: FormGroup<DescriptionTemplateListingFilters> = new FormGroup({
        showDeleted: new FormControl<boolean>(false, { nonNullable: true }),
        like: new FormControl<string | null>(null),
        statuses: new FormControl<DescriptionTemplateStatus[] | null>(null),
        typeIds: new FormControl<Guid[] | null>(null),
    })

    get formIsDirty(): boolean {
        return this.internalFilters.controls.showDeleted.dirty || this.internalFilters.controls.statuses.dirty || this.internalFilters.controls.typeIds.dirty;
    }

	protected appliedFilterCount: number = 0;
	constructor(
		public enumUtils: EnumUtils,
		private authService: AuthService,
		private descriptionTemplateTypeService: DescriptionTemplateTypeService,
	) { super(); }

	ngOnInit() {
		const lookup = new DescriptionTemplateTypeLookup();
		lookup.page = { offset: 0, size: 100 };
		lookup.isActive = [IsActive.Active];
		lookup.order = { items: [nameof<DescriptionTemplateType>(x => x.name)] };
		lookup.project = { fields: [nameof<DescriptionTemplateType>(x => x.id), nameof<DescriptionTemplateType>(x => x.name)] };
		this.descriptionTemplateTypeService.query(lookup).pipe(takeUntil(this._destroyed)).subscribe({
			next: result => {
				this.typeNames = new Map((result?.items ?? []).map(t => [t.id.toString(), t.name]));
				this.typeOptions = (result?.items ?? []).map(t => t.id);
			},
			error: () => { this.typeOptions = []; },
		});
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
		const { statuses, typeIds } = this.internalFilters?.value ?? {}
		this.filterChange.emit({
			// search, deleted state and tenant scope live in the listing toolbar and arrive through `filter`
			...this.filter,
			statuses: statuses?.length > 0 ? statuses : null,
			typeIds: typeIds?.length > 0 ? typeIds : null,
		});
        this.internalFilters.markAsPristine();
	}


	private _parseToInternalFilters(inputFilter: DescriptionTemplateFilter) {
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

	private _computeAppliedFilters(form: FormGroup<DescriptionTemplateListingFilters>): number {
        const filters = form.value;
		let count = 0;
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

interface DescriptionTemplateListingFilters {
	showDeleted: FormControl<boolean>;
	like: FormControl<string | null>;
	statuses: FormControl<DescriptionTemplateStatus[] | null>;
	typeIds: FormControl<Guid[] | null>;
}
