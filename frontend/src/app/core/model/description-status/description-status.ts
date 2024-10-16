import { AppRole } from "@app/core/common/enum/app-role";
import { DescriptionStatusEnum } from "@app/core/common/enum/description-status";
import { PlanUserRole } from "@app/core/common/enum/plan-user-role";
import { BaseEntity } from "@common/base/base-entity.model";

export interface DescriptionStatus extends BaseEntity{
    name: string;
    description: string;
    internalStatus: DescriptionStatusEnum;
    definition: DescriptionStatusDefinition;
}

export interface DescriptionStatusDefinition {
    authorization: DescriptionStatusDefinitionAuthorization
}

export interface DescriptionStatusDefinitionAuthorization {
    edit: DescriptionStatusDefinitionAuthorizationItem;
}

export interface DescriptionStatusDefinitionAuthorizationItem{
    roles: AppRole[],
    planRoles: PlanUserRole[],
    allowAuthenticated: boolean;
    allowAnonymous: boolean
}