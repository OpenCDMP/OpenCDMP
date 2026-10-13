import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { DatabaseMigrationInfo } from '@app/core/model/maintenance/maintenance-run';
import { CommonUiModule } from '@common/ui/common-ui.module';

@Component({
	selector: 'app-migration-history-dialog',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, MatDialogModule],
	template: `
		<h2 mat-dialog-title>{{ 'MAINTENANCE-TASKS.HISTORY.TITLE' | translate }}</h2>
		<mat-dialog-content>
			<p class="mh__intro">{{ 'MAINTENANCE-TASKS.HISTORY.DESCRIPTION' | translate }}</p>
			<p class="mh__summary">
				{{ 'MAINTENANCE-TASKS.HISTORY.SCHEMA' | translate }} <code>{{ info.currentVersion }}</code>
				· {{ 'MAINTENANCE-TASKS.HISTORY.COUNTS' | translate: { applied: info.applied, pending: info.pending, failed: info.failed } }}
			</p>
			<div class="mh__table-wrap">
				<table class="mh__table">
					<thead>
						<tr>
							<th scope="col">{{ 'MAINTENANCE-TASKS.HISTORY.COLUMNS.VERSION' | translate }}</th>
							<th scope="col">{{ 'MAINTENANCE-TASKS.HISTORY.COLUMNS.DESCRIPTION' | translate }}</th>
							<th scope="col">{{ 'MAINTENANCE-TASKS.HISTORY.COLUMNS.TYPE' | translate }}</th>
							<th scope="col">{{ 'MAINTENANCE-TASKS.HISTORY.COLUMNS.APPLIED' | translate }}</th>
							<th scope="col" class="mh__number">{{ 'MAINTENANCE-TASKS.HISTORY.COLUMNS.TIME' | translate }}</th>
							<th scope="col">{{ 'MAINTENANCE-TASKS.HISTORY.COLUMNS.RESULT' | translate }}</th>
						</tr>
					</thead>
					<tbody>
						@for (migration of info.migrations; track $index) {
							<tr>
								<td><code>{{ migration.version }}</code></td>
								<td>{{ migration.description }}</td>
								<td>{{ migration.type }}</td>
								<td>{{ migration.installedOn | dateTimeFormatter: 'short' }}</td>
								<td class="mh__number">{{ migration.executionTime != null ? migration.executionTime + ' ms' : '' }}</td>
								<td [class.mh__failed]="migration.state === 'FAILED'">{{ migration.state }}</td>
							</tr>
						}
					</tbody>
				</table>
			</div>
		</mat-dialog-content>
		<mat-dialog-actions align="end">
			<button mat-button type="button" mat-dialog-close>{{ 'MAINTENANCE-TASKS.HISTORY.CLOSE' | translate }}</button>
		</mat-dialog-actions>
	`,
	styles: [`
		.mh__intro, .mh__summary { margin: 0 0 0.75rem; color: var(--mat-sys-on-surface-variant); }
		code { color: inherit; }
		.mh__table-wrap { overflow-x: auto; }
		.mh__table { width: 100%; border-collapse: collapse; font-size: 0.875rem; }
		.mh__table th { text-align: left; font-weight: 700; padding: 0.5rem; border-bottom: 1px solid var(--mat-sys-outline-variant); white-space: nowrap; }
		.mh__table td { padding: 0.5rem; border-bottom: 1px solid var(--mat-sys-outline-variant); vertical-align: top; }
		.mh__number { text-align: right; white-space: nowrap; }
		.mh__failed { color: var(--mat-sys-error); font-weight: 700; }
	`]
})
export class MigrationHistoryDialogComponent {
	protected readonly info = inject<DatabaseMigrationInfo>(MAT_DIALOG_DATA);
}
