import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatDialog } from '@angular/material/dialog';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { UsageLimitPeriodicityRange } from '@app/core/common/enum/usage-limit-periodicity-range';
import { UsageLimitTargetMetric } from '@app/core/common/enum/usage-limit-target-metric';
import { UsageLimit, UsageLimitDefinition } from '@app/core/model/usage-limit/usage-limit';
import { UsageLimitLookup } from '@app/core/query/usage-limit.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { UsageLimitService } from '@app/core/services/usage-limit/usage.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { nameof } from 'ts-simple-nameof';

type Period = 'total' | 'month' | 'year';

interface Quota {
	key: number;
	id?: Guid;
	hash?: string;
	label: string;
	metric: UsageLimitTargetMetric | null;
	value: number | null;
	period: Period;
	currentValue: number | null;
	shared: boolean;
	dirty: boolean;
}

@Component({
	selector: 'app-quotas',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule],
	templateUrl: './quotas.component.html',
	styleUrl: './quotas.component.scss',
})
export class QuotasComponent implements OnInit {
	private readonly usageLimitService = inject(UsageLimitService);
	private readonly httpErrorHandlingService = inject(HttpErrorHandlingService);
	private readonly uiNotificationService = inject(UiNotificationService);
	private readonly language = inject(TranslateService);
	private readonly dialog = inject(MatDialog);
	private readonly destroyRef = inject(DestroyRef);
	protected readonly enumUtils = inject(EnumUtils);
	private readonly authService = inject(AuthService);

	protected readonly metrics = this.enumUtils.getEnumValues<UsageLimitTargetMetric>(UsageLimitTargetMetric);
	protected readonly canEdit = this.authService.hasPermission(AppPermission.EditUsageLimit);
	protected readonly canDelete = this.authService.hasPermission(AppPermission.DeleteUsageLimit);

	protected readonly quotas = signal<Quota[]>([]);
	protected readonly loading = signal(true);
	protected readonly busyKey = signal<number | null>(null);
	protected readonly usageUnavailable = computed(() => this.quotas().some(x => x.id && x.currentValue == null));
	private nextKey = 0;

	ngOnInit(): void {
		this.load();
	}

	private load(): void {
		const lookup = new UsageLimitLookup();
		lookup.isActive = [IsActive.Active];
		lookup.page = { offset: 0, size: 200 };
		lookup.order = { items: [nameof<UsageLimit>(x => x.createdAt)] };
		lookup.project = {
			fields: [
				nameof<UsageLimit>(x => x.id),
				nameof<UsageLimit>(x => x.label),
				nameof<UsageLimit>(x => x.targetMetric),
				nameof<UsageLimit>(x => x.value),
				[nameof<UsageLimit>(x => x.definition), nameof<UsageLimitDefinition>(x => x.hasPeriodicity)].join('.'),
				[nameof<UsageLimit>(x => x.definition), nameof<UsageLimitDefinition>(x => x.periodicityRange)].join('.'),
				nameof<UsageLimit>(x => x.currentValue),
				nameof<UsageLimit>(x => x.hash),
				nameof<UsageLimit>(x => x.belongsToCurrentTenant),
			]
		};
		this.loading.set(true);
		this.usageLimitService.query(lookup).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: result => {
				this.quotas.set(result.items.map(x => this.toQuota(x)));
				this.loading.set(false);
			},
			error: error => {
				this.loading.set(false);
				this.httpErrorHandlingService.handleBackedRequestError(error);
			},
		});
	}

	private toQuota(item: UsageLimit): Quota {
		const period: Period = !item.definition?.hasPeriodicity ? 'total'
			: item.definition.periodicityRange === UsageLimitPeriodicityRange.Yearly ? 'year' : 'month';
		return {
			key: this.nextKey++,
			id: item.id,
			hash: item.hash,
			label: item.label ?? '',
			metric: item.targetMetric,
			value: item.value,
			period,
			currentValue: item.currentValue ?? null,
			shared: item.belongsToCurrentTenant === false,
			dirty: false,
		};
	}

	protected add(): void {
		this.quotas.update(list => [{ key: this.nextKey++, label: '', metric: null, value: null, period: 'total', currentValue: null, shared: false, dirty: true }, ...list]);
	}

	protected update(quota: Quota, change: Partial<Quota>): void {
		this.quotas.update(list => list.map(x => x.key === quota.key ? { ...x, ...change, dirty: true } : x));
	}

	/** Only the first active quota of a metric is enforced, so a metric is offered to one quota at a time. */
	protected metricTaken(quota: Quota, metric: UsageLimitTargetMetric): boolean {
		return this.quotas().some(x => x.key !== quota.key && x.metric === metric);
	}

	protected valid(quota: Quota): boolean {
		return !!quota.label?.trim() && !!quota.metric && quota.value != null && quota.value >= 1;
	}

	protected percent(quota: Quota): number {
		if (quota.currentValue == null || !quota.value) return 0;
		return Math.min(100, Math.round(quota.currentValue / quota.value * 100));
	}

	protected save(quota: Quota): void {
		if (!this.valid(quota)) return;
		this.busyKey.set(quota.key);
		this.usageLimitService.persist({
			id: quota.id,
			hash: quota.hash,
			label: quota.label.trim(),
			targetMetric: quota.metric,
			value: quota.value,
			definition: {
				hasPeriodicity: quota.period !== 'total',
				periodicityRange: quota.period === 'year' ? UsageLimitPeriodicityRange.Yearly : quota.period === 'month' ? UsageLimitPeriodicityRange.Monthly : null,
			},
		}).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: () => {
				this.busyKey.set(null);
				this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
				this.load();
			},
			error: error => {
				this.busyKey.set(null);
				this.httpErrorHandlingService.handleBackedRequestError(error);
			},
		});
	}

	protected remove(quota: Quota): void {
		if (!quota.id) {
			this.quotas.update(list => list.filter(x => x.key !== quota.key));
			return;
		}
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				isDeleteConfirmation: true,
				message: this.language.instant('QUOTAS.REMOVE-CONFIRM', { name: quota.label }),
				confirmButton: this.language.instant('QUOTAS.REMOVE'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
			},
		}).afterClosed().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(confirmed => {
			if (!confirmed) return;
			this.busyKey.set(quota.key);
			this.usageLimitService.delete(quota.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
				next: () => {
					this.busyKey.set(null);
					this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-DELETE'), SnackBarNotificationLevel.Success);
					this.load();
				},
				error: error => {
					this.busyKey.set(null);
					this.httpErrorHandlingService.handleBackedRequestError(error);
				},
			});
		});
	}
}
