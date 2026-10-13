import { Lookup } from '@common/model/lookup';
import { Guid } from '@common/types/guid';
import { IsActive } from '../common/enum/is-active.enum';
import { TenantLookup } from './tenant.lookup';

export class ReferenceTypeLookup extends Lookup implements ReferenceTypeFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	codes: string[];
	tenantSubQuery: TenantLookup;
	/** false: only the default tenant's shared rows; true: only tenant-owned rows; absent: both. */
	tenantIsSet?: boolean;

	constructor() {
		super();
	}
}

export interface ReferenceTypeFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
    codes: string[];
	tenantSubQuery: TenantLookup | null;
}
