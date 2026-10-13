import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { Guid } from '@common/types/guid';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { RT2_CONDITION_PRESETS, Rt2CaseValue, rt2ApplyCase, rt2Matches } from '../search-rules';

export interface Rt2RuleDialogData {
	placeholder: string;
	value: Rt2CaseValue;
	dependencyTypes: ReferenceType[];
	sourceKeysMap: Map<Guid, string[]>;
	canDelete: boolean;
}

const SEPARATORS = [
	{ id: 'none', value: null as string, label: 'REFERENCE-TYPE-EDITOR.RULE.SEP-NONE' },
	{ id: 'space', value: ' ', label: 'REFERENCE-TYPE-EDITOR.RULE.SEP-SPACE' },
	{ id: 'comma', value: ',', label: 'REFERENCE-TYPE-EDITOR.RULE.SEP-COMMA' },
	{ id: 'custom', value: undefined as string, label: 'REFERENCE-TYPE-EDITOR.RULE.SEP-CUSTOM' },
];

/** Edits one search rule: when the search text matches, what replaces the placeholder. */
@Component({
	selector: 'app-rt2-rule-dialog',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, ReactiveFormsModule, MatDialogModule],
	template: `
		<h2 mat-dialog-title>{{ 'REFERENCE-TYPE-EDITOR.RULE.TITLE' | translate: { token: token } }}</h2>
		<mat-dialog-content>
			<form [formGroup]="form" id="rt2-rule-form" (ngSubmit)="done()" class="rt2-rule">
				<section>
					<h3>{{ 'REFERENCE-TYPE-EDITOR.RULE.WHEN' | translate }}</h3>
					<div class="rt2-rule__pair">
						<mat-form-field subscriptSizing="dynamic">
							<mat-label>{{ 'REFERENCE-TYPE-EDITOR.RULE.CONDITION' | translate }}</mat-label>
							<mat-select formControlName="preset" (selectionChange)="applyPreset($event.value)">
								@for (p of presets; track p.id) { <mat-option [value]="p.id">{{ p.label | translate }}</mat-option> }
							</mat-select>
						</mat-form-field>
						<mat-form-field subscriptSizing="dynamic">
							<mat-label>{{ 'REFERENCE-TYPE-EDITOR.RULE.PATTERN' | translate }}</mat-label>
							<input matInput formControlName="likePattern" class="rt2-mono" (input)="form.controls.preset.setValue(presetFor(form.controls.likePattern.value))">
							<mat-hint>{{ 'REFERENCE-TYPE-EDITOR.RULE.PATTERN-HINT' | translate }}</mat-hint>
						</mat-form-field>
					</div>
					<div class="rt2-rule__pair">
						<mat-form-field subscriptSizing="dynamic">
							<mat-label>{{ 'REFERENCE-TYPE-EDITOR.RULE.SPLIT' | translate }}</mat-label>
							<mat-select formControlName="separatorKind">
								@for (s of separators; track s.id) { <mat-option [value]="s.id">{{ s.label | translate }}</mat-option> }
							</mat-select>
						</mat-form-field>
						@if (values().separatorKind === 'custom') {
							<mat-form-field subscriptSizing="dynamic">
								<mat-label>{{ 'REFERENCE-TYPE-EDITOR.RULE.SEPARATOR' | translate }}</mat-label>
								<input matInput formControlName="customSeparator" class="rt2-mono">
							</mat-form-field>
						} @else {
							<span class="rt2-rule__hint">{{ (values().separatorKind === 'none' ? 'REFERENCE-TYPE-EDITOR.RULE.SPLIT-NONE-HINT' : 'REFERENCE-TYPE-EDITOR.RULE.SPLIT-HINT') | translate }}</span>
						}
					</div>
				</section>

				<section>
					<h3>{{ 'REFERENCE-TYPE-EDITOR.RULE.AND-PICKED' | translate }}</h3>
					<div class="rt2-rule__pair">
						<mat-form-field subscriptSizing="dynamic">
							<mat-label>{{ 'REFERENCE-TYPE-EDITOR.RULE.FROM-TYPE' | translate }}</mat-label>
							<mat-select formControlName="referenceTypeId">
								<mat-option [value]="null">{{ 'REFERENCE-TYPE-EDITOR.RULE.NO-CONDITION' | translate }}</mat-option>
								@for (t of data.dependencyTypes; track t.id) { <mat-option [value]="t.id">{{ t.name }}</mat-option> }
							</mat-select>
						</mat-form-field>
						<mat-form-field subscriptSizing="dynamic">
							<mat-label>{{ 'REFERENCE-TYPE-EDITOR.RULE.FROM-SOURCE' | translate }}</mat-label>
							<mat-select formControlName="referenceTypeSourceKey" [required]="!!values().referenceTypeId">
								@for (key of sourceKeys(); track key) { <mat-option [value]="key">{{ key }}</mat-option> }
							</mat-select>
							@if (form.controls.referenceTypeSourceKey.hasError('required')) { <mat-error>{{ 'GENERAL.VALIDATION.REQUIRED' | translate }}</mat-error> }
						</mat-form-field>
					</div>
					<span class="rt2-rule__hint">{{ (data.dependencyTypes.length ? 'REFERENCE-TYPE-EDITOR.RULE.PICKED-HINT' : 'REFERENCE-TYPE-EDITOR.RULE.PICKED-NONE') | translate }}</span>
				</section>

				<section>
					<h3>{{ 'REFERENCE-TYPE-EDITOR.RULE.SEND' | translate: { token: token } }}</h3>
					<mat-form-field subscriptSizing="dynamic" class="rt2-rule__full">
						<mat-label>{{ 'REFERENCE-TYPE-EDITOR.RULE.VALUE' | translate }}</mat-label>
						<input #valueInput matInput formControlName="value" class="rt2-mono" required>
						@if (form.controls.value.hasError('required')) { <mat-error>{{ 'GENERAL.VALIDATION.REQUIRED' | translate }}</mat-error> }
					</mat-form-field>
					@if (unfilledWarning(); as warning) { <span class="rt2-rule__warn">{{ warning | translate }}</span> }
					<div class="rt2-rule__chips">
						<span class="rt2-rule__hint">{{ 'REFERENCE-TYPE-EDITOR.INSERT' | translate }}</span>
						@for (t of valueTokens(); track t) { <button type="button" class="rt2-rule__chip" (click)="insert(t, valueInput)">{{ t }}</button> }
					</div>
				</section>

				@if (!values().referenceTypeId) {
					<div class="rt2-rule__check">
						<mat-form-field subscriptSizing="dynamic">
							<mat-label>{{ 'REFERENCE-TYPE-EDITOR.RULE.CHECK' | translate }}</mat-label>
							<input matInput [value]="check()" (input)="check.set($any($event.target).value)">
						</mat-form-field>
						@if (check()) {
							<div aria-live="polite">
								@if (checkResult() !== null) {
									<span class="rt2-rule__ok">{{ 'REFERENCE-TYPE-EDITOR.RULE.MATCHES' | translate }}</span> <code class="rt2-rule__sent">{{ checkResult() }}</code>
								} @else {
									<span class="rt2-rule__no">{{ 'REFERENCE-TYPE-EDITOR.RULE.NO-MATCH' | translate }}</span>
								}
							</div>
						}
					</div>
				}
			</form>
		</mat-dialog-content>
		<mat-dialog-actions>
			@if (data.canDelete) { <button mat-button type="button" class="rt2-rule__delete" (click)="dialogRef.close('delete')">{{ 'REFERENCE-TYPE-EDITOR.RULE.DELETE' | translate }}</button> }
			<span class="rt2-rule__spacer"></span>
			<button mat-button type="button" mat-dialog-close>{{ 'REFERENCE-TYPE-EDITOR.ACTIONS.CANCEL' | translate }}</button>
			<button mat-flat-button color="primary" type="submit" form="rt2-rule-form">{{ 'REFERENCE-TYPE-EDITOR.RULE.DONE' | translate }}</button>
		</mat-dialog-actions>
	`,
	styles: [`
		.rt2-rule { display: flex; flex-direction: column; gap: 1.25rem; padding-top: .25rem; }
		.rt2-rule section { display: flex; flex-direction: column; gap: .625rem; }
		.rt2-rule h3 { margin: 0; font-size: 1rem; font-weight: 600; }
		.rt2-rule__pair { display: grid; grid-template-columns: minmax(0, 260px) minmax(0, 1fr); gap: .75rem; align-items: start; }
		.rt2-rule__pair mat-form-field, .rt2-rule__full, .rt2-rule__check mat-form-field { width: 100%; }
		.rt2-rule__hint { font-size: .8rem; color: var(--mat-sys-on-surface-variant); align-self: center; line-height: 1.45; }
		.rt2-rule__chips { display: flex; gap: .375rem; align-items: center; flex-wrap: wrap; }
		.rt2-rule__chip { height: 28px; padding: 0 .625rem; border-radius: 999px; border: 1px solid var(--mat-sys-outline-variant); background: var(--mat-sys-surface); font: inherit; font-family: ui-monospace, Menlo, Consolas, monospace; font-size: .8rem; cursor: pointer; }
		.rt2-rule__check { display: grid; grid-template-columns: minmax(0, 240px) minmax(0, 1fr); gap: 1rem; align-items: center; padding: .75rem 1rem; border-radius: 10px; background: var(--mat-sys-primary-container); color: var(--mat-sys-on-primary-container); }
		.rt2-rule__sent { margin-left: .375rem; overflow-wrap: anywhere; color: inherit; font-family: ui-monospace, Menlo, Consolas, monospace; font-size: .85rem; }
		.rt2-rule__warn { font-size: .8rem; padding: .375rem .625rem; border-radius: 8px; background: var(--mat-sys-tertiary-container); color: var(--mat-sys-on-tertiary-container); }
		.rt2-rule__ok { font-weight: 700; }
		.rt2-rule__no { font-weight: 700; color: var(--mat-sys-error); }
		.rt2-rule__delete { color: var(--mat-sys-error); }
		.rt2-rule__spacer { flex: 1 1 0; }
		.rt2-mono { font-family: ui-monospace, Menlo, Consolas, monospace; font-size: .85rem; }
		@media (max-width: 640px) { .rt2-rule__pair, .rt2-rule__check { grid-template-columns: 1fr; } }
	`]
})
export class Rt2RuleDialogComponent {
	readonly data = inject<Rt2RuleDialogData>(MAT_DIALOG_DATA);
	readonly dialogRef = inject<MatDialogRef<Rt2RuleDialogComponent, Rt2CaseValue | 'delete'>>(MatDialogRef);

