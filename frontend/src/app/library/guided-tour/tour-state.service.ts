import { Injectable, computed, inject, signal } from '@angular/core';
import { UserSettingsInformation } from '@app/core/model/user-settings/user-settings.model';
import { UserSetting, UserSettingsService, UserSettingsType } from '@app/core/services/user-settings/user-settings.service';
import { TranslateService } from '@ngx-translate/core';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { GuidedToursEditorResolver } from '@app/ui/admin/tenant-configuration/models/guided-tours/guided-tours-editor.resolver';
import { take } from 'rxjs/operators';
import { GuidedTour, TourStep } from './guided-tour.constants';
import { GuidedTourService } from './guided-tour.service';

/**
 * A page-level "what's new" tour. `version` is part of the seen-key, so bumping it on a release
 * re-shows the tour once to everyone who had dismissed the previous one.
 */
export interface PageTour {
	id: string;
	version: string;
	/** Built lazily so step text picks up the active language. */
	build: () => GuidedTour;
}

/** Per-user record of dismissed tours, stored through the user-settings service (server + localStorage). */
export class GuidedToursSettings {
	seen: string[] = [];
}

const GUIDED_TOURS_SETTINGS: UserSettingsInformation<GuidedToursSettings> = { key: 'GuidedToursUserSettings', type: GuidedToursSettings };
const ANCHOR_POLL_MS = 300;
const ANCHOR_POLL_ATTEMPTS = 20; // 6s hard cap
const ANCHOR_SETTLE_POLLS = 3; // ~1s without a new anchor appearing = the page has laid out

/**
 * Owns the "first visit" logic on top of GuidedTourService: which tour belongs to the current page
 * (so the Support menu can replay it), whether the user has already seen it, and starting it only
 * once the anchors it points at exist. Steps whose anchor is absent (role-gated buttons, empty
 * states) are dropped rather than crashing the tour.
 *
 * The "OpenCDMP has a new look" intro card is prepended exactly once per user: on the first tour
 * they meet after the upgrade, whichever page that is. Replays from the Support menu never show it.
 */
@Injectable({ providedIn: 'root' })
export class TourStateService {
	private readonly guidedTourService = inject(GuidedTourService);
	private readonly userSettingsService = inject(UserSettingsService);
	private readonly language = inject(TranslateService);
	private readonly tenantConfigurationService = inject(TenantConfigurationService);

	private readonly pageTour = signal<PageTour | null>(null);
	private readonly seen = signal<Set<string> | null>(null); // null = not loaded yet
	private stored: UserSetting<GuidedToursSettings> | null = null;
	private pollHandle: ReturnType<typeof setTimeout> | null = null;
	/** Tenant switch (TenantConfigurationType.GuidedTours): null = not loaded yet, absent configuration = enabled. */
	private readonly tenantEnabled = signal<boolean | null>(null);

	/** True when the current page registered a tour and the tenant allows tours, i.e. "Take a tour" makes sense in the Support menu. */
	readonly hasPageTour = computed(() => !!this.pageTour() && this.tenantEnabled() !== false);

	registerPageTour(tour: PageTour): void {
		this.pageTour.set(tour);
		// Kick both lookups off now, in parallel, so they are settled by the time the page's anchors render.
		this.loadTenantSwitch();
		this.loadSeen(() => undefined);
	}

	unregisterPageTour(tour: PageTour): void {
		if (this.pageTour()?.id !== tour.id) { return; }
		this.pageTour.set(null);
		this.cancelPoll();
		// Leaving the page mid-tour would strand the overlay on the next route.
		this.guidedTourService.resetTour();
	}

	/** Replay the current page's tour on demand, regardless of the seen state and without the intro. */
	startPageTour(): void {
		const tour = this.pageTour();
		if (tour && this.tenantEnabled() !== false) { this.startWhenReady(tour, false); }
	}

	/** First-visit behaviour: start the tour once per user per version, with the intro if it is their first tour. */
	startIfUnseen(tour: PageTour): void {
		this.whenTenantAllows(() => this.loadSeen(seen => {
			if (seen.has(this.seenKey(tour))) { return; }
			this.startWhenReady(tour, seen.size === 0);
		}));
	}

	// ---- internals -----------------------------------------------------------

	private tenantSwitchWaiters: (() => void)[] = [];

	/** Fetch the tenant switch once; until it is known, callers queue up. Errors (offline, no permission) count as enabled. */
	private loadTenantSwitch(): void {
		if (this.tenantEnabled() !== null || this.tenantSwitchLoading) { return; }
		this.tenantSwitchLoading = true;
		this.tenantConfigurationService.getActiveType(TenantConfigurationType.GuidedTours, GuidedToursEditorResolver.activeLookupFields()).pipe(take(1)).subscribe({
			next: (config) => this.settleTenantSwitch(config?.guidedTours?.enabled ?? true),
			error: () => this.settleTenantSwitch(true),
		});
	}
	private tenantSwitchLoading = false;

