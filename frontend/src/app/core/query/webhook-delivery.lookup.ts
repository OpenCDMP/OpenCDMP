import { Lookup } from '@common/model/lookup';
import { Guid } from '@common/types/guid';
import { IsActive } from '../common/enum/is-active.enum';
import { WebhookDeliveryStatus } from '../common/enum/webhook-delivery-status.enum';
import { WebhookEventType } from '../common/enum/webhook-event-type.enum';

export class WebhookDeliveryLookup extends Lookup implements WebhookDeliveryFilter {
	ids: Guid[];
	subscriptionIds: Guid[];
	statuses: WebhookDeliveryStatus[];
	eventTypes: WebhookEventType[];
	isActive: IsActive[];

	constructor() {
		super();
	}
}

export interface WebhookDeliveryFilter {
	ids: Guid[];
	subscriptionIds: Guid[];
	statuses: WebhookDeliveryStatus[];
	eventTypes: WebhookEventType[];
	isActive: IsActive[];
}
