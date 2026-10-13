import { Component, EventEmitter, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { TranslateModule } from '@ngx-translate/core';

/**
 * One inline filter of an admin listing, shown as a chip ("Status: Any", "Status: Draft", "Status: 2 selected")
 * that opens a small popover.
 *
 * - With `options`: a multi-select of those values, applied on every tick (no Apply button); `labelOf` renders a value.
 * - Without `options`: the projected content is the filter (an autocomplete, a date range), applied with the popover's
 *   Apply button; `summary` is the text shown on the chip when something is set.
 */
@Component({
	selector: 'app-listing-filter-chip',
	standalone: true,
	imports: [TranslateModule, MatButtonModule, MatIconModule, MatMenuModule, MatCheckboxModule],
	template: `
		<button type="button" class="al-chip" [class.al-chip--on]="isSet" [matMenuTriggerFor]="menu" [attr.aria-label]="label + ': ' + valueText">
			<span class="al-chip__label">{{ label }}:</span>
			<span class="al-chip__value">{{ isSet ? valueText : ('ADMIN-LISTING.FILTERS.ANY' | translate) }}</span>
			<mat-icon aria-hidden="true">expand_more</mat-icon>
		</button>
		<mat-menu #menu="matMenu" class="al-filter-menu">
			<div class="al-filter-popover" (click)="$event.stopPropagation()" (keydown.tab)="$event.stopPropagation()">
				<div class="al-filter-popover__title">{{ label }}</div>
				@if (options) {
					<div class="al-filter-popover__options">
						@for (option of options; track $index) {
							<mat-checkbox class="al-filter-popover__option" [checked]="isSelected(option)" (change)="toggle(option, $event.checked)">{{ labelOf(option) }}</mat-checkbox>
						}
					</div>
					@if (isSet) {
						<div class="al-filter-popover__footer">
							<button mat-button type="button" (click)="clear()">{{ 'HYBRID-LISTING.NO-RESULTS.CLEAR' | translate }}</button>
						</div>
					}
				} @else {
					<div class="al-filter-popover__options">
						<ng-content />
					</div>
					<div class="al-filter-popover__footer">
						<button mat-button type="button" (click)="clear()">{{ 'HYBRID-LISTING.NO-RESULTS.CLEAR' | translate }}</button>
						<button mat-flat-button color="primary" type="button" (click)="applied.emit()">{{ 'ADMIN-LISTING.FILTERS.APPLY' | translate }}</button>
					</div>
				}
			</div>
		</mat-menu>
	`,
	changeDetection: ChangeDetectionStrategy.Eager,
	styles: [`
		:host { display: inline-block; }
		.al-chip {
			display: inline-flex; align-items: center; gap: .375rem; height: 36px; padding: 0 .625rem 0 .875rem; border-radius: 999px;
			border: 1px solid var(--mat-sys-outline-variant); background: var(--mat-sys-surface); color: var(--mat-sys-on-surface);
			font: inherit; font-size: .85rem; cursor: pointer; max-width: 320px;
			mat-icon { font-size: 18px; width: 18px; height: 18px; color: var(--mat-sys-on-surface-variant); }
			&:hover { background: var(--mat-sys-surface-container); }
			&:focus-visible { outline: 2px solid var(--mat-sys-primary); outline-offset: 2px; }
		}
		.al-chip--on { background: var(--mat-sys-secondary-container); border-color: var(--mat-sys-primary); color: var(--mat-sys-on-secondary-container); }
		.al-chip__label { color: var(--mat-sys-on-surface-variant); }
		.al-chip--on .al-chip__label { color: inherit; opacity: .8; }
		.al-chip__value { font-weight: 700; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
		.al-filter-popover { display: flex; flex-direction: column; min-width: 260px; max-width: 420px; max-height: min(60vh, 420px); padding: .5rem .75rem .5rem; overflow: hidden; }
		.al-filter-popover__title { font-size: .75rem; font-weight: 700; letter-spacing: .06em; text-transform: uppercase; color: var(--mat-sys-on-surface-variant); padding: .25rem .25rem .5rem; flex: none; }
		.al-filter-popover__options { overflow-y: auto; }
		.al-filter-popover__option { display: block; }
		.al-filter-popover__footer { display: flex; justify-content: flex-end; gap: .5rem; padding-top: .5rem; margin-top: .25rem; border-top: 1px solid var(--mat-sys-outline-variant); flex: none; }
	`],
})
export class ListingFilterChipComponent {
	@Input() label = '';
	/** The filter's control on the page's internal filter form; an array for multi-selects, anything for custom content. */
	@Input() control!: FormControl<any>;
	@Input() options: any[] | null = null;
	@Input() labelOf: (value: any) => string = value => String(value ?? '');
	/** Chip text while a custom (non-options) filter is set; falls back to a count for arrays. */
	@Input() summary: string | null = null;
	/** Fired after every change of an options filter; the page re-queries. */
	@Output() changed = new EventEmitter<void>();
	/** Fired by the Apply button of a custom filter. */
	@Output() applied = new EventEmitter<void>();

	protected get values(): any[] { const v = this.control?.value; return Array.isArray(v) ? v : (v == null || v === '' ? [] : [v]); }
	protected get isSet(): boolean { return this.values.length > 0; }
	protected get valueText(): string {
		if (!this.isSet) { return ''; }
		if (this.options) { return this.values.length === 1 ? this.labelOf(this.values[0]) : `${this.values.length}`; }
		return this.summary ?? `${this.values.length}`;
	}
	protected isSelected(option: any): boolean { return this.values.includes(option); }
	protected toggle(option: any, checked: boolean): void {
		const next = checked ? [...this.values.filter(v => v !== option), option] : this.values.filter(v => v !== option);
		this.control.setValue(next.length ? next : null);
		this.control.markAsDirty();
		this.changed.emit();
	}
	protected clear(): void {
		this.control.setValue(Array.isArray(this.control.value) ? null : null);
		this.control.markAsDirty();
		if (this.options) { this.changed.emit(); } else { this.applied.emit(); }
	}
}
