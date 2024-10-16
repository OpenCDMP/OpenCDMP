import { NgModule } from '@angular/core';
import { FormattingModule } from '@app/core/formatting.module';
import { PlanRoutingModule, PublicPlanRoutingModule } from '@app/ui/plan/plan.routing';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { InvitationAcceptedComponent } from './invitation/accepted/plan-invitation-accepted.component';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		FormattingModule,
		PlanRoutingModule,
	],
	declarations: [
		InvitationAcceptedComponent
	],
	exports: [
	]
})
export class PlanModule { }

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		FormattingModule,
		PublicPlanRoutingModule,
	],
	declarations: [
	],
	exports: [
	]
})
export class PublicPlanModule { }
