import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, ElementRef, Injector, afterNextRender, computed, inject, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { MatDialog } from '@angular/material/dialog';
import { MaintenanceRunStatus } from '@app/core/common/enum/maintenance-run-status';
import { MaintenanceTaskRunStatus } from '@app/core/common/enum/maintenance-task-run-status';
import { DatabaseMigrationInfo, InstallationInfo, MaintenanceRun, MaintenanceTaskRun } from '@app/core/model/maintenance/maintenance-run';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { MaintenanceService } from '@app/core/services/maintenance/maintenance.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { TranslateService } from '@ngx-translate/core';
import { Subscription, filter, switchMap, timer } from 'rxjs';
import { AFTER_UPGRADE_PRESET, INDICATOR_RESET_TASK, MAINTENANCE_GROUPS, MaintenanceGroup, isIndicatorPointTask, taskLabelKey } from './maintenance-catalog';
import { MaintenanceRunConfirmData, MaintenanceRunConfirmDialogComponent, MaintenanceRunConfirmResult } from './run-confirm-dialog/maintenance-run-confirm.dialog';
import { MigrationHistoryDialogComponent } from './migration-history-dialog/migration-history.dialog';

const POLL_MILLISECONDS = 2000;

interface RunPanelGroup {
	group: MaintenanceGroup;
	tasks: MaintenanceTaskRun[];
	finished: number;
}

@Component({
	selector: 'app-maintenance-page',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule],
	templateUrl: './maintenance-page.component.html',
	styleUrls: ['./maintenance-page.component.scss']
})
export class MaintenancePageComponent {
	private readonly maintenanceService = inject(MaintenanceService);
	private readonly configurationService = inject(ConfigurationService);
	private readonly dialog = inject(MatDialog);
	private readonly uiNotificationService = inject(UiNotificationService);
	private readonly httpErrorHandlingService = inject(HttpErrorHandlingService);
	private readonly translate = inject(TranslateService);
	private readonly destroyRef = inject(DestroyRef);
	private readonly injector = inject(Injector);
	private readonly runPanel = viewChild<ElementRef<HTMLElement>>('runPanel');

	protected readonly taskLabelKey = taskLabelKey;
	protected readonly taskStatus = MaintenanceTaskRunStatus;
	protected readonly runStatus = MaintenanceRunStatus;
	protected readonly afterUpgradePreset = AFTER_UPGRADE_PRESET;

	protected readonly groups: MaintenanceGroup[] = MAINTENANCE_GROUPS.filter(group =>
		(group.key !== 'ACCOUNTING' || this.configurationService.accountingServiceEnabled === true) &&
		(group.key !== 'KPI' || this.configurationService.kpi?.enabled === true));
	private readonly allTasks: string[] = this.groups.flatMap(group => group.tasks);

	protected readonly migrations = signal<DatabaseMigrationInfo | null>(null);
	protected readonly installation = signal<InstallationInfo | null>(null);
	protected readonly lastRuns = signal<Map<string, MaintenanceTaskRun>>(new Map());
	protected readonly run = signal<MaintenanceRun | null>(null);
	protected readonly runPanelVisible = signal(false);
	protected readonly selected = signal<Set<string>>(new Set());
	protected readonly busy = signal(false);

	protected readonly allTaskCount = this.allTasks.length;
	protected readonly selectedCount = computed(() => this.selected().size);
	protected readonly running = computed(() => this.run()?.status === MaintenanceRunStatus.Running);
	protected readonly runFinishedCount = computed(() => (this.run()?.tasks ?? []).filter(task => this.isFinished(task)).length);
	protected readonly runPanelGroups = computed<RunPanelGroup[]>(() => {
		const tasks = this.run()?.tasks ?? [];
		return MAINTENANCE_GROUPS
			.map(group => {
				const groupTasks = tasks.filter(task => group.tasks.includes(task.task));
				return { group, tasks: groupTasks, finished: groupTasks.filter(task => this.isFinished(task)).length };
			})
			.filter(entry => entry.tasks.length > 0);
	});
	protected readonly runFailedCount = computed(() => (this.run()?.tasks ?? []).filter(task => task.status === MaintenanceTaskRunStatus.Failed).length);

	private polling: Subscription | null = null;

