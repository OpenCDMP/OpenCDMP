import { TranslateService } from '@ngx-translate/core';
import { GuidedTour, Orientation, TourStep } from '@app/library/guided-tour/guided-tour.constants';
import { PageTour } from '@app/library/guided-tour/tour-state.service';

/**
 * "What's new on the plan overview" (2.0.0): five stops — status, versions, Edit, Publish and the
 * collaborators panel. Role-gated buttons (Publish, manage collaborators) simply drop out of the
 * tour for users who don't see them.
 */
export const PLAN_OVERVIEW_TOUR_VERSION = '2.0.0';

const STEPS: { key: string; selector: string; orientation: string }[] = [
	{ key: 'STATUS', selector: '[data-tour="plan-overview-status"]', orientation: Orientation.Bottom },
	{ key: 'VERSIONS', selector: '[data-tour="plan-overview-versions"]', orientation: Orientation.Bottom },
	{ key: 'EDIT', selector: '[data-tour="plan-overview-edit"]', orientation: Orientation.BottomRight },
	{ key: 'PUBLISH', selector: '[data-tour="plan-overview-publish"]', orientation: Orientation.BottomRight },
	{ key: 'COLLABORATORS', selector: '[data-tour="plan-overview-collaborators"]', orientation: Orientation.Left },
];

export function planOverviewPageTour(language: TranslateService): PageTour {
	return {
		id: 'plan-overview',
		version: PLAN_OVERVIEW_TOUR_VERSION,
		build: (): GuidedTour => ({
			tourId: 'plan-overview-tour',
			useOrb: false,
			preventBackdropFromAdvancing: true,
			steps: STEPS.map((s): TourStep => ({
				selector: s.selector,
				title: language.instant(`PLAN-OVERVIEW.TOUR.${s.key}.TITLE`),
				content: language.instant(`PLAN-OVERVIEW.TOUR.${s.key}.CONTENT`),
				orientation: s.orientation,
				highlightPadding: 4,
				scrollAdjustment: 120,
			})),
		}),
	};
}
