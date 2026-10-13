import { ENTER } from '@angular/cdk/keycodes';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { PrefillingSourceSystemTargetType } from '@app/core/common/enum/prefilling-source-system-target-type';
import { SemanticsService } from '@app/core/services/semantic/semantics.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { CommonUiModule } from '@common/ui/common-ui.module';

export interface Pf2FillRuleValue {
	kind: 'field' | 'fixed';
	code: string;
	fixedValue: string;
	systemFieldTarget: string;
	semanticTarget: string;
}

export interface Pf2FillRuleDialogData {
	value: Pf2FillRuleValue;
	codes: { code: string, sample: string }[];
	canDelete: boolean;
}

/** One fill rule of a prefilling source: what to take, and where it goes in the new asset. */
@Component({
	selector: 'app-pf2-fill-rule-dialog',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, ReactiveFormsModule, MatDialogModule, AutoCompleteModule],
	template: `
		<h2 mat-dialog-title>{{ (data.value ? 'PREFILLING-SOURCE-EDITOR.RULE.EDIT-TITLE' : 'PREFILLING-SOURCE-EDITOR.RULE.ADD-TITLE') | translate }}</h2>
		<mat-dialog-content>
			<p class="pf2-rule__hint">{{ 'PREFILLING-SOURCE-EDITOR.RULE.HINT' | translate }}</p>
			<form [formGroup]="form" id="pf2-rule-form" (ngSubmit)="done()" class="pf2-rule">
				<fieldset>
					<legend>{{ 'PREFILLING-SOURCE-EDITOR.RULE.TAKE' | translate }}</legend>
					<mat-radio-group formControlName="kind" class="pf2-rule__radios">
						<mat-radio-button value="field">{{ 'PREFILLING-SOURCE-EDITOR.RULE.FROM-RECORD' | translate }}</mat-radio-button>
						@if (values().kind === 'field') {
							<div class="pf2-rule__indent pf2-rule__pair">
								<mat-form-field subscriptSizing="dynamic">
									<mat-label>{{ 'PREFILLING-SOURCE-EDITOR.RULE.RECORD-FIELD' | translate }}</mat-label>
									<mat-select formControlName="code">
										@for (c of data.codes; track c.code) { <mat-option [value]="c.code"><span class="pf2-mono">{{ c.code }}</span></mat-option> }
									</mat-select>
									@if (!data.codes.length) { <mat-hint>{{ 'PREFILLING-SOURCE-EDITOR.RULE.NO-FIELDS' | translate }}</mat-hint> }
								</mat-form-field>
								<span class="pf2-rule__sample">@if (sample()) { {{ 'PREFILLING-SOURCE-EDITOR.RULE.LAST-TEST' | translate }} <strong>{{ sample() }}</strong> }</span>
							</div>
						}
						<mat-radio-button value="fixed">{{ 'PREFILLING-SOURCE-EDITOR.RULE.FIXED' | translate }}</mat-radio-button>
						@if (values().kind === 'fixed') {
							<mat-form-field subscriptSizing="dynamic" class="pf2-rule__indent">
								<mat-label>{{ 'PREFILLING-SOURCE-EDITOR.RULE.VALUE' | translate }}</mat-label>
								<input matInput formControlName="fixedValue">
								<mat-hint>{{ 'PREFILLING-SOURCE-EDITOR.RULE.FIXED-HINT' | translate }}</mat-hint>
							</mat-form-field>
						}
					</mat-radio-group>
				</fieldset>
				<fieldset>
					<legend>{{ 'PREFILLING-SOURCE-EDITOR.RULE.PUT-INTO' | translate }}</legend>
					<div class="pf2-rule__pair pf2-rule__pair--wide">
						<mat-form-field subscriptSizing="dynamic">
							<mat-label>{{ 'PREFILLING-SOURCE-EDITOR.RULE.ASSET-FIELD' | translate }}</mat-label>
							<mat-select formControlName="systemFieldTarget">
								<mat-option [value]="null">{{ 'TYPES.PREFILLING-SOURCE-SYSTEM-TARGET-TYPE.NONE' | translate }}</mat-option>
								@for (t of systemTargets; track t) { <mat-option [value]="t">{{ enumUtils.toPrefillingSourceSystemTargetTypeString(t) }}</mat-option> }
							</mat-select>
						</mat-form-field>
						<mat-form-field subscriptSizing="dynamic">
							<mat-label>{{ 'PREFILLING-SOURCE-EDITOR.RULE.SEMANTIC' | translate }}</mat-label>
							<app-single-auto-complete formControlName="semanticTarget" [separatorKeysCodes]="separatorKeysCodes" [configuration]="semanticsService.singleAutocompleteConfiguration" placeholder="{{ 'PREFILLING-SOURCE-EDITOR.RULE.SEMANTIC' | translate }}" />
						</mat-form-field>
					</div>
					<span class="pf2-rule__hint">{{ 'PREFILLING-SOURCE-EDITOR.RULE.TARGET-HINT' | translate }}</span>
					@if (missingTarget()) { <span class="pf2-rule__warn">{{ 'PREFILLING-SOURCE-EDITOR.RULE.NEED-TARGET' | translate }}</span> }
				</fieldset>
			</form>
		</mat-dialog-content>
		<mat-dialog-actions>
			@if (data.canDelete) { <button mat-button type="button" class="pf2-rule__delete" (click)="dialogRef.close('delete')">{{ 'PREFILLING-SOURCE-EDITOR.RULE.DELETE' | translate }}</button> }
			<span class="pf2-rule__spacer"></span>
			<button mat-button type="button" mat-dialog-close>{{ 'PLAN-BLUEPRINT-EDITOR.HEADER.CLOSE' | translate }}</button>
			<button mat-flat-button color="primary" type="submit" form="pf2-rule-form">{{ (data.value ? 'PREFILLING-SOURCE-EDITOR.RULE.DONE' : 'PREFILLING-SOURCE-EDITOR.RULE.ADD') | translate }}</button>
		</mat-dialog-actions>
	`,
	styles: [`
		.pf2-rule { display: flex; flex-direction: column; gap: 1.25rem; }
		.pf2-rule fieldset { border: 0; margin: 0; padding: 0; display: flex; flex-direction: column; gap: .625rem; }
		.pf2-rule fieldset + fieldset { border-top: 1px solid var(--mat-sys-outline-variant); padding-top: 1rem; }
		.pf2-rule legend { font-size: 1rem; font-weight: 700; margin-bottom: .5rem; float: none; width: auto; }
		.pf2-rule fieldset + fieldset legend { padding-top: 1rem; }
		.pf2-rule__radios { display: flex; flex-direction: column; gap: .5rem; }
		.pf2-rule__indent { margin-left: 2.25rem; }
		.pf2-rule__pair { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); gap: .75rem; align-items: center; }
		.pf2-rule__pair--wide { grid-template-columns: minmax(0, 220px) minmax(0, 1fr); }
		.pf2-rule__pair mat-form-field, .pf2-rule__indent { width: calc(100% - 2.25rem); }
		.pf2-rule__pair mat-form-field { width: 100%; }
		.pf2-rule__hint, .pf2-rule__sample { font-size: .85rem; color: var(--mat-sys-on-surface-variant); line-height: 1.45; margin: 0; }
		.pf2-rule__sample strong { color: var(--mat-sys-on-surface); overflow-wrap: anywhere; }
		.pf2-rule__warn { font-size: .85rem; padding: .375rem .625rem; border-radius: 8px; background: var(--mat-sys-tertiary-container); color: var(--mat-sys-on-tertiary-container); }
		.pf2-rule__delete { color: var(--mat-sys-error); }
		.pf2-rule__spacer { flex: 1 1 0; }
		.pf2-mono { font-family: ui-monospace, Menlo, Consolas, monospace; font-size: .85rem; }
		@media (max-width: 640px) { .pf2-rule__pair, .pf2-rule__pair--wide { grid-template-columns: 1fr; } }
	`]
})
export class Pf2FillRuleDialogComponent {
	readonly data = inject<Pf2FillRuleDialogData>(MAT_DIALOG_DATA);
	readonly dialogRef = inject<MatDialogRef<Pf2FillRuleDialogComponent, Pf2FillRuleValue | 'delete'>>(MatDialogRef);
	readonly semanticsService = inject(SemanticsService);
	readonly enumUtils = inject(EnumUtils);
	readonly systemTargets = this.enumUtils.getEnumValues<PrefillingSourceSystemTargetType>(PrefillingSourceSystemTargetType);
	readonly separatorKeysCodes: number[] = [ENTER];

