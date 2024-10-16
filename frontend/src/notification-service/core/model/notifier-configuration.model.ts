import { NotificationContactType } from '@notification-service/core/enum/notification-contact-type';

export interface NotifierListConfigurationDataContainer {
	notifiers: { [key: string]: NotificationContactType[] };
}
