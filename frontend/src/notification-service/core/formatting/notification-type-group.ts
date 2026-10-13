import { NotificationType } from '@notification-service/core/enum/notification-type.enum';

export type NotificationGroup = 'PLANS' | 'ASSETS' | 'COMMENTS' | 'ACCOUNT' | 'SUPPORT' | 'OTHER';

export const NOTIFICATION_GROUP_ORDER: NotificationGroup[] = ['PLANS', 'ASSETS', 'COMMENTS', 'ACCOUNT', 'SUPPORT', 'OTHER'];

/** The enum member name of each notification type id, to group the types by what they are about. */
const TYPE_NAMES = new Map<string, string>(Object.entries(NotificationType).map(([name, id]) => [id as string, name]));

export function notificationGroupOf(type: string): NotificationGroup {
	const name = TYPE_NAMES.get(type);
	if (!name) return 'OTHER';
	if (/Annotation/.test(name)) return 'COMMENTS';
	if (name.startsWith('plan')) return 'PLANS';
	if (name.startsWith('description')) return 'ASSETS';
	if (/ContactSupport|contactSupport/.test(name)) return 'SUPPORT';
	if (/^(mergeAccount|removeCredential|tenantSpecificInvitation)/.test(name)) return 'ACCOUNT';
	return 'OTHER';
}
