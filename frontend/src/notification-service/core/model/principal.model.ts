import { NotificationServicePermission } from '@notification-service/core/enum/permission.enum';

export interface NotificationServiceAccount {
	isAuthenticated: boolean;
	permissions: NotificationServicePermission[];
}