	readonly presets = RT2_CONDITION_PRESETS;
	readonly separators = SEPARATORS;
	readonly token = `{${this.data.placeholder || '…'}}`;
	readonly check = signal('');

	readonly form = inject(FormBuilder).group({
		preset: [this.presetFor(this.data.value?.likePattern)],
		likePattern: [this.data.value?.likePattern ?? ''],
		separatorKind: [this.separatorKindFor(this.data.value?.separator)],
		customSeparator: [this.separatorKindFor(this.data.value?.separator) === 'custom' ? this.data.value.separator : ''],
		referenceTypeId: [this.data.value?.referenceTypeId as Guid ?? null],
		referenceTypeSourceKey: [this.data.value?.referenceTypeSourceKey ?? null],
		value: [this.data.value?.value ?? '', Validators.required]
	});
	readonly values = toSignal(this.form.valueChanges, { initialValue: this.form.value });

	readonly sourceKeys = computed(() => {
		const id = this.values().referenceTypeId;
		return id ? [...this.data.sourceKeysMap.entries()].find(([k]) => k?.toString() === id?.toString())?.[1] ?? [] : [];
	});

	readonly valueTokens = computed(() => {
		const v = this.values();
		if (v.referenceTypeId) return ['{reference}', '{label}', '{source}'];
		return v.separatorKind === 'none' ? ['{like}'] : ['{like1}', '{like2}', '{like3}'];
	});

