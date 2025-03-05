import { PlanStatusEnum } from "@app/core/common/enum/plan-status";
import { BaseEntityPersist } from "@common/base/base-entity.model";
import { PlanStatusDefinitionAuthorization } from "./plan-status";
import { PlanStatusAvailableActionType } from "@app/core/common/enum/plan-status-available-action-type";
import { Guid } from "@common/types/guid";

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
    matIconName: string;
    storageFileId: Guid;
    statusColor: string;
}