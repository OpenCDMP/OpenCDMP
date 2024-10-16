import { IsActive } from "@app/core/common/enum/is-active.enum";
import { Lookup } from "@common/model/lookup";
import { Guid } from "@common/types/guid";

export class AnnotationLookup extends Lookup implements AnnotationFilter {

  like: string;
  ids: Guid[];
  excludedIds: Guid[];
  isActive: IsActive[];
  entityIds: Guid[];
  entityTypes: string[];
  anchors: string[];
  groupIds: Guid[];

  constructor() {
    super();
  }

}

export interface AnnotationFilter {
  like: string;
  ids: Guid[];
  excludedIds: Guid[];
  isActive: IsActive[];
  entityIds: Guid[];
  entityTypes: string[];
  anchors: string[];
  groupIds: Guid[];
}