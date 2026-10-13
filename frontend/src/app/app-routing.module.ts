import { NgModule } from '@angular/core';
import { RouterModule, Routes, TitleStrategy } from '@angular/router';
import { AppPermission } from './core/common/enum/permission.enum';
import { AppTitleStrategy } from './core/services/ui/app-title.strategy';
import { BreadcrumbService } from './ui/misc/breadcrumb/breadcrumb.service';
import { ReloadHelperComponent } from './ui/misc/reload-helper/reload-helper.component';
import { AuthGuard } from './core/auth-guard.service';

const appRoutes: Routes = [
	{
		path: '',
		redirectTo: 'home',
		pathMatch: 'full'
	},
	// Legacy redirects: the "Description*" entity routes were renamed to "Asset*" (v1 rename).
	// Prefix redirects preserve trailing segments so old bookmarks/links keep working.
	{ path: 'descriptions', redirectTo: 'assets', pathMatch: 'prefix' },
	{ path: 'explore-descriptions', redirectTo: 'explore-assets', pathMatch: 'prefix' },
	{ path: 'description-template-library', redirectTo: 'asset-template-library', pathMatch: 'prefix' },
	{ path: 'description-templates', redirectTo: 'asset-templates', pathMatch: 'prefix' },
	{ path: 'description-template-type', redirectTo: 'types', pathMatch: 'full' },
	{ path: 'description-template-type/:id', redirectTo: 'types', pathMatch: 'full' },
	{ path: 'description-statuses', redirectTo: 'tenant-configuration/workflows', pathMatch: 'full' },
	{ path: 'description-statuses/:id', redirectTo: 'tenant-configuration/workflows', pathMatch: 'full' },
	{
		path: 'home',
		loadChildren: () => import('./ui/dashboard/dashboard.module').then(m => m.DashboardModule),
		data: {
			breadcrumb: true,
			title: 'GENERAL.TITLES.HOME'
		}
	},
	{
        path: 'assets',
        loadChildren: () => import('./ui/description/description.module').then(m => m.DescriptionModule),
        data: {
            authContext: {
                permissions: [AppPermission.ViewMyDescriptionPage]
            },
            breadcrumb: true,
            ...BreadcrumbService.generateRouteDataConfiguration({
                title: 'GENERAL.TITLES.DESCRIPTIONS'
            }),
            title: 'GENERAL.TITLES.DESCRIPTIONS'
        }
    },
	{
		path: 'explore-assets',
		loadChildren: () => import('./ui/description/description.module').then(m => m.PublicDescriptionModule),
		data: {
			breadcrumb: true,
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'GENERAL.TITLES.EXPLORE'
			}),
			title: 'GENERAL.TITLES.EXPLORE'
		}
	},
	{
		path: 'plans',
		loadChildren: () => import('./ui/plan/plan.module').then(m => m.PlanModule),
		data: {
			authContext: {
				permissions: [AppPermission.ViewMyPlanPage]
			},
			breadcrumb: true,
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'GENERAL.TITLES.PLANS'
			}),
			title: 'GENERAL.TITLES.PLANS'
		}
	},
	{
		path: 'explore-plans',
		loadChildren: () => import('./ui/plan/plan.module').then(m => m.PublicPlanModule),
		data: {
			breadcrumb: true,
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'GENERAL.TITLES.EXPLORE-PLANS'
			}),
			title: 'GENERAL.TITLES.EXPLORE-PLANS'
		}
	},
    {
        path: 'plan-blueprint-library',
        loadChildren: () => import('./ui/library/plan-blueprint-library/plan-blueprint-library.module').then(m => m.PlanBlueprintLibraryModule),
        data: {
            authContext: {
				permissions: [AppPermission.ViewPlanBlueprintLibraryPage]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.PLAN-BLUEPRINTS'
			}),
			title: 'GENERAL.TITLES.BLUEPRINTS'
        }
    },
    {
		path: 'asset-template-library',
        loadChildren: () => import('./ui/library/description-template-library/description-template-library.module').then(m => m.DescriptionTemplateLibraryModule),
		data: {
			authContext: {
				permissions: [AppPermission.ViewDescriptionTemplateLibraryPage]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.DESCRIPTION-TEMPLATES'
			}),
			title: 'GENERAL.TITLES.DESCRIPTION-TEMPLATES'
		}
	},
	{
		path: 'plan-blueprints',
		loadChildren: () => import('./ui/admin/plan-blueprint/plan-blueprint.module').then(m => m.PlanBlueprintModule),
		data: {
			authContext: {
				permissions: [AppPermission.ViewPlanBlueprintPage]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.PLAN-BLUEPRINTS'
			}),
			title: 'GENERAL.TITLES.BLUEPRINTS'
		}
	},
	{
		path: 'types',
		loadComponent: () => import('./ui/admin/types/types-page.component').then(m => m.TypesPageComponent),
		canActivate: [AuthGuard],
		data: {
			authContext: {
				permissions: [AppPermission.ViewPlanBlueprintTypePage, AppPermission.ViewDescriptionTemplateTypePage]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.TYPES'
			}),
			title: 'GENERAL.TITLES.TYPES'
		}
	},
	{ path: 'plan-blueprint-type', redirectTo: 'types', pathMatch: 'full' },
	{ path: 'plan-blueprint-type/:id', redirectTo: 'types', pathMatch: 'full' },


	{
		path: 'about',
		loadChildren: () => import('./ui/about/about.module').then(m => m.AboutModule),
		data: {
			breadcrumb: true,
			title: 'GENERAL.TITLES.ABOUT'
		}
	},

	{
		path: 'asset-templates',
		loadChildren: () => import('./ui/admin/description-template/description-template.module').then(m => m.DescriptionTemplateModule),
		data: {
			authContext: {
				permissions: [AppPermission.ViewDescriptionTemplatePage]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.DESCRIPTION-TEMPLATES'
			}),
			title: 'GENERAL.TITLES.DESCRIPTION-TEMPLATES'
		}
	},
	{ path: 'asset-template-type', redirectTo: 'types', pathMatch: 'full' },
	{ path: 'asset-template-type/:id', redirectTo: 'types', pathMatch: 'full' },
	{
		path: 'contact-support',
		loadChildren: () => import('./ui/contact/contact.module').then(m => m.ContactModule),
		data: {
			breadcrumb: true,
			title: 'CONTACT.SUPPORT.TITLE'
		}
	},
	{
		path: 'glossary',
		loadChildren: () => import('./ui/glossary/glossary.module').then(m => m.GlossaryModule),
		data: {
			breadcrumb: true,
			title: 'GLOSSARY.TITLE'
		}
	},
	{
		path: 'faq',
		loadChildren: () => import('./ui/faq/faq.module').then(m => m.FaqModule),
		data: {
			breadcrumb: true,
			title: 'FAQ.TITLE'
		}
	},
	{
		path: 'user-guide',
		loadChildren: () => import('./ui/user-guide/user-guide.module').then(m => m.UserGuideModule),
		data: {
			breadcrumb: true,
			title: 'GUIDE.TITLE'
		}
	},
	{
		path: 'privacy-policy',
		loadChildren: () => import('./ui/sidebar/sidebar-footer/privacy/privacy.module').then(m => m.PrivacyModule),
		data: {
			breadcrumb: true,
			title: 'GENERAL.TITLES.PRIVACY'
		}
	},
	{
		path: 'opensource-licences',
		loadChildren: () => import('./ui/sidebar/sidebar-footer/opensource-licences/opensource-licenses.module').then(m => m.OpensourceLicencesModule),
		data: {
			breadcrumb: true,
			title: 'GENERAL.TITLES.OPENSOURCE-LICENCES'
		}
	},
	{
		path: 'terms-and-conditions',
		loadChildren: () => import('./ui/sidebar/sidebar-footer/terms/terms.module').then(m => m.TermsModule),
		data: {
			breadcrumb: true,
			title: 'GENERAL.TITLES.TERMS'
		}
	},
	{
		path: 'cookies-policy',
		loadChildren: () => import('./ui/sidebar/sidebar-footer/cookies-policy/cookies-policy.module').then(m => m.CookiesPolicyModule),
		data: {
			breadcrumb: true,
			title: 'GENERAL.TITLES.COOKIES-POLICY'
		}
	},
	{
		path: 'unauthorized',
		loadChildren: () => import('./ui/misc/unauthorized/unauthorized.module').then(m => m.UnauthorizedModule),
		data: {
			breadcrumb: true
		},
	},
	{
		path: 'users',
		loadChildren: () => import('./ui/admin/user/user.module').then(m => m.UsersModule),
		data: {
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.USERS'
			}),
			title: 'GENERAL.TITLES.USERS'
		},
	},
	{
		path: 'tenant-users',
		loadChildren: () => import('./ui/admin/user/user.module').then(m => m.TenantUsersModule),
		data: {
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.TENANT-USERS'
			}),
			title: 'GENERAL.TITLES.TENANT-USERS'
		},
	},
	{
		path: 'profile',
		loadChildren: () => import('./ui/user-profile/user-profile.module').then(m => m.UserProfileModule),
		data: {
			breadcrumb: true,
			title: 'GENERAL.TITLES.PROFILE'
		},
	},
	{
		path: 'languages',
		// shown inside the settings hub, next to its section menu
		loadComponent: () => import('./ui/admin/tenant-configuration/shell/tc-shell.component').then(m => m.TcShellComponent),
		children: [{ path: '', loadChildren: () => import('./ui/admin/language/language.module').then(m => m.LanguageModule) }],
		data: {
			tcWrapped: true,
			authContext: {
				permissions: [AppPermission.ViewLanguagePage]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'TENANT-CONFIGURATION.NAV.LANGUAGES'
			}),
			title: 'GENERAL.TITLES.LANGUAGES'
		},
	},
	{
		path: 'supportive-materials',
		// shown inside the settings hub, next to its section menu
		loadComponent: () => import('./ui/admin/tenant-configuration/shell/tc-shell.component').then(m => m.TcShellComponent),
		children: [{ path: '', loadChildren: () => import('./ui/admin/supportive-material/supportive-material.module').then(m => m.SupportiveMaterialModule) }],
		data: {
			tcWrapped: true,
			authContext: {
				permissions: [AppPermission.ViewSupportiveMaterialPage]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'TENANT-CONFIGURATION.NAV.SUPPORTIVE-MATERIAL'
			}),
			title: 'GENERAL.TITLES.SUPPORTIVE-MATERIAL'
		},
	},
	{
		path: 'references',
		loadChildren: () => import('./ui/admin/reference/reference.module').then(m => m.ReferenceModule),
		data: {
			authContext: {
				permissions: [AppPermission.ViewReferencePage]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.REFERENCES'
			}),
			title: 'GENERAL.TITLES.REFERENCES'
		},
	},
	{
		path: 'reference-type',
		loadChildren: () => import('./ui/admin/reference-type/reference-type.module').then(m => m.ReferenceTypeModule),
		data: {
			authContext: {
				permissions: [AppPermission.ViewReferenceTypePage]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.REFERENCE-TYPES'
			}),
			title: 'GENERAL.TITLES.REFERENCE-TYPES'
		},
	},
	{
		path: 'prefilling-sources',
		loadChildren: () => import('./ui/admin/prefilling-source/prefilling-source.module').then(m => m.PrefillingSourceModule),
		data: {
			authContext: {
				permissions: [AppPermission.ViewPrefillingSourcePage]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.PREFILLING-SOURCES'
			}),
			title: 'GENERAL.TITLES.PREFILLING-SOURCES'
		},
	},
	{
		path: 'tenants',
		loadChildren: () => import('./ui/admin/tenant/tenant.module').then(m => m.TenantModule),
		data: {
			authContext: {
				permissions: [AppPermission.ViewTenantPage]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.TENANTS'
			}),
			title: 'GENERAL.TITLES.TENANTS'
		},
	},
	{
		// the address of the redesigned page while it ran next to the previous one; kept for bookmarks, sub-paths included
		path: 'tenant-configuration-v2',
		redirectTo: 'tenant-configuration',
	},
	{
		path: 'tenant-configuration',
		loadChildren: () => import('./ui/admin/tenant-configuration/tenant-configuration.routes').then(m => m.TENANT_CONFIGURATION_ROUTES),
		data: {
			authContext: {
				permissions: [AppPermission.ViewTenantConfigurationPage, AppPermission.ViewPlanWorkflowPage, AppPermission.ViewDescriptionWorkflowPage]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.TENANT-CONFIGURATION'
			}),
			title: 'GENERAL.TITLES.TENANT-CONFIGURATION'
		},
	},
	{
		path: 'notifications',
		loadChildren: () => import('@notification-service/ui/admin/notification/notification.module').then(m => m.NotificationModule),
		data: {
			authContext: {
				permissions: [AppPermission.ViewNotificationPage]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.NOTIFICATIONS'
			}),
			title: 'GENERAL.TITLES.NOTIFICATIONS'
		},
	},
	{
		path: 'notification-templates',
		// shown inside the settings hub, next to its section menu
		loadComponent: () => import('./ui/admin/tenant-configuration/shell/tc-shell.component').then(m => m.TcShellComponent),
		children: [{ path: '', loadChildren: () => import('@notification-service/ui/admin/notification-template/notification-template.module').then(m => m.NotificationTemplateModule) }],
		data: {
			tcWrapped: true,
			authContext: {
				permissions: [AppPermission.ViewNotificationTemplatePage]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'TENANT-CONFIGURATION.NAV.NOTIFICATION-TEMPLATES'
			}),
			title: 'GENERAL.TITLES.NOTIFICATION-TEMPLATES'
		},
	},
	{
		path: 'mine-notifications',
		loadChildren: () => import('@notification-service/ui/inapp-notification/mine-inapp-notification.module').then(m => m.MineInAppNotificationModule),
		data: {
			authContext: {
				permissions: [AppPermission.ViewMineInAppNotificationPage]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.INAPP-NOTIFICATIONS'
			})
		},
	},
	{
		path: 'entity-locks',
		loadChildren: () => import('./ui/admin/entity-locks/lock.module').then(m => m.LockModule),
		data: {
			authContext: {
				permissions: [AppPermission.ViewEntityLockPage]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.ENTITY-LOCKS'
			}),
			title: 'GENERAL.TITLES.ENTITY-LOCKS'
		},
	},
	{
		path: 'usage-limits',
		loadChildren: () => import('./ui/admin/usage-limit/usage-limit.module').then(m => m.UsageLimitModule),
		data: {
			authContext: {
				permissions: [AppPermission.ViewUsageLimitPage]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.USAGE-LIMITS'
			}),
			title: 'GENERAL.TITLES.USAGE-LIMITS'
		},
	},
	{
		path: 'reports',
		loadChildren: () => import('./ui/admin/reports/reports.routing').then(m => m.ReportsRoutingModule),
		data: {
			authContext: {
				permissions: [AppPermission.ViewReportsPage]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.REPORTS'
			}),
			title: 'GENERAL.TITLES.REPORTS'
		},
	},
	{
		path: 'webhooks',
		// shown inside the settings hub, next to its section menu
		loadComponent: () => import('./ui/admin/tenant-configuration/shell/tc-shell.component').then(m => m.TcShellComponent),
		children: [{ path: '', loadChildren: () => import('./ui/admin/webhooks/webhooks.module').then(m => m.WebhooksModule) }],
		data: {
			tcWrapped: true,
			authContext: {
				permissions: [AppPermission.ViewWebhooksPage]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'TENANT-CONFIGURATION.NAV.WEBHOOKS'
			}),
			title: 'GENERAL.TITLES.WEBHOOKS'
		},
	},
	{
		path: 'annotation-statuses',
		// shown inside the settings hub, next to its section menu
		loadComponent: () => import('./ui/admin/tenant-configuration/shell/tc-shell.component').then(m => m.TcShellComponent),
		children: [{ path: '', loadChildren: () => import('@annotation-service/ui/admin/status/status.module').then(m => m.StatusModule) }],
		data: {
			tcWrapped: true,
			authContext: {
				permissions: [AppPermission.ViewStatusPage]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'TENANT-CONFIGURATION.NAV.ANNOTATION-STATUSES'
			}),
			title: 'GENERAL.TITLES.ANNOTATION-STATUSES'
		},
	},
	{
		path: 'maintenance-tasks',
		loadChildren: () => import('./ui/admin/maintenance/maintenance.routes').then(m => m.MAINTENANCE_ROUTES),
		data: {
			authContext: {
				permissions: [AppPermission.ViewMaintenancePage]
			},
			breadcrumb: true,
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'GENERAL.TITLES.MAINTENANCE-TASKS'
			}),
			title: 'GENERAL.TITLES.MAINTENANCE-TASKS'
		},
	},
	{
		path: 'tenant-transfer',
		loadChildren: () => import('./ui/admin/tenant-transfer/tenant-transfer.module').then(m => m.TenantTransferModule),
		data: {
			authContext: {
				permissions: [AppPermission.ViewTenantTransferPage]
			},
			breadcrumb: true,
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'GENERAL.TITLES.TENANT-TRANSFER'
			}),
			title: 'GENERAL.TITLES.TENANT-TRANSFER'
		},
	},
	// statuses are edited from the Workflows diagram; old bookmarks land there
	{ path: 'plan-statuses', redirectTo: 'tenant-configuration/workflows', pathMatch: 'full' },
	{ path: 'plan-statuses/:id', redirectTo: 'tenant-configuration/workflows', pathMatch: 'full' },
	{ path: 'asset-statuses', redirectTo: 'tenant-configuration/workflows', pathMatch: 'full' },
	{ path: 'asset-statuses/:id', redirectTo: 'tenant-configuration/workflows', pathMatch: 'full' },
	{
		path: 'usage',
		loadChildren: () => import('../kpi-service/ui/admin/indicator-dashboard/indicator-dashboard.routes').then(m => m.INDICATOR_DASHBOARD_ROUTES),
		data: {
			authContext: {
				permissions: [AppPermission.ViewIndicatorDashboardPage]
			},
			breadcrumb: true,
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'GENERAL.TITLES.INDICATOR-DASHBOARD'
			}),
			title: 'GENERAL.TITLES.INDICATOR-DASHBOARD'
		}
	},
	{
		path: 'login',
		loadChildren: () => import('./ui/auth/login/login.module').then(m => m.LoginModule),
		data: {
			breadcrumb: true,
			title: 'GENERAL.TITLES.LOGIN'
		},
	},
	{ path: 'logout', loadChildren: () => import('./ui/auth/logout/logout.module').then(m => m.LogoutModule) },
	{ path: 'reload', component: ReloadHelperComponent },
	{
		path: 'deposit/oauth2',
		loadChildren: () => import('./ui/misc/deposit-oauth2-dialog/deposit-oauth2-dialog.module').then(m => m.DepositOauth2DialogModule),
		data: {
			showOnlyRouterOutlet: true
		}
	},
	{ path: '**', loadComponent: () => import('@common/modules/page-not-found/page-not-found.component').then(m => m.PageNotFoundComponent), canActivate: [AuthGuard]}
];

const tenantEnrichedRoutes: Routes = [
	{
		path: 't/:tenant_code',
		data: {
			breadcrumb: true,
			...BreadcrumbService.generateRouteDataConfiguration({
				skipNavigation: true,
			})
		},
		children: [
			...appRoutes
		]
	},
	...appRoutes
];

@NgModule({
	// Angular 22 defaults to 'always', which would hand every child route the parent's :tenant_code and :id params.
	imports: [RouterModule.forRoot(tenantEnrichedRoutes, { paramsInheritanceStrategy: 'emptyOnly' })],
	exports: [RouterModule],
	providers: [
		// Composes the browser-tab title from the per-route breadcrumb taxonomy.
		{ provide: TitleStrategy, useClass: AppTitleStrategy },
	],
})
export class AppRoutingModule { }
