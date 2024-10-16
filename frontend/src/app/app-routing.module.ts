import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AppPermission } from './core/common/enum/permission.enum';
import { BreadcrumbService } from './ui/misc/breadcrumb/breadcrumb.service';
import { ReloadHelperComponent } from './ui/misc/reload-helper/reload-helper.component';

const appRoutes: Routes = [
	{
		path: '',
		redirectTo: 'home',
		pathMatch: 'full'
	},
	{
		path: 'home',
		loadChildren: () => import('./ui/dashboard/dashboard.module').then(m => m.DashboardModule),
		data: {
			breadcrumb: true,
			title: 'GENERAL.TITLES.HOME'
		}
	},
	{
		path: 'descriptions',
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
		path: 'explore-descriptions',
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
		path: 'about',
		loadChildren: () => import('./ui/about/about.module').then(m => m.AboutModule),
		data: {
			breadcrumb: true,
			title: 'GENERAL.TITLES.ABOUT'
		}
	},

	{
		path: 'description-templates',
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
	{
		path: 'description-template-type',
		loadChildren: () => import('./ui/admin/description-types/description-template-type.module').then(m => m.DescriptionTemplateTypesModule),
		data: {
			authContext: {
				permissions: [AppPermission.ViewDescriptionTemplateTypePage]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.DESCRIPTION-TEMPLATE-TYPES'
			}),
			title: 'GENERAL.TITLES.DESCRIPTION-TEMPLATE-TYPES'
		},
	},
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
		loadChildren: () => import('./ui/admin/language/language.module').then(m => m.LanguageModule),
		data: {
			authContext: {
				permissions: [AppPermission.ViewLanguagePage]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.LANGUAGES'
			}),
			title: 'GENERAL.TITLES.LANGUAGES'
		},
	},
	{
		path: 'supportive-material',
		loadChildren: () => import('./ui/supportive-material-editor/supportive-material-editor.module').then(m => m.SupportiveMaterialEditorModule),
		data: {
			authContext: {
				permissions: [AppPermission.ViewSupportiveMaterialPage]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'GENERAL.TITLES.SUPPORTIVE-MATERIAL'
			})
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
			})
		},
	},
	{
		path: 'tenant-configuration',
		loadChildren: () => import('./ui/admin/tenant-configuration/tenant-configuration.module').then(m => m.TenantConfigurationModule),
		data: {
			authContext: {
				permissions: [AppPermission.ViewTenantConfigurationPage]
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
		loadChildren: () => import('@notification-service/ui/admin/notification-template/notification-template.module').then(m => m.NotificationTemplateModule),
		data: {
			authContext: {
				permissions: [AppPermission.ViewNotificationTemplatePage]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.NOTIFICATION-TEMPLATES'
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
		path: 'annotation-statuses',
		loadChildren: () => import('@annotation-service/ui/admin/status/status.module').then(m => m.StatusModule),
		data: {
			authContext: {
				permissions: [AppPermission.ViewStatusPage]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.ANNOTATION-STATUSES'
			}),
			title: 'GENERAL.TITLES.ANNOTATION-STATUSES'
		},
	},
	{
		path: 'maintenance-tasks',
		loadChildren: () => import('./ui/admin/maintenance-tasks/maintenance-tasks.module').then(m => m.MaintenanceTasksModule),
		data: {
			authContext: {
				permissions: [AppPermission.ViewMaintenancePage]
			},
			breadcrumb: true,
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'GENERAL.TITLES.MAINTENANCE-TASKS'
			})
		},
	},
	{
		path: 'plan-statuses',
		loadChildren: () => import('./ui/admin/plan-status/plan-status.module').then(m => m.PlanStatusModule),
		data: {
			authContext: {
				permissions: [AppPermission.ViewPlanStatusPage]
			},
            breadcrumb: true,
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'GENERAL.TITLES.PLAN-STATUSES'
			}),
            title: 'GENERAL.TITLES.PLAN-STATUSES'
		}
	},
    {
		path: 'description-statuses',
		loadChildren: () => import('./ui/admin/description-status/description-status.module').then(m => m.DescriptionStatusModule),
		data: {
			authContext: {
				permissions: [AppPermission.ViewDescriptionStatusPage]
			},
            breadcrumb: true,
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'GENERAL.TITLES.DESCRIPTION-STATUSES'
			}),
            title: 'GENERAL.TITLES.DESCRIPTION-STATUSES'
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
	}
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
	imports: [RouterModule.forRoot(tenantEnrichedRoutes, {})],
	exports: [RouterModule],
})
export class AppRoutingModule { }
