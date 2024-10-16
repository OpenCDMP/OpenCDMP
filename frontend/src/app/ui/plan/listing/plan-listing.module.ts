import { NgModule } from '@angular/core';
import { FormattingModule } from '@app/core/formatting.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { NewVersionPlanDialogModule } from '../new-version-dialog/plan-new-version-dialog.module';
import { PlanInvitationDialogModule } from '../invitation/dialog/plan-invitation-dialog.module';
import { PlanFilterDialogComponent } from './filtering/plan-filter-dialog/plan-filter-dialog.component';
import { PlanFilterComponent } from './filtering/plan-filter.component';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { PlanFilterService } from './filtering/services/plan-filter.service';
import { ClonePlanDialogModule } from '../clone-dialog/plan-clone-dialog.module';
import { PlanListingComponent } from './plan-listing.component';
import { PlanListingItemComponent } from './listing-item/plan-listing-item.component';
import { PlanListingRoutingModule } from './plan-listing.routing';
import { TextFilterModule } from '@common/modules/text-filter/text-filter.module';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		FormattingModule,
		AutoCompleteModule,
		ClonePlanDialogModule,
		NewVersionPlanDialogModule,
		PlanInvitationDialogModule,
		PlanListingRoutingModule,
        TextFilterModule
	],
	declarations: [
		PlanListingComponent,
		PlanListingItemComponent,
		PlanFilterDialogComponent,
		PlanFilterComponent,
	],
	exports: [
		PlanListingItemComponent
	],
	providers: [ PlanFilterService ]
})
export class PlanListingModule { }
