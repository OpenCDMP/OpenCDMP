import { ChangeDetectionStrategy, Component, computed, inject, input, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { FormControl } from '@angular/forms';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { TranslateService } from '@ngx-translate/core';
import { of, startWith, switchMap } from 'rxjs';
import { formatReferenceTypeFields, parseReferenceTypeFields } from './tc-setting-codecs';

/** Fields the DOCX exporter never prints as details (see its ReferenceFormatter). */
const BOOKKEEPING_FIELDS = new Set(['key', 'referenceType', 'pidTypeField', 'source', 'sourceType']);
/** The reference's identifier (ORCID, DOI...), which every type has whether or not it defines fields. */
const IDENTIFIER = 'pid';

/**
 * Per reference type, which of its details an export shows and in which order. A type left alone shows every
 * detail it has.
 */
@Component({
	selector: 'app-tc-reference-fields-field',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule],
	template: `
		<ul class="tc-rf">
			@for (type of types(); track type.code) {
				@let chosen = values().get(type.code!);
				@let open = expanded() === type.code;
				<li class="tc-rf__type">
					<button type="button" class="tc-rf__head" (click)="expanded.set(open ? null : type.code!)" [attr.aria-expanded]="open">
						<mat-icon aria-hidden="true">{{ open ? 'expand_less' : 'expand_more' }}</mat-icon>
						<span class="tc-rf__name">{{ type.name }}</span>
						<span class="tc-rf__summary">
							@if (chosen) {
								{{ chosen.length === 0 ? ('TENANT-CONFIGURATION.EXPORT.NAME-ONLY' | translate) : summary(type, chosen) }}
							} @else {
								{{ 'TENANT-CONFIGURATION.EXPORT.ALL-DETAILS' | translate }}
							}
						</span>
						@if (chosen) { <span class="tc-rf__tag">{{ 'TENANT-CONFIGURATION.STATE.CHANGED' | translate }}</span> }
					</button>
					@if (open) {
						<div class="tc-rf__body">
							<ol class="tc-rf__fields">
								@for (field of ordered(type, chosen); track field.code; let i = $index; let first = $first; let last = $last) {
									@let on = !chosen || chosen.includes(field.code);
									<li class="tc-rf__field">
										<mat-checkbox [checked]="on" [disabled]="readOnly()" (change)="toggle(type, field.code, $event.checked)">{{ field.label || field.code }}</mat-checkbox>
										@if (!readOnly() && on && chosen) {
											<button mat-icon-button type="button" [disabled]="first" (click)="move(type, field.code, -1)" [attr.aria-label]="'TENANT-CONFIGURATION.EXPORT.MOVE-UP' | translate: { name: field.label || field.code }"><mat-icon aria-hidden="true">arrow_upward</mat-icon></button>
											<button mat-icon-button type="button" [disabled]="i >= chosen.length - 1" (click)="move(type, field.code, 1)" [attr.aria-label]="'TENANT-CONFIGURATION.EXPORT.MOVE-DOWN' | translate: { name: field.label || field.code }"><mat-icon aria-hidden="true">arrow_downward</mat-icon></button>
										}
									</li>
								}
							</ol>
							@if (chosen && !readOnly()) {
								<button mat-button type="button" (click)="reset(type)">{{ 'TENANT-CONFIGURATION.EXPORT.RESET-TYPE' | translate: { name: type.name } }}</button>
							}
						</div>
					}
				</li>
			} @empty {
				<li class="tc-rf__none">{{ 'TENANT-CONFIGURATION.EXPORT.NO-REFERENCE-TYPES' | translate }}</li>
			}
		</ul>
	`,
	styles: [`
		:host { display: block; }
		.tc-rf { margin: 0; padding: 0; list-style: none; border: 1px solid var(--md-sys-color-outline-variant, #e1e5ee); border-radius: 10px; }
		.tc-rf__type { border-top: 1px solid var(--md-sys-color-outline-variant, #eef1f6); }
		.tc-rf__type:first-child { border-top: 0; }
		.tc-rf__head { display: flex; align-items: center; gap: 10px; width: 100%; padding: 12px; border: 0; background: none; color: inherit; font: inherit; text-align: left; cursor: pointer; }
		.tc-rf__name { font-weight: 700; }
		.tc-rf__summary { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 13px; color: var(--md-sys-color-on-surface-variant, #566072); }
		.tc-rf__tag { padding: 1px 8px; border-radius: 999px; font-size: 11px; font-weight: 700; background: var(--md-sys-color-primary-container, #dbe4ff); color: var(--md-sys-color-on-primary-container, #102a6b); }
		.tc-rf__body { padding: 0 12px 12px 46px; }
		.tc-rf__fields { margin: 0 0 6px; padding: 0; list-style: none; }
		.tc-rf__field { display: flex; align-items: center; min-height: 40px; }
		.tc-rf__field mat-checkbox { flex: 1; }
		.tc-rf__none { padding: 10px 12px; font-size: 13px; color: var(--md-sys-color-on-surface-variant, #566072); }
	`],
})
export class TcReferenceFieldsFieldComponent {
	readonly control = input.required<FormControl<string | null>>();
	readonly types = input<ReferenceType[]>([]);
	readonly readOnly = input(false);

