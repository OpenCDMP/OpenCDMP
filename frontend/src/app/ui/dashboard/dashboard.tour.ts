import { TranslateService } from '@ngx-translate/core';
import { GuidedTour, Orientation, TourStep } from '@app/library/guided-tour/guided-tour.constants';
import { PageTour } from '@app/library/guided-tour/tour-state.service';

/**
 * "What's new on the Dashboard" (2.0.1): eight stops — start a plan or asset from the sidebar, the
 * organisation's recommended blueprints and templates, the activity tabs, the search/view/sort toolbar, the usage overview, the getting-started card, the
 * Library and Explore sidebar groups, and the support/language/organisation/account controls.
 * Anchors are `data-tour` attributes; steps whose anchor is absent (collapsed sidebar, a user with
 * nothing yet so the activity tabs are not rendered) are dropped by TourStateService.
 *
 * Bump DASHBOARD_TOUR_VERSION when the steps change materially: users who dismissed the previous
 * version see the new one once.
 */
export const DASHBOARD_TOUR_VERSION = '2.0.1';

const STEPS: { key: string; selector: string; orientation: string; highlightSelectors?: string[] }[] = [
	{ key: 'NEW-PLAN', selector: '[data-tour="sidebar-new-plan"]', orientation: Orientation.Right },
	{ key: 'RECOMMENDATIONS', selector: '[data-tour="dashboard-recommendations"]', orientation: Orientation.Bottom },
	// The tab strip only: the tab group's box would also cover the whole list underneath.
	{ key: 'ACTIVITY', selector: '[data-tour="dashboard-activity"] .mat-mdc-tab-header', orientation: Orientation.Bottom },
	{ key: 'FILTERS', selector: '[data-tour="dashboard-activity-filters"]', orientation: Orientation.Bottom },
	{ key: 'OVERVIEW', selector: '[data-tour="dashboard-overview"]', orientation: Orientation.Left },
	{ key: 'GETTING-STARTED', selector: '[data-tour="dashboard-getting-started"]', orientation: Orientation.Left },
	{
		key: 'LIBRARY-EXPLORE',
		selector: '[data-tour="sidebar-library"], [data-tour="sidebar-explore"]',
		orientation: Orientation.Right,
		highlightSelectors: ['[data-tour="sidebar-library"]', '[data-tour="sidebar-explore"]'],
	},
	{ key: 'ACCOUNT', selector: '[data-tour="navbar-account"]', orientation: Orientation.BottomRight },
];

export function dashboardPageTour(language: TranslateService): PageTour {
	return {
		id: 'dashboard',
		version: DASHBOARD_TOUR_VERSION,
		build: (): GuidedTour => ({
			tourId: 'dashboard-tour',
			useOrb: false,
			preventBackdropFromAdvancing: true,
			// The one-time "new look" intro is prepended by TourStateService on the user's first tour.
			steps: STEPS.map((s): TourStep => ({
				selector: s.selector,
				highlightSelectors: s.highlightSelectors,
				title: language.instant(`DASHBOARD.TOUR.${s.key}.TITLE`),
				content: language.instant(`DASHBOARD.TOUR.${s.key}.CONTENT`),
				orientation: s.orientation,
				highlightPadding: 4,
				scrollAdjustment: 120,
			})),
		}),
	};
}