	/** A value that uses placeholders the split setting never fills would be sent with the braces in it. */
	readonly unfilledWarning = computed(() => {
		const v = this.values();
		if (v.referenceTypeId) return null;
		const split = v.separatorKind !== 'none';
		if (!split && /\{like\d+\}/.test(v.value ?? '')) return 'REFERENCE-TYPE-EDITOR.RULE.WARN-NOT-SPLIT';
		if (split && (v.value ?? '').includes('{like}')) return 'REFERENCE-TYPE-EDITOR.RULE.WARN-SPLIT';
		return null;
	});

	readonly checkResult = computed(() => {
		const like = this.check();
		const rule = this.ruleValue();
		return rt2Matches(like, rule.likePattern) ? rt2ApplyCase(rule, like, '') : null;
	});

	presetFor(pattern: string): string {
		return this.presets.find(p => p.pattern === (pattern ?? ''))?.id ?? 'custom';
	}

	private separatorKindFor(separator: string): string {
		if (separator == null || separator === '') return 'none';
		return SEPARATORS.find(s => s.value === separator)?.id ?? 'custom';
	}

	applyPreset(id: string): void {
		const preset = this.presets.find(p => p.id === id);
		if (preset?.pattern != null) this.form.controls.likePattern.setValue(preset.pattern);
		if (id === 'two-words' && this.form.controls.separatorKind.value === 'none') this.form.controls.separatorKind.setValue('space');
	}

	insert(token: string, input: HTMLInputElement): void {
		const control = this.form.controls.value;
		const value = control.value ?? '';
		const start = input.selectionStart ?? value.length;
		control.setValue(value.slice(0, start) + token + value.slice(input.selectionEnd ?? value.length));
		setTimeout(() => { input.focus(); input.setSelectionRange(start + token.length, start + token.length); });
	}

	private ruleValue(): Rt2CaseValue {
		const v = this.values();
		const separator = v.separatorKind === 'custom' ? (v.customSeparator || null) : SEPARATORS.find(s => s.id === v.separatorKind)?.value ?? null;
		return {
			likePattern: v.likePattern || null,
			separator,
			value: v.value,
			referenceTypeId: v.referenceTypeId || null,
			referenceTypeSourceKey: v.referenceTypeId ? v.referenceTypeSourceKey : null
		};
	}

	done(): void {
		this.form.markAllAsTouched();
		if (this.form.invalid || (this.values().referenceTypeId && !this.values().referenceTypeSourceKey)) return;
		this.dialogRef.close(this.ruleValue());
	}
}
