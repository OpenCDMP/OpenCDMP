import { Lookup } from "@common/model/lookup";
import { Guid } from "@common/types/guid";
import { IsActive } from "../common/enum/is-active.enum";

export class TagLookup extends Lookup implements TagFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	createdByIds: Guid[];

	constructor() {
		super();
	}
}

export class DescriptionTagLookup extends Lookup {
	tagIds: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];

	constructor() {
		super();
	}
}

export interface TagFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	createdByIds: Guid[];
}