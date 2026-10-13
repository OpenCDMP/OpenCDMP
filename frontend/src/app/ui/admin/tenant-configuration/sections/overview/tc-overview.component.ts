import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { TenantConfiguration } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { PluginIntegrationService, PluginSourceOverview } from '@app/core/services/plugin-integration/plugin-integration.service';
import { AuthService } from '@app/core/services/auth/auth.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { RouterLink } from '@angular/router';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { catchError, forkJoin, map, of } from 'rxjs';
import { nameof } from 'ts-simple-nameof';
import { groupTcPages, TC_PAGES, TcPage } from '../../shared/tc-pages';
import { INHERITABLE_TYPES } from '../../shared/tc-part';

/** The front page: every section, and whether the organisation changed anything in it. */
@Component({
	selector: 'app-tc-overview',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, RouterLink],
	templateUrl: './tc-overview.component.html',
	styleUrl: './tc-overview.component.scss',
})
export class TcOverviewComponent {
	private readonly tenantConfigurationService = inject(TenantConfigurationService);
	private readonly integrationService = inject(PluginIntegrationService);
	private readonly routerUtils = inject(RouterUtilsService);

	private readonly authService = inject(AuthService);

	private readonly cards = TC_PAGES.filter(x => x.group && this.authService.hasAnyPermission(x.permissions));
	protected readonly groups = groupTcPages(this.cards);

	private readonly fields = [
		nameof<TenantConfiguration>(x => x.id),
		nameof<TenantConfiguration>(x => x.type),
		nameof<TenantConfiguration>(x => x.updatedAt),
		'belongsToCurrentTenant',
	];

	/** The record in effect per type, the tenant's own or one inherited from the default tenant; missing means the installation default applies. */
	private readonly records = toSignal(forkJoin(
		this.cards.flatMap(x => x.types ?? []).map(type => (INHERITABLE_TYPES.has(type) ? this.tenantConfigurationService.getActiveType(type, this.fields) : this.tenantConfigurationService.getType(type, this.fields)).pipe(
			map(item => [type, item?.id ? item : null] as const),
			catchError(() => of([type, null] as const)),
		)),
	).pipe(map(pairs => new Map<TenantConfigurationType, TenantConfiguration | null>(pairs))));

	private readonly plugins = toSignal(this.integrationService.getSources().pipe(catchError(() => of([] as PluginSourceOverview[]))));

	protected readonly loading = computed(() => !this.records());

	protected readonly pluginSummary = computed(() => {
		const plugins = this.plugins();
		if (!plugins) return null;
		return { on: plugins.filter(x => x.enabled).length, total: plugins.length, problems: plugins.filter(x => x.enabled && x.reachable === false).length };
	});

	protected url(card: TcPage): string {
		return this.routerUtils.generateUrl(card.path);
	}

	protected changedCount(card: TcPage): number {
		const records = this.records();
		return records ? (card.types ?? []).filter(x => records.get(x) && records.get(x).belongsToCurrentTenant !== false).length : 0;
	}

	protected inheritedCount(card: TcPage): number {
		const records = this.records();
		return records ? (card.types ?? []).filter(x => records.get(x)?.belongsToCurrentTenant === false).length : 0;
	}

	protected lastUpdated(card: TcPage): Date | null {
		const records = this.records();
		const dates = (card.types ?? []).filter(x => records?.get(x)?.belongsToCurrentTenant !== false).map(x => records?.get(x)?.updatedAt).filter(Boolean).map(x => new Date(x as unknown as string));
		return dates.length ? new Date(Math.max(...dates.map(x => x.getTime()))) : null;
	}
}
