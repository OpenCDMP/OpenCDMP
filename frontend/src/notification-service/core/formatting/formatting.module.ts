import { ModuleWithProviders, NgModule, Optional, SkipSelf } from '@angular/core';
import { CommonFormattingModule } from '@common/formatting/common-formatting.module';
import { PipeService } from '@common/formatting/pipe.service';
import { IsActiveTypePipe } from '@notification-service/core/formatting/pipes/is-active-type.pipe';
import { NotificationInAppTrackingTypePipe } from '@notification-service/core/formatting/pipes/notification-inapp-tracking-type.pipe';
import { NotificationServiceEnumUtils } from './enum-utils.service';

//
//
// This is shared module that provides all notification service formatting utils. Its imported only once.
//
//
@NgModule({
	imports: [
		CommonFormattingModule,
	],
	declarations: [
		IsActiveTypePipe,
		NotificationInAppTrackingTypePipe
	],
	exports: [
		CommonFormattingModule,
		IsActiveTypePipe,
		NotificationInAppTrackingTypePipe,
	],
	providers: [
		PipeService,
		IsActiveTypePipe,
		NotificationInAppTrackingTypePipe,
		NotificationServiceEnumUtils
	]
})
export class NotificationServiceFormattingModule { }
