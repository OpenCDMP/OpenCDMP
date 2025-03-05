import { Lookup } from "@common/model/lookup";
import { Guid } from "@common/types/guid";
import { IsActive } from "../common/enum/is-active.enum";
import { TenantLookup } from "./tenant.lookup";

export class LanguageLookup extends Lookup implements LanguageFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	codes: string[];
	tenantSubQuery: TenantLookup;

	constructor() {
		super();
	}
}

export interface LanguageFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	codes: string[];
	viewOnlyTenant?: boolean;
	tenantSubQuery: TenantLookup;
}