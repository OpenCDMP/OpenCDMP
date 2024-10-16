import { PlanStatusEnum } from "@app/core/common/enum/plan-status";
import { BaseEntityPersist } from "@common/base/base-entity.model";
import { PlanStatusDefinition } from "./plan-status";

export interface PlanStatusPersist extends BaseEntityPersist {
    name: string;
    description: string;
    internalStatus: PlanStatusEnum;
    definition: PlanStatusDefinition;
}