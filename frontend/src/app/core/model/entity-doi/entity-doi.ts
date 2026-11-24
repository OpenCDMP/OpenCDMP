import { DepositAuthMethod } from "@app/core/common/enum/deposit-auth-method";
import { EntityDoiStatus } from "@app/core/common/enum/entity-doi-status";
import { EntityType } from "@app/core/common/enum/entity-type";
import { BaseEntity } from "@common/base/base-entity.model";
import { Guid } from "@common/types/guid";

export interface EntityDoi extends BaseEntity {
	id: Guid;
	entityType: EntityType;
	entityId: Guid;
	repositoryId: string;
	doi: string;
	status?: EntityDoiStatus;
	metadata?: EntityDoiMetadata;
}

export interface EntityDoiMetadata {
	depositMethod: DepositAuthMethod;
	userId: Guid;
	accountId: string;
	accountName: string;
}