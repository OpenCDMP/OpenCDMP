import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FormattingModule } from '@app/core/formatting.module';
import { DashboardComponent } from '@app/ui/dashboard/dashboard.component';
import { DashboardRoutingModule } from '@app/ui/dashboard/dashboard.routing';
import { ConfirmationDialogModule } from '@common/modules/confirmation-dialog/confirmation-dialog.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { DescriptionListingModule } from '../description/listing/description-listing.module';
import { PlanListingModule } from '../plan/listing/plan-listing.module';
import { StartNewPlanDialogModule } from '../plan/new/start-new-plan-dialogue/start-new-plan-dialog.module';
import { RecentEditedActivityComponent } from './recent-edited-activity/recent-edited-activity.component';
import {TopPlanBlueprintsComponent} from "@app/ui/dashboard/top-plan-blueprints/top-plan-blueprints.component";
import { DashboardOverviewComponent } from './dashboard-overview/dashboard-overview.component';
import { ActiveReferenceFiltersComponent } from '@app/ui/reference/reference-usage-filter/active-reference-filters.component';
import { A11yModule } from '@angular/cdk/a11y';

@NgModule({
    imports: [
        CommonUiModule,
        ActiveReferenceFiltersComponent,
        A11yModule,
        FormattingModule,
        DashboardRoutingModule,
        ConfirmationDialogModule,
        FormsModule,
        ReactiveFormsModule,
        PlanListingModule,
        DescriptionListingModule,
        StartNewPlanDialogModule,
        DashboardOverviewComponent,
        DashboardComponent,
        RecentEditedActivityComponent,
        TopPlanBlueprintsComponent
    ]
})
export class DashboardModule { }
