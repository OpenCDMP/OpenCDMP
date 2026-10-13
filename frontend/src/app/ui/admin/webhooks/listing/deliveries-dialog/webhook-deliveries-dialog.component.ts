import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { WebhookDeliveryStatus } from '@app/core/common/enum/webhook-delivery-status.enum';
import { WebhookDelivery } from '@app/core/model/webhook/webhook';
import { WebhookDeliveryLookup } from '@app/core/query/webhook-delivery.lookup';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { WebhookService } from '@app/core/services/webhook/webhook.service';
import { Guid } from '@common/types/guid';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { nameof } from 'ts-simple-nameof';

export interface WebhookDeliveriesDialogData {
	subscriptionId: Guid;
	subscriptionLabel: string;
}

@Component({
	selector: 'app-webhook-deliveries-dialog',
	standalone: true,
	imports: [DatePipe, MatDialogModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule, MatTooltipModule, TranslateModule],
	templateUrl: './webhook-deliveries-dialog.component.html',
	styleUrl: './webhook-deliveries-dialog.component.scss',
	changeDetection: ChangeDetectionStrategy.OnPush
})
export class WebhookDeliveriesDialogComponent {

	private readonly dialogRef = inject(MatDialogRef<WebhookDeliveriesDialogComponent>);
	private readonly destroyRef = inject(DestroyRef);
	private readonly webhookService = inject(WebhookService);
	private readonly uiNotificationService = inject(UiNotificationService);
	private readonly language = inject(TranslateService);
	protected readonly enumUtils = inject(EnumUtils);
	protected readonly data = inject<WebhookDeliveriesDialogData>(MAT_DIALOG_DATA);

	protected readonly deliveryStatus = WebhookDeliveryStatus;
	protected readonly pageSize = 50;

	protected readonly loading = signal(true);
	protected readonly deliveries = signal<WebhookDelivery[]>([]);
	protected readonly totalCount = signal(0);

	constructor() {
		this.load();
	}

	protected load(): void {
		this.loading.set(true);
		this.webhookService.queryDeliveries(this.buildLookup())
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe({
				next: (result) => {
					this.deliveries.set(result?.items ?? []);
					this.totalCount.set(result?.count ?? 0);
					this.loading.set(false);
				},
				error: () => this.loading.set(false)
			});
	}

	private buildLookup(): WebhookDeliveryLookup {
		const lookup = new WebhookDeliveryLookup();
		lookup.subscriptionIds = [this.data.subscriptionId];
		lookup.isActive = [IsActive.Active];
		lookup.metadata = { countAll: true };
		lookup.page = { offset: 0, size: this.pageSize };
		lookup.order = { items: ['-' + nameof<WebhookDelivery>(x => x.createdAt)] };
		lookup.project = {
			fields: [
				nameof<WebhookDelivery>(x => x.id),
				nameof<WebhookDelivery>(x => x.eventType),
				nameof<WebhookDelivery>(x => x.status),
				nameof<WebhookDelivery>(x => x.attempts),
				nameof<WebhookDelivery>(x => x.lastHttpStatus),
				nameof<WebhookDelivery>(x => x.lastError),
				nameof<WebhookDelivery>(x => x.nextRetryAt),
				nameof<WebhookDelivery>(x => x.deliveredAt),
				nameof<WebhookDelivery>(x => x.createdAt)
			]
		};
		return lookup;
	}

	protected canRedeliver(delivery: WebhookDelivery): boolean {
		return delivery?.status === WebhookDeliveryStatus.Failed || delivery?.status === WebhookDeliveryStatus.Delivered;
	}

	protected redeliver(delivery: WebhookDelivery): void {
		this.webhookService.redeliver(delivery.id)
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe({
				next: () => {
					this.uiNotificationService.snackBarNotification(this.language.instant('WEBHOOK-LISTING.DELIVERIES-DIALOG.REDELIVER-QUEUED'), SnackBarNotificationLevel.Success);
					this.load();
				}
			});
	}

	protected close(): void {
		this.dialogRef.close();
	}
}
