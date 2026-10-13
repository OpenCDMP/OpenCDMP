import { DescriptionStatusEnum } from "@app/core/common/enum/description-status";
import { BaseEntityPersist } from "@common/base/base-entity.model";
import { DescriptionStatusDefinitionAuthorization } from "./description-status";
import { DescriptionStatusAvailableActionType } from "@app/core/common/enum/description-status-available-action-type";
import { IconConfigurationPersist } from "../icon-configuration/icon-configuration";

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
    iconConfiguration?: IconConfigurationPersist;
    statusColor: string;
}