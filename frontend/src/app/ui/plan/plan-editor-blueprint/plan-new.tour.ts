import { TranslateService } from '@ngx-translate/core';
import { GuidedTour, Orientation, TourStep } from '@app/library/guided-tour/guided-tour.constants';
import { PageTour } from '@app/library/guided-tour/tour-state.service';

/**
 * "Start a new plan" tour (2.0.0): the blueprint wizard shown at /plans/new. Five stops — the two-step
 * header, importing an existing plan, the blueprint list, the preview of the selected blueprint and
 * the Proceed button. Registered by the plan editor while it is in "new" mode; once the plan exists
 * the editor's own tour takes over. Anchors are `data-tour` attributes on the blueprint preview
 * component; steps whose anchor is absent (no blueprints yet) are dropped by TourStateService.
 *
 * Bump PLAN_NEW_TOUR_VERSION when the steps change materially: users who dismissed the previous
 * version see the new one once.
 */
export const PLAN_NEW_TOUR_VERSION = '2.0.0';

const STEPS: { key: string; selector: string; orientation: string }[] = [
	{ key: 'STEPS', selector: '[data-tour="plan-new-steps"]', orientation: Orientation.Bottom },
	{ key: 'IMPORT', selector: '[data-tour="plan-new-import"]', orientation: Orientation.Right },
	// The first blueprint row: the list itself can be thousands of pixels tall.
	{ key: 'BLUEPRINTS', selector: '[data-tour="plan-new-blueprint"]', orientation: Orientation.Right },
	{ key: 'PREVIEW', selector: '[data-tour="plan-new-preview"]', orientation: Orientation.Bottom },
	{ key: 'PROCEED', selector: '[data-tour="plan-new-proceed"]', orientation: Orientation.Top },
];

export function planNewPageTour(language: TranslateService): PageTour {
	return {
		id: 'plan-new',
		version: PLAN_NEW_TOUR_VERSION,
		build: (): GuidedTour => ({
			tourId: 'plan-new-tour',
			useOrb: false,
			preventBackdropFromAdvancing: true,
			// The one-time "new look" intro is prepended by TourStateService on the user's first tour.
			steps: STEPS.map((s): TourStep => ({
				selector: s.selector,
				title: language.instant(`PLAN-BLUEPRINT-PREVIEW.TOUR.${s.key}.TITLE`),
				content: language.instant(`PLAN-BLUEPRINT-PREVIEW.TOUR.${s.key}.CONTENT`),
				orientation: s.orientation,
				highlightPadding: 4,
				scrollAdjustment: 120,
			})),
		}),
	};
}
