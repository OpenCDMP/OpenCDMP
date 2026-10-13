import { ChangeDetectionStrategy, Component, computed, input, signal } from '@angular/core';
import { KPIDashboardChartComponent } from '@citesa/kpi-client';
import { BaseIndicatorDashboardChartConfig, IndicatorQueryParams } from '@citesa/kpi-client/types';
import { CommonUiModule } from '@common/ui/common-ui.module';
import type { ViewChart } from '../indicator-dashboard.component';

@Component({
	selector: 'app-indicator-chart-card',
	standalone: true,
	// the library chart sets its own fields from subscriptions and needs its ancestors checked by default change detection
	changeDetection: ChangeDetectionStrategy.Eager,
	imports: [CommonUiModule, KPIDashboardChartComponent],
	templateUrl: './indicator-chart-card.component.html',
	styleUrl: './indicator-chart-card.component.scss',
})
export class IndicatorChartCardComponent {
	readonly title = input.required<string>();
	readonly help = input<string>();
	readonly charts = input.required<ViewChart[]>();
	readonly queryParams = input.required<IndicatorQueryParams>();
	readonly palette = input<string[]>();

	protected readonly selectedIndex = signal(0);

	// the card shows the title and help, so the library must not draw its own description under the chart
	protected readonly chartConfig = computed<BaseIndicatorDashboardChartConfig>(() => {
		const chart = this.charts()[this.selectedIndex()] as ViewChart & { colorPalette?: string[] };
		return { ...chart, colorPalette: chart.colorPalette ?? this.palette(), description: undefined, descriptionLanguage: undefined };
	});
}
