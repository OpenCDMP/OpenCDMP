import { Injector, Provider } from "@angular/core"; import { TranslateService } from "@ngx-translate/core";
import { INDICATOR_POINT_SERVICE, IndicatorPointServiceInterface, KPI_DASHBOARD_RESOLVER, KPI_FILTER_CONFIG, KPI_VALUE_CALLBACK_CONFIG, KPIDashboardResolver } from "@citesa/kpi-client/tokens";
import { KPI_TRANSLATION_CONFIG, TranslationConfiguration } from "@citesa/kpi-client";
import { IndicatorPointService } from "../../services/indicator-point.service";
import { IndicatorDashboardService } from "../../services/indicator-dashboard.service";
import { map, Observable, of, switchMap, tap } from "rxjs";
import { PlanStatusService } from "@app/core/services/plan/plan-status.service";
import { IsActive } from "@notification-service/core/enum/is-active.enum";
import { DescriptionStatusService } from "@app/core/services/description-status/description-status.service";
import { ReferenceTypeService } from "@app/core/services/reference-type/reference-type.service";
import { ReferenceService } from "@app/core/services/reference/reference.service";
import { DescriptionTemplateService } from "@app/core/services/description-template/description-template.service";
import { PlanBlueprintService } from "@app/core/services/plan/plan-blueprint.service";
import { PlanService } from "@app/core/services/plan/plan.service";
import { Guid } from "@common/types/guid";
import { SingleAutoCompleteConfiguration } from "@citesa/kpi-client/types";
import { PlanBlueprintTypeService } from "@app/core/services/plan-blueprint-type/plan-blueprint-type.service";
import { DescriptionTemplateTypeService } from "@app/core/services/description-template-type/description-template-type.service";
import { StatisticsService } from "@app/core/services/statistics.service";

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

//  ** DASHBOARD RESOLVER

function provideDashboardResolver(): Provider {
    return {
        provide: KPI_DASHBOARD_RESOLVER,
        useFactory: (indicatorDashboardService: IndicatorDashboardService) => {
            const dashboardResolver: KPIDashboardResolver = {
                getDashboard: ({ dashboardKey }) =>
                    indicatorDashboardService.getDashboard(dashboardKey)
            }

            return dashboardResolver;
        },
        deps: [IndicatorDashboardService]
    }
}


//  ** DASHOARD VALUE MAP RESOLVER

