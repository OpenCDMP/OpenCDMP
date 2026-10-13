import { Lookup } from '@common/model/lookup';
import { Guid } from '@common/types/guid';
import { ReferenceUsageEntityType } from '../common/enum/reference-usage-entity-type';

/**
 * The references linked to the plans (or descriptions) the caller can see, with counts.
 * Paging applies to the references, most used first. Project reference fields as `reference.<field>`.
 */
export class ReferenceUsageLookup extends Lookup {
	entityType: ReferenceUsageEntityType;
	typeIds: Guid[];
	excludedTypeIds: Guid[];
	like: string;

	constructor() {
		super();
	}
}
