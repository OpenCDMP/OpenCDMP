import { Lookup } from "@common/model/lookup";
import { Guid } from "@common/types/guid";
import { IsActive } from "../common/enum/is-active.enum";
import { PlanBlueprintTypeStatus } from "../common/enum/plan-blueprint-type-status";

export class PlanBlueprintTypeLookup extends Lookup implements PlanBlueprintTypeFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	statuses: PlanBlueprintTypeStatus[];

	constructor() {
		super();
	}
}

export interface PlanBlueprintTypeFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	statuses: PlanBlueprintTypeStatus[];
}