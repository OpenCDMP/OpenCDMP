import { Injectable } from '@angular/core';
import { MatomoInitializerService, MatomoTracker } from 'ngx-matomo-client';
import { AuthService } from '../auth/auth.service';
import { ConfigurationService } from '../configuration/configuration.service';
import { AnalyticsProvider } from '@app/core/model/configuration-models/analytics-providers.model';

@Injectable()
export class MatomoService {

	constructor(
		private configurationService: ConfigurationService,
		private matomoInitializerService: MatomoInitializerService,
		private matomoTracker: MatomoTracker,
		private authService: AuthService
	) {

	}

	init() {
		if (this.configurationService.matomoEnabled) {
			this.matomoInitializerService.initializeTracker({ trackerUrl: this.configurationService.matomoSiteUrl, siteId: this.configurationService.matomoSiteId });
		}
	}

	trackPageView(provider: AnalyticsProvider, customTitle?: string): void {
		if (provider.enabled) {
			var principalid = this.authService.userId();
			if (principalid != null) { this.matomoTracker.setUserId(principalid.toString()); }
			this.matomoTracker.trackPageView(customTitle);
		}
	}

	trackSiteSearch(keyword: string, category?: string, resultsCount?: number): void {
		if (this.configurationService.matomoEnabled) {
			var principalid = this.authService.userId();
			if (principalid != null) { this.matomoTracker.setUserId(principalid.toString()); }
			this.matomoTracker.trackSiteSearch(keyword, category, resultsCount);
		}
	}

	trackDownload(category: string, type: string, id: string): void {
		if (this.configurationService.matomoEnabled) {
			var principalid = this.authService.userId();
			if (principalid != null) { this.matomoTracker.setUserId(principalid.toString()); }
			this.matomoTracker.trackLink(this.configurationService.server + category + "/" + type + "/" + id, "download");
		}
	}
}
