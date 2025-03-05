import { Component, computed, HostBinding, Inject, OnInit, ViewChild } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ReferencesWithType } from '@app/core/query/description.lookup';
import { PlanLookup } from '@app/core/query/plan.lookup';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { PlanFilterComponent, PlanListingFilters } from '../plan-filter.component';

@Component({
    selector: 'plan-filter-dialog-component',
    templateUrl: './plan-filter-dialog.component.html',
    styleUrls: ['./plan-filter-dialog.component.scss'],
    standalone: false
})

export class PlanFilterDialogComponent implements OnInit {
	@ViewChild(PlanFilterComponent, { static: true }) filter: PlanFilterComponent;
	filters: PlanListingFilters;

	constructor(
		public dialogRef: MatDialogRef<PlanFilterDialogComponent>,
		private analyticsService: AnalyticsService,
		@Inject(MAT_DIALOG_DATA) public data: {
			isPublic: boolean,
			hasSelectedTenant: boolean,
			lookup: PlanLookup,
			referencesWithTypeItems: ReferencesWithType[],
		}) {
		this.filters = this._buildPlanFilters(data?.lookup, data?.referencesWithTypeItems)
	}

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.PlanFilterDialog);
	}

	private _buildPlanFilters(lookup: PlanLookup, references: ReferencesWithType[]): PlanListingFilters {
		return {
			statusId: lookup.statusIds?.[0] ?? null,
			isActive: lookup.isActive?.[0] == IsActive.Active,
			viewOnlyTenant: lookup.tenantSubQuery?.codes?.length > 0,
			descriptionTemplates: lookup.planDescriptionTemplateSubQuery?.descriptionTemplateGroupIds ? lookup.planDescriptionTemplateSubQuery?.descriptionTemplateGroupIds : [],
			planBlueprints: lookup.planBlueprintSubQuery?.ids ?? [],
			role: lookup.planUserSubQuery?.userRoles?.[0] ?? null,
			references: references ?? []
		}
	}

	onNoClick(): void {
		this.dialogRef.close();
	}

	onClose(): void {
		this.dialogRef.close();
	}

	onFilterChanged(filters: PlanListingFilters) {
		this.dialogRef.close(filters);
	}

}