	constructor() {
		this.maintenanceService.databaseMigrations().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: info => this.migrations.set(info),
			error: error => this.onError(error)
		});
		this.maintenanceService.installation().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: info => this.installation.set(info),
			error: error => this.onError(error)
		});
		this.loadLastRuns();
		this.maintenanceService.latestRun().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: run => {
				this.run.set(run);
				if (run?.status === MaintenanceRunStatus.Running) {
					this.runPanelVisible.set(true);
					this.poll();
				}
			},
			error: error => this.onError(error)
		});
	}

	protected isSelected(task: string): boolean {
		return this.selected().has(task);
	}

	protected toggle(task: string): void {
		const next = new Set(this.selected());
		if (next.has(task)) next.delete(task); else next.add(task);
		this.selected.set(next);
	}

	protected allSelected(group: MaintenanceGroup): boolean {
		return group.tasks.every(task => this.selected().has(task));
	}

	protected toggleGroup(group: MaintenanceGroup): void {
		const next = new Set(this.selected());
		const select = !this.allSelected(group);
		group.tasks.forEach(task => select ? next.add(task) : next.delete(task));
		this.selected.set(next);
	}

	protected clearSelection(): void {
		this.selected.set(new Set());
	}

	protected runPreset(): void {
		this.confirmAndStart(this.allTasks, AFTER_UPGRADE_PRESET);
	}

	protected runGroup(group: MaintenanceGroup): void {
		this.confirmAndStart(group.tasks, null);
	}

	protected runSelected(): void {
		this.confirmAndStart(this.allTasks.filter(task => this.selected().has(task)), null);
	}

	protected stop(): void {
		const run = this.run();
		if (!run) return;
		this.maintenanceService.stopRun(run.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: updated => this.run.set(updated),
			error: error => this.onError(error)
		});
	}

	protected hideRunPanel(): void {
		this.runPanelVisible.set(false);
	}

	protected showRunPanel(): void {
		this.runPanelVisible.set(true);
		afterNextRender(() => this.runPanel()?.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'start' }), { injector: this.injector });
	}

	protected openHistory(): void {
		const info = this.migrations();
		if (!info) return;
		this.dialog.open(MigrationHistoryDialogComponent, { data: info, width: '64rem', maxWidth: '95vw' });
	}

	protected clearIndexes(): void {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.translate.instant('MAINTENANCE-TASKS.CLEAR-INDEX.CONFIRM'),
				confirmButton: this.translate.instant('MAINTENANCE-TASKS.CLEAR-INDEX.ACTION'),
				cancelButton: this.translate.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
				isDeleteConfirmation: true
			},
			maxWidth: '30em'
		}).afterClosed().pipe(filter(Boolean), switchMap(() => this.maintenanceService.clearElasticIndex()), takeUntilDestroyed(this.destroyRef)).subscribe({
			next: () => this.uiNotificationService.snackBarNotification(this.translate.instant('MAINTENANCE-TASKS.CLEAR-INDEX.SUCCESS'), SnackBarNotificationLevel.Success),
			error: error => this.onError(error)
		});
	}

	protected setShareUsage(change: MatCheckboxChange): void {
		this.maintenanceService.persistInstallation({ shareUsage: change.checked }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: info => {
				this.installation.set(info);
				this.uiNotificationService.snackBarNotification(this.translate.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
			},
			error: error => {
				change.source.checked = !change.checked;
				this.onError(error);
			}
		});
	}

	protected lastRun(task: string): MaintenanceTaskRun | undefined {
		return this.lastRuns().get(task);
	}

	private confirmAndStart(tasks: string[], preset: string | null): void {
		if (tasks.length === 0 || this.running()) return;
		const data: MaintenanceRunConfirmData = {
			preset: preset === AFTER_UPGRADE_PRESET,
			total: tasks.length,
			groups: this.groups
				.map(group => ({ key: group.key, count: group.tasks.filter(task => tasks.includes(task)).length }))
				.filter(group => group.count > 0),
			pointsWithoutReset: tasks.some(isIndicatorPointTask) && !tasks.includes(INDICATOR_RESET_TASK)
		};
		this.dialog.open<MaintenanceRunConfirmDialogComponent, MaintenanceRunConfirmData, MaintenanceRunConfirmResult>(MaintenanceRunConfirmDialogComponent, { data, width: '34rem', maxWidth: '95vw' })
			.afterClosed()
			.pipe(
				filter(result => !!result),
				switchMap(result => {
					this.busy.set(true);
					return this.maintenanceService.startRun({ tasks, stopOnFailure: result.stopOnFailure, preset });
				}),
				takeUntilDestroyed(this.destroyRef))
			.subscribe({
				next: run => {
					this.busy.set(false);
					this.run.set(run);
					this.showRunPanel();
					this.clearSelection();
					this.poll();
				},
				error: error => {
					this.busy.set(false);
					this.onError(error);
				}
			});
	}

	private poll(): void {
		this.polling?.unsubscribe();
		this.polling = timer(POLL_MILLISECONDS, POLL_MILLISECONDS)
			.pipe(switchMap(() => this.maintenanceService.latestRun()), takeUntilDestroyed(this.destroyRef))
			.subscribe({
				next: run => {
					this.run.set(run);
					if (run?.status !== MaintenanceRunStatus.Running) {
						this.polling?.unsubscribe();
						this.loadLastRuns();
					}
				},
				error: error => {
					this.polling?.unsubscribe();
					this.onError(error);
				}
			});
	}

	private loadLastRuns(): void {
		this.maintenanceService.lastTaskRuns().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: runs => this.lastRuns.set(new Map(runs.map(run => [run.task, run]))),
			error: error => this.onError(error)
		});
	}

	private isFinished(task: MaintenanceTaskRun): boolean {
		return task.status === MaintenanceTaskRunStatus.Succeeded || task.status === MaintenanceTaskRunStatus.Failed || task.status === MaintenanceTaskRunStatus.Skipped;
	}

	private onError(error: HttpErrorResponse): void {
		this.httpErrorHandlingService.handleBackedRequestError(error);
	}
}
