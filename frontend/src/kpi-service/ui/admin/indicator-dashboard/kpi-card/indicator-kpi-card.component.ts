import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { ChartHelperService } from '@citesa/kpi-client/services';
import { AggregateResponseItemModel, BaseIndicatorDashboardGaugeConfig, Bucket, BucketAggregateType, DateInterval, IndicatorQueryParams, MetricAggregateType } from '@citesa/kpi-client/types';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { IndicatorPointService } from 'kpi-service/services/indicator-point.service';
import moment from 'moment';
import { catchError, forkJoin, map, of, switchMap } from 'rxjs';

interface KpiCardData {
	error?: boolean;
	total?: number;
	recent?: number;
	sparkline?: string;
	sparklineArea?: string;
}

const SPARKLINE_MONTHS = 12;

@Component({
	selector: 'app-indicator-kpi-card',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule],
	templateUrl: './indicator-kpi-card.component.html',
	styleUrl: './indicator-kpi-card.component.scss',
})
export class IndicatorKpiCardComponent {
	private readonly chartHelperService = inject(ChartHelperService);
	private readonly indicatorPointService = inject(IndicatorPointService);

	readonly gauge = input.required<BaseIndicatorDashboardGaugeConfig>();
	readonly queryParams = input.required<IndicatorQueryParams>();

	protected readonly data = toSignal(toObservable(computed(() => ({ gauge: this.gauge(), queryParams: this.queryParams() }))).pipe(
		switchMap(({ gauge, queryParams }) => {
			const history = this.chartHelperService.buildGaugeIndicatorLookup(gauge, queryParams);
			history.bucket = { type: BucketAggregateType.DateHistogram, field: 'created_at', interval: DateInterval.Month, metrics: history.metrics } as unknown as Bucket;

			const recent = this.chartHelperService.buildGaugeIndicatorLookup(gauge, queryParams);
			recent.filters.dateRangeFilters = [{ field: 'created_at', from: moment().subtract(30, 'days'), to: moment() }];

			return forkJoin([
				this.indicatorPointService.getIndicatorPointReport(gauge.indicatorId, gauge.indicatorCode, history, true),
				this.indicatorPointService.getIndicatorPointReport(gauge.indicatorId, gauge.indicatorCode, recent, true),
			]).pipe(
				map(([historyResponse, recentResponse]) => this.toData(historyResponse.items ?? [], recentResponse.items ?? [])),
				catchError(() => of<KpiCardData>({ error: true })),
			);
		}),
	));

	private toData(historyItems: AggregateResponseItemModel[], recentItems: AggregateResponseItemModel[]): KpiCardData {
		const sum = (item: AggregateResponseItemModel) => item.values?.find(x => x.aggregateType === MetricAggregateType.Sum)?.value ?? 0;
		// the response also carries the overall total as an item without a month; the buckets already add up to it
		const buckets = historyItems.filter(x => x.group?.items?.['created_at']);
		const totalUntil = (end: number) => buckets.filter(x => Date.parse(x.group.items['created_at']) <= end).map(sum).reduce((a, b) => a + b, 0);

		// months without points have no bucket, so walk the calendar months instead of the last buckets
		const series: number[] = [];
		for (let monthsAgo = SPARKLINE_MONTHS - 1; monthsAgo >= 0; monthsAgo--) {
			series.push(totalUntil(moment().subtract(monthsAgo, 'months').endOf('month').valueOf()));
		}
		const runningTotal = series[series.length - 1];
		const min = Math.min(...series);
		const range = (Math.max(...series) - min) || 1;
		const points = series.map((value, index) => `${series.length > 1 ? (index / (series.length - 1) * 100).toFixed(1) : 0},${(30 - (value - min) / range * 26).toFixed(1)}`).join(' ');

		return {
			total: runningTotal,
			recent: recentItems.map(sum).reduce((a, b) => a + b, 0),
			sparkline: points,
			sparklineArea: series.length ? `0,32 ${points} 100,32` : '',
		};
	}
}
