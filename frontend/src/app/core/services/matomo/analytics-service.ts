import { Injectable } from "@angular/core";
import { ConfigurationService } from "../configuration/configuration.service";
import { MatomoService } from "./matomo-service";
import { AnalyticsProviderType, AnalyticsProviders } from "@app/core/model/configuration-models/analytics-providers.model";

@Injectable()
export class AnalyticsService {

	//#region track page view
	public static Dashboard: string = 'Home Dashboard';
	public static About: string = 'About';
	public static DescriptionTemplateEditor: string = 'Admin: DMP Blueprints';
	public static DescriptionTemplateListing: string = 'Admin: DMP Templates';
	public static PlanBlueprintEditor: string = 'Admin: DMP Blueprints';
	public static PlanBlueprintListing: string = 'Admin: DMP Templates';
	public static LanguagesEditor: string = 'Admin: Languages';
	public static PrefillingSourcesEditor: string = 'Admin: PrefillingSources';
	public static ReferencesEditor: string = 'Admin: References';
	public static TenantsEditor: string = 'Admin: Tenants';
	public static TenantConfigurationsColorsEditor: string = 'Admin: TenantConfigurations';
	public static TenantConfigurationsUserLocaleEditor: string = 'Admin: TenantConfigurations';
	public static DepositEditor: string = 'Admin: TenantConfigurations';
	public static FileTransformerEditor: string = 'Admin: TenantConfigurations';
	public static LogoEditor: string = 'Admin: TenantConfigurations';
	public static PluginConfigurationEditor: string = 'Admin: TenantConfigurations';
	public static PlanWorkflowEditor: string = 'Admin: TenantConfigurations';
	public static DescriptionWorkflowEditor: string = 'Admin: TenantConfigurations';
	public static ContactContent: string = 'Contact Content';
	public static RecentEditedActivity: string = 'Recent DMP Activity';
	public static DescriptionEditor: string = 'Description Editor';
	public static DescriptionListing: string = 'Descriptions';
	public static DescriptionFilterDialog: string = 'Dataset Criteria';
	public static DescriptionListingItem: string = 'Description Listing Item';
	public static DescriptionOverview: string = 'Description Overview';
	public static PlanEditor: string = 'DMP Editor';
	public static PlanListing: string = 'DMPs';
	public static PlanFilterDialog: string = 'DMP Criteria';
	public static PlanListingItem: string = 'DMP Listing Item';
	public static StartNewPlanDialog: string = 'Start New DMP Dialog';
	public static PlanUploadDialog: string = 'DMP Upload Dialog';
	public static PlanOverview: string = 'DMP Overview';
	public static FAQ: string = 'FAQ';
	public static Glossary: string = 'Glossary';
	public static Navbar: string = 'Navbar';
	public static Sidebar: string = 'Sidebar';
	public static SidebarFooter: string = 'Sidebar Footer';
	public static Terms: string = 'Terms of Service';
	public static UserGuideContent: string = 'User Guide Content';
	public static UserProfile: string = 'User Profile';
	public static NotificationTempplateEditor: string = 'Admin: Notification Tempplates';
    public static PlanStatusListing: string = 'Plan Status Listing';
    public static PlanStatusEditor: string = 'Plan Status Editor';
    public static DescriptionStatusListing: string = 'Description Status Listing';
    public static DescriptionStatusEditor: string = 'Description Status Editor';
	public static SupportiveMaterialEditor: string = 'SupportiveMaterialEditor';

	//#endregion

	//#region trackDownload
	public static trackPlan: string = "dmps"
	public static trackDescriptions: string = "descriptions"
	//#endregion

	constructor(
		private configurationService: ConfigurationService,
		private matomoService: MatomoService
	) { }

	trackPageView(customTitle?: string): void {
		const analytics: AnalyticsProviders = this.configurationService.analyticsProviders;
		for (let provider of analytics.providers) {
			switch (provider.type) {
				case (AnalyticsProviderType.Matomo):
					this.matomoService.trackPageView(provider, customTitle);
					break;
			}
		}
	}

	trackSiteSearch(keyword: string, category?: string, resultsCount?: number): void {
		const analytics: AnalyticsProviders = this.configurationService.analyticsProviders;

		for (let provider of analytics.providers) {
			switch (provider.type) {
				case (AnalyticsProviderType.Matomo):
					this.matomoService.trackSiteSearch(keyword, category, resultsCount);
					break;
			}
		}
	}

	trackDownload(category: string, type: string, id: string): void {
		const analytics: AnalyticsProviders = this.configurationService.analyticsProviders;

		for (let provider of analytics.providers) {
			switch (provider.type) {
				case (AnalyticsProviderType.Matomo):
					this.matomoService.trackDownload(category, type, id);
					break;
			}
		}
	}
}