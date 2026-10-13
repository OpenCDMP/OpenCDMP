import { AppRole } from "@app/core/common/enum/app-role";
import { PlanStatusEnum } from "@app/core/common/enum/plan-status";
import { PlanStatusAvailableActionType } from "@app/core/common/enum/plan-status-available-action-type";
import { PlanUserRole } from "@app/core/common/enum/plan-user-role";
import { BaseEntity } from "@common/base/base-entity.model";
import { IconConfiguration } from "../icon-configuration/icon-configuration";

export interface PlanStatus extends BaseEntity{
    name: string;
    description: string;
    action: string;
    ordinal: number;
    internalStatus: PlanStatusEnum;
    definition: PlanStatusDefinition;
}

export interface PlanStatusDefinition {
    authorization: PlanStatusDefinitionAuthorization
    availableActions: PlanStatusAvailableActionType[]
    iconConfiguration?: IconConfiguration;
    statusColor: string;
}

export interface PlanStatusDefinitionAuthorization {
    edit: PlanStatusDefinitionAuthorizationItem;
}

export interface PlanStatusDefinitionAuthorizationItem{
    roles: AppRole[],
    planRoles: PlanUserRole[],
    allowAuthenticated: boolean;
    allowAnonymous: boolean
}