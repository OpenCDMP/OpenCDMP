import { NgModule } from '@angular/core';
import { ArrayToStringPipe } from '@common/formatting/pipes/array-to-string.pipe';
import { DataTableDateOnlyFormatPipe, DateOnlyPipe } from '@common/formatting/pipes/date-only-format.pipe';
import { IsActiveTypePipe } from './pipes/is-active-type.pipe';
import { NotificationContactTypePipe } from './pipes/notification-contact-type.pipe';
import { NotificationNotifyStatePipe } from './pipes/notification-notify-state.pipe';
import { NotificationTemplateChannelPipe } from './pipes/notification-template-channel.pipe';
import { NotificationTemplateKindPipe } from './pipes/notification-template-kind.pipe';
import { NotificationTrackingProcessPipe } from './pipes/notification-tracking-process.pipe';
import { NotificationTrackingStatePipe } from './pipes/notification-tracking-state.pipe';
import { NotificationTypePipe } from './pipes/notification-type.pipe';
import { ReferenceSourceTypePipe } from './pipes/reference-source-type.pipe';
import { LockTargetTypePipe } from './pipes/lock-target-type.pipe';
import { UsageLimitTargetMetricPipe } from './pipes/usage-limits-target-metric.pipe';
import { SupportiveMaterialFieldTypePipe } from './pipes/supportive-material-field-type.pipe';

//
//
// This is shared module that provides all formatting utils. Its imported only once on the AppModule.
//
//
@NgModule({
	declarations: [
		ArrayToStringPipe,
		DateOnlyPipe,
		DataTableDateOnlyFormatPipe,
		IsActiveTypePipe,
		ReferenceSourceTypePipe,
		NotificationTemplateChannelPipe,
		NotificationTemplateKindPipe,
		NotificationTypePipe,
		NotificationContactTypePipe,
		NotificationNotifyStatePipe,
		NotificationTrackingProcessPipe,
		NotificationTrackingStatePipe,
		LockTargetTypePipe,
		UsageLimitTargetMetricPipe,
		SupportiveMaterialFieldTypePipe
	],
	exports: [
		ArrayToStringPipe,
		DateOnlyPipe,
		DataTableDateOnlyFormatPipe,
		IsActiveTypePipe,
		ReferenceSourceTypePipe,
		NotificationTemplateChannelPipe,
		NotificationTemplateKindPipe,
		NotificationTypePipe,
		NotificationContactTypePipe,
		NotificationNotifyStatePipe,
		NotificationTrackingProcessPipe,
		NotificationTrackingStatePipe,
		LockTargetTypePipe,
		UsageLimitTargetMetricPipe,
		SupportiveMaterialFieldTypePipe
	],
	providers: [
		ArrayToStringPipe,
		DateOnlyPipe,
		DataTableDateOnlyFormatPipe,
		IsActiveTypePipe,
		ReferenceSourceTypePipe,
		NotificationTemplateChannelPipe,
		NotificationTemplateKindPipe,
		NotificationTypePipe,
		NotificationContactTypePipe,
		NotificationNotifyStatePipe,
		NotificationTrackingProcessPipe,
		NotificationTrackingStatePipe,
		LockTargetTypePipe,
		UsageLimitTargetMetricPipe,
		SupportiveMaterialFieldTypePipe
	]
})
export class CommonFormattingModule { }
