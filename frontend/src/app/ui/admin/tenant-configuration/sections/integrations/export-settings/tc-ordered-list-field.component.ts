import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';
import { ChangeDetectionStrategy, Component, computed, input, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormsModule } from '@angular/forms';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { of, startWith, switchMap } from 'rxjs';
import { OrderedListEntry, formatOrderedList, parseOrderedList } from './tc-setting-codecs';

export interface TcOption { value: string; label: string; }

/**
 * Picks some of a setting's options and orders them, e.g. the rows of a cover facts table. Each row can be renamed.
 * While the setting is empty the plugin default is shown; the first change starts from it.
 */
@Component({
	selector: 'app-tc-ordered-list-field',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, FormsModule, DragDropModule],
	template: `
		<ol class="tc-ol" cdkDropList [cdkDropListDisabled]="readOnly()" (cdkDropListDropped)="drop($event)" [attr.aria-label]="label()">
			@for (entry of entries(); track entry.value; let i = $index; let first = $first; let last = $last) {
				<li class="tc-ol__row" cdkDrag>
					@if (!readOnly()) { <mat-icon class="tc-ol__handle" cdkDragHandle aria-hidden="true">drag_indicator</mat-icon> }
					<span class="tc-ol__pos" aria-hidden="true">{{ i + 1 }}</span>
					<span class="tc-ol__main">
						@if (editing() === entry.value) {
							<mat-form-field class="tc-ol__rename" subscriptSizing="dynamic">
								<mat-label>{{ 'TENANT-CONFIGURATION.EXPORT.ROW-LABEL' | translate: { name: optionLabel(entry.value) } }}</mat-label>
								<input matInput [ngModel]="entry.label ?? ''" (ngModelChange)="rename(i, $event)" (keydown.enter)="editing.set(null)" [placeholder]="optionLabel(entry.value)" cdkFocusInitial>
							</mat-form-field>
						} @else {
							<span class="tc-ol__label">{{ entry.label || optionLabel(entry.value) }}</span>
							@if (entry.label) {
								<span class="tc-ol__hint">{{ 'TENANT-CONFIGURATION.EXPORT.RENAMED-FROM' | translate: { name: optionLabel(entry.value) } }}</span>
							}
						}
					</span>
					@if (!readOnly()) {
						<button mat-icon-button type="button" (click)="editing.set(editing() === entry.value ? null : entry.value)" [attr.aria-label]="'TENANT-CONFIGURATION.EXPORT.RENAME' | translate: { name: optionLabel(entry.value) }" [attr.aria-pressed]="editing() === entry.value"><mat-icon aria-hidden="true">edit</mat-icon></button>
						<button mat-icon-button type="button" [disabled]="first" (click)="move(i, -1)" [attr.aria-label]="'TENANT-CONFIGURATION.EXPORT.MOVE-UP' | translate: { name: optionLabel(entry.value) }"><mat-icon aria-hidden="true">arrow_upward</mat-icon></button>
						<button mat-icon-button type="button" [disabled]="last" (click)="move(i, 1)" [attr.aria-label]="'TENANT-CONFIGURATION.EXPORT.MOVE-DOWN' | translate: { name: optionLabel(entry.value) }"><mat-icon aria-hidden="true">arrow_downward</mat-icon></button>
						<button mat-icon-button type="button" (click)="remove(i)" [attr.aria-label]="'TENANT-CONFIGURATION.EXPORT.REMOVE' | translate: { name: optionLabel(entry.value) }"><mat-icon aria-hidden="true">close</mat-icon></button>
					}
				</li>
			} @empty {
				<li class="tc-ol__empty">{{ 'TENANT-CONFIGURATION.EXPORT.NO-ROWS' | translate }}</li>
			}
		</ol>
		@if (!readOnly() && remaining().length > 0) {
			<mat-form-field class="tc-ol__add" subscriptSizing="dynamic">
				<mat-label>{{ 'TENANT-CONFIGURATION.EXPORT.ADD-ROW' | translate }}</mat-label>
				<mat-select [value]="null" (selectionChange)="add($event.value)">
					@for (option of remaining(); track option.value) {
						<mat-option [value]="option.value">{{ option.label }}</mat-option>
					}
				</mat-select>
			</mat-form-field>
		}
		<p class="tc-ol__sr" aria-live="polite">{{ announcement() }}</p>
	`,
	styles: [`
		:host { display: block; }
		.tc-ol { margin: 0; padding: 0; list-style: none; border: 1px solid var(--md-sys-color-outline-variant, #e1e5ee); border-radius: 10px; }
		.tc-ol__row { display: flex; align-items: center; gap: 8px; min-height: 48px; padding: 2px 6px 2px 10px; border-top: 1px solid var(--md-sys-color-outline-variant, #eef1f6); background: var(--md-sys-color-surface, #fff); }
		.tc-ol__row:first-child { border-top: 0; border-radius: 10px 10px 0 0; }
		.tc-ol__row:last-child { border-radius: 0 0 10px 10px; }
		.tc-ol__handle { cursor: grab; color: var(--md-sys-color-on-surface-variant, #566072); }
		.tc-ol__pos { width: 20px; font-size: 12px; font-weight: 700; color: var(--md-sys-color-on-surface-variant, #566072); }
		.tc-ol__main { flex: 1; min-width: 0; display: flex; flex-direction: column; }
		.tc-ol__label { font-weight: 600; }
		.tc-ol__hint { font-size: 12px; color: var(--md-sys-color-on-surface-variant, #566072); }
		.tc-ol__rename { width: 100%; }
		.tc-ol__empty { padding: 12px; font-size: 13px; color: var(--md-sys-color-on-surface-variant, #566072); }
		.tc-ol__add { margin-top: 10px; min-width: 260px; }
		.tc-ol__sr { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0 0 0 0); }
		.cdk-drag-preview { box-shadow: 0 6px 18px rgb(0 0 0 / 18%); }
	`],
})
export class TcOrderedListFieldComponent {
	readonly control = input.required<FormControl<string | null>>();
	readonly options = input<TcOption[]>([]);
	readonly defaultValue = input<string | null>(null);
	readonly readOnly = input(false);
	readonly label = input('');

