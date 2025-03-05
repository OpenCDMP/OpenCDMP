import { AppRole } from "@app/core/common/enum/app-role";
import { DescriptionStatusEnum } from "@app/core/common/enum/description-status";
import { DescriptionStatusAvailableActionType } from "@app/core/common/enum/description-status-available-action-type";
import { PlanUserRole } from "@app/core/common/enum/plan-user-role";
import { BaseEntity } from "@common/base/base-entity.model";
import { StorageFile } from "../storage-file/storage-file";

export interface DescriptionStatus extends BaseEntity{
    name: string;
    description: string;
    action: string;
    ordinal: number;
    internalStatus: DescriptionStatusEnum;
    definition: DescriptionStatusDefinition;
}

export interface DescriptionStatusDefinition {
    authorization: DescriptionStatusDefinitionAuthorization
    availableActions: DescriptionStatusAvailableActionType[];
    matIconName: string;
    storageFile: StorageFile;
    statusColor: string;
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