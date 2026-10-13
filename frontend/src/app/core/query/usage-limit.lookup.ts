import { Lookup } from '@common/model/lookup';
import { Guid } from '@common/types/guid';
import { IsActive } from '../common/enum/is-active.enum';
import { UsageLimitTargetMetric } from '../common/enum/usage-limit-target-metric';
import { TenantLookup } from './tenant.lookup';

export class UsageLimitLookup extends Lookup implements UsageLimitFilter {
	ids: Guid[];
	usageLimitTargetMetrics: UsageLimitTargetMetric[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	tenantSubQuery: TenantLookup;
	/** false: only the default tenant's shared rows; true: only tenant-owned rows; absent: both. */
	tenantIsSet?: boolean;

	constructor() {
		super();
	}
}

export interface UsageLimitFilter {
	ids: Guid[];
	usageLimitTargetMetrics: UsageLimitTargetMetric[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	tenantSubQuery: TenantLookup | null;
}