import { AppPermission } from '@app/core/common/enum/permission.enum';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';

export interface TcPage {
	path: string;
	label: string;
	permissions: AppPermission[];
	/** Key under TENANT-CONFIGURATION.NAV.GROUPS; none for the overview. */
	group?: string;
	icon?: string;
	hint?: string;
	/** Tenant configuration types stored for this page; empty for pages kept elsewhere. */
	types?: TenantConfigurationType[];
}

const TC = [AppPermission.ViewTenantConfigurationPage];
const WORKFLOWS = [AppPermission.ViewTenantConfigurationPage, AppPermission.ViewPlanWorkflowPage, AppPermission.ViewDescriptionWorkflowPage];

/** Every page of the settings hub, in menu order: the section menu and the overview both list these. */
export const TC_PAGES: TcPage[] = [
	{ path: '/tenant-configuration/overview', label: 'TENANT-CONFIGURATION.NAV.OVERVIEW', permissions: TC },
	{ path: '/tenant-configuration/branding', label: 'TENANT-CONFIGURATION.NAV.BRANDING', permissions: TC, group: 'APPEARANCE', icon: 'palette', hint: 'TENANT-CONFIGURATION.OVERVIEW.BRANDING', types: [TenantConfigurationType.CssColors, TenantConfigurationType.Logo] },
	{ path: '/tenant-configuration/region', label: 'TENANT-CONFIGURATION.NAV.REGION', permissions: TC, group: 'APPEARANCE', icon: 'public', hint: 'TENANT-CONFIGURATION.OVERVIEW.REGION', types: [TenantConfigurationType.DefaultUserLocale] },
	{ path: '/languages', label: 'TENANT-CONFIGURATION.NAV.LANGUAGES', permissions: [AppPermission.ViewLanguagePage], group: 'APPEARANCE', icon: 'translate', hint: 'TENANT-CONFIGURATION.OVERVIEW.LANGUAGES' },
	{ path: '/tenant-configuration/integrations', label: 'TENANT-CONFIGURATION.NAV.INTEGRATIONS', permissions: TC, group: 'INTEGRATIONS', icon: 'extension', hint: 'TENANT-CONFIGURATION.OVERVIEW.INTEGRATIONS', types: [TenantConfigurationType.FileTransformerPlugins, TenantConfigurationType.DepositPlugins, TenantConfigurationType.EvaluatorPlugins, TenantConfigurationType.PluginConfiguration] },
	{ path: '/webhooks', label: 'TENANT-CONFIGURATION.NAV.WEBHOOKS', permissions: [AppPermission.ViewWebhooksPage], group: 'INTEGRATIONS', icon: 'webhook', hint: 'TENANT-CONFIGURATION.OVERVIEW.WEBHOOKS' },
	{ path: '/tenant-configuration/workflows', label: 'TENANT-CONFIGURATION.NAV.WORKFLOWS', permissions: WORKFLOWS, group: 'WORKFLOWS', icon: 'account_tree', hint: 'TENANT-CONFIGURATION.OVERVIEW.WORKFLOWS' },
	{ path: '/annotation-statuses', label: 'TENANT-CONFIGURATION.NAV.ANNOTATION-STATUSES', permissions: [AppPermission.ViewStatusPage], group: 'WORKFLOWS', icon: 'mode_comment', hint: 'TENANT-CONFIGURATION.OVERVIEW.ANNOTATION-STATUSES' },
	{ path: '/tenant-configuration/content-defaults', label: 'TENANT-CONFIGURATION.NAV.CONTENT-DEFAULTS', permissions: TC, group: 'CONTENT', icon: 'dashboard_customize', hint: 'TENANT-CONFIGURATION.OVERVIEW.CONTENT-DEFAULTS', types: [TenantConfigurationType.DefaultPlanBlueprint, TenantConfigurationType.FeaturedEntities, TenantConfigurationType.ViewPreferences] },
	{ path: '/supportive-materials', label: 'TENANT-CONFIGURATION.NAV.SUPPORTIVE-MATERIAL', permissions: [AppPermission.ViewSupportiveMaterialPage], group: 'CONTENT', icon: 'help_center', hint: 'TENANT-CONFIGURATION.OVERVIEW.SUPPORTIVE-MATERIAL' },
	{ path: '/tenant-configuration/notifications', label: 'TENANT-CONFIGURATION.NAV.NOTIFICATIONS', permissions: TC, group: 'COMMUNICATION', icon: 'notifications', hint: 'TENANT-CONFIGURATION.OVERVIEW.NOTIFICATIONS' },
	{ path: '/notification-templates', label: 'TENANT-CONFIGURATION.NAV.NOTIFICATION-TEMPLATES', permissions: [AppPermission.ViewNotificationTemplatePage], group: 'COMMUNICATION', icon: 'mail', hint: 'TENANT-CONFIGURATION.OVERVIEW.NOTIFICATION-TEMPLATES' },
	{ path: '/tenant-configuration/onboarding', label: 'TENANT-CONFIGURATION.NAV.ONBOARDING', permissions: TC, group: 'COMMUNICATION', icon: 'waving_hand', hint: 'TENANT-CONFIGURATION.OVERVIEW.ONBOARDING', types: [TenantConfigurationType.TermsAndConditions, TenantConfigurationType.GuidedTours] },
];

/** Consecutive pages of the same group, as the menu and the overview show them. */
export function groupTcPages<T extends TcPage>(pages: T[]): { key: string | null, pages: T[] }[] {
	return pages.reduce((groups, page) => {
		const last = groups[groups.length - 1];
		if (last && last.key === (page.group ?? null)) last.pages.push(page);
		else groups.push({ key: page.group ?? null, pages: [page] });
		return groups;
	}, [] as { key: string | null, pages: T[] }[]);
}
