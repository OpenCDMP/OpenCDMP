import { NgModule } from '@angular/core';
import { FormattingModule } from '@app/core/formatting.module';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { PlanDepositDropdownModule } from '../editor/plan-deposit-dropdown/plan-deposit-dropdown.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { ConfirmationDialogModule } from '@common/modules/confirmation-dialog/confirmation-dialog.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { PlanFinalizeDialogModule } from '../plan-finalize-dialog/plan-finalize-dialog.module';
import { PlanOverviewRoutingModule } from './plan-overview.routing';
import { MultipleChoiceDialogModule } from '@common/modules/multiple-choice-dialog/multiple-choice-dialog.module';
import { PlanDeleteDialogModule } from '../plan-delete-dialog/plan-delete-dialog.module';
import { PlanOverviewComponent } from './plan-overview.component';
import { PlanEvaluationHistoryComponent } from '../listing/plan-evaluation-history/plan-evaluation-history.component';
import { FileAsyncPipe } from '@app/core/pipes/file-async.pipe';
import { BenchmarkDialogModule } from '@app/ui/evaluation/benchmark-dialog/benchmark-dialog.module';
import { PlanActiveUsersComponent } from '../plan-active-users/plan-active-users.component';
import { DescriptionPreviewComponent } from '@app/ui/description/overview/description-preview/description-preview.component';
import { PlanAuthorsModule } from '../plan-authors/plan-authors.module';
import { ToggleShowTextComponent } from '@app/library/toggle-show-text/toggle-show-text.component';
import { InvokeMemoizedPipe } from '@common/formatting/pipes/invoke-memoized.pipe';
import { AsyncPipe } from '@angular/common';
import { ReferenceCardComponent } from '@app/ui/reference/reference-card/reference-card.component';

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
        PlanAuthorsModule,
        PlanDepositDropdownModule,
        PlanEvaluationHistoryComponent,
        FileAsyncPipe,
        PlanActiveUsersComponent,
        DescriptionPreviewComponent,
        ToggleShowTextComponent,
        InvokeMemoizedPipe,
        AsyncPipe,
        ReferenceCardComponent,
        PlanOverviewComponent
    ]
})
export class PlanOverviewModule { }
