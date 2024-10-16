import { IsActive } from '@app/core/common/enum/is-active.enum';
import { Guid } from '@common/types/guid';

export interface BaseEntity {
	id?: Guid;
	isActive?: IsActive;
	createdAt?: Date;
	updatedAt?: Date;
	hash?: string;
	belongsToCurrentTenant?: boolean;
}

export interface BaseEntityPersist {
	id?: Guid;
	hash?: string;
}
