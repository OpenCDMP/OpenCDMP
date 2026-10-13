import { TranslateService } from '@ngx-translate/core';
import { GuidedTour, Orientation, TourStep } from '@app/library/guided-tour/guided-tour.constants';
import { PageTour } from '@app/library/guided-tour/tour-state.service';

/**
 * "What's new in the plan editor" tour (2.0.0 redesign). Six stops, toolbar first, then the table of
 * contents. Anchors are `data-tour` attributes so template refactors don't silently break the tour;
 * steps whose anchor is missing (role-gated buttons) are dropped by TourStateService.
 *
 * Bump PLAN_EDITOR_TOUR_VERSION when the steps change materially: users who dismissed the previous
 * version see the new one once.
 */
export const PLAN_EDITOR_TOUR_VERSION = '2.0.0';

const STEPS: { key: string; selector: string; orientation: string; highlightSelectors?: string[] }[] = [
	{ key: 'STATUS', selector: '[data-tour="plan-editor-status"]', orientation: Orientation.BottomLeft },
	{
		key: 'EXPORT',
		selector: '[data-tour="plan-editor-export"], [data-tour="plan-editor-evaluate"], [data-tour="plan-editor-publish"]',
		orientation: Orientation.Bottom,
		highlightSelectors: ['[data-tour="plan-editor-export"]', '[data-tour="plan-editor-evaluate"]', '[data-tour="plan-editor-publish"]'],
	},
	{
		key: 'COMMENTS',
		selector: '[data-tour="plan-editor-comments"]',
		orientation: Orientation.BottomRight,
		highlightSelectors: ['[data-tour="plan-editor-collaborators"]'],
	},
	{ key: 'SAVE', selector: '[data-tour="plan-editor-save"]', orientation: Orientation.BottomRight },
	{ key: 'TOC', selector: '[data-tour="plan-editor-toc-section"]', orientation: Orientation.Right },
	{ key: 'ADD-ASSET', selector: '[data-tour="plan-editor-add-asset"]', orientation: Orientation.Right },
];

/**
 * "What's new when editing an asset inside the plan" (2.0.0): the context band, the asset's status control,
 * its row in the table of contents, and per-question comments. Registered by the plan editor while a
 * description is selected, so Support > Take a tour replays the right one for what is on screen.
 */
export const PLAN_ASSET_EDITOR_TOUR_VERSION = '2.0.0';

const ASSET_STEPS: { key: string; selector: string; orientation: string }[] = [
	{ key: 'CONTEXT', selector: '[data-tour="plan-editor-context-band"]', orientation: Orientation.Bottom },
	{ key: 'STATUS', selector: '[data-tour="plan-editor-asset-status"]', orientation: Orientation.Bottom },
	{ key: 'TOC', selector: '[data-tour="plan-editor-toc-asset"]', orientation: Orientation.Right },
	{ key: 'COMMENTS', selector: '[data-tour="plan-editor-question-comments"]', orientation: Orientation.Left },
];

export function planAssetEditorPageTour(language: TranslateService): PageTour {
	return {
		id: 'plan-asset-editor',
		version: PLAN_ASSET_EDITOR_TOUR_VERSION,
		build: (): GuidedTour => ({
			tourId: 'plan-asset-editor-tour',
			useOrb: false,
			preventBackdropFromAdvancing: true,
			steps: ASSET_STEPS.map((s): TourStep => ({
				selector: s.selector,
				title: language.instant(`PLAN-EDITOR.ASSET-TOUR.${s.key}.TITLE`),
				content: language.instant(`PLAN-EDITOR.ASSET-TOUR.${s.key}.CONTENT`),
				orientation: s.orientation,
				highlightPadding: 4,
				scrollAdjustment: 120,
			})),
		}),
	};
}

export function planEditorPageTour(language: TranslateService): PageTour {
	return {
		id: 'plan-editor',
		version: PLAN_EDITOR_TOUR_VERSION,
		build: (): GuidedTour => ({
			tourId: 'plan-editor-tour',
			useOrb: false,
			preventBackdropFromAdvancing: true,
			// The one-time "new look" intro is prepended by TourStateService on the user's first tour.
			steps: STEPS.map((s): TourStep => ({
				selector: s.selector,
				highlightSelectors: s.highlightSelectors,
				title: language.instant(`PLAN-EDITOR.TOUR.${s.key}.TITLE`),
				content: language.instant(`PLAN-EDITOR.TOUR.${s.key}.CONTENT`),
				orientation: s.orientation,
				highlightPadding: 4,
				scrollAdjustment: 120,
			})),
		}),
	};
}
