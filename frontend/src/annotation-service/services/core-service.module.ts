//
//
// This is shared module that provides all annotation service's services. Its imported only once on the AppModule.
//

import { ModuleWithProviders, NgModule, Optional, SkipSelf } from "@angular/core";
import { BaseHttpV2Service } from "@app/core/services/http/base-http-v2.service";
import { PrincipalService } from "@app/core/services/http/principal.service";
import { LoggingService } from "@app/core/services/logging/logging-service";
import { FormService } from "@common/forms/form-service";
import { HttpErrorHandlingService } from "@common/modules/errors/error-handling/http-error-handling.service";
import { FilterService } from "@common/modules/text-filter/filter-service";
import { AnnotationService } from "@annotation-service/services/http/annotation.service";
import { StatusService } from "./http/status.service";

@NgModule({})
export class CoreAnnotationServiceModule {
	constructor(@Optional() @SkipSelf() parentModule: CoreAnnotationServiceModule) {
		if (parentModule) {
			throw new Error(
				'CoreAnnotationServiceModule is already loaded. Import it in the AppModule only');
		}
	}
	static forRoot(): ModuleWithProviders<CoreAnnotationServiceModule> {
		return {
			ngModule: CoreAnnotationServiceModule,
			providers: [
				BaseHttpV2Service,
				HttpErrorHandlingService,
				FilterService,
				FormService,
				LoggingService,
				PrincipalService,
				AnnotationService,
				StatusService
			],
		};
	}
}