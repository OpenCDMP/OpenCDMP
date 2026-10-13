import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '@app/core/services/auth/auth.service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { TranslateModule } from '@ngx-translate/core';
import { filter } from 'rxjs';
import { groupTcPages, TC_PAGES } from '../shared/tc-pages';

/** Layout of the settings hub: the section menu and the open section, which may be one of the admin listings it wraps. */
@Component({
	selector: 'app-tc-shell',
	standalone: true,
	// Not OnPush: the wrapped admin listings set plain fields from HTTP responses, and an OnPush host would leave them
	// unpainted until the next click inside them.
	changeDetection: ChangeDetectionStrategy.Eager,
	imports: [RouterOutlet, RouterLink, RouterLinkActive, TranslateModule, CommonUiModule],
	templateUrl: './tc-shell.component.html',
	styleUrl: './tc-shell.component.scss',
})
export class TcShellComponent {
	private readonly authService = inject(AuthService);
	private readonly routerUtils = inject(RouterUtilsService);

	// a wrapped listing lives at its own address, outside /tenant-configuration
	protected readonly wrapped = inject(ActivatedRoute).snapshot.data['tcWrapped'] === true;

	constructor() {
		if (this.wrapped) return;
		// one page view per section
		const analytics = inject(AnalyticsService);
		analytics.trackPageView(AnalyticsService.TenantConfigurationsColorsEditor);
		inject(Router).events.pipe(filter(x => x instanceof NavigationEnd), takeUntilDestroyed())
			.subscribe(() => analytics.trackPageView(AnalyticsService.TenantConfigurationsColorsEditor));
	}

	protected readonly groups = groupTcPages(TC_PAGES
		.filter(x => this.authService.hasAnyPermission(x.permissions))
		.map(x => ({ ...x, url: this.routerUtils.generateUrl(x.path) })));

	protected readonly settingsUrl = this.routerUtils.generateUrl('/tenant-configuration/overview');
}
