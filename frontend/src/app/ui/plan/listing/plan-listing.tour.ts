import { TranslateService } from '@ngx-translate/core';
import { GuidedTour, Orientation, TourStep } from '@app/library/guided-tour/guided-tour.constants';
import { PageTour } from '@app/library/guided-tour/tour-state.service';

/**
 * "What's new on My plans" (2.0.0): four stops — start a plan from the sidebar, the two list views,
 * sort + filters, and what a plan card now shows. Anchors are `data-tour` attributes; steps whose
 * anchor is absent (collapsed sidebar, empty listing) are dropped by TourStateService.
 */
export const PLAN_LISTING_TOUR_VERSION = '2.0.0';

const STEPS: { key: string; selector: string; orientation: string }[] = [
	{ key: 'NEW-PLAN', selector: '[data-tour="sidebar-new-plan"]', orientation: Orientation.Right },
	{ key: 'VIEWS', selector: '[data-tour="plan-listing-views"]', orientation: Orientation.Bottom },
	{ key: 'FILTERS', selector: '[data-tour="plan-listing-filters"]', orientation: Orientation.BottomRight },
	{ key: 'CARD', selector: '[data-tour="plan-listing-item"]', orientation: Orientation.Bottom },
];

export function planListingPageTour(language: TranslateService): PageTour {
	return {
		id: 'plan-listing',
		version: PLAN_LISTING_TOUR_VERSION,
		build: (): GuidedTour => ({
			tourId: 'plan-listing-tour',
			useOrb: false,
			preventBackdropFromAdvancing: true,
			steps: STEPS.map((s): TourStep => ({
				selector: s.selector,
				title: language.instant(`PLAN-LISTING.TOUR.${s.key}.TITLE`),
				content: language.instant(`PLAN-LISTING.TOUR.${s.key}.CONTENT`),
				orientation: s.orientation,
				highlightPadding: 4,
				scrollAdjustment: 120,
			})),
		}),
	};
}
