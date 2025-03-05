import { Component, EventEmitter, Input, OnInit, Output, SimpleChanges, OnChanges } from '@angular/core';
import { BaseComponent } from '@common/base/base.component';
import { NotificationServiceEnumUtils } from '@notification-service/core/formatting/enum-utils.service';
import { InAppNotificationFilter } from '@notification-service/core/query/inapp-notification.lookup';
import { nameof } from 'ts-simple-nameof';
import { NotificationInAppTracking } from '@notification-service/core/enum/notification-inapp-tracking.enum';
import { NotificationType } from '@notification-service/core/enum/notification-type.enum';
import { FormControl, FormGroup } from '@angular/forms';
@Component({
    selector: 'app-mine-inapp-notification-listing-filters',
    templateUrl: './mine-inapp-notification-listing-filters.component.html',
    styleUrls: ['./mine-inapp-notification-listing-filters.component.scss'],
    standalone: false
})
export class MineInAppNotificationListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: InAppNotificationFilter;
	@Output() filterChange = new EventEmitter<InAppNotificationFilter>();

	notificationInAppTrackingEnumValues = this.enumUtils.getEnumValues<NotificationInAppTracking>(NotificationInAppTracking);
	notificationTypeEnumValues = this.enumUtils.getEnumValues<NotificationType>(NotificationType);

	// * State
	internalFilters: FormGroup<InAppNotificationListingFilters> = new FormGroup({
        like: new FormControl(null),
        trackingState: new FormControl(null),
        type: new FormControl(null)
    })
    

	protected appliedFilterCount: number = 0;
	constructor(
		public enumUtils: NotificationServiceEnumUtils
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
		this._parseToInternalFilters(this.filter);
		this.appliedFilterCount = this._computeAppliedFilters(this.internalFilters);
	}

	protected applyFilters(): void {
		const { like, trackingState, type } = this.internalFilters?.value ?? {}
		this.filterChange.emit({
			...this.filter,
			like,
			trackingState,
			type
		});
        this.internalFilters.markAsPristine();
	}


	private _parseToInternalFilters(inputFilter: InAppNotificationFilter) {
		if (!inputFilter) {
			this._getEmptyFilters();
		}

		let { like, trackingState, type } = inputFilter;

		this.internalFilters.setValue({
			like: like ?? null,
			trackingState: trackingState ?? null,
			type: type ?? null
		});

	}

	private _getEmptyFilters() {
		this.internalFilters.setValue({
			like: null,
			trackingState: null,
			type: null
		});
	}

	private _computeAppliedFilters(formGroup: FormGroup<InAppNotificationListingFilters>): number {
		const filters = formGroup?.value;
        let count = 0;
        if(filters?.like){
            count++;
        }
        if(filters?.trackingState?.length){
            count++;
        }
        if(filters?.type?.length){
            count++;
        }
        return count;
	}

	clearFilters() {
		this._getEmptyFilters();
        this.internalFilters.markAsDirty();
	}
}

interface InAppNotificationListingFilters {
	like: FormControl<string>;
	trackingState: FormControl<NotificationInAppTracking[]>;
	type: FormControl<NotificationType[]>;
}
