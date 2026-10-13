import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { BaseIndicatorDashboardGaugeConfig, IndicatorDashboardChart, IndicatorQueryParams } from '@citesa/kpi-client/types';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { IndicatorDashboardService } from 'kpi-service/services/indicator-dashboard.service';
import { catchError, map, of, startWith, switchMap } from 'rxjs';
import { IndicatorChartCardComponent } from './chart-card/indicator-chart-card.component';
import { IndicatorKpiCardComponent } from './kpi-card/indicator-kpi-card.component';
import { IndicatorTenantTableComponent } from './tenant-table/indicator-tenant-table.component';

interface DashboardScope {
	key: string;
	isTenant: boolean;
	queryParams: IndicatorQueryParams;
}

export type ViewChart = IndicatorDashboardChart & { viewLanguageName?: string };

interface DashboardBlock {
	type: 'gauge' | 'chartgroup' | 'tenant-table';
	name: string;
	languageName?: string;
	descriptionLanguage?: string;
	width?: 'full' | 'wide' | 'half' | 'third';
	charts?: ViewChart[];
	gauges?: BaseIndicatorDashboardGaugeConfig[];
	indicatorId?: string;
}

interface DashboardTab {
	name: string;
	languageName?: string;
	chartGroups: DashboardBlock[];
}

interface DashboardView {
	status: 'loading' | 'ready' | 'error';
	gauges?: BaseIndicatorDashboardGaugeConfig[];
	tabs?: DashboardTab[];
}

@Component({
	selector: 'app-indicator-dashboard',
	standalone: true,
	// default change detection, like the chart card: the library charts below it update outside signals
	changeDetection: ChangeDetectionStrategy.Eager,
	imports: [CommonUiModule, IndicatorKpiCardComponent, IndicatorChartCardComponent, IndicatorTenantTableComponent],
	templateUrl: './indicator-dashboard.component.html',
	styleUrl: './indicator-dashboard.component.scss',
})
export class IndicatorDashboardComponent {
	private readonly authService = inject(AuthService);
	private readonly configurationService = inject(ConfigurationService);
	private readonly indicatorDashboardService = inject(IndicatorDashboardService);
	protected readonly tenantCode = this.authService.selectedTenant() ?? 'default';
	protected readonly scopes: DashboardScope[] = this.buildScopes();
	protected readonly selectedKey = signal<string>(this.scopes[0]?.key);
	protected readonly selectedTab = signal(0);
	protected readonly selectedScope = computed(() => this.scopes.find(x => x.key === this.selectedKey()));
	protected readonly palette = this.buildPalette();

	protected readonly view = toSignal(toObservable(this.selectedScope).pipe(
		switchMap(scope => !scope ? of<DashboardView>(null) : this.indicatorDashboardService.getDashboard(scope.key).pipe(
			map(config => this.toView(config.tabs as unknown as DashboardTab[])),
			catchError(() => of<DashboardView>({ status: 'error' })),
			startWith<DashboardView>({ status: 'loading' }),
		)),
	));

	private buildScopes(): DashboardScope[] {
		const kpi = this.configurationService.kpi;
		if (!kpi?.enabled || !this.authService.hasPermission(AppPermission.ViewIndicatorDashboardPage)) return [];
		const roles = this.authService.getSelectedRoles() ?? [];
		return (kpi.dashboards ?? [])
			.filter(x => x.dashboardId && x.availableToRoles?.some(role => roles.includes(role)))
			.map(x => ({
				key: x.dashboardId,
				isTenant: !!x.keywordFilter,
				queryParams: {
					dashboard: x.dashboardId,
					displayName: undefined,
					keywordFilters: x.keywordFilter ? [{ field: x.keywordFilter, values: [this.tenantCode] }] : undefined,
				},
			}));
	}

	// the number cards come from the first gauge block and sit above the tabs
	private toView(tabs: DashboardTab[]): DashboardView {
		const gaugeBlock = tabs?.flatMap(tab => tab.chartGroups).find(block => block.type === 'gauge');
		return {
			status: 'ready',
			gauges: gaugeBlock?.gauges ?? [],
			tabs: (tabs ?? []).map(tab => ({ ...tab, chartGroups: tab.chartGroups.filter(block => block !== gaugeBlock) })),
		};
	}

	// ECharts can't read CSS variables, so resolve the tenant theme colours once and mix lighter tones from the primary
	private buildPalette(): string[] {
		const probe = document.createElement('span');
		document.body.appendChild(probe);
		const resolve = (token: string): number[] => {
			probe.style.color = `var(${token})`;
			return getComputedStyle(probe).color.match(/\d+(\.\d+)?/g).slice(0, 3).map(Number);
		};
		const primary = resolve('--mat-sys-primary');
		const tertiary = resolve('--mat-sys-tertiary');
		const secondary = resolve('--mat-sys-secondary');
		probe.remove();
		const tint = (rgb: number[], amount: number) => rgb.map(c => Math.round(c + (255 - c) * amount));
		const css = (rgb: number[]) => `rgb(${rgb.join(', ')})`;
		return [css(primary), css(tint(primary, 0.45)), css(tertiary), css(tint(primary, 0.7)), css(secondary), css(tint(tertiary, 0.5))];
	}
}
