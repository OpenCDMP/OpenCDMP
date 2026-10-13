import { LockTargetType } from '@app/core/common/enum/lock-target-type';
import { Guid } from '@common/types/guid';
import { User } from '../user/user';
import { BaseEntityPersist } from '@common/base/base-entity.model';

export interface Lock {
	id: Guid;
	target: Guid;
	targetLabel?: string;
	targetType: LockTargetType;
	lockedBy: User;
	lockedAt: Date;
	touchedAt: Date;
	belongsToCurrentTenant?: boolean;
	hash: string;
}


// Persist
export interface LockPersist extends BaseEntityPersist {
	target: Guid;
	targetType: LockTargetType;
}

export interface LockStatus {
	status: boolean;
	lock: Lock;
}

export interface UnlockMultipleTargetsPersist {
	targetIds: Guid[];
}
