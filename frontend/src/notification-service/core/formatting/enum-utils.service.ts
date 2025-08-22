import { Injectable } from '@angular/core';
import { BaseEnumUtilsService } from '@common/base/base-enum-utils.service';
import { TranslateService } from '@ngx-translate/core';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { NotificationInAppTracking } from '@notification-service/core/enum/notification-inapp-tracking.enum';
import { NotificationNotifyState } from '@notification-service/core/enum/notification-notify-state.enum';
import { NotificationTemplateChannel } from '@notification-service/core/enum/notification-template-channel.enum';
import { NotificationTemplateKind } from '@notification-service/core/enum/notification-template-kind.enum';
import { NotificationTrackingProcess } from '@notification-service/core/enum/notification-tracking-process.enum';
import { NotificationTrackingState } from '@notification-service/core/enum/notification-tracking-state.enum';
import { NotificationType } from '@notification-service/core/enum/notification-type.enum';
import { NotificationDataType } from '../enum/notification-data-type';
import { EmailOverrideMode } from '../enum/email-override-mode';
import { NotificationContactType } from '../enum/notification-contact-type';

@Injectable()
export class NotificationServiceEnumUtils extends BaseEnumUtilsService {
	constructor(private language: TranslateService) { super(); }


	public toIsActiveString(value: IsActive): string {
		switch (value) {
			case IsActive.Active: return this.language.instant('NOTIFICATION-SERVICE.TYPES.IS-ACTIVE.ACTIVE');
			case IsActive.Inactive: return this.language.instant('NOTIFICATION-SERVICE.TYPES.IS-ACTIVE.INACTIVE');
			default: return '';
		}
	}

	public toNotificationTypeString(value: NotificationType): string {
		switch (value) {
			case NotificationType.planInvitationExternalUserType: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-NOTIFICATION-TYPE.PLAN-INVITATION-EXTERNAL-USER');
			case NotificationType.planInvitationExistingUserType: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-NOTIFICATION-TYPE.PLAN-INVITATION-EXISTING-USER');
			case NotificationType.planModifiedType: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-NOTIFICATION-TYPE.PLAN-MODIFIED');
			case NotificationType.planFinalisedType: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-NOTIFICATION-TYPE.PLAN-FINALISED');
			case NotificationType.planStatusChangedType: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-NOTIFICATION-TYPE.PLAN-STATUS-CHANGED');
			case NotificationType.planAnnotationCreatedType: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-NOTIFICATION-TYPE.PLAN-ANNOTATION-CREATED');
			case NotificationType.planAnnotationStatusChangedType: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-NOTIFICATION-TYPE.PLAN-ANNOTATION-STATUS-CHANGED');
			case NotificationType.descriptionCreatedType: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-NOTIFICATION-TYPE.DESCRIPTION-CREATED');
			case NotificationType.descriptionModifiedType: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-NOTIFICATION-TYPE.DESCRIPTION-MODIFIED');
			case NotificationType.descriptionFinalisedType: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-NOTIFICATION-TYPE.DESCRIPTION-FINALISED');
			case NotificationType.descriptionStatusChangedType: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-NOTIFICATION-TYPE.DESCRIPTION-STATUS-CHANGED');
			case NotificationType.descriptionAnnotationCreatedType: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-NOTIFICATION-TYPE.DESCRIPTION-ANNOTATION-CREATED');
			case NotificationType.descriptionAnnotationStatusChangedType: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-NOTIFICATION-TYPE.DESCRIPTION-ANNOTATION-STATUS-CHANGED');
			case NotificationType.mergeAccountConfirmationType: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-NOTIFICATION-TYPE.MERGE-ACCOUNT-CONFIRMATION');
			case NotificationType.removeCredentialConfirmationType: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-NOTIFICATION-TYPE.REMOVE-CREDENTIAL-CONFIRMATION');
			case NotificationType.planDepositType: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-NOTIFICATION-TYPE.PLAN-DEPOSIT');
			case NotificationType.descriptionTemplateInvitationType: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-NOTIFICATION-TYPE.DESCRIPTION-TEMPLATE-INVITATION');
			case NotificationType.contactSupportType: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-NOTIFICATION-TYPE.CONTACT-SUPPORT');
			case NotificationType.publicContactSupportType: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-NOTIFICATION-TYPE.PUBLIC-CONTACT-SUPPORT');
			case NotificationType.tenantSpecificInvitationExternalUserType: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-NOTIFICATION-TYPE.TENANT-SPECIFIC-INVITATION-EXTERNAL-USER');
			case NotificationType.tenantSpecificInvitationExistingUserType: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-NOTIFICATION-TYPE.TENANT-SPECIFIC-INVITATION-EXISTING-USER');
			case NotificationType.planEvaluationType: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-NOTIFICATION-TYPE.PLAN-EVALUATION');
			case NotificationType.descriptionEvaluationType: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-NOTIFICATION-TYPE.DESCRIPTION-EVALUATION');
			case NotificationType.taggedPlanAnnotationCreatedType: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-NOTIFICATION-TYPE.TAGGED-PLAN-ANNOTATION-CREATED');
			case NotificationType.taggedDescriptionAnnotationCreatedType: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-NOTIFICATION-TYPE.TAGGED-DESCRIPTION-ANNOTATION-CREATED');
			default: return '';
		}
	}

