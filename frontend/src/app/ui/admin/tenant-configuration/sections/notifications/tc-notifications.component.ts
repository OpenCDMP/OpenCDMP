import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject } from '@angular/core';
import { FormService } from '@common/forms/form-service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { TenantConfigurationService } from '@notification-service/services/http/tenant-configuration.service';
import { NotificationChannelChange, NotificationChannelsTableComponent, NotifierMap } from '@notification-service/ui/notification-channels-table/notification-channels-table.component';
import { TcSaveBarComponent } from '../../shared/tc-save-bar.component';
import { TcSectionService } from '../../shared/tc-section.service';
import { TcStateTagComponent } from '../../shared/tc-state-tag.component';
import { TcNotifierList } from './tc-notifier-list';

/** Notifications: per notification type, which channels are used and which one is tried first. */
@Component({
	selector: 'app-tc-notifications',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, TcSaveBarComponent, TcStateTagComponent, NotificationChannelsTableComponent],
	providers: [TcSectionService],
	templateUrl: './tc-notifications.component.html',
	styleUrl: './tc-notifications.component.scss',
})
export class TcNotificationsComponent {
	protected readonly section = inject(TcSectionService);

	protected readonly notifiers = new TcNotifierList({
		tenantConfigurationService: inject(TenantConfigurationService),
		formService: inject(FormService),
		httpErrorHandlingService: inject(HttpErrorHandlingService),
		destroyRef: inject(DestroyRef),
	}, () => !this.section.canEdit);

	protected readonly editable = this.section.canEdit;

	protected readonly enabled = computed<NotifierMap>(() =>
		Object.fromEntries(Object.keys(this.notifiers.available()).map(type => [type, this.notifiers.effective(type)])));

	constructor() {
		this.section.register(this.notifiers);
		this.notifiers.load().subscribe();
	}

	canDeactivate(): boolean {
		return this.section.dirtyCount() === 0;
	}

	protected onToggle(change: NotificationChannelChange): void {
		this.notifiers.toggle(change.type, change.channel, change.enabled);
	}

	protected onTryFirst(change: NotificationChannelChange): void {
		this.notifiers.tryFirst(change.type, change.channel);
	}

	protected reset(): void {
		this.section.confirmReset(() => this.notifiers.reset());
	}
}
