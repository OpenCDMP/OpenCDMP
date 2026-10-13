import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

/** "Default", "Changed" or "Set by the installation" for one setting, with a Reset link back to the installation default or a link to set the organisation's own value. */
@Component({
	selector: 'app-tc-state-tag',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [TranslateModule],
	template: `
		@if (inherited()) {
			<span class="tc-tag tc-tag--default">{{ 'TENANT-CONFIGURATION.STATE.INHERITED' | translate }}</span>
			@if (canCustomise()) {
				<button type="button" class="tc-link" (click)="customiseRequested.emit()">{{ 'TENANT-CONFIGURATION.STATE.CUSTOMISE' | translate }}</button>
			}
		} @else if (changed()) {
			<span class="tc-tag tc-tag--changed">{{ 'TENANT-CONFIGURATION.STATE.CHANGED' | translate }}</span>
			@if (canReset()) {
				<button type="button" class="tc-link" (click)="resetRequested.emit()">{{ 'TENANT-CONFIGURATION.STATE.RESET' | translate }}</button>
			}
		} @else {
			<span class="tc-tag tc-tag--default">{{ 'TENANT-CONFIGURATION.STATE.DEFAULT' | translate }}</span>
		}
	`,
	styles: [`
		:host { display: inline-flex; align-items: center; gap: 10px; flex-shrink: 0; }
		.tc-tag { font-size: 11px; font-weight: 700; padding: 3px 8px; border-radius: 999px; white-space: nowrap; }
		.tc-tag--default { background: var(--md-sys-color-surface-variant, #eef1f6); color: var(--md-sys-color-on-surface-variant, #4e5869); }
		.tc-tag--changed { background: var(--md-sys-color-primary-container, #e3ebfb); color: var(--md-sys-color-on-primary-container, #1c4fb0); }
		.tc-link { font: inherit; font-size: 12px; font-weight: 700; color: var(--link-color, #1f57c3); background: none; border: 0; padding: 0; cursor: pointer; text-decoration: underline; }
	`]
})
export class TcStateTagComponent {
	readonly changed = input(false);
	readonly inherited = input(false);
	readonly canReset = input(true);
	readonly canCustomise = input(false);
	readonly resetRequested = output<void>();
	readonly customiseRequested = output<void>();
}
