import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { PageEvent } from '@angular/material/paginator';
import { RelativeTimeFormatPipe } from '@app/core/pipes/relative-time.pipe';
import { UserService } from '@app/core/services/user/user.service';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { NotificationContactType } from '@notification-service/core/enum/notification-contact-type';
import { NotificationNotifyState } from '@notification-service/core/enum/notification-notify-state.enum';
import { NotificationType } from '@notification-service/core/enum/notification-type.enum';
import { NotificationServiceEnumUtils } from '@notification-service/core/formatting/enum-utils.service';
import { Notification } from '@notification-service/core/model/notification.model';
import { NotificationLookup } from '@notification-service/core/query/notification.lookup';
import { NotificationService } from '@notification-service/services/http/notification-service';
import { nameof } from 'ts-simple-nameof';
import { NotificationLogDrawerComponent } from './notification-log-drawer/notification-log-drawer.component';
import { NotificationResult, resultOf } from './notification-result';

type QuickFilter = 'failed' | 'waiting' | 'skipped';
type TimeRange = 'day' | 'week' | 'month' | 'any';

const QUICK_STATES: Record<QuickFilter, NotificationNotifyState[]> = {
	failed: [NotificationNotifyState.ERROR],
	waiting: [NotificationNotifyState.PENDING, NotificationNotifyState.PROCESSING],
	skipped: [NotificationNotifyState.OMITTED],
};

const RANGE_DAYS: Record<TimeRange, number | null> = { day: 1, week: 7, month: 30, any: null };

const daysAgo = (days: number): Date => new Date(Date.now() - days * 24 * 60 * 60 * 1000);

/** Every notification the platform sent, in every organisation, with why it failed and a way to send it again. */
@Component({
	selector: 'app-notification-log',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, ReactiveFormsModule, AutoCompleteModule, RelativeTimeFormatPipe, NotificationLogDrawerComponent],
	templateUrl: './notification-log.component.html',
	styleUrl: './notification-log.component.scss',
})
export class NotificationLogComponent {
	private readonly notificationService = inject(NotificationService);
	private readonly httpErrorHandlingService = inject(HttpErrorHandlingService);
	private readonly destroyRef = inject(DestroyRef);
	protected readonly userService = inject(UserService);
	protected readonly enumUtils = inject(NotificationServiceEnumUtils);

	protected readonly quickFilters: QuickFilter[] = ['failed', 'waiting', 'skipped'];
	protected readonly ranges: TimeRange[] = ['day', 'week', 'month', 'any'];
	protected readonly types = Object.values(NotificationType)
		.map(type => ({ type, name: this.enumUtils.toNotificationTypeString(type) || type }))
		.sort((a, b) => a.name.localeCompare(b.name));
	protected readonly channels = [NotificationContactType.EMAIL, NotificationContactType.IN_APP];

	protected readonly person = new FormControl<Guid>(null);
	protected readonly quick = signal<QuickFilter | null>(null);
	protected readonly type = signal<NotificationType | null>(null);
	protected readonly channel = signal<NotificationContactType | null>(null);
	protected readonly range = signal<TimeRange>('any');

	protected readonly rows = signal<Notification[]>([]);
	protected readonly total = signal(0);
	protected readonly pageIndex = signal(0);
	protected readonly pageSize = signal(25);
	protected readonly loading = signal(false);
	protected readonly counts = signal<Record<QuickFilter, number | null>>({ failed: null, waiting: null, skipped: null });
	protected readonly selected = signal<Notification | null>(null);

	private readonly fields = [
		nameof<Notification>(x => x.id),
		nameof<Notification>(x => x.type),
		nameof<Notification>(x => x.contactTypeHint),
		nameof<Notification>(x => x.notifyState),
		nameof<Notification>(x => x.notifiedWith),
		nameof<Notification>(x => x.notifiedAt),
		nameof<Notification>(x => x.retryCount),
		nameof<Notification>(x => x.trackingState),
		nameof<Notification>(x => x.trackingProcess),
		nameof<Notification>(x => x.user.id),
		nameof<Notification>(x => x.user.name),
		nameof<Notification>(x => x.createdAt),
		nameof<Notification>(x => x.updatedAt),
		nameof<Notification>(x => x.hash),
	];

	constructor() {
		this.person.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => this.reload());
		this.load();
		this.loadCounts();
	}

	protected toggleQuick(filter: QuickFilter): void {
		const on = this.quick() !== filter;
		this.quick.set(on ? filter : null);
		if (on && filter === 'failed') this.range.set('week');
		this.reload();
	}

	protected setType(type: NotificationType | null): void {
		this.type.set(type);
		this.reload();
	}

	protected setChannel(channel: NotificationContactType | null): void {
		this.channel.set(channel);
		this.reload();
	}

	protected setRange(range: TimeRange): void {
		this.range.set(range);
		this.reload();
	}

	protected onPage(event: PageEvent): void {
		this.pageIndex.set(event.pageIndex);
		this.pageSize.set(event.pageSize);
		this.load();
	}

	protected resultOf(item: Notification): NotificationResult {
		return resultOf(item);
	}

	protected channelOf(item: Notification): NotificationContactType {
		return item.notifiedWith ?? item.contactTypeHint;
	}

	protected onChanged(): void {
		this.selected.set(null);
		this.load();
		this.loadCounts();
	}

	private reload(): void {
		this.pageIndex.set(0);
		this.load();
	}

	private load(): void {
		const lookup = this.baseLookup();
		lookup.metadata = { countAll: true };
		lookup.page = { offset: this.pageIndex() * this.pageSize(), size: this.pageSize() };
		lookup.project = { fields: this.fields };
		const quick = this.quick();
		if (quick) lookup.notifyState = QUICK_STATES[quick];
		if (this.type()) lookup.type = [this.type()];
		if (this.channel() != null) lookup.contactType = [this.channel()];
		if (this.person.value) lookup.userIds = [this.person.value];
		const days = RANGE_DAYS[this.range()];
		if (days) lookup.createdAfter = daysAgo(days);

		this.loading.set(true);
		this.notificationService.query(lookup).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: result => {
				this.rows.set(result.items ?? []);
				this.total.set(result.count ?? 0);
				this.loading.set(false);
			},
			error: error => {
				this.loading.set(false);
				this.httpErrorHandlingService.handleBackedRequestError(error);
			},
		});
	}

	private loadCounts(): void {
		this.quickFilters.forEach(filter => {
			const lookup = this.baseLookup();
			lookup.metadata = { countAll: true };
			lookup.page = { offset: 0, size: 1 };
			lookup.project = { fields: [nameof<Notification>(x => x.id)] };
			lookup.notifyState = QUICK_STATES[filter];
			if (filter === 'failed') lookup.createdAfter = daysAgo(7);
			this.notificationService.query(lookup).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
				next: result => this.counts.update(counts => ({ ...counts, [filter]: result.count ?? 0 })),
				error: error => this.httpErrorHandlingService.handleBackedRequestError(error),
			});
		});
	}

	private baseLookup(): NotificationLookup {
		const lookup = new NotificationLookup();
		lookup.isActive = [IsActive.Active];
		lookup.order = { items: ['-' + nameof<Notification>(x => x.createdAt)] };
		return lookup;
	}
}