	readonly form = inject(FormBuilder).group({
		kind: [this.data.value?.kind ?? 'field'],
		code: [this.data.value?.code ?? null],
		fixedValue: [this.data.value?.fixedValue ?? null],
		systemFieldTarget: [this.data.value?.systemFieldTarget ?? null],
		semanticTarget: [this.data.value?.semanticTarget ?? null],
	});
	readonly values = toSignal(this.form.valueChanges, { initialValue: this.form.value });
	readonly sample = computed(() => this.data.codes.find(c => c.code === this.values().code)?.sample ?? null);
	private readonly submitted = signal(false);
	readonly missingTarget = computed(() => this.submitted() && !this.values().systemFieldTarget && !this.values().semanticTarget);

	done(): void {
		this.submitted.set(true);
		const v = this.form.getRawValue();
		const takes = v.kind === 'field' ? !!v.code : !!v.fixedValue;
		if (!takes || (!v.systemFieldTarget && !v.semanticTarget)) return;
		this.dialogRef.close({
			kind: v.kind as 'field' | 'fixed',
			code: v.kind === 'field' ? v.code : null,
			fixedValue: v.kind === 'fixed' ? v.fixedValue : null,
			systemFieldTarget: v.systemFieldTarget || null,
			semanticTarget: v.semanticTarget || null
		});
	}
}
