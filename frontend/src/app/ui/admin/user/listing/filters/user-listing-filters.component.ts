import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ChangeDetectionStrategy } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { AppRole } from '@app/core/common/enum/app-role';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { UserRoleLookup } from '@app/core/query/user-role.lookup';
import { UserFilter } from '@app/core/query/user.lookup';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { nameof } from 'ts-simple-nameof';
import { ListingFilterChipComponent } from '../../../../../../common/modules/admin-listing/listing-filter-chip.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-user-listing-filters',
    templateUrl: './user-listing-filters.component.html',
    styleUrls: ['./user-listing-filters.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [ListingFilterChipComponent, TranslatePipe]
})
export class UserListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: UserFilter;
	@Input() hasTenantAdminMode: boolean = false;
	@Output() filterChange = new EventEmitter<UserFilter>();
	appRole = AppRole;

	roles: string[] = []
	appRoleEnumValues = this.enumUtils.getEnumValues<AppRole>(AppRole);

	// * State
	/** Roles offered by the chip: tenant roles always, platform roles only outside tenant-admin mode. */
	protected get roleOptions(): AppRole[] {
		const tenantRoles = [AppRole.TenantAdmin, AppRole.TenantPlanManager, AppRole.TenantConfigManager, AppRole.TenantUser];
		return this.hasTenantAdminMode ? tenantRoles : [AppRole.Admin, AppRole.InstallationAdmin, AppRole.User, ...tenantRoles];
	}
	protected readonly labelOfRoles = (value: AppRole): string => this.enumUtils.toAppRoleString(value);

	internalFilters: FormGroup<UserListingFilters> = new FormGroup({
        showDeleted: new FormControl<boolean>(false, { nonNullable: true }),
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
		const { showDeleted, like, roles } = this.internalFilters?.value ?? {};
		const filter = {
			...this.filter,
            userRoleSubQuery: roles?.length ? {...new UserRoleLookup(), roles} : null
		}
		this.filterChange.emit(filter);
        this.internalFilters.markAsPristine();
	}


	private _parseToInternalFilters(inputFilter: UserFilter) {
		if (!inputFilter) {
			return this._getEmptyFilters();
		}

		const { isActive, like, userRoleSubQuery } = inputFilter;

		this.internalFilters.setValue({
			showDeleted: (isActive ?? []).includes(IsActive.Inactive),
			like: like ?? null,
			roles: userRoleSubQuery?.roles ?? null
		});

	}

	private _getEmptyFilters() {
		this.internalFilters.setValue({
			showDeleted: false,
			like: null,
			roles: null
		});
	}

	private _computeAppliedFilters(formGroup: FormGroup<UserListingFilters>): number {
		const filters = formGroup?.value;
        let count = 0;
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
	showDeleted: FormControl<boolean>;
	like: FormControl<string>;
	roles: FormControl<string[]>;
}