	protected readonly editing = signal<string | null>(null);
	protected readonly announcement = signal('');

	private readonly value = toSignal(toObservable(this.control).pipe(switchMap(c => c ? c.valueChanges.pipe(startWith(c.value)) : of(null))));

	/** The rows in effect: the setting's own, or the plugin default while it has none. */
	protected readonly entries = computed(() => {
		const own = parseOrderedList(this.value());
		return own.length > 0 ? own : parseOrderedList(this.defaultValue());
	});
	protected readonly remaining = computed(() => {
		const taken = new Set(this.entries().map(x => x.value.toLowerCase()));
		return this.options().filter(x => !taken.has(x.value.toLowerCase()));
	});

	protected optionLabel(value: string): string {
		return this.options().find(x => x.value.toLowerCase() === value.toLowerCase())?.label ?? value;
	}

	protected drop(event: CdkDragDrop<unknown>): void {
		if (event.previousIndex === event.currentIndex) return;
		const entries = [...this.entries()];
		moveItemInArray(entries, event.previousIndex, event.currentIndex);
		this.write(entries);
	}

	protected move(index: number, delta: number): void {
		const entries = [...this.entries()];
		moveItemInArray(entries, index, index + delta);
		this.write(entries);
		this.announcement.set(`${this.optionLabel(entries[index + delta].value)}: ${index + delta + 1}`);
	}

	protected remove(index: number): void {
		const entries = [...this.entries()];
		const [removed] = entries.splice(index, 1);
		this.write(entries);
		this.announcement.set(this.optionLabel(removed.value));
	}

	protected add(value: string | null): void {
		if (!value) return;
		this.write([...this.entries(), { value, label: null }]);
	}

	protected rename(index: number, label: string): void {
		const entries = this.entries().map((x, i) => i === index ? { ...x, label: label || null } : x);
		this.write(entries);
	}

	private write(entries: OrderedListEntry[]): void {
		const control = this.control();
		control.markAsDirty();
		// plugins read an empty value as "use the default", so removing every row brings the default rows back
		control.setValue(formatOrderedList(entries));
	}
}
