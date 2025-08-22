import { ModuleWithProviders, NgModule, Optional, SkipSelf } from '@angular/core';
import { CanDeactivateGuard } from '@app/library/deactivate/can-deactivate.guard';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { CookieService } from 'ngx-cookie-service';
import { AuthGuard } from './auth-guard.service';
import { AuthService } from './services/auth/auth.service';
import { ContactSupportService } from './services/contact-support/contact-support.service';
import { CultureService } from './services/culture/culture-service';
import { LanguageInfoService } from './services/culture/language-info-service';
import { DashboardService } from './services/dashboard/dashboard.service';
import { DefaultUserLocaleService } from './services/default-user-locale/default-user-locale.service';
import { DepositHttpService } from './services/deposit/deposit.http.service';
import { DescriptionStatusService } from './services/description-status/description-status.service';
import { DescriptionTemplateTypeService } from './services/description-template-type/description-template-type.service';
import { DescriptionTemplateService } from './services/description-template/description-template.service';
import { DescriptionWorkflowService } from './services/description-workflow/description-workflow.service';
import { DescriptionService } from './services/description/description.service';
import { EvaluatorHttpService } from './services/evaluator/evaluator.http.service';
import { FileTransformerHttpService } from './services/file-transformer/file-transformer.http.service';
import { FileTransformerService } from './services/file-transformer/file-transformer.service';
// import { FontAccessibilityService } from './services/font-accessibility.service';
import { BaseHttpV2Service } from './services/http/base-http-v2.service';
import { PrincipalService } from './services/http/principal.service';
import { LanguageHttpService } from './services/language/language.http.service';
import { LanguageService } from './services/language/language.service';
import { LockService } from './services/lock/lock.service';
import { LoggingService } from './services/logging/logging-service';
import { MaintenanceService } from './services/maintenance/maintenance.service';
import { UiNotificationService } from './services/notification/ui-notification-service';
import { PlanBlueprintService } from './services/plan/plan-blueprint.service';
import { PlanStatusService } from './services/plan/plan-status.service';
import { PlanWorkflowService } from './services/plan/plan-workflow.service';
import { PlanService } from './services/plan/plan.service';
import { PrefillingSourceService } from './services/prefilling-source/prefilling-source.service';
import { ProgressIndicationService } from './services/progress-indication/progress-indication-service';
import { ReferenceTypeService } from './services/reference-type/reference-type.service';
import { ReferenceService } from './services/reference/reference.service';
import { RouterUtilsService } from './services/router/router-utils.service';
import { SemanticsService } from './services/semantic/semantics.service';
import { StorageFileService } from './services/storage-file/storage-file.service';
import { SupportiveMaterialService } from './services/supportive-material/supportive-material.service';
import { TagService } from './services/tag/tag.service';
import { TenantConfigurationService } from './services/tenant-configuration/tenant-configuration.service';
import { TenantHandlingService } from './services/tenant/tenant-handling.service';
import { TenantService } from './services/tenant/tenant.service';
import { TimezoneService } from './services/timezone/timezone-service';
import { UsageLimitService } from './services/usage-limit/usage.service';
import { UserSettingsHttpService } from './services/user-settings/user-settings-http.service';
import { UserSettingsService } from './services/user-settings/user-settings.service';
import { UserService } from './services/user/user.service';
import { CollectionUtils } from './services/utilities/collection-utils.service';
import { FileUtils } from './services/utilities/file-utils.service';
import { QueryParamsService } from './services/utilities/query-params.service';
import { TypeUtils } from './services/utilities/type-utils.service';
import { AccountingAuthGuard } from './services/accounting-auth-guard.service';
import { PluginConfigurationService } from './services/plugin/plugin-configuration.service';

//
//
// This is shared module that provides all the services. Its imported only once on the AppModule.
//
//

@NgModule({
})
export class CoreServiceModule {
	constructor(@Optional() @SkipSelf() parentModule: CoreServiceModule) {
		if (parentModule) {
			throw new Error(
				'CoreModule is already loaded. Import it in the AppModule only');
		}
	}
	static forRoot(): ModuleWithProviders<CoreServiceModule> {
		return {
			ngModule: CoreServiceModule,
			providers: [
				AuthService,
				CookieService,
				BaseHttpV2Service,
				AuthGuard,
				CultureService,
				TimezoneService,
				TypeUtils,
				CollectionUtils,
				UiNotificationService,
				ProgressIndicationService,
				LoggingService,
				DashboardService,
				DepositHttpService,
				PlanBlueprintService,
				ContactSupportService,
				LanguageService,
				LockService,
				PrincipalService,
				SupportiveMaterialService,
				LanguageInfoService,
				DescriptionTemplateTypeService,
				DefaultUserLocaleService,
				HttpErrorHandlingService,
				QueryParamsService,
				UserSettingsService,
				UserSettingsHttpService,
				FilterService,
				FileUtils,
				ReferenceService,
				DescriptionTemplateService,
				ReferenceTypeService,
				TenantService,
				UserService,
				LanguageHttpService,
				PlanService,
				DescriptionService,
				MaintenanceService,
				TagService,
				CanDeactivateGuard,
				FileTransformerService,
				FileTransformerHttpService,
				EvaluatorHttpService,
				SemanticsService,
				PrefillingSourceService,
				VisibilityRulesService,
				TenantConfigurationService,
				StorageFileService,
				TenantHandlingService,
				RouterUtilsService,
				UsageLimitService,
				PlanStatusService,
				DescriptionStatusService,
				PlanWorkflowService,
				DescriptionWorkflowService,
				// FontAccessibilityService,
				AccountingAuthGuard,
				PluginConfigurationService
			],
		};
	}
}
