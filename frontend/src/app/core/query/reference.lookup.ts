import { Lookup } from '@common/model/lookup';
import { Guid } from '@common/types/guid';
import { IsActive } from '../common/enum/is-active.enum';
import { ReferenceSourceType } from '../common/enum/reference-source-type';
import { TenantLookup } from './tenant.lookup';

export class ReferenceLookup extends Lookup implements ReferenceFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	sourceTypes: ReferenceSourceType[];
	typeIds: Guid[];
	tenantSubQuery: TenantLookup;
	/** false: only the default tenant's shared rows; true: only tenant-owned rows; absent: both. */
	tenantIsSet?: boolean;
	/** true: only entries a current plan or asset uses. */
	isUsed?: boolean;

	constructor() {
		super();
	}
}
export class DescriptionReferenceLookup extends Lookup {
	referenceIds: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];

	constructor() {
		super();
	}
}

export class PlanReferenceLookup extends Lookup {
	planIds: Guid[];
	referenceIds: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];

	constructor() {
		super();
	}
}

export interface ReferenceFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	sourceTypes: ReferenceSourceType[];
	typeIds: Guid[];
	tenantSubQuery: TenantLookup | null;
}
