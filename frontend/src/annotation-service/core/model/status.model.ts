import { BaseEntity, BaseEntityPersist } from "@common/base/base-entity.model";
import { InternalStatus } from "../enum/internal-status.enum";

export interface Status extends BaseEntity {
	label: string;
	internalStatus: InternalStatus;
}

export interface StatusPersist extends BaseEntityPersist {
	label: string;
	internalStatus: InternalStatus;
}