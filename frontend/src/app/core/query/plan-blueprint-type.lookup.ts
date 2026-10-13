import { Lookup } from "@common/model/lookup";
import { Guid } from "@common/types/guid";
import { IsActive } from "../common/enum/is-active.enum";
import { PlanBlueprintTypeStatus } from "../common/enum/plan-blueprint-type-status";
import { TenantLookup } from "./tenant.lookup";

export class PlanBlueprintTypeLookup extends Lookup implements PlanBlueprintTypeFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	statuses: PlanBlueprintTypeStatus[];
	tenantSubQuery: TenantLookup;
	/** false: only the default tenant's shared rows; true: only tenant-owned rows; absent: both. */
	tenantIsSet?: boolean;

	constructor() {
		super();
	}
}

export interface PlanBlueprintTypeFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	statuses: PlanBlueprintTypeStatus[];
	tenantSubQuery: TenantLookup;
}