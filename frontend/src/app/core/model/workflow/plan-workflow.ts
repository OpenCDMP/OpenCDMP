import { BaseEntity } from "@common/base/base-entity.model";
import { PlanStatus } from "../plan-status/plan-status";

export interface PlanWorkflow extends BaseEntity {
    name: string;
    description: string;
    definition: PlanWorkflowDefinition;
}

export interface PlanWorkflowDefinition {
    startingStatus: PlanStatus;
    statusTransitions: PlanWorkflowDefinitionTransition[];
}

export interface PlanWorkflowDefinitionTransition {
    fromStatus: PlanStatus;
    toStatus: PlanStatus;
}