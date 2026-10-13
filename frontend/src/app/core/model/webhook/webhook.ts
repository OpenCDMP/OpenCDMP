import { WebhookDeliveryStatus } from "@app/core/common/enum/webhook-delivery-status.enum";
import { WebhookEventType } from "@app/core/common/enum/webhook-event-type.enum";
import { BaseEntity, BaseEntityPersist } from "@common/base/base-entity.model";
import { Guid } from "@common/types/guid";

export interface WebhookSubscription extends BaseEntity {
	label: string;
	url: string;
	/** Returned only by the creating persist call — reads always return null. */
	secret?: string;
	eventTypes: WebhookEventType[];
	enabled: boolean;
}

export interface WebhookDelivery extends BaseEntity {
	subscriptionId: Guid;
	eventType: WebhookEventType;
	payload: string;
	status: WebhookDeliveryStatus;
	attempts: number;
	lastHttpStatus: number;
	lastError: string;
	nextRetryAt: Date;
	deliveredAt: Date;
}

// persist

export interface WebhookSubscriptionPersist extends BaseEntityPersist {
	label: string;
	url: string;
	eventTypes: WebhookEventType[];
	enabled: boolean;
}
