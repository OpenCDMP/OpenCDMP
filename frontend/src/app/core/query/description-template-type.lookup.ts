import { Lookup } from "@common/model/lookup";
import { Guid } from "@common/types/guid";
import { IsActive } from "../common/enum/is-active.enum";
import { DescriptionTemplateTypeStatus } from "../common/enum/description-template-type-status";
import { TenantLookup } from "./tenant.lookup";

export class DescriptionTemplateTypeLookup extends Lookup implements DescriptionTemplateTypeFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	statuses: DescriptionTemplateTypeStatus[];
	tenantSubQuery: TenantLookup;
	/** false: only the default tenant's shared rows; true: only tenant-owned rows; absent: both. */
	tenantIsSet?: boolean;

	constructor() {
		super();
	}
}

export interface DescriptionTemplateTypeFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	statuses: DescriptionTemplateTypeStatus[];
	tenantSubQuery: TenantLookup;
}