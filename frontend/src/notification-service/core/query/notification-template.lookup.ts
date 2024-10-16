import { Lookup } from "@common/model/lookup";
import { Guid } from "@common/types/guid";
import { IsActive } from "@notification-service/core/enum/is-active.enum";
import { NotificationTemplateChannel } from "@notification-service/core/enum/notification-template-channel.enum";
import { NotificationTemplateKind } from "@notification-service/core/enum/notification-template-kind.enum";
import { NotificationType } from "@notification-service/core/enum/notification-type.enum";

export class NotificationTemplateLookup extends Lookup implements NotificationTemplateFilter {
	ids: Guid[];
	excludedIds: Guid[];
	isActive: IsActive[];
	kinds: NotificationTemplateKind[];
	channels: NotificationTemplateChannel[];
	notificationTypes: NotificationType[];

	constructor() {
		super();
	}
}

export interface NotificationTemplateFilter {
	ids: Guid[];
	excludedIds: Guid[];
	isActive: IsActive[];
	kinds: NotificationTemplateKind[];
	channels: NotificationTemplateChannel[];
	notificationTypes: NotificationType[];
}