	protected readonly expanded = signal<string | null>(null);
	private readonly language = inject(TranslateService);

	private readonly value = toSignal(toObservable(this.control).pipe(switchMap(c => c ? c.valueChanges.pipe(startWith(c.value)) : of(null))));
	protected readonly values = computed(() => parseReferenceTypeFields(this.value()));

	/** The details an export can show for a type: its identifier, then the fields the type defines. */
	protected available(type: ReferenceType): { code: string, label: string }[] {
		const own = (type.definition?.fields ?? []).filter(x => x.code && !BOOKKEEPING_FIELDS.has(x.code)).map(x => ({ code: x.code, label: x.label || x.code }));
		return [{ code: IDENTIFIER, label: this.language.instant('TENANT-CONFIGURATION.EXPORT.IDENTIFIER') }, ...own.filter(x => x.code !== IDENTIFIER)];
	}

	/** Chosen details first, in their order, then the others in the type's own order. */
	protected ordered(type: ReferenceType, chosen: string[] | undefined) {
		const fields = this.available(type);
		if (!chosen) return fields;
		return [...chosen.map(code => fields.find(x => x.code === code) ?? { code, label: code }), ...fields.filter(x => !chosen.includes(x.code))];
	}

	protected summary(type: ReferenceType, chosen: string[]): string {
		const fields = this.available(type);
		return chosen.map(code => fields.find(x => x.code === code)?.label || code).join(', ');
	}

	protected toggle(type: ReferenceType, code: string, on: boolean): void {
		// the first change starts from "every detail"
		const current = this.values().get(type.code!) ?? this.available(type).map(x => x.code);
		const next = on ? [...current.filter(x => x !== code), code] : current.filter(x => x !== code);
		this.update(type, next);
	}

	protected move(type: ReferenceType, code: string, delta: number): void {
		const current = [...(this.values().get(type.code!) ?? [])];
		const index = current.indexOf(code);
		if (index < 0 || index + delta < 0 || index + delta >= current.length) return;
		[current[index], current[index + delta]] = [current[index + delta], current[index]];
		this.update(type, current);
	}

	protected reset(type: ReferenceType): void {
		const values = new Map(this.values());
		values.delete(type.code!);
		this.write(values);
	}

	private update(type: ReferenceType, fields: string[]): void {
		const values = new Map(this.values());
		values.set(type.code!, fields);
		this.write(values);
	}

	private write(values: Map<string, string[]>): void {
		const control = this.control();
		control.markAsDirty();
		control.setValue(formatReferenceTypeFields(values));
	}
}
