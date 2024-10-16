import { DescriptionStatusEnum } from "@app/core/common/enum/description-status";
import { BaseEntityPersist } from "@common/base/base-entity.model";
import { DescriptionStatusDefinition } from "./description-status";

export interface DescriptionStatusPersist extends BaseEntityPersist {
    name: string;
    description: string;
    internalStatus: DescriptionStatusEnum;
    definition: DescriptionStatusDefinition;
}