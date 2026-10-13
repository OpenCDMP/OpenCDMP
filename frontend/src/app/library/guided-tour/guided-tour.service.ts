import { debounceTime } from 'rxjs/operators';
import { ErrorHandler, Inject, Injectable, DOCUMENT } from '@angular/core';
import { Observable, Subject, fromEvent } from 'rxjs';
import { GuidedTour, TourStep, Orientation, OrientationConfiguration } from './guided-tour.constants';

import { WindowRefService } from "./windowref.service";

@Injectable()
export class GuidedTourService {
	public guidedTourCurrentStepStream: Observable<TourStep>;
	public guidedTourOrbShowingStream: Observable<boolean>;

	private _guidedTourCurrentStepSubject = new Subject<TourStep>();
	private _guidedTourOrbShowingSubject = new Subject<boolean>();
	private _currentTourStepIndex = 0;
	private _currentTour: GuidedTour = null;
	private _onFirstStep = true;
	private _onLastStep = true;
	private _onResizeMessage = false;

	constructor(
		public errorHandler: ErrorHandler,
		private windowRef: WindowRefService,
		@Inject(DOCUMENT) private dom
	) {
		this.guidedTourCurrentStepStream = this._guidedTourCurrentStepSubject.asObservable();
		this.guidedTourOrbShowingStream = this._guidedTourOrbShowingSubject.asObservable();

		fromEvent(this.windowRef.nativeWindow, 'resize').pipe(debounceTime(200)).subscribe(() => {
			if (this._currentTour && this._currentTourStepIndex > -1) {
				if (this._currentTour.minimumScreenSize && this._currentTour.minimumScreenSize >= this.windowRef.nativeWindow.innerWidth) {
					this._onResizeMessage = true;
					const dialog = this._currentTour.resizeDialog || {
						title: 'Please resize',
						content: 'You have resized the tour to a size that is too small to continue. Please resize the browser to a larger size to continue the tour or close the tour.'
					};

					this._guidedTourCurrentStepSubject.next(dialog);
				} else {
					this._onResizeMessage = false;
					this._guidedTourCurrentStepSubject.next(this.getPreparedTourStep(this._currentTourStepIndex));
				}
			}
		});
	}

	public nextStep(): void {
		if (this._currentTour.steps[this._currentTourStepIndex].closeAction) {
			this._currentTour.steps[this._currentTourStepIndex].closeAction();
		}
		if (this._currentTour.steps[this._currentTourStepIndex + 1]) {
			this._currentTourStepIndex++;
			this._setFirstAndLast();
			if (this._currentTour.steps[this._currentTourStepIndex].action) {
				this._currentTour.steps[this._currentTourStepIndex].action();
				// Usually an action is opening something so we need to give it time to render.
				setTimeout(() => {
					if (this._checkSelectorValidity()) {
						this._guidedTourCurrentStepSubject.next(this.getPreparedTourStep(this._currentTourStepIndex));
					} else {
						this.nextStep();
					}
				});
			} else {
				setTimeout(() => {
					if (this._checkSelectorValidity()) {
						this._guidedTourCurrentStepSubject.next(this.getPreparedTourStep(this._currentTourStepIndex));
					} else {
						this.nextStep();
					}
				}, 500);
            }
		} else {
			if (this._currentTour.completeCallback) {
				this._currentTour.completeCallback();
			}
			this.resetTour();
		}
	}

	public backStep(): void {
		if (this._currentTour.steps[this._currentTourStepIndex].closeAction) {
			this._currentTour.steps[this._currentTourStepIndex].closeAction();
		}
		if (this._currentTour.steps[this._currentTourStepIndex - 1]) {
			this._currentTourStepIndex--;
			this._setFirstAndLast();
			if (this._currentTour.steps[this._currentTourStepIndex].action) {
				this._currentTour.steps[this._currentTourStepIndex].action();
				setTimeout(() => {
					if (this._checkSelectorValidity()) {
						this._guidedTourCurrentStepSubject.next(this.getPreparedTourStep(this._currentTourStepIndex));
					} else {
						this.backStep();
					}
				});
			} else {
				if (this._checkSelectorValidity()) {
					this._guidedTourCurrentStepSubject.next(this.getPreparedTourStep(this._currentTourStepIndex));
				} else {
					this.backStep();
				}
			}
		} else {
			this.resetTour();
		}
	}

