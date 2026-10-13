import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule } from '@ngx-translate/core';

/** The one save bar of a tenant configuration section: counts unsaved changes, saves or discards them together. */
@Component({
	selector: 'app-tc-save-bar',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [MatButtonModule, MatProgressSpinnerModule, TranslateModule],
	// out of the way until there is something to save
	host: { '[class.tc-save-bar--idle]': 'dirtyCount() === 0 && !saving()' },
	template: `
		<div class="tc-save-bar" role="region" [attr.aria-label]="'TENANT-CONFIGURATION.SAVE-BAR.LABEL' | translate">
			<span class="tc-save-bar__state" aria-live="polite">
				@if (dirtyCount() === 0) {
					{{ 'TENANT-CONFIGURATION.SAVE-BAR.NO-CHANGES' | translate }}
				} @else if (dirtyCount() === 1) {
					<strong>{{ 'TENANT-CONFIGURATION.SAVE-BAR.ONE-CHANGE' | translate }}</strong>
				} @else {
					<strong>{{ 'TENANT-CONFIGURATION.SAVE-BAR.CHANGES' | translate: { count: dirtyCount() } }}</strong>
				}
			</span>
			@if (canSave()) {
				<div class="tc-save-bar__actions">
					<button mat-stroked-button type="button" [disabled]="dirtyCount() === 0 || saving()" (click)="discard.emit()">{{ 'TENANT-CONFIGURATION.SAVE-BAR.DISCARD' | translate }}</button>
					<button mat-flat-button color="primary" type="button" [disabled]="dirtyCount() === 0 || saving()" (click)="save.emit()">
						@if (saving()) { <mat-spinner diameter="18" aria-hidden="true"></mat-spinner> }
						{{ 'TENANT-CONFIGURATION.SAVE-BAR.SAVE' | translate }}
					</button>
				</div>
			}
		</div>
	`,
	styles: [`
		:host { position: sticky; bottom: 0; display: block; z-index: 2; margin-top: 24px; margin-right: 64px; }
		:host(.tc-save-bar--idle) { display: none; }
		.tc-save-bar { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 12px 20px;
			background: var(--md-sys-color-surface, #fff); border: 1px solid var(--md-sys-color-outline-variant, #e1e5ee);
			border-radius: 12px; box-shadow: 0 -2px 12px rgba(16, 24, 40, 0.06); }
		.tc-save-bar__state { font-size: 14px; color: var(--md-sys-color-on-surface-variant, #566072); }
		.tc-save-bar__state strong { color: var(--md-sys-color-on-surface, #1c2230); }
		.tc-save-bar__actions { display: flex; gap: 10px; }
		mat-spinner { display: inline-block; margin-right: 8px; vertical-align: middle; }
	`]
})
export class TcSaveBarComponent {
	readonly dirtyCount = input(0);
	readonly saving = input(false);
	readonly canSave = input(true);
	readonly save = output<void>();
	readonly discard = output<void>();
}
