import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { NotificationFilter } from '@notification-service/core/query/notification.lookup';
import { UserService } from '@app/core/services/user/user.service';
import { NotificationServiceEnumUtils } from '@notification-service/core/formatting/enum-utils.service';
import { MultipleAutoCompleteConfiguration } from '@app/library/auto-complete/multiple/multiple-auto-complete-configuration';
import { BaseComponent } from '@common/base/base.component';
import { Guid } from '@common/types/guid';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { NotificationContactType } from '@notification-service/core/enum/notification-contact-type';
import { NotificationNotifyState } from '@notification-service/core/enum/notification-notify-state.enum';
import { NotificationTrackingProcess } from '@notification-service/core/enum/notification-tracking-process.enum';
import { NotificationTrackingState } from '@notification-service/core/enum/notification-tracking-state.enum';
import { NotificationType } from '@notification-service/core/enum/notification-type.enum';
import { nameof } from 'ts-simple-nameof';

@Component({
	selector: 'app-notification-listing-filters',
	templateUrl: './notification-listing-filters.component.html',
	styleUrls: ['./notification-listing-filters.component.scss']
})
export class NotificationListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: NotificationFilter;
	@Output() filterChange = new EventEmitter<NotificationFilter>();
	notificationContactTypeEnumValues = this.enumUtils.getEnumValues<NotificationContactType>(NotificationContactType)
	notificationNotifyStateEnumValues = this.enumUtils.getEnumValues<NotificationNotifyState>(NotificationNotifyState);
	notificationTrackingStateEnumValues = this.enumUtils.getEnumValues<NotificationTrackingState>(NotificationTrackingState);
	notificationTrackingProcessEnumValues = this.enumUtils.getEnumValues<NotificationTrackingProcess>(NotificationTrackingProcess);
	notificationTypeEnumValues = this.enumUtils.getEnumValues<NotificationType>(NotificationType);
	userAutoCompleteConfiguration: MultipleAutoCompleteConfiguration;

	readonly separatorKeysCodes: number[] = [ENTER, COMMA];

	// * State
	internalFilters: NotificationListingFilters = this._getEmptyFilters();

	protected appliedFilterCount: number = 0;
	constructor(
		public enumUtils: NotificationServiceEnumUtils,
		private userService: UserService,
	) { super(); }

	ngOnInit() {
		this.userAutoCompleteConfiguration = this.userService.multipleAutocompleteConfiguration;
	}

	ngOnChanges(changes: SimpleChanges): void {
		const filterChange = changes[nameof<NotificationListingFiltersComponent>(x => x.filter)]?.currentValue as NotificationFilter;
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
		const { isActive, type, notifyState, notifiedWith, contactType, trackingState, trackingProcess, userIds } = this.internalFilters ?? {}
		this.filterChange.emit({
			...this.filter,
			isActive: isActive ? [IsActive.Active] : [IsActive.Inactive],
			type: type?.length > 0 ? type : null,
			notifyState: notifyState?.length > 0 ? notifyState : null,
			notifiedWith: notifiedWith?.length > 0 ? notifiedWith : null,
			contactType: contactType?.length > 0 ? contactType : null,
			trackingState: trackingState?.length > 0 ? trackingState : null,
			trackingProcess: trackingProcess?.length > 0 ? trackingProcess : null,
			userIds: userIds?.length > 0 ? userIds : null,
		})
	}


	private _parseToInternalFilters(inputFilter: NotificationFilter): NotificationListingFilters {
		if (!inputFilter) {
			return this._getEmptyFilters();
		}

		let { isActive, type, notifyState, notifiedWith, contactType, trackingState, trackingProcess, userIds } = inputFilter;

		return {
			isActive: (isActive ?? [])?.includes(IsActive.Active) || !isActive?.length,
			type: type,
			notifyState: notifyState,
			notifiedWith: notifiedWith,
			contactType: contactType,
			trackingState: trackingState,
			trackingProcess: trackingProcess,
			userIds: userIds
		}

	}

	private _getEmptyFilters(): NotificationListingFilters {
		return {
			isActive: true,
			type: null,
			notifyState: null,
			notifiedWith: null,
			contactType: null,
			trackingState: null,
			trackingProcess: null,
			userIds: null
		}
	}

	private _computeAppliedFilters(filters: NotificationListingFilters): number {
		let count = 0;
		if (!filters?.isActive) {
			count++;
		}
		if (filters?.type?.length) {
			count++;
		}
		if (filters?.notifyState?.length) {
			count++;
		}
		if (filters?.notifiedWith?.length) {
			count++;
		}
		if (filters?.contactType?.length) {
			count++;
		}
		if (filters?.trackingState?.length) {
			count++;
		}
		if (filters?.trackingProcess?.length) {
			count++;
		}
		if (filters?.userIds?.length) {
			count++;
		}

		return count;
	}

	clearFilters() {
		this.internalFilters = this._getEmptyFilters();
	}
}

interface NotificationListingFilters {
	isActive: boolean;
	type: NotificationType[];
	notifyState: NotificationNotifyState[];
	notifiedWith: NotificationContactType[];
	contactType: NotificationContactType[];
	trackingState: NotificationTrackingState[];
	trackingProcess: NotificationTrackingProcess[];
	userIds: Guid[];
}
