import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { AppRole } from '@app/core/common/enum/app-role';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { UserRoleLookup } from '@app/core/query/user-role.lookup';
import { UserFilter } from '@app/core/query/user.lookup';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { nameof } from 'ts-simple-nameof';

@Component({
    selector: 'app-user-listing-filters',
    templateUrl: './user-listing-filters.component.html',
    styleUrls: ['./user-listing-filters.component.scss'],
    standalone: false
})
export class UserListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: UserFilter;
	@Input() hasTenantAdminMode: boolean = false;
	@Output() filterChange = new EventEmitter<UserFilter>();
	appRole = AppRole;

	roles: string[] = []
	appRoleEnumValues = this.enumUtils.getEnumValues<AppRole>(AppRole);

	// * State
	internalFilters: FormGroup<UserListingFilters> = new FormGroup({
        isActive: new FormControl(true),
        like: new FormControl(null),
        roles: new FormControl(null)
    });
    

	protected appliedFilterCount: number = 0;
	constructor(
		public enumUtils: EnumUtils,
	) { super(); }

	ngOnInit() {
	}

	ngOnChanges(changes: SimpleChanges): void {
		const filterChange = changes[nameof<UserListingFiltersComponent>(x => x.filter)]?.currentValue as UserFilter;
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
		const { isActive, like, roles } = this.internalFilters?.value ?? {};
		let filter = {
			...this.filter,
			like,
			isActive: isActive ? [IsActive.Active] : [IsActive.Inactive],
            userRoleSubQuery: roles?.length ? {...new UserRoleLookup(), roles} : null
		}
		this.filterChange.emit(filter);
        this.internalFilters.markAsPristine();
	}


	private _parseToInternalFilters(inputFilter: UserFilter) {
		if (!inputFilter) {
			this._getEmptyFilters();
		}

		let { isActive, like, userRoleSubQuery } = inputFilter;

		this.internalFilters.setValue({
			isActive: (isActive ?? [])?.includes(IsActive.Active) || !isActive?.length,
			like: like ?? null,
			roles: userRoleSubQuery?.roles ?? null
		});

	}

	private _getEmptyFilters() {
		this.internalFilters.setValue({
			isActive: true,
			like: null,
			roles: null
		});
	}

	private _computeAppliedFilters(formGroup: FormGroup<UserListingFilters>): number {
		const filters = formGroup?.value;
        let count = 0;
		if (!filters?.isActive) {
			count++
		}
		if (filters?.like) {
			count++;
		}
		if (filters?.roles?.length) {
			count++;
		}

		return count;
	}

	clearFilters() {
		this._getEmptyFilters();
        this.internalFilters.markAsDirty();
	}
}

interface UserListingFilters {
	isActive: FormControl<boolean>;
	like: FormControl<string>;
	roles: FormControl<string[]>;
}
