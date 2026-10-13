import { NotificationContactType } from '@notification-service/core/enum/notification-contact-type';
import { NotificationNotifyState } from '@notification-service/core/enum/notification-notify-state.enum';
import { NotificationTrackingState } from '@notification-service/core/enum/notification-tracking-state.enum';
import { Notification } from '@notification-service/core/model/notification.model';

export type NotificationResult = 'sent' | 'read' | 'waiting' | 'failed' | 'skipped';

export function resultOf(item: Notification): NotificationResult {
	switch (item.notifyState) {
		case NotificationNotifyState.SUCCESSFUL:
			// the in-app tracker reports a message the person opened as delivered
			return item.notifiedWith === NotificationContactType.IN_APP && item.trackingState === NotificationTrackingState.DELIVERED ? 'read' : 'sent';
		case NotificationNotifyState.ERROR: return 'failed';
		case NotificationNotifyState.OMITTED: return 'skipped';
		default: return 'waiting';
	}
}
