import { Lookup } from '@common/model/lookup';
import { Guid } from '@common/types/guid';
import { TenantConfigurationType } from '../enum/tenant-configuration-type';
import { IsActive } from '../enum/is-active.enum';

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