function provideValueMapResolver(): Provider {
    return {
        provide: KPI_VALUE_CALLBACK_CONFIG,
        useFactory: () => {
            return {
                plan_status_id: (values: string[], injector: Injector): Observable<Map<string, string>> => { //
                    const ids = values.filter(x => !!x);
                    if (!ids?.length) { return of(new Map<string, string>([])) }
                    const statisticsService = injector.get(StatisticsService);
                    return statisticsService.queryPlanStatusNames(ids.map((item)=>Guid.parse(item))).pipe(
                        map(({items})=>new Map<string,string>((items ?? []).map(item => [item.id.toString() || '' ,item.name])))
                    );
                },
                blueprint_group_id: (values: string[], injector: Injector): Observable<Map<string, string>> => { //
                    const ids = values.filter(x => !!x);
                    if (!ids?.length) { return of(new Map<string, string>([])) }
                    const statisticsService = injector.get(StatisticsService);
                     return statisticsService.queryBlueprintGroupNames(ids.map((item)=>Guid.parse(item))).pipe(
                        map(({items})=>new Map<string,string>((items ?? []).map(item => [item.id.toString() || '' ,item.name])))
                    );
                },
                description_status_id: (values: string[], injector: Injector): Observable<Map<string, string>> => { //
                    const ids = values.filter(x => !!x);
                    if (!ids?.length) { return of(new Map<string, string>([])) }
                    const statisticsService = injector.get(StatisticsService);
                     return statisticsService.queryDescriptionStatusNames(ids.map((item)=>Guid.parse(item))).pipe(
                        map(({items})=>new Map<string,string>((items ?? []).map(item => [item.id.toString() || '' ,item.name])))
                    );
                    }
                ,
                template_group_id: (values: string[], injector: Injector): Observable<Map<string, string>> => {
                    const ids = values.filter(x => !!x);
                    if (!ids?.length) { return of(new Map<string, string>([])) }
                    const statisticsService = injector.get(StatisticsService);
                     return statisticsService.queryTemplateGroupNames(ids.map((item)=>Guid.parse(item))).pipe(
                        map(({items})=>new Map<string,string>((items ?? []).map(item => [item.id.toString() || '' ,item.name])))
                    );
                },
                reference_type_id: (values: string[], injector: Injector): Observable<Map<string, string>> => {
                    const ids = values.filter(x => !!x);
                    if (!ids?.length) { return of(new Map<string, string>([])) }
                       const statisticsService = injector.get(StatisticsService);
                     return statisticsService.queryReferenceTypeNames(ids.map((item)=>Guid.parse(item))).pipe(
                        map(({items})=>new Map<string,string>((items ?? []).map(item => [item.id.toString() || '' ,item.name])))
                    );
                },
                latest_version: (values: string[], injector: Injector): Observable<Map<string, string>> => {
                    const translateService = injector.get(TranslateService);
                    const map = new Map<string, string>();
                    for (const v of values ?? []) {
                        if (v === "totalCount") map.set(v, translateService.instant("KPI-DASHBOARDS.VERSION.TOTAL-COUNT"));
                        else if (v === "latestVersion") map.set(v, translateService.instant("KPI-DASHBOARDS.VERSION.LATEST-VERSION"));
                    }
                    return of(map);
                },
                plan_visibility: (values: string[], injector: Injector): Observable<Map<string, string>> => {
                    const translateService = injector.get(TranslateService);
                    const map = new Map<string, string>();
                    for (const v of values ?? []) {
                        if (v === "public") map.set(v, translateService.instant("KPI-DASHBOARDS.VISIBILITY.PUBLIC"));
                        else if (v === "restricted") map.set(v, translateService.instant("KPI-DASHBOARDS.VISIBILITY.RESTRICTED"));
                        else map.set(v, translateService.instant("KPI-DASHBOARDS.VISIBILITY.NULL"));
                    }
                    return of(map);
                },
                plan_id: (values: string[], injector: Injector): Observable<Map<string, string>> => {
                    const ids = (values ?? []).filter(x => !!x);
                    if (!ids.length) return of(new Map<string, string>([]));
                     const statisticsService = injector.get(StatisticsService);
                     return statisticsService.queryPlanNames(ids.map((item)=>Guid.parse(item))).pipe(
                        map(({items})=>new Map<string,string>((items ?? []).map(item => [item.id.toString() || '' ,item.name])))
                    );
                },
                reference_id: (values: string[], injector: Injector): Observable<Map<string, string>> => {
                    const ids = (values ?? []).filter(x => !!x);
                    if (!ids.length) return of(new Map<string, string>([]));
                   const statisticsService = injector.get(StatisticsService);
                     return statisticsService.queryReferenceNames(ids.map((item)=>Guid.parse(item))).pipe(
                        map(({items})=>new Map<string,string>((items ?? []).map(item => [item.id.toString() || '' ,item.name])))
                    );
                },
                plan_blueprint_type_id: (values: string[], injector: Injector): Observable<Map<string, string>> => {
                    const ids = values.filter(x => !!x);
                    if (!ids?.length) { return of(new Map<string, string>([])) }
                        const statisticsService = injector.get(StatisticsService);
                    return statisticsService.queryPlanBlueprintTypeNames(ids.map((item)=>Guid.parse(item))).pipe(
                        map(({items})=>new Map<string,string>((items ?? []).map(item => [item.id.toString() || '' ,item.name])))
                    );
                },
                description_template_type_id: (values: string[], injector: Injector): Observable<Map<string, string>> => {
                    const ids = values.filter(x => !!x);
                    if (!ids?.length) { return of(new Map<string, string>([])) }
                        const statisticsService = injector.get(StatisticsService);
                    return statisticsService.queryDescriptionTemplateTypeNames(ids.map((item)=>Guid.parse(item))).pipe(
                        map(({items})=>new Map<string,string>((items ?? []).map(item => [item.id.toString() || '' ,item.name])))
                    );
                },
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