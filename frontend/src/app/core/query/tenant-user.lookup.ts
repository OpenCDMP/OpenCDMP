import { Lookup } from '@common/model/lookup';
import { Guid } from '@common/types/guid';
import { IsActive } from '../common/enum/is-active.enum';

export class TenantUserLookup extends Lookup implements TenantUserFilter {
	ids: Guid[];
	excludedIds: Guid[];
	userIds: Guid[];
	tenantIds: Guid[];
	isActive: IsActive[];

	constructor() {
		super();
	}
}

export interface TenantUserFilter {
	ids: Guid[];
	excludedIds: Guid[];
	userIds: Guid[];
	tenantIds: Guid[];
	isActive: IsActive[];
}