import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
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
	styleUrls: ['./lock-listing-filters.component.scss']
})
export class LockListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: LockFilter;
	@Output() filterChange = new EventEmitter<LockFilter>();

	lockTargetTypeEnumValues = this.enumUtils.getEnumValues<LockTargetType>(LockTargetType);

	readonly separatorKeysCodes: number[] = [ENTER, COMMA];

	// * State
	internalFilters: LockListingFilters = this._getEmptyFilters();

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


	onSearchTermChange(searchTerm: string): void {
		this.applyFilters()
	}


	protected updateFilters(): void {
		this.internalFilters = this._parseToInternalFilters(this.filter);
		this.appliedFilterCount = this._computeAppliedFilters(this.internalFilters);
	}

	protected applyFilters(): void {
		const { targetTypes, like, userIds } = this.internalFilters ?? {}
		this.filterChange.emit({
			...this.filter,
			targetTypes: targetTypes?.length > 0 ? targetTypes : null,
			like,
			userIds: userIds?.length > 0 ? userIds : null,
		})
	}


	private _parseToInternalFilters(inputFilter: LockFilter): LockListingFilters {
		if (!inputFilter) {
			return this._getEmptyFilters();
		}

		let { targetTypes, like, userIds } = inputFilter;

		return {
			targetTypes: targetTypes,
			like: like,
			userIds: userIds
		}

	}

	private _getEmptyFilters(): LockListingFilters {
		return {
			targetTypes: null,
			like: null,
			userIds: null

		}
	}

	private _computeAppliedFilters(filters: LockListingFilters): number {
		let count = 0;

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
		this.internalFilters = this._getEmptyFilters();
	}
}

interface LockListingFilters {
	targetTypes: LockTargetType[];
	like: string;
	userIds: Guid[];
}
