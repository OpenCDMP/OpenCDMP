import { Lookup } from "@common/model/lookup";
import { Guid } from "@common/types/guid";
import { LockTargetType } from "../common/enum/lock-target-type";

export class LockLookup extends Lookup implements LockLookup {
	like: string
	ids: Guid[];
	excludedIds: Guid[];
    targetIds: Guid[];
	targetTypes: LockTargetType[];
	userIds: Guid[];

	constructor() {
		super();
	}
}

export interface LockFilter {
	like: string
	ids: Guid[];
	excludedIds: Guid[];
    targetIds: Guid[];
	targetTypes: LockTargetType[];
	userIds: Guid[];
}