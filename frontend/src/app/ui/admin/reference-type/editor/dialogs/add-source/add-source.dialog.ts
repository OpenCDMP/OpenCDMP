import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { ExternalFetcherSourceType } from '@app/core/common/enum/external-fetcher-source-type';
import { CommonUiModule } from '@common/ui/common-ui.module';

export interface Rt2AddSourceDialogData {
	typeName: string;
	existingKeys: string[];
}

export type Rt2AddSourceResult = { type: ExternalFetcherSourceType, label: string, key: string, url?: string } | 'import' | undefined;

type Rt2SourceKind = 'api' | 'static' | 'import';

/** Picks how a reference type's entries are found, then names the new source. */
@Component({
	selector: 'app-rt2-add-source-dialog',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, ReactiveFormsModule, MatDialogModule],
	template: `
		<h2 mat-dialog-title>{{ 'REFERENCE-TYPE-EDITOR.ADD-SOURCE.TITLE' | translate }}</h2>
		<mat-dialog-content>
			<p class="rt2-add__hint">{{ 'REFERENCE-TYPE-EDITOR.ADD-SOURCE.HINT' | translate: { type: data.typeName } }}</p>
			<div class="rt2-add__kinds" role="radiogroup" [attr.aria-label]="'REFERENCE-TYPE-EDITOR.ADD-SOURCE.TITLE' | translate">
				@for (k of kinds; track k.kind) {
					<button type="button" role="radio" class="rt2-add__kind" [class.rt2-add__kind--on]="kind() === k.kind" [attr.aria-checked]="kind() === k.kind" (click)="kind.set(k.kind)">
						<mat-icon aria-hidden="true">{{ k.icon }}</mat-icon>
						<strong>{{ k.title | translate }}</strong>
						<span>{{ k.hint | translate }}</span>
					</button>
				}
			</div>
			@if (kind() === 'import') {
				<p class="rt2-add__warn">{{ 'REFERENCE-TYPE-EDITOR.ADD-SOURCE.IMPORT-WARNING' | translate }}</p>
			} @else {
				<form [formGroup]="form" id="rt2-add-source-form" (ngSubmit)="add()" class="rt2-add__form">
					<mat-form-field>
						<mat-label>{{ 'REFERENCE-TYPE-EDITOR.SOURCE.SHOWN-AS' | translate }}</mat-label>
						<input matInput formControlName="label" (input)="suggestKey()" required>
						@if (form.controls.label.hasError('required')) { <mat-error>{{ 'GENERAL.VALIDATION.REQUIRED' | translate }}</mat-error> }
					</mat-form-field>
					<mat-form-field>
						<mat-label>{{ 'REFERENCE-TYPE-EDITOR.SOURCE.KEY' | translate }}</mat-label>
						<input matInput formControlName="key" (input)="keyTouched = true" required>
						<mat-hint>{{ 'REFERENCE-TYPE-EDITOR.SOURCE.KEY-HINT' | translate }}</mat-hint>
						@if (form.controls.key.hasError('required')) { <mat-error>{{ 'GENERAL.VALIDATION.REQUIRED' | translate }}</mat-error> }
						@if (form.controls.key.hasError('taken')) { <mat-error>{{ 'REFERENCE-TYPE-EDITOR.SOURCE.KEY-TAKEN' | translate }}</mat-error> }
					</mat-form-field>
					@if (kind() === 'api') {
						<mat-form-field class="rt2-add__full">
							<mat-label>{{ 'REFERENCE-TYPE-EDITOR.REQUEST.URL' | translate }}</mat-label>
							<input matInput formControlName="url" placeholder="https://api.example.org/search?q={like}">
							<mat-hint>{{ 'REFERENCE-TYPE-EDITOR.ADD-SOURCE.URL-HINT' | translate }}</mat-hint>
						</mat-form-field>
					}
				</form>
			}
		</mat-dialog-content>
		<mat-dialog-actions align="end">
			<button mat-button type="button" mat-dialog-close>{{ 'REFERENCE-TYPE-EDITOR.ACTIONS.CANCEL' | translate }}</button>
			@if (kind() === 'import') {
				<button mat-flat-button color="primary" type="button" (click)="dialogRef.close('import')">{{ 'REFERENCE-TYPE-EDITOR.ACTIONS.IMPORT-XML' | translate }}</button>
			} @else {
				<button mat-flat-button color="primary" type="submit" form="rt2-add-source-form">{{ 'REFERENCE-TYPE-EDITOR.ACTIONS.ADD-SOURCE' | translate }}</button>
			}
		</mat-dialog-actions>
	`,
	styles: [`
		.rt2-add__hint { margin: 0 0 1rem; color: var(--mat-sys-on-surface-variant); }
		.rt2-add__kinds { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: .75rem; margin-bottom: 1.25rem; }
		.rt2-add__kind { display: flex; flex-direction: column; align-items: flex-start; gap: .375rem; padding: 1rem; border-radius: 12px; border: 1px solid var(--mat-sys-outline-variant); background: var(--mat-sys-surface); font: inherit; color: inherit; text-align: left; cursor: pointer; }
		.rt2-add__kind span { font-size: .85rem; color: var(--mat-sys-on-surface-variant); line-height: 1.4; }
		.rt2-add__kind--on { border: 2px solid var(--mat-sys-primary); padding: calc(1rem - 1px); background: var(--mat-sys-primary-container); color: var(--mat-sys-on-primary-container); }
		.rt2-add__kind--on span { color: inherit; }
		.rt2-add__form { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 220px); gap: 0 1rem; }
		.rt2-add__full { grid-column: 1 / -1; }
		.rt2-add__warn { margin: 0; padding: .75rem 1rem; border-radius: 10px; background: var(--mat-sys-tertiary-container); color: var(--mat-sys-on-tertiary-container); }
		@media (max-width: 600px) { .rt2-add__form { grid-template-columns: 1fr; } }
	`]
})
export class Rt2AddSourceDialogComponent {
	readonly data = inject<Rt2AddSourceDialogData>(MAT_DIALOG_DATA);
	readonly dialogRef = inject<MatDialogRef<Rt2AddSourceDialogComponent, Rt2AddSourceResult>>(MatDialogRef);

