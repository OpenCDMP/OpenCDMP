import { IsActive } from "@app/core/common/enum/is-active.enum";
import { Lookup } from "@common/model/lookup";
import { Guid } from "@common/types/guid";
import { InternalStatus } from "../enum/internal-status.enum";

export class StatusLookup extends Lookup implements StatusFilter {

  like: string;
  ids: Guid[];
  excludedIds: Guid[];
  isActive: IsActive[];
  internalStatuses: InternalStatus[];
  
  constructor() {
    super();
  }

}

export interface StatusFilter {
  like: string;
  ids: Guid[];
  excludedIds: Guid[];
  isActive: IsActive[];
  internalStatuses: InternalStatus[];
}