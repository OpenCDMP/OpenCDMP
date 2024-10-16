import { Guid } from "@common/types/guid";
import { IsActive } from "../common/enum/is-active.enum";
import { Lookup } from "@common/model/lookup";

export class PlanUserLookup extends Lookup implements PlanUserFilter {
  ids: Guid[];
  isActive: IsActive[];
  planIds: Guid[];
  userIds: Guid[];
  sectionIds: Guid[];
  userRoles: Guid[];

  constructor() {
    super();
  }
}

export interface PlanUserFilter {
	ids: Guid[];
	isActive: IsActive[];
  
	planIds: Guid[];
	userIds: Guid[];
	sectionIds: Guid[];
	userRoles: Guid[];
}