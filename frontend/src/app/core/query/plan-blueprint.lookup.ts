import { Lookup } from '@common/model/lookup';
import { Guid } from '@common/types/guid';
import { IsActive } from '../common/enum/is-active.enum';
import { PlanBlueprintStatus } from '../common/enum/plan-blueprint-status';
import { PlanBlueprintVersionStatus } from '../common/enum/plan-blueprint-version-status';

export class PlanBlueprintLookup extends Lookup implements PlanBlueprintFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	statuses: PlanBlueprintStatus[];
	groupIds: Guid[];
	versionStatuses: PlanBlueprintVersionStatus[];

	constructor() {
		super();
	}
}

export interface PlanBlueprintFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	statuses: PlanBlueprintStatus[];
	versionStatuses: PlanBlueprintVersionStatus[];
}
