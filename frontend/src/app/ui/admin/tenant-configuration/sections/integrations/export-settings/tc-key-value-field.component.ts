import { ChangeDetectionStrategy, Component, computed, input, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormsModule } from '@angular/forms';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { of, startWith, switchMap } from 'rxjs';
import { TcOption } from './tc-ordered-list-field.component';
import { formatKeyValue, parseKeyValue } from './tc-setting-codecs';

/** Replaces built-in texts (the options: key → built-in text) with the organisation's own. */
@Component({
	selector: 'app-tc-key-value-field',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, FormsModule],
	template: `
		<div class="tc-kv__tools">
			<mat-form-field class="tc-kv__search" subscriptSizing="dynamic">
				<mat-label>{{ 'TENANT-CONFIGURATION.EXPORT.SEARCH-TEXTS' | translate }}</mat-label>
				<mat-icon matPrefix aria-hidden="true">search</mat-icon>
				<input matInput type="search" [ngModel]="search()" (ngModelChange)="search.set($event)">
			</mat-form-field>
			<mat-slide-toggle [checked]="onlyChanged()" (change)="onlyChanged.set($event.checked)">{{ 'TENANT-CONFIGURATION.EXPORT.ONLY-CHANGED' | translate }}</mat-slide-toggle>
		</div>
		<div class="tc-kv__scroll">
			<table class="tc-kv">
				<thead>
					<tr>
						<th scope="col">{{ 'TENANT-CONFIGURATION.EXPORT.BUILT-IN-TEXT' | translate }}</th>
						<th scope="col">{{ 'TENANT-CONFIGURATION.EXPORT.YOUR-TEXT' | translate }}</th>
						<th scope="col"><span class="tc-kv__sr">{{ 'TENANT-CONFIGURATION.EXPORT.ACTIONS' | translate }}</span></th>
					</tr>
				</thead>
				<tbody>
					@for (row of rows(); track row.value) {
						@let own = values().get(row.value);
						<tr [class.tc-kv--changed]="own !== undefined">
							<th scope="row">{{ row.label }}<span class="tc-kv__key">{{ row.value }}</span></th>
							<td>
								<input class="tc-kv__input" [value]="own ?? ''" [placeholder]="row.label" [disabled]="readOnly()"
									[attr.aria-label]="'TENANT-CONFIGURATION.EXPORT.YOUR-TEXT-FOR' | translate: { name: row.label }"
									(change)="set(row.value, $any($event.target).value)">
								@if (own === '') { <span class="tc-kv__hidden">{{ 'TENANT-CONFIGURATION.EXPORT.HIDDEN' | translate }}</span> }
							</td>
							<td class="tc-kv__actions">
								@if (!readOnly()) {
									@if (own !== undefined) {
										<button mat-button type="button" (click)="reset(row.value)">{{ 'TENANT-CONFIGURATION.STATE.RESET' | translate }}</button>
									} @else {
										<button mat-button type="button" (click)="hide(row.value)" [matTooltip]="'TENANT-CONFIGURATION.EXPORT.HIDE-HINT' | translate">{{ 'TENANT-CONFIGURATION.EXPORT.HIDE' | translate }}</button>
									}
								}
							</td>
						</tr>
					} @empty {
						<tr><td colspan="3" class="tc-kv__empty">{{ 'TENANT-CONFIGURATION.EXPORT.NO-TEXTS' | translate }}</td></tr>
					}
				</tbody>
			</table>
		</div>
		<p class="tc-kv__count">{{ 'TENANT-CONFIGURATION.EXPORT.TEXTS-COUNT' | translate: { shown: rows().length, total: allRows().length, changed: values().size } }}</p>
	`,
	styles: [`
		:host { display: block; }
		.tc-kv__tools { display: flex; align-items: center; gap: 16px; flex-wrap: wrap; margin-bottom: 10px; }
		.tc-kv__search { flex: 1 1 240px; max-width: 360px; }
		.tc-kv__scroll { max-height: 460px; overflow: auto; border: 1px solid var(--md-sys-color-outline-variant, #e1e5ee); border-radius: 10px; }
		.tc-kv { width: 100%; border-collapse: collapse; font-size: 13px; }
		.tc-kv th, .tc-kv td { padding: 8px 12px; border-top: 1px solid var(--md-sys-color-outline-variant, #eef1f6); text-align: left; vertical-align: middle; }
		.tc-kv thead th { position: sticky; top: 0; z-index: 1; border-top: 0; font-size: 12px; background: var(--md-sys-color-surface-container, #f4f6fa); }
		.tc-kv tbody th { font-weight: 600; width: 38%; }
		.tc-kv__key { display: block; font-size: 11px; font-weight: 400; font-family: monospace; color: var(--md-sys-color-on-surface-variant, #566072); }
		.tc-kv__input { width: 100%; box-sizing: border-box; padding: 7px 9px; border: 1px solid var(--md-sys-color-outline, #8a93a6); border-radius: 6px; font: inherit; background: var(--md-sys-color-surface, #fff); color: inherit; }
		.tc-kv--changed .tc-kv__input { border-color: var(--md-sys-color-primary, #2457c5); }
		.tc-kv__hidden { font-size: 11px; color: var(--md-sys-color-on-surface-variant, #566072); }
		.tc-kv__actions { width: 1%; white-space: nowrap; }
		.tc-kv__empty, .tc-kv__count { color: var(--md-sys-color-on-surface-variant, #566072); }
		.tc-kv__count { margin: 8px 0 0; font-size: 12px; }
		.tc-kv__sr { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0 0 0 0); }
	`],
})
export class TcKeyValueFieldComponent {
	readonly control = input.required<FormControl<string | null>>();
	readonly options = input<TcOption[]>([]);
	readonly readOnly = input(false);

	protected readonly search = signal('');
	protected readonly onlyChanged = signal(false);

	private readonly value = toSignal(toObservable(this.control).pipe(switchMap(c => c ? c.valueChanges.pipe(startWith(c.value)) : of(null))));
	protected readonly values = computed(() => parseKeyValue(this.value()));

	/** Every known text, plus keys already stored that the plugin no longer lists. */
	protected readonly allRows = computed(() => {
		const rows = [...this.options()];
		for (const key of this.values().keys()) if (!rows.some(x => x.value === key)) rows.push({ value: key, label: key });
		return rows;
	});
	protected readonly rows = computed(() => {
		const term = this.search().trim().toLowerCase();
		const values = this.values();
		return this.allRows().filter(x => (!this.onlyChanged() || values.has(x.value))
			&& (!term || x.label.toLowerCase().includes(term) || x.value.toLowerCase().includes(term) || (values.get(x.value) ?? '').toLowerCase().includes(term)));
	});

	/** An emptied field brings the built-in text back; hiding a text is a separate, explicit action. */
	protected set(key: string, text: string): void {
		if (!text.trim()) return this.reset(key);
		const values = new Map(this.values());
		values.set(key, text.trim());
		this.write(values);
	}

	protected hide(key: string): void {
		const values = new Map(this.values());
		values.set(key, '');
		this.write(values);
	}

	protected reset(key: string): void {
		const values = new Map(this.values());
		values.delete(key);
		this.write(values);
	}

	private write(values: Map<string, string>): void {
		const control = this.control();
		control.markAsDirty();
		control.setValue(formatKeyValue(values));
	}
}
