import { BaseEntityPersist } from "@common/base/base-entity.model";
import { Guid } from "@common/types/guid";

export interface DescriptionWorkflowPersist extends BaseEntityPersist {
    name: string;
    description: string;
    definition: DescriptionWorkflowDefinitionPersist;
}

export interface DescriptionWorkflowDefinitionPersist {
    startingStatusId: Guid;
    statusTransitions: DescriptionWorkflowDefinitionTransitionPersist[];
}

export interface DescriptionWorkflowDefinitionTransitionPersist {
    fromStatusId: Guid;
    toStatusId: Guid;
}