import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatDialog } from '@angular/material/dialog';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { NotificationServiceEnumUtils } from '@notification-service/core/formatting/enum-utils.service';
import { Notification } from '@notification-service/core/model/notification.model';
import { NotificationService } from '@notification-service/services/http/notification-service';
import { TranslateService } from '@ngx-translate/core';
import { filter } from 'rxjs';
import { resultOf } from '../notification-result';

/** One notification: what happened to it, with the raw states for support and a way to send it again. */
@Component({
	selector: 'app-notification-log-drawer',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule],
	templateUrl: './notification-log-drawer.component.html',
	styleUrl: './notification-log-drawer.component.scss',
	host: { '(document:keydown.escape)': 'close()' },
})
export class NotificationLogDrawerComponent {
	private readonly notificationService = inject(NotificationService);
	private readonly uiNotificationService = inject(UiNotificationService);
	private readonly httpErrorHandlingService = inject(HttpErrorHandlingService);
	private readonly language = inject(TranslateService);
	private readonly dialog = inject(MatDialog);
	private readonly destroyRef = inject(DestroyRef);
	protected readonly enumUtils = inject(NotificationServiceEnumUtils);

	readonly notification = input.required<Notification>();
	readonly closed = output<void>();
	readonly changed = output<void>();

	protected readonly busy = signal(false);
	protected readonly result = computed(() => resultOf(this.notification()));
	protected readonly channel = computed(() => this.notification().notifiedWith ?? this.notification().contactTypeHint);
	protected readonly canResend = computed(() => this.result() === 'failed' || this.result() === 'skipped');

	protected close(): void {
		this.closed.emit();
	}

	protected resend(): void {
		this.busy.set(true);
		this.notificationService.resend(this.notification().id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: () => {
				this.busy.set(false);
				this.uiNotificationService.snackBarNotification(this.language.instant('NOTIFICATION-LOG.DRAWER.RESENT'), SnackBarNotificationLevel.Success);
				this.changed.emit();
			},
			error: error => {
				this.busy.set(false);
				this.httpErrorHandlingService.handleBackedRequestError(error);
			},
		});
	}

	protected delete(): void {
		this.dialog.open(ConfirmationDialogComponent, {
			maxWidth: '400px',
			data: {
				isDeleteConfirmation: true,
				message: this.language.instant('NOTIFICATION-LOG.DRAWER.DELETE-CONFIRM'),
				confirmButton: this.language.instant('NOTIFICATION-LOG.DRAWER.DELETE'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
			},
		}).afterClosed().pipe(filter(Boolean), takeUntilDestroyed(this.destroyRef)).subscribe(() => {
			this.busy.set(true);
			this.notificationService.delete(this.notification().id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
				next: () => {
					this.busy.set(false);
					this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-DELETE'), SnackBarNotificationLevel.Success);
					this.changed.emit();
				},
				error: error => {
					this.busy.set(false);
					this.httpErrorHandlingService.handleBackedRequestError(error);
				},
			});
		});
	}
}
