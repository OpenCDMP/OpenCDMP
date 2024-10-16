import { Lookup } from '@common/model/lookup';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { NotificationInAppTracking } from '@notification-service/core/enum/notification-inapp-tracking.enum';
import { NotificationType } from '@notification-service/core/enum/notification-type.enum';

export class InAppNotificationLookup extends Lookup implements InAppNotificationFilter {
	like: string;
	isActive: IsActive[];
	type: NotificationType[];
	trackingState: NotificationInAppTracking[];

	constructor() {
		super();
	}
}

export interface InAppNotificationFilter {
	like: string;
	isActive: IsActive[];
	trackingState: NotificationInAppTracking[];
	type: NotificationType[];
}
