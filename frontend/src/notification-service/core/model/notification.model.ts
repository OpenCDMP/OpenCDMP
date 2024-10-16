import { User } from '@app/core/model/user/user';
import { Guid } from '@common/types/guid';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { NotificationNotifyState } from '@notification-service/core/enum/notification-notify-state.enum';
import { NotificationTrackingProcess } from '@notification-service/core/enum/notification-tracking-process.enum';
import { NotificationTrackingState } from '@notification-service/core/enum/notification-tracking-state.enum';
import { NotificationType } from '@notification-service/core/enum/notification-type.enum';
import { NotificationContactType } from '../enum/notification-contact-type';

export interface Notification {
	id?: Guid;
	user: User;
	type: NotificationType;
	contactTypeHint: NotificationContactType;
	contactHint: string;
	data: string;
	notifyState: NotificationNotifyState;
	notifiedWith: NotificationContactType;
	notifiedAt: Date;
	retryCount: number;
	trackingState: NotificationTrackingState;
	trackingProcess: NotificationTrackingProcess;
	trackingData: string;
	isActive?: IsActive;
	createdAt?: Date;
	updatedAt?: Date;
	hash?: string;
	belongsToCurrentTenant?: boolean;
}
