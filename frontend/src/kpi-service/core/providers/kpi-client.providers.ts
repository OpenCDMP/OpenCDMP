import { Provider } from "@angular/core";import { TranslateService } from "@ngx-translate/core";
import { INDICATOR_POINT_SERVICE, IndicatorPointServiceInterface, KPI_DASHBOARD_RESOLVER, KPIDashboardResolver, SHARE_CHART_CONFIGURATION, ShareChartConfig } from "@citesa/kpi-client/tokens";
import { KPI_TRANSLATION_CONFIG, TranslationConfiguration } from "@citesa/kpi-client";
import { IndicatorPointService } from "../../services/indicator-point.service";
import { IndicatorDashboardService } from "../../services/indicator-dashboard.service";

export const KPIProviders = {
    provideTranslations,
    provideIndicatorPointService,
    provideDashboardResolver,
}



/**  */

//** TRANSLATIONS */

function provideTranslations(factory: (params: { language: TranslateService }) => TranslationConfiguration): Provider {
    return {
        provide: KPI_TRANSLATION_CONFIG,
        useFactory: (language) => factory({ language }),
        deps: [
            TranslateService
        ]
    }
}

//  * INDICATOR POINT
function provideIndicatorPointService(): Provider {
    return {
        provide: INDICATOR_POINT_SERVICE,
        useFactory: (indicatorPointService: IndicatorPointService) => {
            const service: IndicatorPointServiceInterface = {
                exportJSON: (id, lookup, skipLoader) => indicatorPointService.exportJSON(id, lookup, skipLoader),
                exportXlsx: (id, lookup, skipLoader) => indicatorPointService.exportXlsx(id, lookup, skipLoader),
                getIndicatorPointQueryDistinct: (lookup) => indicatorPointService.getIndicatorPointQueryDistinct(lookup),
                getIndicatorPointReport: ({ id, lookup, skipLoader, tokenParams }) => {
                    return indicatorPointService.getIndicatorPointReport(id, lookup, skipLoader);
                    

                }
            }

            return service;
        },
        deps: [
            IndicatorPointService,
        ]
    }
}

//  ** DASHOARD RESOLVER

function provideDashboardResolver(): Provider{
    return {
        provide: KPI_DASHBOARD_RESOLVER,
        useFactory: (indicatorDashboardService: IndicatorDashboardService) => {
            const dashboardResolver: KPIDashboardResolver = {
                getDashboard: ({ dashboardKey }) => 
                indicatorDashboardService.getDashboard(dashboardKey)
            }

            return dashboardResolver;
        },
        deps:[IndicatorDashboardService]
    }
}