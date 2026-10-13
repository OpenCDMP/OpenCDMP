import { TranslateService } from '@ngx-translate/core';
import { GuidedTour, Orientation, TourStep } from '@app/library/guided-tour/guided-tour.constants';
import { PageTour } from '@app/library/guided-tour/tour-state.service';

/** "What's new on My assets" (2.0.0): the mirror of the My plans tour with the asset listing's anchors. */
export const DESCRIPTION_LISTING_TOUR_VERSION = '2.0.0';

const STEPS: { key: string; selector: string; orientation: string }[] = [
	{ key: 'NEW-ASSET', selector: '[data-tour="sidebar-new-asset"]', orientation: Orientation.Right },
	{ key: 'VIEWS', selector: '[data-tour="description-listing-views"]', orientation: Orientation.Bottom },
	{ key: 'FILTERS', selector: '[data-tour="description-listing-filters"]', orientation: Orientation.BottomRight },
	{ key: 'CARD', selector: '[data-tour="description-listing-item"]', orientation: Orientation.Bottom },
];

export function descriptionListingPageTour(language: TranslateService): PageTour {
	return {
		id: 'description-listing',
		version: DESCRIPTION_LISTING_TOUR_VERSION,
		build: (): GuidedTour => ({
			tourId: 'description-listing-tour',
			useOrb: false,
			preventBackdropFromAdvancing: true,
			steps: STEPS.map((s): TourStep => ({
				selector: s.selector,
				title: language.instant(`DESCRIPTION-LISTING.TOUR.${s.key}.TITLE`),
				content: language.instant(`DESCRIPTION-LISTING.TOUR.${s.key}.CONTENT`),
				orientation: s.orientation,
				highlightPadding: 4,
				scrollAdjustment: 120,
			})),
		}),
	};
}
