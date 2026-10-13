import { Injectable } from '@angular/core';
import { LiveAnnouncer } from '@angular/cdk/a11y';

/**
 * Thin wrapper around the Angular CDK {@link LiveAnnouncer} so the app has a single,
 * injectable entry point for screen-reader status announcements (WCAG 4.1.3 Status
 * Messages). Use this for async outcomes that have no natural focus change: route
 * changes, save/autosave results, validation summaries, upload status, etc.
 *
 * - `politeness: 'polite'` (default) queues the message without interrupting.
 * - `politeness: 'assertive'` interrupts — use only for errors/warnings.
 */
@Injectable({ providedIn: 'root' })
export class A11yAnnouncerService {
	constructor(private liveAnnouncer: LiveAnnouncer) {}

	announce(message: string, politeness: 'polite' | 'assertive' = 'polite'): void {
		if (!message) {
			return;
		}
		// LiveAnnouncer returns a promise; we intentionally don't await it.
		this.liveAnnouncer.announce(message, politeness);
	}
}
