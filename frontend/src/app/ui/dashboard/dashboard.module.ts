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

@NgModule({
	imports: [
		CommonUiModule,
		FormattingModule,
		DashboardRoutingModule,
		ConfirmationDialogModule,
		FormsModule,
		ReactiveFormsModule,

		PlanListingModule,
		DescriptionListingModule,
		StartNewPlanDialogModule
	],
	declarations: [
		DashboardComponent,
        RecentEditedActivityComponent,
	]
})
export class DashboardModule { }
