import { Lookup } from '@common/model/lookup';
import { Guid } from '@common/types/guid';
import { IsActive } from '../common/enum/is-active.enum';
import { SupportiveMaterialFieldType } from '../common/enum/supportive-material-field-type';
import { TenantLookup } from './tenant.lookup';

export class SupportiveMaterialLookup extends Lookup implements SupportiveMaterialFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	types: SupportiveMaterialFieldType[];
    languageCodes: string[];
	tenantIds: Guid[];
	tenantSubQuery: TenantLookup;

	constructor() {
		super();
	}
}

export interface SupportiveMaterialFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	types: SupportiveMaterialFieldType[];
    languageCodes: string[];
	tenantIds: Guid[];
	viewOnlyTenant?: boolean;
	tenantSubQuery: TenantLookup;
}
