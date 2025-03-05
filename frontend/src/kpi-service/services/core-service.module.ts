import { ModuleWithProviders, NgModule, Optional, SkipSelf } from '@angular/core';
import { KPIProviders } from '../../kpi-service/core/providers/kpi-client.providers';
import { IndicatorPointService } from '../../kpi-service/services/indicator-point.service';
import { IndicatorDashboardService } from '../../kpi-service/services/indicator-dashboard.service';
import { BaseHttpV2Service } from '@app/core/services/http/base-http-v2.service';
import { FormService } from '@common/forms/form-service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { PrincipalService } from '@app/core/services/http/principal.service';
import { KpiAuthGuard } from 'kpi-service/core/kpi-auth-guard.service';
import { HttpErrorHandlingService } from '@citesa/kpi-client/services';

//
//
// This is shared module that provides all the services. Its imported only once on the AppModule.
//
//

@NgModule({
})
export class CoreKpiServiceModule {
	constructor(@Optional() @SkipSelf() parentModule: CoreKpiServiceModule) {
		if (parentModule) {
			throw new Error(
				'CoreKpiServiceModule is already loaded. Import it in the AppModule only');
		}
	}
	static forRoot(): ModuleWithProviders<CoreKpiServiceModule> {
		return {
			ngModule: CoreKpiServiceModule,
			providers: [
				BaseHttpV2Service,
				HttpErrorHandlingService,
				FormService,
				FilterService,
				LoggingService,
				PrincipalService,
				KpiAuthGuard,
				IndicatorDashboardService,
				IndicatorPointService,
				KPIProviders.provideIndicatorPointService(),
				KPIProviders.provideTranslations(
					({ language }) => ({
						KPI_DASHBOARD: {
							COULD_NOT_LOAD_DASHBOARD: language.get('KPI-SERVICE.KPI_DASHBOARD.NOT-FOUND'),
							INVALID_CONFIGURATION: language.get('KPI-SERVICE.KPI_DASHBOARD.INVALID-CONFIGURATION'),
						},
						KPI_DASHBOARD_CHART: {
							UNEXPECTED_ERROR: language.get('KPI-SERVICE.KPI_DASHBOARD_CHART.UNEXPECTED_ERROR'),
							INVALID_CONFIGURATION: language.get('KPI-SERVICE.KPI_DASHBOARD_CHART.INVALID_CONFIGURATION'),
						},
						KPI_CHART_FILTERS: {
							FILTERS_FOR_TITLE: language.get('KPI-SERVICE.KPI_CHART_FILTERS.FILTERS_FOR_TITLE'),
							REQUIRED: language.get('KPI-SERVICE.KPI_CHART_FILTERS.REQUIRED'),
							CANCEL: language.get('KPI-SERVICE.KPI_CHART_FILTERS.CANCEL'),
							SUBMIT: language.get('KPI-SERVICE.KPI_CHART_FILTERS.SUBMIT'),
						}
					})
				),
			],
		};
	}
}
