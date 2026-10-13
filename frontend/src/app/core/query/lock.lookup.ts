import { Lookup } from "@common/model/lookup";
import { Guid } from "@common/types/guid";
import { LockTargetType } from "../common/enum/lock-target-type";
import { TenantLookup } from "./tenant.lookup";

export class LockLookup extends Lookup implements LockLookup {
	like: string
	ids: Guid[];
	excludedIds: Guid[];
    targetIds: Guid[];
	targetTypes: LockTargetType[];
	userIds: Guid[];
	tenantSubQuery: TenantLookup;
	/** false: only the default tenant's shared rows; true: only tenant-owned rows; absent: both. */
	tenantIsSet?: boolean;

	constructor() {
		super();
	}
}

export interface LockFilter {
	like: string
	ids: Guid[];
	excludedIds: Guid[];
    targetIds: Guid[];
	targetTypes: LockTargetType[];
	userIds: Guid[];
	tenantSubQuery: TenantLookup | null;
}