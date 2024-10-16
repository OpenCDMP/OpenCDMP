
import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { UntypedFormBuilder, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { DashboardStatistics } from '@app/core/model/dashboard/dashboard-statistics';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { DashboardService } from '@app/core/services/dashboard/dashboard.service';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { GuidedTour, Orientation } from '@app/library/guided-tour/guided-tour.constants';
import { GuidedTourService } from '@app/library/guided-tour/guided-tour.service';
import { BaseComponent } from '@common/base/base.component';
import { TranslateService } from '@ngx-translate/core';
import moment from 'moment';
import { CookieService } from 'ngx-cookie-service';
import { takeUntil } from 'rxjs/operators';
import { StartNewDescriptionDialogComponent } from '../description/start-new-description-dialog/start-new-description-dialog.component';
import { StartNewPlanDialogComponent } from '../plan/new/start-new-plan-dialogue/start-new-plan-dialog.component';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';


@Component({
	selector: 'app-dashboard',
	templateUrl: './dashboard.component.html',
	styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent extends BaseComponent implements OnInit {

	public dashboardStatistics: DashboardStatistics;
	public grantCount = 0;
	public organizationCount = 0;
	currentType: ActivityListingType = ActivityListingType.Recent;
	newReleaseNotificationVisible = false;
	isIntroCardVisible = true;

    ActivityListingType = ActivityListingType;

	constructor(
		public routerUtils: RouterUtilsService,
		private router: Router,
		private route: ActivatedRoute,
		private dashboardService: DashboardService,
		private authentication: AuthService,
		private dialog: MatDialog,
		private language: TranslateService,
		private guidedTourService: GuidedTourService,
		private analyticsService: AnalyticsService,
		public referenceTypeService: ReferenceTypeService,
		private fb: UntypedFormBuilder,
		private cookieService: CookieService,
		public configurationService: ConfigurationService
	) {
		super();
	}


	ngOnInit() {
		this.route.queryParams.subscribe(params => {
			let type = params['type'];
            this.currentType = type ??  ActivityListingType.Recent
		});

		this.analyticsService.trackPageView(AnalyticsService.Dashboard);

		if (!this.isAuthenticated()) {
			this.dashboardService.getPublicDashboardStatistics()
				.pipe(takeUntil(this._destroyed))
				.subscribe(results => {
					this.dashboardStatistics = results;
					this.grantCount = this.dashboardStatistics.referenceTypeStatistics.filter(x => x.referenceType.id == this.referenceTypeService.getGrantReferenceType())?.find(Boolean).count;
					this.organizationCount = this.dashboardStatistics.referenceTypeStatistics.filter(x => x.referenceType.id == this.referenceTypeService.getOrganizationReferenceType())?.find(Boolean).count;
				});
		} else {
			this.dashboardService.getMyDashboardStatistics()
				.pipe(takeUntil(this._destroyed))
				.subscribe(results => {
					this.dashboardStatistics = results;
					this.grantCount = this.dashboardStatistics.referenceTypeStatistics.filter(x => x.referenceType.id == this.referenceTypeService.getGrantReferenceType())?.find(Boolean).count;
					this.organizationCount = this.dashboardStatistics.referenceTypeStatistics.filter(x => x.referenceType.id == this.referenceTypeService.getOrganizationReferenceType())?.find(Boolean).count;

					if (this.dashboardStatistics && this.dashboardStatistics.planCount === 0) {
						this.openDashboardTour();
					}
				});
		}

		this.newReleaseNotificationVisible = this.isNewReleaseNotificationVisible();
	}

	public get indexFromCurrentType() {

        switch(this.currentType){
            case ActivityListingType.Recent: return 0;
            case ActivityListingType.Drafts: return 1;
            case ActivityListingType.Plans: return this.isAuthenticated() ? 2 : 1;
            case ActivityListingType.Descriptions: return this.isAuthenticated() ? 3 : 2;
        }
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
				disableClose: false,
				data: {
					isDialog: true
				}
			});
		}
	}

	public hasPlans(): boolean {
		if (this.dashboardStatistics) {
			return this.dashboardStatistics.planCount !== 0
				|| this.dashboardStatistics.descriptionCount !== 0
				|| this.dashboardStatistics.referenceTypeStatistics.length !== 0
		} else {
			return false;
		}
	}

	addNewDescription(addDescription: boolean): void {
		if (addDescription == false) return;

		const formGroup = this.fb.group({
			planId: this.fb.control(null, Validators.required),
		})

		const dialogRef = this.dialog.open(StartNewDescriptionDialogComponent, {
			disableClose: false,
			restoreFocus: false,
			data: {
				startNewPlan: false,
				formGroup: formGroup
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result) {
				if (result.startNewPlan) {
					this.openNewPlanDialog();
				} else {
					this.router.navigate([this.routerUtils.generateUrl(['/plans/', 'edit/', result.formGroup.get('planId').value])]);
				}
			}
		});
	}

	public dashboardTour: GuidedTour = {
		tourId: 'dashboard-tour',
		useOrb: true,
		steps: [
			{
				selector: '.new-plan-dialog',
				content: 'Step 1',
				orientation: Orientation.BottomRight,
				isStepUnique: false,
				highlightPadding: 10,
				closeAction: () => this.openNewPlanDialog()
			},
			{
				selector: '.import-file',
				content: 'Step 2',
				orientation: Orientation.Bottom,
				isStepUnique: false,
				highlightPadding: 10
			},
			{
				selector: '.start-wizard',
				content: 'Step 3',
				orientation: Orientation.Bottom,
				isStepUnique: false,
				highlightPadding: 10,
				closeAction: () => this.dialog.closeAll()
			},
			{
				selector: '.new-description-tour',
				content: 'Step 4',
				orientation: Orientation.BottomLeft,
				isStepUnique: false,
				highlightPadding: 10
			}
		]
	};

	public setDashboardTourPlanText(): void {
		const planText = this.language.instant('DASHBOARD.TOUR-GUIDE.PLAN') + '\n\n' +
			this.language.instant('DASHBOARD.TOUR-GUIDE.START-NEW');
		this.dashboardTour.steps[0].title = planText;
	}

	public setDashboardImportFileText(): void {
		const importFileText = this.language.instant('DASHBOARD.TOUR-GUIDE.IMPORT-PLAN');
		this.dashboardTour.steps[1].title = importFileText;
	}

	public setDashboardStartWizardText(): void {
		const startWizardText = this.language.instant('DASHBOARD.TOUR-GUIDE.START-NEW-PLAN');
		this.dashboardTour.steps[2].title = startWizardText;
	}

	public setDescriptionText(): void {
		const descriptionText = this.language.instant('DASHBOARD.TOUR-GUIDE.DESCRIPTION') + '\n\n' +
			this.language.instant('DASHBOARD.TOUR-GUIDE.NEW-DESCRIPTION');
		this.dashboardTour.steps[3].title = descriptionText;
	}

	openDashboardTour() {
		this.setDashboardTourPlanText();
		this.setDashboardImportFileText();
		this.setDashboardStartWizardText();
		this.setDescriptionText();
		this.guidedTourService.startTour(this.dashboardTour);
	}

	dismissIntroCard() {
		this.isIntroCardVisible = false;
	}

	dismissNewReleaseNotification() {
		this.cookieService.set('new-release-dismiss-' + this.configurationService.newReleaseNotificationVersionCode, 'true', 5000, null, null, false, 'Lax');
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