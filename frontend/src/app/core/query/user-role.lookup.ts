import { Lookup } from "@common/model/lookup";
import { Guid } from "@common/types/guid";

export class UserRoleLookup extends Lookup implements UserRoleFilter {
	ids: Guid[];
	excludedIds: Guid[];
	userIds: Guid[];
    roles: string[]

	constructor() {
		super();
	}
}

export interface UserRoleFilter {
	ids: Guid[];
	excludedIds: Guid[];
	userIds: Guid[];
    roles: string[];
}