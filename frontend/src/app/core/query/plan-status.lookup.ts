import { Lookup } from "@common/model/lookup";
import { Guid } from "@common/types/guid";
import { IsActive } from "../common/enum/is-active.enum";
import { PlanStatusEnum } from "../common/enum/plan-status";
import { TenantLookup } from "./tenant.lookup";

export class PlanStatusLookup extends Lookup implements PlanStatusFilter {
    ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	internalStatuses?: PlanStatusEnum[];
	tenantSubQuery: TenantLookup;
	/** false: only the default tenant's shared rows; true: only tenant-owned rows; absent: both. */
	tenantIsSet?: boolean;

    constructor() {
		super();
	}
}

export interface PlanStatusFilter {
    ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
    internalStatuses?: PlanStatusEnum[];
	tenantSubQuery: TenantLookup;
}