	public skipTour(): void {
		if (this._currentTour.skipCallback) {
			this._currentTour.skipCallback(this._currentTourStepIndex);
		}
		this.resetTour();
	}

	public resetTour(): void {
		this.dom.body.classList.remove('tour-open');
		this._currentTour = null;
		this._currentTourStepIndex = 0;
		this._guidedTourCurrentStepSubject.next(null);
	}

	/**
	 * CUSTOM ADD: resolve a step selector to the first VISIBLE match. Templates are often rendered twice
	 * (a mobile copy hidden on desktop and vice versa); querySelector would pick the hidden one and the
	 * callout would be positioned off-screen.
	 */
	public static findVisible(dom: Document, selector: string): HTMLElement | null {
		const all = Array.from(dom.querySelectorAll<HTMLElement>(selector));
		return all.find(el => { const r = el.getBoundingClientRect(); return r.width > 0 && r.height > 0; }) ?? all[0] ?? null;
	}

	public startTour(tour: GuidedTour): void {
		// Deep-clone the definition so step preparation can't mutate the caller's object, but keep the
		// function properties: JSON.stringify drops them, which silently disabled skip/complete callbacks
		// and every step's action/closeAction.
		const cloned: GuidedTour = JSON.parse(JSON.stringify(tour)); //replaced lodash deepClone
		this._currentTour = {
			...cloned,
			skipCallback: tour.skipCallback,
			completeCallback: tour.completeCallback,
			steps: (tour.steps ?? []).map((step, i) => ({ ...cloned.steps[i], action: step.action, closeAction: step.closeAction })),
		};
		this._currentTour.steps = this._currentTour.steps.filter(step => !step.skipStep);
		this._currentTourStepIndex = 0;
		this._setFirstAndLast();
		this._guidedTourOrbShowingSubject.next(this._currentTour.useOrb);
		if (
			this._currentTour.steps.length > 0
			&& (!this._currentTour.minimumScreenSize
				|| (this.windowRef.nativeWindow.innerWidth >= this._currentTour.minimumScreenSize))
		) {
			if (!this._currentTour.useOrb) {
				this.dom.body.classList.add('tour-open');
			}
			if (this._currentTour.steps[this._currentTourStepIndex].action) {
				this._currentTour.steps[this._currentTourStepIndex].action();
			}
			if (this._checkSelectorValidity()) {
				this._guidedTourCurrentStepSubject.next(this.getPreparedTourStep(this._currentTourStepIndex));
			} else {
				this.nextStep();
			}
		}
	}

	public activateOrb(): void {
		this._guidedTourOrbShowingSubject.next(false);
		this.dom.body.classList.add('tour-open');
	}

	private _setFirstAndLast(): void {
		this._onLastStep = (this._currentTour.steps.length - 1) === this._currentTourStepIndex;
		this._onFirstStep = this._currentTourStepIndex === 0;
	}

	/**
	 * CUSTOM ADD: bring a step's target into view before anyone measures it.
	 *
	 * The rest of this component scrolls `window`, which does nothing when the anchor lives inside
	 * an inner scroll container (the plan editor's asset form is one). Native `scrollIntoView`
	 * walks every scrollable ancestor, so it works for both. It is synchronous with respect to
	 * layout, so a `getBoundingClientRect()` right after already reflects the new position.
	 */
	public static bringIntoView(element: HTMLElement): void {
		try {
			element?.scrollIntoView({ block: 'center', inline: 'nearest' });
		} catch {
			// older engines: the options object form is unsupported, the boolean form still centres enough
			element?.scrollIntoView(true);
		}
	}

