import { Lookup } from '@common/model/lookup';
import { Guid } from '@common/types/guid';
import { IsActive } from '../common/enum/is-active.enum';
import { UserRoleLookup } from './user-role.lookup';
import { TenantUserLookup } from './tenant-user.lookup';

export class UserLookup extends Lookup implements UserFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	emails: string[];
	isActive: IsActive[];
	userRoleSubQuery: UserRoleLookup;
	tenantUserSubQuery: TenantUserLookup;

	constructor() {
		super();
	}
}

export interface UserFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	emails: string[];
	isActive: IsActive[];
	userRoleSubQuery: UserRoleLookup;
	tenantUserSubQuery: TenantUserLookup;
}
