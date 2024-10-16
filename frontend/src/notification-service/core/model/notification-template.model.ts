import { Guid } from '@common/types/guid';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { NotificationTemplateChannel } from '@notification-service/core/enum/notification-template-channel.enum';
import { NotificationTemplateKind } from '@notification-service/core/enum/notification-template-kind.enum';
import { NotificationType } from '@notification-service/core/enum/notification-type.enum';
import { EmailOverrideMode } from '../enum/email-override-mode';
import { NotificationDataType } from '../enum/notification-data-type';

export interface NotificationTemplate {
	id?: Guid;
	channel: NotificationTemplateChannel;
	notificationType: NotificationType;
	kind: NotificationTemplateKind;
	languageCode: string;
	value: NotificationTemplateValue;
	isActive?: IsActive;
	createdAt?: Date;
	updatedAt?: Date;
	hash?: string;
	belongsToCurrentTenant?: boolean;
}

export interface NotificationTemplateValue {
	subjectText: string;
    subjectKey: string;
	subjectFieldOptions: NotificationFieldOptions;
	bodyText: string;
    bodyKey: string;
    priorityKey: string;
    allowAttachments: Boolean;
    cc: string[];
    ccMode: EmailOverrideMode;
    bcc: string[];
    bccMode: EmailOverrideMode;
    extraDataKeys: string[];
	bodyFieldOptions: NotificationFieldOptions;
}

export interface NotificationFieldOptions {
	mandatory?: string[];
	optional?: NotificationFieldInfo[];
	formatting?: { [key: string]: string };
}

export interface NotificationFieldInfo {
	key: string;
    type: NotificationDataType,
	value: string;
}

// Persist

export interface NotificationTemplatePersist {
	id?: Guid;
	channel: NotificationTemplateChannel;
	notificationType: NotificationType;
	kind: NotificationTemplateKind;
	languageCode: string;
	value: NotificationTemplateValuePersist;
	hash?: string;
}

export interface NotificationTemplateValuePersist {
	subjectText: string;
    subjectKey: string;
	subjectFieldOptions: NotificationFieldOptionsPersist;
	bodyText: string;
    bodyKey: string;
    priorityKey: string;
    allowAttachments: Boolean;
    cc: string[];
    ccMode: EmailOverrideMode;
    bcc: string[];
    bccMode: EmailOverrideMode;
    extraDataKeys: string[];
	bodyFieldOptions: NotificationFieldOptionsPersist;
}

export interface NotificationFieldOptionsPersist {
	mandatory?: string[];
	optional?: NotificationFieldInfoPersist[];
	formatting?: { [key: string]: string };
}

export interface NotificationFieldInfoPersist {
	key: string;
    type: NotificationDataType,
	value: string;
}
