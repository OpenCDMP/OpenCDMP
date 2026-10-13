import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { FileTransformerService } from '@app/core/services/file-transformer/file-transformer.service';
import { PluginConnectionTestResult, PluginIntegrationService } from '@app/core/services/plugin-integration/plugin-integration.service';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { startWith } from 'rxjs';
import { TcConnectionFieldsComponent } from './tc-connection-fields.component';
import { TC_PLUGIN_KINDS, TcPluginKind } from './tc-plugin-sources';

export interface TcAddPluginDialogData {
	idTaken: (kind: TcPluginKind, id: string) => boolean;
}

export interface TcAddPluginDialogResult {
	kind: TcPluginKind;
	values: Record<string, unknown>;
}

const DEFAULT_MAX_IN_MEMORY_SIZE_IN_BYTES = 6554000;

/** Adds one of the tenant's own plugins: its kind, id and connection, which can be tried before adding. */
@Component({
	selector: 'app-tc-add-plugin-dialog',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, ReactiveFormsModule, MatDialogModule, TcConnectionFieldsComponent],
	template: `
		<h2 mat-dialog-title>{{ 'TENANT-CONFIGURATION.INTEGRATIONS.ADD-DIALOG.TITLE' | translate }}</h2>
		<mat-dialog-content>
			<p class="tc-add__hint">{{ 'TENANT-CONFIGURATION.INTEGRATIONS.ADD-DIALOG.HINT' | translate }}</p>
			<form [formGroup]="form" (ngSubmit)="add()" id="tc-add-plugin-form">
				<mat-form-field class="tc-add__full">
					<mat-label>{{ 'TENANT-CONFIGURATION.INTEGRATIONS.ADD-DIALOG.KIND' | translate }}</mat-label>
					<mat-select formControlName="kind">
						@for (kind of kinds; track kind.slug) {
							<mat-option [value]="kind.slug">{{ kind.label | translate }}</mat-option>
						}
					</mat-select>
				</mat-form-field>
				<mat-form-field class="tc-add__full">
					<mat-label>{{ 'TENANT-CONFIGURATION.INTEGRATIONS.CONNECTION.ID' | translate }}</mat-label>
					<input matInput formControlName="id" required>
					<mat-hint>{{ 'TENANT-CONFIGURATION.INTEGRATIONS.CONNECTION.ID-HINT' | translate }}</mat-hint>
					@if (form.controls.id.hasError('required')) { <mat-error>{{ 'GENERAL.VALIDATION.REQUIRED' | translate }}</mat-error> }
					@if (form.controls.id.hasError('taken')) { <mat-error>{{ 'TENANT-CONFIGURATION.INTEGRATIONS.ADD-DIALOG.ID-TAKEN' | translate }}</mat-error> }
				</mat-form-field>

				<app-tc-connection-fields [group]="form.controls.connection" />

				@if (kind()?.slug === 'deposit') {
					<div class="tc-add__pair">
						<mat-form-field>
							<mat-label>{{ 'TENANT-CONFIGURATION.INTEGRATIONS.CONNECTION.PDF-TRANSFORMER' | translate }}</mat-label>
							<mat-select formControlName="pdfTransformerId">
								@for (id of transformerIds(); track id) { <mat-option [value]="id">{{ id }}</mat-option> }
							</mat-select>
							@if (form.controls.pdfTransformerId.hasError('required')) { <mat-error>{{ 'GENERAL.VALIDATION.REQUIRED' | translate }}</mat-error> }
						</mat-form-field>
						<mat-form-field>
							<mat-label>{{ 'TENANT-CONFIGURATION.INTEGRATIONS.CONNECTION.RDA-TRANSFORMER' | translate }}</mat-label>
							<mat-select formControlName="rdaTransformerId">
								@for (id of transformerIds(); track id) { <mat-option [value]="id">{{ id }}</mat-option> }
							</mat-select>
							@if (form.controls.rdaTransformerId.hasError('required')) { <mat-error>{{ 'GENERAL.VALIDATION.REQUIRED' | translate }}</mat-error> }
						</mat-form-field>
					</div>
				}
			</form>

			<div class="tc-add__test" aria-live="polite">
				<button mat-stroked-button type="button" (click)="test()" [disabled]="testing()">
					{{ 'TENANT-CONFIGURATION.INTEGRATIONS.CONNECTION.TEST' | translate }}
				</button>
				@if (testing()) {
					<mat-spinner diameter="18" />
				} @else if (result(); as r) {
					<span [class.tc-add__ok]="r.success" [class.tc-add__fail]="!r.success">
						@if (r.success) {
							{{ 'TENANT-CONFIGURATION.INTEGRATIONS.CONNECTION.TEST-OK' | translate }}
							@if (r.reportedId && r.reportedId !== form.controls.id.value) {
								{{ 'TENANT-CONFIGURATION.INTEGRATIONS.CONNECTION.TEST-OTHER-ID' | translate: { id: r.reportedId } }}
							}
						} @else {
							{{ 'TENANT-CONFIGURATION.INTEGRATIONS.CONNECTION.TEST-FAILED' | translate: { message: r.message } }}
						}
					</span>
				}
			</div>
		</mat-dialog-content>
		<mat-dialog-actions align="end">
			<button mat-button type="button" mat-dialog-close>{{ 'GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL' | translate }}</button>
			<button mat-flat-button color="primary" type="submit" form="tc-add-plugin-form">{{ 'TENANT-CONFIGURATION.INTEGRATIONS.ADD-DIALOG.ADD' | translate }}</button>
		</mat-dialog-actions>
	`,
	styles: [`
		.tc-add__hint { margin: 0 0 16px; font-size: 13px; color: var(--md-sys-color-on-surface-variant, #566072); }
		.tc-add__full { width: 100%; }
		.tc-add__pair { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
		.tc-add__test { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; margin-top: 8px; font-size: 13px; }
		.tc-add__ok { color: var(--tc-status-ok, #1f8a4c); font-weight: 600; }
		.tc-add__fail { color: var(--md-sys-color-error, #b3261e); font-weight: 600; }
		@media (max-width: 560px) { .tc-add__pair { grid-template-columns: 1fr; } }
	`],
})
export class TcAddPluginDialogComponent {
	private readonly data = inject<TcAddPluginDialogData>(MAT_DIALOG_DATA);
	private readonly dialogRef = inject(MatDialogRef<TcAddPluginDialogComponent, TcAddPluginDialogResult>);
	private readonly integrationService = inject(PluginIntegrationService);
	private readonly fileTransformerService = inject(FileTransformerService);
	private readonly destroyRef = inject(DestroyRef);
	private readonly fb = inject(FormBuilder);