	private _checkSelectorValidity(): boolean {
		if (this._currentTour.steps[this._currentTourStepIndex].selector) {
			const selectedElement = GuidedTourService.findVisible(this.dom, this._currentTour.steps[this._currentTourStepIndex].selector);
			// Scroll first, then judge reachability: an anchor below the fold is a normal tour stop,
			// not a bad match. Only something that is still off screen after scrolling (clipped or
			// detached) is treated as unreachable and skipped.
			if (selectedElement) {
				GuidedTourService.bringIntoView(selectedElement);
			}
			if (!selectedElement || !GuidedTourService.isReachable(this.windowRef.nativeWindow, selectedElement)) {
				this.errorHandler.handleError(
					// If error handler is configured this should not block the browser.
					new Error(`Error finding selector ${this._currentTour.steps[this._currentTourStepIndex].selector} on step ${this._currentTourStepIndex + 1} during guided tour: ${this._currentTour.tourId}`)
				);
				return false;
			}
		}
		return true;
	}

	/**
	 * CUSTOM ADD: a selector can resolve to an element that technically exists but belongs to a
	 * different entity than the one the tour actually means (e.g. a per-item action button that is
	 * also rendered, unscoped, for sibling items elsewhere on the page). Such a match still has a
	 * bounding rect, so `findVisible`'s width/height check alone accepts it — and if that rect sits
	 * outside the viewport the tour step renders unreachable while the full-page click-blocking mask
	 * stays up, trapping the user. Require the match to actually be on screen before trusting it.
	 */
	private static isReachable(win: Window, element: HTMLElement): boolean {
		const rect = element.getBoundingClientRect();
		return rect.bottom > 0 && rect.right > 0 && rect.top < win.innerHeight && rect.left < win.innerWidth;
	}

	public get onLastStep(): boolean {
		return this._onLastStep;
	}

	public get onFirstStep(): boolean {
		return this._onFirstStep;
	}

	public get onResizeMessage(): boolean {
		return this._onResizeMessage;
	}

	/** CUSTOM ADD: an untargeted first step is an intro, not a stop — the "x of y" counter skips it. */
	public get introStepOffset(): number {
		return this._currentTour?.steps?.[0] && !this._currentTour.steps[0].selector ? 1 : 0;
	}

	public get currentTourStepDisplay(): number {
		return this._currentTourStepIndex + 1;
	}

	public get currentTourStepCount(): number {
		return this._currentTour && this._currentTour.steps ? this._currentTour.steps.length : 0;
	}

	public get preventBackdropFromAdvancing(): boolean {
		return this._currentTour && this._currentTour.preventBackdropFromAdvancing;
	}

	private getPreparedTourStep(index: number): TourStep {
		return this.setTourOrientation(this._currentTour.steps[index]);
	}

	private setTourOrientation(step: TourStep): TourStep {
		const convertedStep = JSON.parse(JSON.stringify(step));
		if (
			convertedStep.orientation
			&& !(typeof convertedStep.orientation === 'string')
			&& (convertedStep.orientation as OrientationConfiguration[]).length
		) {
			(convertedStep.orientation as OrientationConfiguration[]).sort((a: OrientationConfiguration, b: OrientationConfiguration) => {
				if (!b.maximumSize) {
					return 1;
				}
				if (!a.maximumSize) {
					return -1;
				}
				return b.maximumSize - a.maximumSize;
			});

			let currentOrientation: Orientation = Orientation.Top;
			(convertedStep.orientation as OrientationConfiguration[]).forEach(
				(orientationConfig: OrientationConfiguration) => {
					if (!orientationConfig.maximumSize || this.windowRef.nativeWindow.innerWidth <= orientationConfig.maximumSize) {
						currentOrientation = orientationConfig.orientationDirection;
					}
				}
			);

			convertedStep.orientation = currentOrientation;
		}
		return convertedStep;
	}
}
