import { Injector, Provider } from "@angular/core";import { TranslateService } from "@ngx-translate/core";
import { INDICATOR_POINT_SERVICE, IndicatorPointServiceInterface, KPI_DASHBOARD_RESOLVER, KPI_FILTER_CONFIG, KPI_VALUE_CALLBACK_CONFIG, KPIDashboardResolver, SHARE_CHART_CONFIGURATION, ShareChartConfig } from "@citesa/kpi-client/tokens";
import { KPI_TRANSLATION_CONFIG, TranslationConfiguration } from "@citesa/kpi-client";
import { IndicatorPointService } from "../../services/indicator-point.service";
import { IndicatorDashboardService } from "../../services/indicator-dashboard.service";
import { map, Observable, of } from "rxjs";
import { PlanStatusService } from "@app/core/services/plan/plan-status.service";
import { IsActive } from "@notification-service/core/enum/is-active.enum";
import { DescriptionStatusService } from "@app/core/services/description-status/description-status.service";
import { ReferenceTypeService } from "@app/core/services/reference-type/reference-type.service";
import { DescriptionTemplateService } from "@app/core/services/description-template/description-template.service";
import { PlanBlueprintService } from "@app/core/services/plan/plan-blueprint.service";
import { SingleAutoCompleteConfiguration } from "@citesa/kpi-client/types";

export const KPIProviders = {
    provideTranslations,
    provideIndicatorPointService,
    provideDashboardResolver,
    provideValueMapResolver,
    provideFilterConfigResolver
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
                exportJSON: (id, code, lookup, skipLoader) => indicatorPointService.exportJSON(id, code, lookup, skipLoader),
                exportXlsx: (id, code, lookup, skipLoader) => indicatorPointService.exportXlsx(id, code, lookup, skipLoader),
                getIndicatorPointQueryDistinct: (lookup) => indicatorPointService.getIndicatorPointQueryDistinct(lookup),
                getIndicatorPointReport: ({ id, code, lookup, skipLoader, tokenParams }) => {
                    return indicatorPointService.getIndicatorPointReport(id, code, lookup, skipLoader);
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

//  ** DASHOARD VALUE MAP RESOLVER

function provideValueMapResolver(): Provider {
    return {
        provide: KPI_VALUE_CALLBACK_CONFIG,
        useFactory: () => {
            return {
                plan_status_id: (values: string[], injector: Injector): Observable<Map<string, string>> => {
                    const ids = values.filter(x => !!x);
                    if(!ids?.length){ return of(new Map<string, string>([]))}
                    const planStatusService = injector.get(PlanStatusService);
                    return planStatusService.query(
                        planStatusService.buildAutocompleteLookup([IsActive.Active, IsActive.Inactive])
                    ).pipe(map((planStatuses) => new Map<string, string>(planStatuses?.items?.map(item => ([item.id.toString(), item.name])))))
                },
                blueprint_group_id: (values: string[], injector: Injector): Observable<Map<string, string>> => {
                    const ids = values.filter(x => !!x);
                    if(!ids?.length){ return of(new Map<string, string>([]))}
                    const planBlueprintService = injector.get(PlanBlueprintService);
                    return planBlueprintService.query(
                        planBlueprintService.buildPlanBlueprintGroupAutocompleteLookup({isActive: [IsActive.Active, IsActive.Inactive]})
                    ).pipe(map((planBlueprints) => new Map<string, string>(planBlueprints?.items?.map(item => ([item.groupId.toString(), item.label])))))
                },
                description_status_id: (values: string[], injector: Injector): Observable<Map<string, string>> => {
                    const ids = values.filter(x => !!x);
                    if(!ids?.length){ return of(new Map<string, string>([]))}
                    const descriptionStatusService = injector.get(DescriptionStatusService);
                    return descriptionStatusService.query(
                        descriptionStatusService.buildAutocompleteLookup([IsActive.Active, IsActive.Inactive])
                    ).pipe(map((desctriptionStatuses) => new Map<string, string>(desctriptionStatuses?.items?.map(item => ([item.id.toString(), item.name])))))
                },
                template_group_id: (values: string[], injector: Injector): Observable<Map<string, string>> => {
                    const ids = values.filter(x => !!x);
                    if(!ids?.length){ return of(new Map<string, string>([]))}
                    const descriptionTemplateService = injector.get(DescriptionTemplateService);
                    return descriptionTemplateService.query(
                        descriptionTemplateService.buildDescriptionTemplateGroupAutocompleteLookup({isActive: [IsActive.Active, IsActive.Inactive]})
                    ).pipe(map((descriptionTemplates) => new Map<string, string>(descriptionTemplates?.items?.map(item => ([item.groupId.toString(), item.label])))))
                },
                reference_type_id: (values: string[], injector: Injector): Observable<Map<string, string>> => {
                    const ids = values.filter(x => !!x);
                    if(!ids?.length){ return of(new Map<string, string>([]))}
                    const referenceTypeService = injector.get(ReferenceTypeService);
                    return referenceTypeService.query(
                        referenceTypeService.buildAutocompleteLookup([IsActive.Active, IsActive.Inactive])
                    ).pipe(map((referenceTypes) => new Map<string, string>(referenceTypes?.items?.map(item => ([item.id.toString(), item.name])))))
                }
            }
        },
        deps: [Injector]
    }
}


function provideFilterConfigResolver(): Provider {
    return {
        provide: KPI_FILTER_CONFIG,
        useFactory: () => {
            return {
                autoComplete: {}
            }
        },
        deps: [Injector]
    }
}