	public toContactTypeString(value: NotificationContactType): string {
		switch (value) {
			case NotificationContactType.EMAIL: return this.language.instant('NOTIFICATION-SERVICE.TYPES.CONTACT-TYPE.EMAIL');
			case NotificationContactType.IN_APP: return this.language.instant('NOTIFICATION-SERVICE.TYPES.CONTACT-TYPE.IN-APP');
			default: return '';
		}
	}

	public toNotificationNotifyStateString(value: NotificationNotifyState): string {
		switch (value) {
			case NotificationNotifyState.PENDING: return this.language.instant('NOTIFICATION-SERVICE.TYPES.NOTIFICATION-NOTIFY-STATE.PENDING');
			case NotificationNotifyState.PROCESSING: return this.language.instant('NOTIFICATION-SERVICE.TYPES.NOTIFICATION-NOTIFY-STATE.PROCESSING');
			case NotificationNotifyState.SUCCESSFUL: return this.language.instant('NOTIFICATION-SERVICE.TYPES.NOTIFICATION-NOTIFY-STATE.SUCCESSFUL');
			case NotificationNotifyState.ERROR: return this.language.instant('NOTIFICATION-SERVICE.TYPES.NOTIFICATION-NOTIFY-STATE.ERROR');
			case NotificationNotifyState.OMITTED: return this.language.instant('NOTIFICATION-SERVICE.TYPES.NOTIFICATION-NOTIFY-STATE.OMITTED');
			default: return '';
		}
	}

	public toNotificationTrackingStateString(value: NotificationTrackingState): string {
		switch (value) {
			case NotificationTrackingState.UNDEFINED: return this.language.instant('TYPES.NOTIFICATION-TRACKING-STATE.UNDEFINED');
			case NotificationTrackingState.NA: return this.language.instant('TYPES.NOTIFICATION-TRACKING-STATE.NA');
			case NotificationTrackingState.QUEUED: return this.language.instant('TYPES.NOTIFICATION-TRACKING-STATE.QUEUED');
			case NotificationTrackingState.SENT: return this.language.instant('TYPES.NOTIFICATION-TRACKING-STATE.SENT');
			case NotificationTrackingState.DELIVERED: return this.language.instant('TYPES.NOTIFICATION-TRACKING-STATE.DELIVERED');
			case NotificationTrackingState.UNDELIVERED: return this.language.instant('TYPES.NOTIFICATION-TRACKING-STATE.UNDELIVERED');
			case NotificationTrackingState.FAILED: return this.language.instant('TYPES.NOTIFICATION-TRACKING-STATE.FAILED');
			case NotificationTrackingState.UNSENT: return this.language.instant('TYPES.NOTIFICATION-TRACKING-STATE.UNSENT');
			default: return '';
		}
	}

