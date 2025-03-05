import { Component, computed, HostBinding, Inject, OnInit, ViewChild } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { DescriptionLookup, ReferencesWithType } from '@app/core/query/description.lookup';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { DescriptionFilterComponent, DescriptionListingFilters } from '../description-filter.component';

@Component({
    selector: 'description-filter-dialog-component',
    templateUrl: './description-filter-dialog.component.html',
    styleUrls: ['./description-filter-dialog.component.scss'],
    standalone: false
})

export class DescriptionFilterDialogComponent implements OnInit {
	@ViewChild(DescriptionFilterComponent, { static: true }) filter: DescriptionFilterComponent;

	filters: DescriptionListingFilters;

	constructor(
		public dialogRef: MatDialogRef<DescriptionFilterComponent>,
		private analyticsService: AnalyticsService,
		@Inject(MAT_DIALOG_DATA) public data: {
			isPublic: boolean,
			hasSelectedTenant: boolean,
			lookup: DescriptionLookup,
			referencesWithTypeItems: ReferencesWithType[],
		}
	) {
		this.filters = this._buildDescriptionFilters(data?.lookup, data?.referencesWithTypeItems);
	}

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.DescriptionFilterDialog);
	}

	onNoClick(): void {
		this.dialogRef.close();
	}

	onClose(): void {
		this.dialogRef.close();
	}

	onFilterChanged(filters: DescriptionListingFilters) {
		this.dialogRef.close(filters);
	}

	private _buildDescriptionFilters(lookup: DescriptionLookup, references: ReferencesWithType[]): DescriptionListingFilters {
		return {
			statusId: lookup.statusIds?.[0] ?? null,
			isActive: lookup.isActive?.[0] == IsActive.Active,
			viewOnlyTenant: lookup.tenantSubQuery?.codes?.length > 0,
			role: lookup.planSubQuery?.planUserSubQuery?.userRoles?.[0] ?? null,
			descriptionTemplates: lookup.descriptionTemplateSubQuery?.ids ?? [],
			associatedPlanIds: lookup.planSubQuery?.ids ?? [],
			tags: lookup.descriptionTagSubQuery?.tagIds ?? [],
			references: references ?? []
		};
	}
}
