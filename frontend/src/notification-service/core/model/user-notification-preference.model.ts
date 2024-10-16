import { Guid } from '@common/types/guid';
import { NotificationType } from '@notification-service/core/enum/notification-type.enum';
import { NotificationContactType } from '../enum/notification-contact-type';
import { BaseEntity, BaseEntityPersist } from '@common/base/base-entity.model';

export interface UserNotificationPreference extends BaseEntity {
	userId?: Guid;
	type: NotificationType;
	channel: NotificationContactType;
	ordinal: number;
	createdAt?: Date;
}

export interface UserNotificationPreferencePersist extends BaseEntityPersist {
	userId?: Guid;
	notificationPreferences: { [key: string]: NotificationContactType[] };
}
