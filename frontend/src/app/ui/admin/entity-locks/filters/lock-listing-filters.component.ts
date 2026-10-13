import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ChangeDetectionStrategy } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { LockTargetType } from '@app/core/common/enum/lock-target-type';
import { LockFilter } from '@app/core/query/lock.lookup';
import { UserService } from '@app/core/services/user/user.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { Guid } from '@common/types/guid';
import { nameof } from 'ts-simple-nameof';
import { ListingFilterChipComponent } from '../../../../../common/modules/admin-listing/listing-filter-chip.component';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { ReactiveAsteriskDirective } from '../../../../../common/forms/reactive-asterisk-directive';
import { MultipleAutoCompleteComponent } from '../../../../library/auto-complete/multiple/multiple-auto-complete.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-lock-listing-filters',
    templateUrl: './lock-listing-filters.component.html',
    styleUrls: ['./lock-listing-filters.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [ListingFilterChipComponent, MatFormField, MatLabel, ReactiveAsteriskDirective, MultipleAutoCompleteComponent, FormsModule, ReactiveFormsModule, TranslatePipe]
})
export class LockListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: LockFilter;
	@Output() filterChange = new EventEmitter<LockFilter>();

	lockTargetTypeEnumValues = this.enumUtils.getEnumValues<LockTargetType>(LockTargetType);

	readonly separatorKeysCodes: number[] = [ENTER, COMMA];

	// * State
	protected readonly labelOfTargetTypes = (value: any): string => this.enumUtils.toLockTargetTypeString(value);
	/** The "More filters" chip lights up from this control. */
	get moreFiltersControl() { return this.internalFilters.controls.userIds; }
	get moreFiltersSummary(): string | null { const v = this.internalFilters.controls.userIds.value; return Array.isArray(v) && v.length ? String(v.length) : (v ? '1' : null); }

	internalFilters: FormGroup<LockListingFilters> = new FormGroup({
        like: new FormControl(null),
        targetTypes: new FormControl(null),
        userIds: new FormControl(null)
    });
    get formIsDirty(): boolean {
        return this.internalFilters.controls.targetTypes.dirty || this.internalFilters.controls.userIds.dirty;
    }

	protected appliedFilterCount: number = 0;
	constructor(
		public enumUtils: EnumUtils,
		public userService: UserService,
	) { super(); }

	ngOnInit() {
	}

	ngOnChanges(changes: SimpleChanges): void {
		const filterChange = changes[nameof<LockListingFiltersComponent>(x => x.filter)]?.currentValue as LockFilter;
		if (filterChange) {
			this.updateFilters()
		}
	}


	protected updateFilters(): void {
		this._parseToInternalFilters(this.filter);
		this.appliedFilterCount = this._computeAppliedFilters(this.internalFilters);
	}

	protected applyFilters(): void {
		const { targetTypes, userIds } = this.internalFilters.value ?? {}
		this.filterChange.emit({
			// search lives in the listing toolbar and arrives through `filter`
			...this.filter,
			targetTypes: targetTypes?.length > 0 ? targetTypes : null,
			userIds: userIds?.length > 0 ? userIds : null,
		});
        this.internalFilters.markAsPristine();
	}


	private _parseToInternalFilters(inputFilter: LockFilter) {
		if (!inputFilter) {
			return this._getEmptyFilters();
		}

		const { targetTypes, like, userIds } = inputFilter;

		this.internalFilters.setValue({
			targetTypes: targetTypes ?? null,
			like: like ?? null,
			userIds: userIds ?? null
		});

	}

	private _getEmptyFilters() {
		this.internalFilters.setValue({
			targetTypes: null,
			like: null,
			userIds: null
		});
	}

	private _computeAppliedFilters(form: FormGroup<LockListingFilters>): number {
		let count = 0;
        const filters = form.value;
		if (filters?.like) {
			count++;
		}
		if (filters?.targetTypes?.length) {
			count++;
		}
		if (filters?.userIds?.length) {
			count++;
		}

		return count;
	}

	clearFilters() {
		this._getEmptyFilters();
        this.internalFilters.controls.targetTypes.markAsDirty();
        this.internalFilters.controls.userIds.markAsDirty();
	}
}

interface LockListingFilters {
	targetTypes: FormControl<LockTargetType[]>;
	like: FormControl<string>;
	userIds: FormControl<Guid[]>;
}
