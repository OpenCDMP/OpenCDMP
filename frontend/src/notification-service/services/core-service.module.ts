import { ModuleWithProviders, NgModule, Optional, SkipSelf } from '@angular/core';
import { BaseHttpV2Service } from '@app/core/services/http/base-http-v2.service';
import { FormService } from '@common/forms/form-service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { UserNotificationPreferenceService } from '@notification-service/services/http/user-notification-preference.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { NotificationServiceEnumUtils } from '@notification-service/core/formatting/enum-utils.service';
import { InAppNotificationService } from './http/inapp-notification.service';
import { NotificationService } from './http/notification-service';
import { NotificationTemplateService } from './http/notification-template.service';
import { TenantConfigurationService } from './http/tenant-configuration.service';
import { NotificationPrincipalService } from './http/principal.service';
import { PrincipalService } from '@app/core/services/http/principal.service';

//
//
// This is shared module that provides all notification service's services. Its imported only once on the AppModule.
//
//
@NgModule({})
export class CoreNotificationServiceModule {
	constructor(@Optional() @SkipSelf() parentModule: CoreNotificationServiceModule) {
		if (parentModule) {
			throw new Error(
				'CoreNotificationServiceModule is already loaded. Import it in the AppModule only');
		}
	}
	static forRoot(): ModuleWithProviders<CoreNotificationServiceModule> {
		return {
			ngModule: CoreNotificationServiceModule,
			providers: [
				BaseHttpV2Service,
				HttpErrorHandlingService,
				NotificationServiceEnumUtils,
				FormService,
				FilterService,
				LoggingService,
				PrincipalService,
				NotificationService,
				InAppNotificationService,
				NotificationTemplateService,
				TenantConfigurationService,
				UserNotificationPreferenceService,
				NotificationPrincipalService
			],
		};
	}
}
