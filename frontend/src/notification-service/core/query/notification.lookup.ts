import { Lookup } from "@common/model/lookup";
import { Guid } from "@common/types/guid";
import { IsActive } from "@notification-service/core/enum/is-active.enum";
import { NotificationContactType } from "@notification-service/core/enum/notification-contact-type";
import { NotificationNotifyState } from "@notification-service/core/enum/notification-notify-state.enum";
import { NotificationTrackingProcess } from "@notification-service/core/enum/notification-tracking-process.enum";
import { NotificationTrackingState } from "@notification-service/core/enum/notification-tracking-state.enum";
import { NotificationType } from "@notification-service/core/enum/notification-type.enum";

export class NotificationLookup extends Lookup implements NotificationFilter {
	ids: Guid[];
	excludedIds: Guid[];
	isActive: IsActive[];
	type: NotificationType[];
	notifyState: NotificationNotifyState[];
	notifiedWith: NotificationContactType[];
	contactType: NotificationContactType[];
	trackingState: NotificationTrackingState[];
	trackingProcess: NotificationTrackingProcess[];
	userIds: Guid[];

	constructor() {
		super();
	}
}

export interface NotificationFilter {
	ids: Guid[];
	excludedIds: Guid[];
	isActive: IsActive[];
	type: NotificationType[];
	notifyState: NotificationNotifyState[];
	notifiedWith: NotificationContactType[];
	contactType: NotificationContactType[];
	trackingState: NotificationTrackingState[];
	trackingProcess: NotificationTrackingProcess[];
	userIds: Guid[];
}