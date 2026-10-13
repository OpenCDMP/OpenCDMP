import { PlanStatusEnum } from "@app/core/common/enum/plan-status";
import { BaseEntityPersist } from "@common/base/base-entity.model";
import { PlanStatusDefinitionAuthorization } from "./plan-status";
import { PlanStatusAvailableActionType } from "@app/core/common/enum/plan-status-available-action-type";
import { IconConfigurationPersist } from "../icon-configuration/icon-configuration";

export interface PlanStatusPersist extends BaseEntityPersist {
    name: string;
    description: string;
    action: string;
    ordinal: number;
    internalStatus: PlanStatusEnum;
    definition: PlanStatusDefinitionPersist;
}

export interface PlanStatusDefinitionPersist {
    authorization: PlanStatusDefinitionAuthorization
    availableActions: PlanStatusAvailableActionType[]
    iconConfiguration?: IconConfigurationPersist;
    statusColor: string;
}