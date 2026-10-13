import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { MaintenanceGroupKey } from '../maintenance-catalog';

export interface MaintenanceRunConfirmData {
	preset: boolean;
	total: number;
	groups: { key: MaintenanceGroupKey, count: number }[];
	pointsWithoutReset: boolean;
}

export interface MaintenanceRunConfirmResult {
	stopOnFailure: boolean;
}

@Component({
	selector: 'app-maintenance-run-confirm-dialog',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, MatDialogModule],
	template: `
		@if (data.preset) {
			<div class="mrc__eyebrow">{{ 'MAINTENANCE-TASKS.PRESET.EYEBROW' | translate }} · {{ 'MAINTENANCE-TASKS.PRESET.TITLE' | translate }}</div>
		}
		<h2 mat-dialog-title>{{ 'MAINTENANCE-TASKS.CONFIRM.TITLE' | translate: { count: data.total } }}</h2>
		<mat-dialog-content>
			<p class="mrc__intro">{{ 'MAINTENANCE-TASKS.CONFIRM.DESCRIPTION' | translate }}</p>
			<ol class="mrc__steps">
				@for (group of data.groups; track group.key; let i = $index) {
					<li class="mrc__step">
						<span class="mrc__step-number" aria-hidden="true">{{ i + 1 }}</span>
						<span class="mrc__step-title">{{ 'MAINTENANCE-TASKS.GROUPS.' + group.key + '.TITLE' | translate }}</span>
						<span class="mrc__step-count">{{ 'MAINTENANCE-TASKS.CONFIRM.TASK-COUNT' | translate: { count: group.count } }}</span>
					</li>
				}
			</ol>
			@if (data.pointsWithoutReset) {
				<p class="mrc__warning" role="note">
					<mat-icon aria-hidden="true">warning</mat-icon>
					<span>{{ 'MAINTENANCE-TASKS.CONFIRM.POINTS-WARNING' | translate }}</span>
				</p>
			}
			<mat-checkbox [checked]="stopOnFailure()" (change)="stopOnFailure.set($event.checked)">
				<span class="mrc__option">{{ 'MAINTENANCE-TASKS.CONFIRM.STOP-ON-FAILURE' | translate }}</span>
				<span class="mrc__option-hint">{{ 'MAINTENANCE-TASKS.CONFIRM.STOP-ON-FAILURE-HINT' | translate }}</span>
			</mat-checkbox>
		</mat-dialog-content>
		<mat-dialog-actions align="end">
			<button mat-button type="button" mat-dialog-close>{{ 'GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL' | translate }}</button>
			<button mat-flat-button color="primary" type="button" (click)="run()">{{ 'MAINTENANCE-TASKS.CONFIRM.RUN' | translate: { count: data.total } }}</button>
		</mat-dialog-actions>
	`,
	styles: [`
		.mrc__eyebrow { padding: 1.5rem 1.5rem 0; font-size: 0.75rem; font-weight: 700; letter-spacing: 0.06em; text-transform: uppercase; color: var(--mat-sys-on-surface-variant); }
		.mrc__intro { margin: 0 0 1rem; color: var(--mat-sys-on-surface-variant); }
		.mrc__steps { list-style: none; margin: 0 0 1rem; padding: 0; border: 1px solid var(--mat-sys-outline-variant); border-radius: 8px; }
		.mrc__step { display: flex; align-items: center; gap: 0.75rem; padding: 0.75rem 0.875rem; }
		.mrc__step + .mrc__step { border-top: 1px solid var(--mat-sys-outline-variant); }
		.mrc__step-number { display: inline-flex; align-items: center; justify-content: center; width: 1.5rem; height: 1.5rem; border-radius: 50%; background: var(--mat-sys-secondary-container); color: var(--mat-sys-on-secondary-container); font-size: 0.8125rem; font-weight: 700; }
		.mrc__step-title { flex-grow: 1; font-weight: 600; }
		.mrc__step-count { color: var(--mat-sys-on-surface-variant); font-size: 0.875rem; }
		.mrc__warning { display: flex; gap: 0.5rem; align-items: flex-start; margin: 0 0 1rem; padding: 0.75rem; border-radius: 8px; background: var(--mat-sys-error-container); color: var(--mat-sys-on-error-container); }
		.mrc__option { display: block; font-weight: 600; }
		.mrc__option-hint { display: block; font-size: 0.875rem; color: var(--mat-sys-on-surface-variant); }
	`]
})
export class MaintenanceRunConfirmDialogComponent {
	protected readonly data = inject<MaintenanceRunConfirmData>(MAT_DIALOG_DATA);
	private readonly dialogRef = inject<MatDialogRef<MaintenanceRunConfirmDialogComponent, MaintenanceRunConfirmResult>>(MatDialogRef);

	protected readonly stopOnFailure = signal(true);

	protected run(): void {
		this.dialogRef.close({ stopOnFailure: this.stopOnFailure() });
	}
}
