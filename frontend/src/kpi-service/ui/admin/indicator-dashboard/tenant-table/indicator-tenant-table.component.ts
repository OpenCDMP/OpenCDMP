import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { AggregateResponseModel, AggregationMetricSortField, Bucket, BucketAggregateType, IndicatorPointDateRangeFilter, IndicatorPointReportLookup, MetricAggregateType } from '@citesa/kpi-client/types';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { IndicatorPointService } from 'kpi-service/services/indicator-point.service';
import moment from 'moment';
import { catchError, forkJoin, map, Observable, of, switchMap } from 'rxjs';

interface TenantRow {
	tenantCode: string;
	plan: number;
	description: number;
	user: number;
	planBlueprint: number;
	descriptionTemplate: number;
	newPlans: number;
}

type CountField = 'plan' | 'description' | 'user' | 'planBlueprint' | 'descriptionTemplate';

@Component({
	selector: 'app-indicator-tenant-table',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule],
	templateUrl: './indicator-tenant-table.component.html',
	styleUrl: './indicator-tenant-table.component.scss',
})
export class IndicatorTenantTableComponent {
	private readonly indicatorPointService = inject(IndicatorPointService);

	readonly title = input.required<string>();
	readonly indicatorId = input.required<string>();
	readonly currentTenant = input<string>();

	protected readonly rows = toSignal<TenantRow[]>(toObservable(this.indicatorId).pipe(
		switchMap(indicatorId => forkJoin([
			this.report(indicatorId, ['plan', 'description', 'user'], []),
			// blueprints and templates send a point per version too, so only count the totals
			this.report(indicatorId, ['planBlueprint', 'descriptionTemplate'], [{ field: 'latest_version', values: ['totalCount'] }]),
			this.report(indicatorId, ['plan'], [], { field: 'created_at', from: moment().subtract(30, 'days'), to: moment() }),
		]).pipe(
			map(([counts, versionedCounts, newPlans]) => this.toRows([...counts.items ?? [], ...versionedCounts.items ?? []], newPlans)),
			catchError(() => of<TenantRow[]>(null)),
		)),
	));

	protected readonly maxPlans = computed(() => Math.max(1, ...(this.rows() ?? []).map(x => x.plan)));

	private report(indicatorId: string, entityTypes: string[], extraFilters: { field: string, values: string[] }[], dateRange?: IndicatorPointDateRangeFilter): Observable<AggregateResponseModel> {
		const metrics = [{ type: MetricAggregateType.Sum, field: 'value', pipelines: [] }];
		const lookup = {
			metrics,
			bucket: {
				type: BucketAggregateType.Composite,
				field: 'tenant_code',
				sources: [{ field: 'tenant_code' }, { field: 'entity_type' }],
				metrics,
				bucketSort: {
					sortFields: [{ field: 'value', metricAggregateType: MetricAggregateType.Sum, order: AggregationMetricSortField.Order.Descending }],
					paging: { offset: 0, size: 1000 },
				},
			} as unknown as Bucket,
			filters: {
				keywordFilters: [
					{ field: 'entity_type', values: entityTypes },
					{ field: 'metric_type', values: ['overtime'] },
					...extraFilters,
				],
				dateRangeFilters: dateRange ? [dateRange] : undefined,
			},
		} as IndicatorPointReportLookup;
		return this.indicatorPointService.getIndicatorPointReport(indicatorId, null, lookup, true);
	}

	private toRows(countItems: AggregateResponseModel['items'], newPlans: AggregateResponseModel): TenantRow[] {
		const rows = new Map<string, TenantRow>();
		const rowFor = (tenantCode: string) => {
			if (!rows.has(tenantCode)) rows.set(tenantCode, { tenantCode, plan: 0, description: 0, user: 0, planBlueprint: 0, descriptionTemplate: 0, newPlans: 0 });
			return rows.get(tenantCode);
		};
		const sum = (item: AggregateResponseModel['items'][number]) => item.values?.find(x => x.aggregateType === MetricAggregateType.Sum)?.value ?? 0;

		// each response also carries its overall total as an item without a tenant
		for (const item of countItems.filter(x => x.group.items['tenant_code'])) {
			rowFor(item.group.items['tenant_code'])[item.group.items['entity_type'] as CountField] += sum(item);
		}
		for (const item of (newPlans.items ?? []).filter(x => x.group.items['tenant_code'])) {
			rowFor(item.group.items['tenant_code']).newPlans += sum(item);
		}
		return [...rows.values()].sort((a, b) => b.plan - a.plan);
	}
}
