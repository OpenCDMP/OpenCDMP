import { EntityType } from "@app/core/common/enum/entity-type";
import { IsActive } from "@app/core/common/enum/is-active.enum";
import { Lookup } from "@common/model/lookup";
import { Guid } from "@common/types/guid";
import { EvaluationStatus } from "./evaluation-status";

export class EvaluationLookup extends Lookup {
    isActive: IsActive[];
    ids: Guid[];
    excludedIds: Guid[];
    entityIds: Guid[];
    status: EvaluationStatus[];
    createdByIds: Guid[];
    entityTypes: EntityType[];

    constructor() {
		super();
	}
}