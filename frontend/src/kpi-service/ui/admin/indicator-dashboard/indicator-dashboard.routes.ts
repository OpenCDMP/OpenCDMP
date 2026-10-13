import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { KpiAuthGuard } from 'kpi-service/core/kpi-auth-guard.service';

export const INDICATOR_DASHBOARD_ROUTES: Routes = [
	{
		path: '',
		loadComponent: () => import('./indicator-dashboard.component').then(m => m.IndicatorDashboardComponent),
		canActivate: [(next: ActivatedRouteSnapshot, state: RouterStateSnapshot) => inject(KpiAuthGuard).kpiCanActivate(next, state)],
		data: {
			authContext: {
				permissions: [AppPermission.ViewIndicatorDashboardPage]
			},
			breadcrumb: true
		}
	},
	{ path: '**', loadComponent: () => import('@common/modules/page-not-found/page-not-found.component').then(m => m.PageNotFoundComponent) }
];
