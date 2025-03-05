import { HttpResponse } from '@angular/common/http';
import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { RankModel } from '@app/core/model/evaluator/evaluator-plan-model.model';
import { SuccessStatus } from '@app/core/model/evaluator/evaluator-success-status.model';
import { RankConfig } from '@app/core/model/evaluator/rank-config';
import { RankType } from '@app/core/model/evaluator/rank-type';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';

@Component({
    selector: 'app-plan-evaluate-dialog',
    templateUrl: './plan-evaluate-dialog.component.html',
    styleUrl: './plan-evaluate-dialog.component.scss',
    standalone: false
})
export class PlanEvaluateDialogComponent {
    // Injecting the dialog data into the component
    constructor(
      public dialogRef: MatDialogRef<PlanEvaluateDialogComponent>,
      public enumUtils: EnumUtils,
      @Inject(MAT_DIALOG_DATA) public data: { rankData: RankModel, rankConfig: RankConfig},
    ) { }

    get isValueRangeRankType(): boolean{
      return this.data?.rankConfig?.rankType === RankType.ValueRange;
    }

    get isSelectionRankType(): boolean{
      return this.data?.rankConfig?.rankType === RankType.Selection;
    }

    get selectionRankResult(): SuccessStatus{
      return this.data?.rankConfig?.selectionConfiguration?.valueSetList?.find(x => x.key == this.data?.rankData?.rank)?.successStatus || null;
    }

    close(){
        this.dialogRef.close();
    }
}
