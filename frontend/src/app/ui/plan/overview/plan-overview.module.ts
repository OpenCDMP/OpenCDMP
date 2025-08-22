import { NgModule } from '@angular/core';
import { FormattingModule } from '@app/core/formatting.module';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { PlanDepositDropdown } from '../editor/plan-deposit-dropdown/plan-deposit-dropdown.component';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { ConfirmationDialogModule } from '@common/modules/confirmation-dialog/confirmation-dialog.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { NgDialogAnimationService } from 'ng-dialog-animation';
import { PlanFinalizeDialogModule } from '../plan-finalize-dialog/plan-finalize-dialog.module';
import { PlanOverviewRoutingModule } from './plan-overview.routing';
import { MultipleChoiceDialogModule } from '@common/modules/multiple-choice-dialog/multiple-choice-dialog.module';
import { PlanDeleteDialogModule } from '../plan-delete-dialog/plan-delete-dialog.module';
import { PlanOverviewComponent } from './plan-overview.component';
import { PlanAuthorsComponent } from '../plan-authors/plan-authors.component';
import { PlanEvaluationHistoryComponent } from '../listing/plan-evaluation-history/plan-evaluation-history.component';
import { FileAsyncPipe } from '@app/core/pipes/file-async.pipe';
import { BenchmarkDialogModule } from '@app/ui/evaluation/benchmark-dialog/benchmark-dialog.module';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		ConfirmationDialogModule,
		PlanDeleteDialogModule,
		BenchmarkDialogModule,
		MultipleChoiceDialogModule,
		FormattingModule,
		AutoCompleteModule,
		PlanOverviewRoutingModule,
		PlanFinalizeDialogModule,
        PlanAuthorsComponent,
        PlanEvaluationHistoryComponent,
        FileAsyncPipe
	],
	declarations: [
		PlanOverviewComponent,
		PlanDepositDropdown
	],
	providers: [
		NgDialogAnimationService
	]
})
export class PlanOverviewModule { }
