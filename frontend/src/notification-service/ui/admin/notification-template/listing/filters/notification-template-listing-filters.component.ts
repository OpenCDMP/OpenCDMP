import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { NotificationTemplateFilter } from '@notification-service/core/query/notification-template.lookup';
import { NotificationServiceEnumUtils } from '@notification-service/core/formatting/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { NotificationTemplateChannel } from '@notification-service/core/enum/notification-template-channel.enum';
import { NotificationTemplateKind } from '@notification-service/core/enum/notification-template-kind.enum';
import { NotificationType } from '@notification-service/core/enum/notification-type.enum';
import { nameof } from 'ts-simple-nameof';
import { FormControl, FormGroup } from '@angular/forms';

@Component({
    selector: 'app-notification-template-listing-filters',
    templateUrl: './notification-template-listing-filters.component.html',
    styleUrls: ['./notification-template-listing-filters.component.scss'],
    standalone: false
})
export class NotificationTemplateListingFiltersComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() readonly filter: NotificationTemplateFilter;
	@Output() filterChange = new EventEmitter<NotificationTemplateFilter>();
	notificationTemplateKindEnumValues = this.enumUtils.getEnumValues<NotificationTemplateKind>(NotificationTemplateKind)
	notificationTemplateChannelEnumValues = this.enumUtils.getEnumValues<NotificationTemplateChannel>(NotificationTemplateChannel);
	notificationTypeEnumValues = this.enumUtils.getEnumValues<NotificationType>(NotificationType);
    

	// * State
	internalFilters: FormGroup<NotificationTemplateListingFilters> = new FormGroup({
        isActive: new FormControl(true),
        kinds: new FormControl(null),
        channels: new FormControl(null),
        notificationTypes: new FormControl(null)
    })

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
		this._parseToInternalFilters(this.filter);
		this.appliedFilterCount = this._computeAppliedFilters(this.internalFilters);
	}

	protected applyFilters(): void {
		const { isActive, kinds, channels, notificationTypes } = this.internalFilters?.value ?? {}
		this.filterChange.emit({
			...this.filter,
			isActive: isActive ? [IsActive.Active] : [IsActive.Inactive],
			kinds: kinds?.length > 0 ? kinds : null,
			channels: channels?.length > 0 ? channels : null,
			notificationTypes: notificationTypes?.length > 0 ? notificationTypes : null,
		});
        this.internalFilters.markAsPristine();
	}


	private _parseToInternalFilters(inputFilter: NotificationTemplateFilter) {
		if (!inputFilter) {
			this._getEmptyFilters();
		}

		let { isActive, kinds, channels, notificationTypes } = inputFilter;

		this.internalFilters.setValue({
			isActive: (isActive ?? [])?.includes(IsActive.Active) || !isActive?.length,
			kinds: kinds ?? null,
			channels: channels ?? null,
			notificationTypes: notificationTypes ?? null
		});
	}

	private _getEmptyFilters() {
		this.internalFilters.setValue({
			isActive: true,
			kinds: null,
			channels: null,
			notificationTypes: null
		});
	}

	private _computeAppliedFilters(formGroup: FormGroup<NotificationTemplateListingFilters>): number {
		const filters = formGroup?.value
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
		this._getEmptyFilters();
        this.internalFilters.markAsDirty();
	}
}

interface NotificationTemplateListingFilters {
	isActive: FormControl<boolean>;
	kinds: FormControl<NotificationTemplateKind[]>;
	channels: FormControl<NotificationTemplateChannel[]>;
	notificationTypes: FormControl<NotificationType[]>;
}