	public toNotificationTrackingProcessString(value: NotificationTrackingProcess): string {
		switch (value) {
			case NotificationTrackingProcess.PENDING: return this.language.instant('TYPES.NOTIFICATION-TRACKING-PROCESS.PENDING');
			case NotificationTrackingProcess.PROCESSING: return this.language.instant('TYPES.NOTIFICATION-TRACKING-PROCESS.PROCESSING');
			case NotificationTrackingProcess.SUCCESSFUL: return this.language.instant('TYPES.NOTIFICATION-TRACKING-PROCESS.SUCCESSFUL');
			case NotificationTrackingProcess.ERROR: return this.language.instant('TYPES.NOTIFICATION-TRACKING-PROCESS.ERROR');
			case NotificationTrackingProcess.OMITTED: return this.language.instant('TYPES.NOTIFICATION-TRACKING-PROCESS.OMITTED');
			default: return '';
		}
	}

	public toNotificationInAppTrackingString(value: NotificationInAppTracking): string {
		switch (value) {
			case NotificationInAppTracking.Delivered: return this.language.instant('TYPES.NOTIFICATION-INAPP-TRACKING.DELIVERED');
			case NotificationInAppTracking.Stored: return this.language.instant('TYPES.NOTIFICATION-INAPP-TRACKING.STORED');
			default: return '';
		}
	}

	public toNotificationChannelString(value: NotificationTemplateChannel): string {
		switch (value) {
			case NotificationTemplateChannel.Email: return this.language.instant('NOTIFICATION-SERVICE.TYPES.NOTIFICATION-TEMPLATE-CHANNEL.EMAIL');
			case NotificationTemplateChannel.InApp: return this.language.instant('NOTIFICATION-SERVICE.TYPES.NOTIFICATION-TEMPLATE-CHANNEL.IN-APP');
			default: return '';
		}
	}

	public toNotificationTemplateKindString(value: NotificationTemplateKind): string {
		switch (value) {
			case NotificationTemplateKind.Draft: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-KIND.DRAFT');
			case NotificationTemplateKind.Secondary: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-KIND.SECONDARY');
			case NotificationTemplateKind.Primary: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-KIND.PRIMARY');
			case NotificationTemplateKind.Default: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-KIND.DEFAULT');
			default: return '';
		}
	}

	toNotificationTemplateChannelString(status: NotificationTemplateChannel): string {
		switch (status) {
			case NotificationTemplateChannel.Email: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-CHANNEL.EMAIL');
			case NotificationTemplateChannel.InApp: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-CHANNEL.INAPP');
		}
	}

	toNotificationTemplateDataTypeString(status: NotificationDataType): string {
		switch (status) {
			case NotificationDataType.Integer: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-DATA-TYPE.INTEGER');
			case NotificationDataType.Demical: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-DATA-TYPE.DEMICAL');
			case NotificationDataType.Double: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-DATA-TYPE.DOUBLE');
			case NotificationDataType.DateTime: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-DATA-TYPE.DATE-TIME');
			case NotificationDataType.String: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-DATA-TYPE.STRING');
		}
	}

	toEmailOverrideModeString(status: EmailOverrideMode): string {
		switch (status) {
			case EmailOverrideMode.NotOverride: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-EMAIL-OVERRIDE-MODE.NOT');
			case EmailOverrideMode.Additive: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-EMAIL-OVERRIDE-MODE.ADDITIVE');
			case EmailOverrideMode.Replace: return this.language.instant('TYPES.NOTIFICATION-TEMPLATE-EMAIL-OVERRIDE-MODE.REPLACE');
		}
	}

	public toNotificationContactTypeString(value: NotificationContactType): string {
		switch (value) {
			case NotificationContactType.EMAIL: return this.language.instant('TYPES.NOTIFICATION-CONTACT-TYPE.EMAIL');
			case NotificationContactType.IN_APP: return this.language.instant('TYPES.NOTIFICATION-CONTACT-TYPE.INAPP');
		}
	}
}