	private settleTenantSwitch(enabled: boolean): void {
		this.tenantEnabled.set(enabled);
		this.tenantSwitchLoading = false;
		const waiters = this.tenantSwitchWaiters; this.tenantSwitchWaiters = [];
		if (enabled) { waiters.forEach(w => w()); }
	}

	private whenTenantAllows(then: () => void): void {
		const known = this.tenantEnabled();
		if (known === true) { then(); return; }
		if (known === false) { return; }
		this.tenantSwitchWaiters.push(then);
		this.loadTenantSwitch();
	}

	private seenKey(tour: PageTour): string {
		return `${tour.id}@${tour.version}`;
	}

	private seenWaiters: ((seen: Set<string>) => void)[] = [];
	private seenLoading = false;

	private loadSeen(then: (seen: Set<string>) => void): void {
		const cached = this.seen();
		if (cached) { then(cached); return; }
		this.seenWaiters.push(then);
		if (this.seenLoading) { return; }
		this.seenLoading = true;
		const settle = (set: Set<string>) => { this.seenLoading = false; const w = this.seenWaiters; this.seenWaiters = []; w.forEach(fn => fn(set)); };
		this.userSettingsService.get(GUIDED_TOURS_SETTINGS).pipe(take(1)).subscribe({
			next: (settings) => {
				this.stored = settings?.defaultSetting ?? settings?.settings?.[0] ?? null;
				const set = new Set<string>(this.stored?.value?.seen ?? []);
				this.seen.set(set);
				settle(set);
			},
			// No settings yet (404 for a first-time user) or unavailable: behave as "never seen" and remember that,
			// so the first Skip/Done creates the row instead of asking the server again.
			error: () => { const set = new Set<string>(); this.seen.set(set); settle(set); },
		});
	}

	/**
	 * Called from the library's skip/complete callbacks, which run BEFORE it tears the overlay down —
	 * so this must never throw, or the backdrop would be left on screen.
	 */
	private markSeen(tour: PageTour): void {
		const key = this.seenKey(tour);
		const set = new Set(this.seen() ?? []);
		if (set.has(key)) { return; }
		set.add(key);
		this.seen.set(set);
		try {
			const setting: UserSetting<GuidedToursSettings> = {
				id: this.stored?.id ?? null,
				name: this.stored?.name ?? 'Default',
				key: GUIDED_TOURS_SETTINGS.key,
				type: UserSettingsType.Setting,
				value: { seen: [...set] },
				hash: this.stored?.hash ?? null,
				isDefault: true,
			};
			this.userSettingsService.set(setting, true, GUIDED_TOURS_SETTINGS);
		} catch (err) {
			console.warn('Could not persist the guided-tour seen state', err);
		}
	}

	/** The one-time "new look" card: untargeted, so it renders centred, in the intro variant. */
	private introStep(): TourStep {
		return {
			variant: 'intro',
			eyebrow: this.language.instant('GUIDED-TOUR.INTRO.EYEBROW'),
			title: this.language.instant('GUIDED-TOUR.INTRO.TITLE'),
			content: this.language.instant('GUIDED-TOUR.INTRO.CONTENT'),
			nextText: this.language.instant('GUIDED-TOUR.START'),
		};
	}

	/** Wait for the anchors to render (pages lay out after their data arrives), then start. */
	private startWhenReady(tour: PageTour, withIntro: boolean, attempt = 0, lastCount = -1, stablePolls = 0): void {
		this.cancelPoll();
		const built = tour.build();
		const present = built.steps.filter(step => !step.selector || !!GuidedTourService.findVisible(document, step.selector));
		const allPresent = present.length === built.steps.length;
		// Start when every anchor is on screen, or when the set of visible anchors has stopped growing for
		// a moment (a role-gated button that will never appear must not hold the tour for the full cap).
		const settled = present.some(s => !!s.selector) && present.length === lastCount && stablePolls >= ANCHOR_SETTLE_POLLS;
		if (!allPresent && !settled && attempt < ANCHOR_POLL_ATTEMPTS) {
			const nextStable = present.length === lastCount ? stablePolls + 1 : 0;
			this.pollHandle = setTimeout(() => this.startWhenReady(tour, withIntro, attempt + 1, present.length, nextStable), ANCHOR_POLL_MS);
			return;
		}
		if (!present.length) { return; }
		const steps = withIntro ? [this.introStep(), ...present] : present;
		this.guidedTourService.startTour({
			...built,
			steps: steps.map(step => this.withUniqueFlag(step, steps.length)),
			skipCallback: () => this.markSeen(tour),
			completeCallback: () => this.markSeen(tour),
		});
	}

	private withUniqueFlag(step: TourStep, count: number): TourStep {
		return count === 1 ? { ...step, isStepUnique: true } : step;
	}

	private cancelPoll(): void {
		if (this.pollHandle) { clearTimeout(this.pollHandle); this.pollHandle = null; }
	}
}
