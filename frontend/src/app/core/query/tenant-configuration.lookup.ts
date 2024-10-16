import { Lookup } from '@common/model/lookup';
import { Guid } from '@common/types/guid';
import { IsActive } from '../common/enum/is-active.enum';
import { TenantConfigurationType } from '../common/enum/tenant-configuration-type';

export class TenantConfigurationLookup extends Lookup implements TenantConfigurationFilter {
	ids: Guid[];
	excludedIds: Guid[];
	types: TenantConfigurationType[];
	isActive: IsActive[];
	tenantIds: Guid[];
	tenantIsSet: boolean;

	constructor() {
		super();
	}
}

export interface TenantConfigurationFilter {
	ids: Guid[];
	excludedIds: Guid[];
	types: TenantConfigurationType[];
	tenantIds: Guid[];
	tenantIsSet: boolean;
	isActive: IsActive[];
}
