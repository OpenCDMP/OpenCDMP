import { Lookup } from '@common/model/lookup';
import { Guid } from '@common/types/guid';
import { IsActive } from '../common/enum/is-active.enum';

export class ReferenceTypeLookup extends Lookup implements ReferenceTypeFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	codes: string[];

	constructor() {
		super();
	}
}

export interface ReferenceTypeFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
    codes: string[];
}
