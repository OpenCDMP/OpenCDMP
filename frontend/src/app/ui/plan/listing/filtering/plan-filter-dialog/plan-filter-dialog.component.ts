import { Inject, Component, ViewChild, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { UntypedFormGroup } from '@angular/forms';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { PlanFilterComponent, PlanListingFilters } from '../plan-filter.component';
import { ReferencesWithType } from '@app/core/query/description.lookup';
import { PlanLookup } from '@app/core/query/plan.lookup';

@Component({
	selector: 'plan-filter-dialog-component',
	templateUrl: './plan-filter-dialog.component.html',
	styleUrls: ['./plan-filter-dialog.component.scss']
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
			status: lookup.statuses?.[0] ?? null,
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
