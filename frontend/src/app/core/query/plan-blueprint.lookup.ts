import { Lookup } from '@common/model/lookup';
import { Guid } from '@common/types/guid';
import { IsActive } from '../common/enum/is-active.enum';
import { PlanBlueprintStatus } from '../common/enum/plan-blueprint-status';
import { PlanBlueprintVersionStatus } from '../common/enum/plan-blueprint-version-status';
import { TenantLookup } from './tenant.lookup';

export class PlanBlueprintLookup extends Lookup implements PlanBlueprintFilter {
	ids: Guid[];
	excludedIds: Guid[];
	excludedGroupIds: Guid[];
	like: string;
	isActive: IsActive[];
	statuses: PlanBlueprintStatus[];
	groupIds: Guid[];
	versionStatuses: PlanBlueprintVersionStatus[];
	typeIds:  Guid[];
	onlyCanEdit: boolean;
	tenantSubQuery: TenantLookup;
	/** false: only the default tenant's shared rows; true: only tenant-owned rows; absent: both. */
	tenantIsSet?: boolean;

	constructor() {
		super();
	}
}

export interface PlanBlueprintFilter {
	ids: Guid[];
	typeIds:  Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	statuses: PlanBlueprintStatus[];
	versionStatuses: PlanBlueprintVersionStatus[];
	onlyCanEdit: boolean;
	tenantSubQuery: TenantLookup | null;
}
