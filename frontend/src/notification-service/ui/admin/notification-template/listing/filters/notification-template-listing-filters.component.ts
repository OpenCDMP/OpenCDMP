import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { NotificationTemplateFilter } from '@notification-service/core/query/notification-template.lookup';
import { NotificationServiceEnumUtils } from '@notification-service/core/formatting/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { NotificationTemplateChannel } from '@notification-service/core/enum/notification-template-channel.enum';
import { NotificationTemplateKind } from '@notification-service/core/enum/notification-template-kind.enum';
import { NotificationType } from '@notification-service/core/enum/notification-type.enum';
import { nameof } from 'ts-simple-nameof';

@Component({
	selector: 'app-notification-template-listing-filters',
	templateUrl: './notification-template-listing-filters.component.html',
	styleUrls: ['./notification-template-listing-filters.component.scss']
})
export class NotificationTemplateListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: NotificationTemplateFilter;
	@Output() filterChange = new EventEmitter<NotificationTemplateFilter>();
	notificationTemplateKindEnumValues = this.enumUtils.getEnumValues<NotificationTemplateKind>(NotificationTemplateKind)
	notificationTemplateChannelEnumValues = this.enumUtils.getEnumValues<NotificationTemplateChannel>(NotificationTemplateChannel);
	notificationTypeEnumValues = this.enumUtils.getEnumValues<NotificationType>(NotificationType);

	// * State
	internalFilters: NotificationTemplateListingFilters = this._getEmptyFilters();

	protected appliedFilterCount: number = 0;
	constructor(
		public enumUtils: NotificationServiceEnumUtils,
	) { super(); }

	ngOnInit() {
	}

	ngOnChanges(changes: SimpleChanges): void {
		const filterChange = changes[nameof<NotificationTemplateListingFiltersComponent>(x => x.filter)]?.currentValue as NotificationTemplateFilter;
		if (filterChange) {
			this.updateFilters()
		}
	}


	onSearchTermChange(searchTerm: string): void {
		this.applyFilters()
	}


	protected updateFilters(): void {
		this.internalFilters = this._parseToInternalFilters(this.filter);
		this.appliedFilterCount = this._computeAppliedFilters(this.internalFilters);
	}

	protected applyFilters(): void {
		const { isActive, kinds, channels, notificationTypes } = this.internalFilters ?? {}
		this.filterChange.emit({
			...this.filter,
			isActive: isActive ? [IsActive.Active] : [IsActive.Inactive],
			kinds: kinds?.length > 0 ? kinds : null,
			channels: channels?.length > 0 ? channels : null,
			notificationTypes: notificationTypes?.length > 0 ? notificationTypes : null,
		})
	}


	private _parseToInternalFilters(inputFilter: NotificationTemplateFilter): NotificationTemplateListingFilters {
		if (!inputFilter) {
			return this._getEmptyFilters();
		}

		let { isActive, kinds, channels, notificationTypes } = inputFilter;

		return {
			isActive: (isActive ?? [])?.includes(IsActive.Active) || !isActive?.length,
			kinds: kinds,
			channels: channels,
			notificationTypes: notificationTypes
		}

	}

	private _getEmptyFilters(): NotificationTemplateListingFilters {
		return {
			isActive: true,
			kinds: null,
			channels: null,
			notificationTypes: null
		}
	}

	private _computeAppliedFilters(filters: NotificationTemplateListingFilters): number {
		let count = 0;
		if (!filters?.isActive) {
			count++
		}
		if (filters?.channels?.length) {
			count++;
		}
		if (filters?.kinds?.length) {
			count++;
		}
		if (filters?.notificationTypes?.length) {
			count++;
		}

		return count;
	}

	clearFilters() {
		this.internalFilters = this._getEmptyFilters();
	}
}

interface NotificationTemplateListingFilters {
	isActive: boolean;
	kinds: NotificationTemplateKind[];
	channels: NotificationTemplateChannel[];
	notificationTypes: NotificationType[];
}
