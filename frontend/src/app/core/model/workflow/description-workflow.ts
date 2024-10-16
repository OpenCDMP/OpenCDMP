import { BaseEntity } from "@common/base/base-entity.model";
import { DescriptionStatus } from "../description-status/description-status";

export interface DescriptionWorkflow extends BaseEntity {
    name: string;
    description: string;
    definition: DescriptionWorkflowDefinition;
}

export interface DescriptionWorkflowDefinition {
    startingStatus: DescriptionStatus;
    statusTransitions: DescriptionWorkflowDefinitionTransition[];
}

export interface DescriptionWorkflowDefinitionTransition {
    fromStatus: DescriptionStatus;
    toStatus: DescriptionStatus;
}