import { Lookup } from "@common/model/lookup";
import { Guid } from "@common/types/guid";
import { IsActive } from "../common/enum/is-active.enum";
import { PlanStatusEnum } from "../common/enum/plan-status";

export class PlanStatusLookup extends Lookup implements PlanStatusFilter {
    ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	internalStatuses?: PlanStatusEnum[];

    constructor() {
		super();
	}
}

export interface PlanStatusFilter {
    ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
    internalStatuses?: PlanStatusEnum[];
}