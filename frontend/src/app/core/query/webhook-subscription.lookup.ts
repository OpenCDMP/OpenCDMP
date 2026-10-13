import { Lookup } from '@common/model/lookup';
import { Guid } from '@common/types/guid';
import { IsActive } from '../common/enum/is-active.enum';
import { TenantLookup } from './tenant.lookup';

export class WebhookSubscriptionLookup extends Lookup implements WebhookSubscriptionFilter {
	ids: Guid[];
	like: string;
	isActive: IsActive[];
	enabled: boolean;
	tenantSubQuery: TenantLookup;
	/** false: only the default tenant's shared rows; true: only tenant-owned rows; absent: both. */
	tenantIsSet?: boolean;

	constructor() {
		super();
	}
}

export interface WebhookSubscriptionFilter {
	ids: Guid[];
	like: string;
	isActive: IsActive[];
	enabled: boolean;
	tenantSubQuery: TenantLookup | null;
}
