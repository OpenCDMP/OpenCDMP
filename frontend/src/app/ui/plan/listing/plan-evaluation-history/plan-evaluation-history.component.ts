import { Component, input, signal } from '@angular/core';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Evaluation } from '@app/core/model/evaluation/evaluation';
import { EvaluationStatus } from '@app/core/model/evaluation/evaluation-status';
import { BaseComponent } from '@common/base/base.component';
import { PlanEvaluateDialogComponent } from '../../plan-evaluate-dialog/plan-evaluate-dialog.component';
import { CommonModule } from '@angular/common';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { FormattingModule } from '@app/core/formatting.module';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-plan-evaluation-history',
  templateUrl: './plan-evaluation-history.component.html',
  styleUrl: './plan-evaluation-history.component.scss',
  imports: [CommonModule, MatTooltipModule, MatButtonModule, MatDialogModule, MatIconModule, FormattingModule, TranslateModule]
})
export class PlanEvaluationHistoryComponent extends BaseComponent{
    evaluations = input<Evaluation[]>();
    pageSize = input<number>(5);

    showMore = signal<boolean>(false);
    toggleShowMore(){
        this.showMore.update((val) => !val);
    }

    EvaluationStatusEnum = EvaluationStatus;
    constructor(private dialog: MatDialog){
        super();
    }

    openEvaluationInfo(evaluation: Evaluation) {
        this.dialog.open(PlanEvaluateDialogComponent, {
            data: {
                rankData: evaluation.data?.rankModel,
                rankConfig: evaluation.data?.rankConfig
            }
        });
    }
}
