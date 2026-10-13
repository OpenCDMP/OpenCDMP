import { Lookup } from "@common/model/lookup";
import { Guid } from "@common/types/guid";
import { IsActive } from "../common/enum/is-active.enum";
import { DescriptionStatusEnum } from "../common/enum/description-status";
import { TenantLookup } from "./tenant.lookup";

export class DescriptionStatusLookup extends Lookup implements DescriptionStatusFilter {
    ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	internalStatuses?: DescriptionStatusEnum[];
	tenantSubQuery: TenantLookup;
	/** false: only the default tenant's shared rows; true: only tenant-owned rows; absent: both. */
	tenantIsSet?: boolean;

    constructor() {
		super();
	}
}

export interface DescriptionStatusFilter {
    ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
    internalStatuses?: DescriptionStatusEnum[];
	tenantSubQuery: TenantLookup;
}