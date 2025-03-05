import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { RankModel } from '@app/core/model/evaluator/evaluator-plan-model.model';

@Component({
    selector: 'app-evaluate-description-dialog',
    templateUrl: './evaluate-description-dialog.component.html',
    styleUrl: './evaluate-description-dialog.component.scss',
    standalone: false
})
export class EvaluateDescriptionDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<EvaluateDescriptionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { rankData: RankModel }
  ) { }
}

