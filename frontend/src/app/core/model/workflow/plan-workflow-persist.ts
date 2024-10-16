import { BaseEntityPersist } from "@common/base/base-entity.model";
import { Guid } from "@common/types/guid";

export interface PlanWorkflowPersist extends BaseEntityPersist {
    name: string;
    description: string;
    definition: PlanWorkflowDefinitionPersist;
}

export interface PlanWorkflowDefinitionPersist {
    startingStatusId: Guid;
    statusTransitions: PlanWorkflowDefinitionTransitionPersist[];
}

export interface PlanWorkflowDefinitionTransitionPersist {
    fromStatusId: Guid;
    toStatusId: Guid;
}