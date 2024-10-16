import { EntityType } from "@app/core/common/enum/entity-type";
import { BaseEntity } from "@common/base/base-entity.model";
import { Guid } from "@common/types/guid";

export interface EntityDoi extends BaseEntity {
	id: Guid;
	entityType: EntityType;
	entityId: Guid;
	repositoryId: string;
	doi: string;
}