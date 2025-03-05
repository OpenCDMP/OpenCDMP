import { DescriptionStatusEnum } from "@app/core/common/enum/description-status";
import { BaseEntityPersist } from "@common/base/base-entity.model";
import { DescriptionStatusDefinition, DescriptionStatusDefinitionAuthorization } from "./description-status";
import { DescriptionStatusAvailableActionType } from "@app/core/common/enum/description-status-available-action-type";
import { Guid } from "@common/types/guid";

export interface DescriptionStatusPersist extends BaseEntityPersist {
    name: string;
    description: string;
    action: string;
    ordinal: number;
    internalStatus: DescriptionStatusEnum;
    definition: DescriptionStatusDefinitionPersist;
}

export interface DescriptionStatusDefinitionPersist {
    authorization: DescriptionStatusDefinitionAuthorization
    availableActions: DescriptionStatusAvailableActionType[]
    matIconName: string;
    storageFileId: Guid;
    statusColor: string;
}