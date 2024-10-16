import { Lookup } from '@common/model/lookup';
import { Guid } from '@common/types/guid';
import { NotificationType } from '@notification-service/core/enum/notification-type.enum';
import { NotificationContactType } from '../enum/notification-contact-type';

export class UserNotificationPreferenceLookup extends Lookup implements UserNotificationPreferenceFilter {
	ids: Guid[];
	excludedIds: Guid[];
	userIds?: Guid[];
	type?: NotificationType[];
	channel?: NotificationContactType[];

	constructor() {
		super();
	}
}

export interface UserNotificationPreferenceFilter {
	ids: Guid[];
	excludedIds: Guid[];
	userIds?: Guid[];
	type?: NotificationType[];
	channel?: NotificationContactType[];
}
