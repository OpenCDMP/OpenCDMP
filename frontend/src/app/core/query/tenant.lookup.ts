import { Lookup } from '@common/model/lookup';
import { Guid } from '@common/types/guid';
import { IsActive } from '../common/enum/is-active.enum';

export class TenantLookup extends Lookup implements TenantFilter {
	ids: Guid[];
	codes: string[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];

	constructor() {
		super();
	}
}

export interface TenantFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
}