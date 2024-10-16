import { NgModule } from '@angular/core';
import { FormattingModule } from '@app/core/formatting.module';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { ConfirmationDialogModule } from '@common/modules/confirmation-dialog/confirmation-dialog.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { DescriptionCopyDialogModule } from '../description-copy-dialog/description-copy-dialog.module';
import { DescriptionOverviewComponent } from './description-overview.component';
import { DescriptionOverviewRoutingModule } from './description-overview.routing';
import { PlanAuthorsComponent } from '@app/ui/plan/plan-authors/plan-authors.component';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		ConfirmationDialogModule,
		FormattingModule,
		AutoCompleteModule,
		DescriptionCopyDialogModule,
		DescriptionOverviewRoutingModule,
        PlanAuthorsComponent
	],
	declarations: [
		DescriptionOverviewComponent
	]
})
export class DescriptionOverviewModule { }