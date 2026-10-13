import { TranslateService } from '@ngx-translate/core';
import { GuidedTour, Orientation, TourStep } from '@app/library/guided-tour/guided-tour.constants';
import { PageTour } from '@app/library/guided-tour/tour-state.service';

/** "What's new on the asset overview" (2.0.0): status, the parent plan link and Edit. */
export const DESCRIPTION_OVERVIEW_TOUR_VERSION = '2.0.0';

const STEPS: { key: string; selector: string; orientation: string }[] = [
	{ key: 'STATUS', selector: '[data-tour="description-overview-status"]', orientation: Orientation.Bottom },
	{ key: 'EDIT', selector: '[data-tour="description-overview-edit"]', orientation: Orientation.BottomRight },
	{ key: 'PLAN', selector: '[data-tour="description-overview-plan"]', orientation: Orientation.Bottom },
];

export function descriptionOverviewPageTour(language: TranslateService): PageTour {
	return {
		id: 'description-overview',
		version: DESCRIPTION_OVERVIEW_TOUR_VERSION,
		build: (): GuidedTour => ({
			tourId: 'description-overview-tour',
			useOrb: false,
			preventBackdropFromAdvancing: true,
			steps: STEPS.map((s): TourStep => ({
				selector: s.selector,
				title: language.instant(`DESCRIPTION-OVERVIEW.TOUR.${s.key}.TITLE`),
				content: language.instant(`DESCRIPTION-OVERVIEW.TOUR.${s.key}.CONTENT`),
				orientation: s.orientation,
				highlightPadding: 4,
				scrollAdjustment: 120,
			})),
		}),
	};
}
