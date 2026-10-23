
import { Component, computed, effect, inject, OnInit, AfterViewInit, OnDestroy, ChangeDetectionStrategy, forwardRef } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { DashboardService } from '@app/core/services/dashboard/dashboard.service';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { TourStateService } from '@app/library/guided-tour/tour-state.service';
import { BaseComponent } from '@common/base/base.component';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import moment from 'moment';
import { CookieService } from 'ngx-cookie-service';
import { takeUntil } from 'rxjs/operators';
import { StartNewPlanDialogComponent } from '../plan/new/start-new-plan-dialogue/start-new-plan-dialog.component';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { ViewPreferencesEditorResolver } from '../admin/tenant-configuration/models/view-preferences/view-preferences-editor.resolver';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { ConsentType, CookieConsentService } from '@app/core/services/cookie-consent/cookieconsent.service';
import { UIStatisticsService } from '@app/core/services/statistics/ui-statistics.service';
import { dashboardPageTour } from './dashboard.tour';
import { MatTabGroup, MatTab } from '@angular/material/tabs';
import { RecentEditedActivityComponent } from './recent-edited-activity/recent-edited-activity.component';
import { DashboardOverviewComponent } from './dashboard-overview/dashboard-overview.component';
import { DashboardRecommendationsComponent } from './dashboard-recommendations/dashboard-recommendations.component';
import { NgTemplateOutlet } from '@angular/common';
import { MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatCard, MatCardContent } from '@angular/material/card';


@Component({
    selector: 'app-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatTabGroup, MatTab, forwardRef(() => RecentEditedActivityComponent), DashboardOverviewComponent, DashboardRecommendationsComponent, NgTemplateOutlet, MatIconButton, MatIcon, MatCard, MatCardContent, TranslatePipe]
})
export class DashboardComponent extends BaseComponent implements OnInit, AfterViewInit, OnDestroy {
	public dashboardStatistics = this.statisticsService.dashboardStatistics;
	public grantCount = 0;
	public organizationCount = 0;
	newReleaseNotificationVisible = false;
	isIntroCardVisible = true;

    ActivityListingType = ActivityListingType;
	orderedPlanPreferencesList:ReferenceType[];
	orderedDescriptionPreferencesList:ReferenceType[];

	private readonly tourState = inject(TourStateService);
	private readonly pageTour = dashboardPageTour(inject(TranslateService));

	constructor(
		public routerUtils: RouterUtilsService,
		private router: Router,
		private route: ActivatedRoute,
		private dashboardService: DashboardService,
		private authentication: AuthService,
		private dialog: MatDialog,
		private analyticsService: AnalyticsService,
		public referenceTypeService: ReferenceTypeService,
		private cookieService: CookieService,
		private tenantConfigurationService: TenantConfigurationService,
		public configurationService: ConfigurationService,
        private cookieConsentService: CookieConsentService,
        private statisticsService: UIStatisticsService
	) {
		super();
		// A user with nothing yet has no activity list, so the tour can start as soon as the overview card is up.
		effect(() => {
			if (this.dashboardStatistics() && !this.hasPlans()) { this.requestTour(); }
		});
	}

	/** The activity list is the last thing the page renders; the first-visit tour waits for it (see `onActivityLoaded`). */
	private tourRequested = false;

	onActivityLoaded(): void {
		this.requestTour();
	}

	private requestTour(): void {
		if (this.tourRequested || !this.isAuthenticated()) { return; }
		this.tourRequested = true;
		this.tourState.startIfUnseen(this.pageTour);
	}


	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.Dashboard);



		if (this.isAuthenticated()) {
			// "What's new" tour for the 2.0.0 dashboard; started once the page has laid out (requestTour).
			this.tourState.registerPageTour(this.pageTour);

			this.tenantConfigurationService.getActiveType(TenantConfigurationType.ViewPreferences, ViewPreferencesEditorResolver.lookupFields())
				.pipe(takeUntil(this._destroyed))
                .subscribe(
					data => {
						this.orderedPlanPreferencesList = data?.viewPreferences?.planPreferences?.filter(x => x.referenceType?.isActive === IsActive.Active)?.map(x => x.referenceType) || [];
						this.orderedDescriptionPreferencesList = data?.viewPreferences?.descriptionPreferences?.filter(x => x.referenceType?.isActive === IsActive.Active)?.map(x => x.referenceType) || [];
                    },
                    error => {
                        this.orderedPlanPreferencesList = []
                    }
				);
		}

		this.newReleaseNotificationVisible = this.isNewReleaseNotificationVisible();
	}

	override ngOnDestroy(): void {
		this.tourState.unregisterPageTour(this.pageTour);
		super.ngOnDestroy();
	}

    initTab: number = 0;
    ngAfterViewInit(){
        this.route.queryParams.pipe(takeUntil(this._destroyed)).subscribe(params => {
			const type = params['type'] as ActivityListingType;
            if(type){
                switch(type){
                    case ActivityListingType.Drafts: {
                        this.initTab = 1;
                        break;
                    }
                    case ActivityListingType.Plans: {
                        this.initTab = 2;
                        break;
                    }
                    case ActivityListingType.Descriptions: {
                        this.initTab = 3;
                        break;
                    }
                    default: {
                        this.initTab = 0;
                    }
                }
            }
		});
    }


	public isAuthenticated(): boolean {
		return this.authentication.currentAccountIsAuthenticated();
	}

	openNewPlanDialog() {
		if (this.dialog.openDialogs.length > 0) {
			this.dialog.closeAll();
		}
		else {
			const dialogRef = this.dialog.open(StartNewPlanDialogComponent, {
                maxWidth: 'min(95vw, 33rem)',
				disableClose: false,
				data: {
					isDialog: true
				},
			});
		}
	}

	public hasPlans = computed(() => {
		if (this.dashboardStatistics()) {
			return [
                this.dashboardStatistics().planCount,
                this.dashboardStatistics().descriptionCount,
				this.dashboardStatistics().referenceTypeStatistics?.length,
                this.dashboardStatistics().publicPlanCount,
                this.dashboardStatistics().publicDescriptionCount,
				this.dashboardStatistics().publicReferenceTypeStatistics?.length,
            ].some(x => !!x)
		} else {
			return false;
		}
    })

	dismissIntroCard() {
		this.isIntroCardVisible = false;
	}

	dismissNewReleaseNotification() {
        if(this.cookieConsentService.currentConsents()?.categories?.includes(ConsentType.Functionality)){
            this.cookieService.set('new-release-dismiss-' + this.configurationService.newReleaseNotificationVersionCode, 'true', 5000, null, null, false, 'Lax');
        }
		this.newReleaseNotificationVisible = false;
	}

	isNewReleaseNotificationVisible() {
		if (this.configurationService.newReleaseNotificationVersionCode == null) {
			return false;
		}
		if (this.configurationService.newReleaseNotificationExpires == null && this.configurationService.newReleaseNotificationLink == null) {
			return false;
		}
		if (this.configurationService.newReleaseNotificationExpires != null && moment(this.configurationService.newReleaseNotificationExpires).tz('UTC') < moment.utc()) {
			return false;
		}
		if (this.cookieService.get('new-release-dismiss-' + this.configurationService.newReleaseNotificationVersionCode) === 'true') {
			return false;
		}

		return true;
	}
}

export enum ActivityListingType {
    'Recent'= 'recent',
    'Drafts' = 'drafts',
    'Plans' = 'plans',
    'Descriptions' = 'descriptions'
}
