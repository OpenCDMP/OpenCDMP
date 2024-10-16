import { AppRole } from "@app/core/common/enum/app-role";
import { PlanStatusEnum } from "@app/core/common/enum/plan-status";
import { PlanUserRole } from "@app/core/common/enum/plan-user-role";
import { BaseEntity } from "@common/base/base-entity.model";

export interface PlanStatus extends BaseEntity{
    name: string;
    description: string;
    internalStatus: PlanStatusEnum;
    definition: PlanStatusDefinition;
}

export interface PlanStatusDefinition {
    authorization: PlanStatusDefinitionAuthorization
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