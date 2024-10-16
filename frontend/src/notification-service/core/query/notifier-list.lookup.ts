import { NotificationType } from '@notification-service/core/enum/notification-type.enum';

export class NotifierListLookup implements TenantConfigurationFilter {
	notificationTypes?: NotificationType[];

	constructor() { }
}

export interface TenantConfigurationFilter {
	notificationTypes?: NotificationType[];
}