	readonly kinds: { kind: Rt2SourceKind, icon: string, title: string, hint: string }[] = [
		{ kind: 'api', icon: 'travel_explore', title: 'REFERENCE-TYPE-EDITOR.ADD-SOURCE.API', hint: 'REFERENCE-TYPE-EDITOR.ADD-SOURCE.API-HINT' },
		{ kind: 'static', icon: 'list', title: 'REFERENCE-TYPE-EDITOR.ADD-SOURCE.STATIC', hint: 'REFERENCE-TYPE-EDITOR.ADD-SOURCE.STATIC-HINT' },
		{ kind: 'import', icon: 'upload', title: 'REFERENCE-TYPE-EDITOR.ADD-SOURCE.IMPORT', hint: 'REFERENCE-TYPE-EDITOR.ADD-SOURCE.IMPORT-HINT' },
	];
	readonly kind = signal<Rt2SourceKind>('api');
	keyTouched = false;

	readonly form = inject(FormBuilder).nonNullable.group({
		label: ['', Validators.required],
		key: ['', [Validators.required, (control: AbstractControl): ValidationErrors | null => this.data.existingKeys.includes(control.value) ? { taken: true } : null]],
		url: ['']
	});

	suggestKey(): void {
		if (this.keyTouched) return;
		this.form.controls.key.setValue(this.form.controls.label.value.trim().toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, ''));
	}

	add(): void {
		this.form.markAllAsTouched();
		if (this.form.invalid) return;
		const value = this.form.getRawValue();
		this.dialogRef.close({
			type: this.kind() === 'static' ? ExternalFetcherSourceType.STATIC : ExternalFetcherSourceType.API,
			label: value.label.trim(),
			key: value.key.trim(),
			url: this.kind() === 'api' ? value.url.trim() || undefined : undefined
		});
	}
}
