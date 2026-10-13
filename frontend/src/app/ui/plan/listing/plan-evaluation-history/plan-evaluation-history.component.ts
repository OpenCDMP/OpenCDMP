import { Component, computed, input, signal, ChangeDetectionStrategy } from '@angular/core';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { EntityType } from '@app/core/common/enum/entity-type';
import { Evaluation } from '@app/core/model/evaluation/evaluation';
import { Guid } from '@common/types/guid';
import { EvaluationStatus } from '@app/core/model/evaluation/evaluation-status';
import { BaseComponent } from '@common/base/base.component';
import { EvaluateDialogComponent } from '../../../evaluation/evaluate-dialog/evaluate-dialog.component';
import { LowerCasePipe } from '@angular/common';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { FormattingModule } from '@app/core/formatting.module';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-plan-evaluation-history',
  templateUrl: './plan-evaluation-history.component.html',
  styleUrl: './plan-evaluation-history.component.scss',
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [MatTooltipModule, MatButtonModule, MatDialogModule, MatIconModule, FormattingModule, TranslateModule, LowerCasePipe]
})
export class PlanEvaluationHistoryComponent extends BaseComponent{
    evaluations = input<Evaluation[]>();
    pageSize = input<number>(3);
    /**
     * Plan the evaluations belong to, used only to let the result dialog link to the asset a
     * result covers. Left unset by public views, where that route does not apply, and the
     * dialog then renders no link.
     */
    planId = input<Guid>();

    // Most recent execution first.
    sortedEvaluations = computed(() => {
        const list = this.evaluations() ?? [];
        return [...list].sort((a, b) => new Date(b.evaluatedAt).getTime() - new Date(a.evaluatedAt).getTime());
    });

    showMore = signal<boolean>(false);
    toggleShowMore(){
        this.showMore.update((val) => !val);
    }

    EvaluationStatusEnum = EvaluationStatus;
    constructor(private dialog: MatDialog){
        super();
    }

    openEvaluationInfo(evaluation: Evaluation) {
        if(evaluation.status != this.EvaluationStatusEnum.Completed){
            return;
        }
        this.dialog.open(EvaluateDialogComponent, {
			maxWidth: '90vw',
            data: {
                rankData: evaluation.data?.rankResult,
                rankConfig: evaluation.data?.rankConfig,
                evaluatorId: evaluation.data?.evaluatorId,
                planId: evaluation.entityType === EntityType.Plan ? this.planId() : null
            }
        });
    }
}
