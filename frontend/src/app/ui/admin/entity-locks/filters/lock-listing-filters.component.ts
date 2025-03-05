import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, computed, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { LockTargetType } from '@app/core/common/enum/lock-target-type';
import { LockFilter } from '@app/core/query/lock.lookup';
import { UserService } from '@app/core/services/user/user.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { Guid } from '@common/types/guid';
import { nameof } from 'ts-simple-nameof';

@Component({
    selector: 'app-lock-listing-filters',
    templateUrl: './lock-listing-filters.component.html',
    styleUrls: ['./lock-listing-filters.component.scss'],
    standalone: false
})
export class LockListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: LockFilter;
	@Output() filterChange = new EventEmitter<LockFilter>();

	lockTargetTypeEnumValues = this.enumUtils.getEnumValues<LockTargetType>(LockTargetType);

	readonly separatorKeysCodes: number[] = [ENTER, COMMA];

	// * State
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
		const { targetTypes, like, userIds } = this.internalFilters.value ?? {}
		this.filterChange.emit({
			...this.filter,
			targetTypes: targetTypes?.length > 0 ? targetTypes : null,
			like,
			userIds: userIds?.length > 0 ? userIds : null,
		});
        this.internalFilters.markAsPristine();
	}


	private _parseToInternalFilters(inputFilter: LockFilter) {
		if (!inputFilter) {
			return this._getEmptyFilters();
		}

		let { targetTypes, like, userIds } = inputFilter;

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
