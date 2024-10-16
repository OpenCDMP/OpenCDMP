import { Lookup } from "@common/model/lookup";
import { Guid } from "@common/types/guid";
import { IsActive } from "../common/enum/is-active.enum";
import { DescriptionStatusEnum } from "../common/enum/description-status";

export class DescriptionStatusLookup extends Lookup implements DescriptionStatusFilter {
    ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	internalStatuses?: DescriptionStatusEnum[];

    constructor() {
		super();
	}
}

export interface DescriptionStatusFilter {
    ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
    internalStatuses?: DescriptionStatusEnum[];
}