	protected readonly kinds = TC_PLUGIN_KINDS;

	protected readonly form = this.fb.group({
		kind: this.fb.nonNullable.control<TcPluginKind['slug']>('file-transformer'),
		id: this.fb.nonNullable.control('', [Validators.required, (control: AbstractControl) => this.idValidator(control)]),
		connection: this.fb.nonNullable.group({
			url: ['', Validators.required],
			issuerUrl: ['', Validators.required],
			clientId: ['', Validators.required],
			clientSecret: ['', Validators.required],
			scope: ['', Validators.required],
		}),
		pdfTransformerId: this.fb.control<string | null>(null),
		rdaTransformerId: this.fb.control<string | null>(null),
	});

	private readonly kindSlug = toSignal(this.form.controls.kind.valueChanges.pipe(startWith(this.form.controls.kind.value)));
	protected readonly kind = computed(() => this.kinds.find(x => x.slug === this.kindSlug()));
	protected readonly transformerIds = computed(() => (this.fileTransformerService.availableFormats() ?? []).map(x => x.fileTransformerId).filter(Boolean));

	protected readonly testing = signal(false);
	protected readonly result = signal<PluginConnectionTestResult | null>(null);

	constructor() {
		this.form.controls.kind.valueChanges.pipe(takeUntilDestroyed()).subscribe(slug => {
			const deposit = slug === 'deposit';
			for (const control of [this.form.controls.pdfTransformerId, this.form.controls.rdaTransformerId]) {
				control.setValidators(deposit ? Validators.required : null);
				control.updateValueAndValidity({ emitEvent: false });
			}
			this.form.controls.id.updateValueAndValidity();
			this.result.set(null);
		});
		this.form.controls.connection.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => this.result.set(null));
	}

	protected test(): void {
		const kind = this.kind();
		const connection = this.form.controls.connection;
		connection.markAllAsTouched();
		if (!kind || connection.invalid) return;
		this.testing.set(true);
		this.integrationService.test({ pluginType: kind.pluginType, id: this.form.controls.id.value, ...connection.getRawValue() })
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe({
				next: result => { this.testing.set(false); this.result.set(result); },
				error: () => { this.testing.set(false); this.result.set({ success: false, reportedId: null, message: null }); },
			});
	}

	protected add(): void {
		this.form.markAllAsTouched();
		const kind = this.kind();
		if (!kind || this.form.invalid) return;
		const value = this.form.getRawValue();
		const values: Record<string, unknown> = { ...value.connection, [kind.idKey]: value.id.trim() };
		if (kind.slug === 'deposit') {
			values['pdfTransformerId'] = value.pdfTransformerId;
			values['rdaTransformerId'] = value.rdaTransformerId;
			values['maxInMemorySizeInBytes'] = DEFAULT_MAX_IN_MEMORY_SIZE_IN_BYTES;
		}
		this.dialogRef.close({ kind, values });
	}

	private idValidator(control: AbstractControl): ValidationErrors | null {
		const kind = this.kinds.find(x => x.slug === this.form?.controls.kind.value);
		const id = (control.value ?? '').trim();
		return kind && id && this.data.idTaken(kind, id) ? { taken: true } : null;
	}
}
