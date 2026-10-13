import { ChangeDetectionStrategy, Component, computed, inject, input, output } from '@angular/core';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { MatSelectChange } from '@angular/material/select';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { NotificationContactType } from '@notification-service/core/enum/notification-contact-type';
import { NotificationType } from '@notification-service/core/enum/notification-type.enum';
import { NotificationServiceEnumUtils } from '@notification-service/core/formatting/enum-utils.service';
import { NOTIFICATION_GROUP_ORDER, NotificationGroup, notificationGroupOf } from '@notification-service/core/formatting/notification-type-group';

export type NotifierMap = Record<string, NotificationContactType[]>;

export interface NotificationChannelChange {
	type: string;
	channel: NotificationContactType;
	enabled?: boolean;
}

/** Channels shown left to right; channels not listed here follow, in numeric order. */
const CHANNEL_ORDER: NotificationContactType[] = [NotificationContactType.IN_APP, NotificationContactType.EMAIL];

interface ChannelCell {
	channel: NotificationContactType;
	available: boolean;
	enabled: boolean;
	/** The only channel left on: it cannot be turned off, a notification needs at least one way to reach people. */
	locked: boolean;
	label: string;
}

interface NotificationRow {
	type: string;
	name: string;
	cells: ChannelCell[];
	enabled: NotificationContactType[];
}

interface NotificationRowGroup {
	group: NotificationGroup;
	rows: NotificationRow[];
}

/** Per notification type, which channels are used and which one is tried first. */
@Component({
	selector: 'app-notification-channels-table',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule],
	templateUrl: './notification-channels-table.component.html',
	styleUrl: './notification-channels-table.component.scss',
})
export class NotificationChannelsTableComponent {
	private readonly enumUtils = inject(NotificationServiceEnumUtils);

	/** The channels each notification type supports. */
	readonly available = input.required<NotifierMap>();
	/** The channels each notification type is sent through, first one tried first. */
	readonly enabled = input.required<NotifierMap>();
	readonly editable = input(true);
	/** False shows the channels without letting them be turned on or off; only the first channel can change. */
	readonly canToggle = input(true);
	readonly labelledBy = input<string>(null);
	readonly describedBy = input<string>(null);

	readonly toggled = output<NotificationChannelChange>();
	readonly tryFirst = output<NotificationChannelChange>();

	/** Every channel any notification type supports. */
	protected readonly channels = computed(() => {
		const all = new Set<NotificationContactType>();
		Object.values(this.available()).forEach(list => (list ?? []).forEach(x => all.add(x)));
		const rank = (x: NotificationContactType) => {
			const index = CHANNEL_ORDER.indexOf(x);
			return index < 0 ? CHANNEL_ORDER.length : index;
		};
		return [...all].sort((x, y) => rank(x) - rank(y) || x - y);
	});

	protected readonly groups = computed<NotificationRowGroup[]>(() => {
		const available = this.available();
		const channels = this.channels();
		const byGroup = new Map<NotificationGroup, NotificationRow[]>();
		Object.keys(available).forEach(type => {
			const supported = available[type] ?? [];
			const enabled = this.enabled()[type] ?? [];
			const name = this.enumUtils.toNotificationTypeString(type as NotificationType) || type;
			const row: NotificationRow = {
				type,
				name,
				enabled,
				cells: channels.map(channel => ({
					channel,
					available: supported.includes(channel),
					enabled: enabled.includes(channel),
					locked: enabled.length === 1 && enabled[0] === channel,
					label: `${name}, ${this.channelName(channel)}`,
				})),
			};
			const group = notificationGroupOf(type);
			byGroup.set(group, [...(byGroup.get(group) ?? []), row]);
		});
		return NOTIFICATION_GROUP_ORDER.filter(group => byGroup.has(group)).map(group => ({ group, rows: byGroup.get(group) }));
	});

	protected channelName(channel: NotificationContactType): string {
		return this.enumUtils.toContactTypeString(channel) || String(channel);
	}

	protected onToggle(row: NotificationRow, cell: ChannelCell, event: MatCheckboxChange): void {
		this.toggled.emit({ type: row.type, channel: cell.channel, enabled: event.checked });
	}

	protected onTryFirst(row: NotificationRow, event: MatSelectChange): void {
		this.tryFirst.emit({ type: row.type, channel: event.value as NotificationContactType });
	}
}
