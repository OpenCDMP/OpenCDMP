import { Component, EventEmitter, Input, OnInit, Output, SimpleChanges, OnChanges } from '@angular/core';
import { BaseComponent } from '@common/base/base.component';
import { NotificationServiceEnumUtils } from '@notification-service/core/formatting/enum-utils.service';
import { InAppNotificationFilter } from '@notification-service/core/query/inapp-notification.lookup';
import { nameof } from 'ts-simple-nameof';
import { NotificationInAppTracking } from '@notification-service/core/enum/notification-inapp-tracking.enum';
import { NotificationType } from '@notification-service/core/enum/notification-type.enum';
@Component({
	selector: 'app-mine-inapp-notification-listing-filters',
	templateUrl: './mine-inapp-notification-listing-filters.component.html',
	styleUrls: ['./mine-inapp-notification-listing-filters.component.scss']
})
export class MineInAppNotificationListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: InAppNotificationFilter;
	@Output() filterChange = new EventEmitter<InAppNotificationFilter>();

	notificationInAppTrackingEnumValues = this.enumUtils.getEnumValues<NotificationInAppTracking>(NotificationInAppTracking);
	notificationTypeEnumValues = this.enumUtils.getEnumValues<NotificationType>(NotificationType);

	// * State
	internalFilters: InAppNotificationListingFilters = this._getEmptyFilters();

	protected appliedFilterCount: number = 0;
	constructor(
		public enumUtils: NotificationServiceEnumUtils,
	) { super(); }

	ngOnInit() {
	}

	ngOnChanges(changes: SimpleChanges): void {
		const filterChange = changes[nameof<MineInAppNotificationListingFiltersComponent>(x => x.filter)]?.currentValue as InAppNotificationFilter;
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
		const { like, trackingState, type } = this.internalFilters ?? {}
		this.filterChange.emit({
			...this.filter,
			like,
			trackingState,
			type
		})
	}


	private _parseToInternalFilters(inputFilter: InAppNotificationFilter): InAppNotificationListingFilters {
		if (!inputFilter) {
			return this._getEmptyFilters();
		}

		let { like, trackingState, type } = inputFilter;

		return {
			like: like,
			trackingState: trackingState,
			type: type
		}

	}

	private _getEmptyFilters(): InAppNotificationListingFilters {
		return {
			like: null,
			trackingState: null,
			type: null
		}
	}

	private _computeAppliedFilters(filters: InAppNotificationListingFilters): number {
		let count = 0;
		return count;
	}

	clearFilters() {
		this.internalFilters = this._getEmptyFilters();
	}
}

interface InAppNotificationListingFilters {
	like: string;
	trackingState: NotificationInAppTracking[];
	type: NotificationType[];
}
