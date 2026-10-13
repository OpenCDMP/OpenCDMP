import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, ChangeDetectionStrategy } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { WebhookSubscriptionFilter } from '@app/core/query/webhook-subscription.lookup';
import { BaseComponent } from '@common/base/base.component';
import { nameof } from 'ts-simple-nameof';

@Component({
    selector: 'app-webhook-listing-filters',
    templateUrl: './webhook-listing-filters.component.html',
    styleUrls: ['./webhook-listing-filters.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager
})
export class WebhookListingFiltersComponent extends BaseComponent implements OnChanges {

	@Input() readonly filter: WebhookSubscriptionFilter;
	@Output() filterChange = new EventEmitter<WebhookSubscriptionFilter>();

	internalFilters: FormGroup<WebhookListingFilters> = new FormGroup({
		showDeleted: new FormControl<boolean>(false, { nonNullable: true }),
		like: new FormControl<string | null>(null)
	})

	get formIsDirty(): boolean {
		return this.internalFilters.controls.showDeleted.dirty;
	}

	protected appliedFilterCount: number = 0;

	constructor() { super(); }

	ngOnChanges(changes: SimpleChanges): void {
		const filterChange = changes[nameof<WebhookListingFiltersComponent>(x => x.filter)]?.currentValue as WebhookSubscriptionFilter;
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
		const { showDeleted, like } = this.internalFilters?.value ?? {}
		this.filterChange.emit({
			// search, deleted state and tenant scope live in the listing toolbar and arrive through `filter`
			...this.filter,
		});
		this.internalFilters.markAsPristine();
	}

	private _parseToInternalFilters(inputFilter: WebhookSubscriptionFilter) {
		if (!inputFilter) {
			return this._getEmptyFilters();
		}

		const { isActive, like } = inputFilter;

		this.internalFilters.setValue({
			showDeleted: (isActive ?? []).includes(IsActive.Inactive),
			like: like ?? null
		});
	}

	private _getEmptyFilters() {
		this.internalFilters.setValue({
			showDeleted: false,
			like: null
		});
	}

	private _computeAppliedFilters(formGroup: FormGroup<WebhookListingFilters>): number {
		const filters = formGroup?.value;
		let count = 0;
		if (filters?.like) {
			count++;
		}

		return count;
	}

	clearFilters() {
		this._getEmptyFilters();
		this.internalFilters.controls.showDeleted.markAsDirty();
	}
}

interface WebhookListingFilters {
	showDeleted: FormControl<boolean>;
	like: FormControl<string | null>;
}
