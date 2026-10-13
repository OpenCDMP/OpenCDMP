import { Routes } from '@angular/router';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { AuthGuard } from '@app/core/auth-guard.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { PendingChangesGuard } from '@common/forms/pending-form-changes/pending-form-changes-guard.service';
import { TcShellComponent } from './shell/tc-shell.component';

const title = (key: string) => BreadcrumbService.generateRouteDataConfiguration({ title: `TENANT-CONFIGURATION.NAV.${key}` });
const section = (key: string, permissions: AppPermission[] = [AppPermission.ViewTenantConfigurationPage]) => ({ ...title(key), authContext: { permissions } });

/** The tenant configuration page: a shell with the section menu, and one route per section. */
export const TENANT_CONFIGURATION_ROUTES: Routes = [
	{
		path: '',
		component: TcShellComponent,
		canActivate: [AuthGuard],
		providers: [PendingChangesGuard],
		children: [
			{ path: '', pathMatch: 'full', redirectTo: 'overview' },
			{ path: 'overview', data: section('OVERVIEW'), canActivate: [AuthGuard], loadComponent: () => import('./sections/overview/tc-overview.component').then(m => m.TcOverviewComponent) },
			{ path: 'branding', data: section('BRANDING'), canActivate: [AuthGuard], loadComponent: () => import('./sections/branding/tc-branding.component').then(m => m.TcBrandingComponent), canDeactivate: [PendingChangesGuard] },
			{ path: 'region', data: section('REGION'), canActivate: [AuthGuard], loadComponent: () => import('./sections/region/tc-region.component').then(m => m.TcRegionComponent), canDeactivate: [PendingChangesGuard] },
			{
				path: 'integrations',
				data: section('INTEGRATIONS'),
				canActivate: [AuthGuard],
				children: [
					{ path: '', loadComponent: () => import('./sections/integrations/tc-integrations.component').then(m => m.TcIntegrationsComponent), canDeactivate: [PendingChangesGuard] },
					{
						// the kind is part of the address but not a page of its own
						path: ':kind',
						data: BreadcrumbService.generateRouteDataConfiguration({ hideNavigationItem: true }),
						children: [
							{ path: ':id', loadComponent: () => import('./sections/integrations/tc-plugin.component').then(m => m.TcPluginComponent), canDeactivate: [PendingChangesGuard] },
						],
					},
				],
			},
			{ path: 'workflows', data: section('WORKFLOWS', [AppPermission.ViewTenantConfigurationPage, AppPermission.ViewPlanWorkflowPage, AppPermission.ViewDescriptionWorkflowPage]), canActivate: [AuthGuard], loadComponent: () => import('./sections/workflows/tc-workflows.component').then(m => m.TcWorkflowsComponent), canDeactivate: [PendingChangesGuard] },
			{ path: 'content-defaults', data: section('CONTENT-DEFAULTS'), canActivate: [AuthGuard], loadComponent: () => import('./sections/content-defaults/tc-content-defaults.component').then(m => m.TcContentDefaultsComponent), canDeactivate: [PendingChangesGuard] },
			{ path: 'notifications', data: section('NOTIFICATIONS'), canActivate: [AuthGuard], loadComponent: () => import('./sections/notifications/tc-notifications.component').then(m => m.TcNotificationsComponent), canDeactivate: [PendingChangesGuard] },
			{ path: 'onboarding', data: section('ONBOARDING'), canActivate: [AuthGuard], loadComponent: () => import('./sections/onboarding/tc-onboarding.component').then(m => m.TcOnboardingComponent), canDeactivate: [PendingChangesGuard] },
		],
	},
];
