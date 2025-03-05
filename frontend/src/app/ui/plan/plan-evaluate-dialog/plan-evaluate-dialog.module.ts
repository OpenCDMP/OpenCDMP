import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { NgIf } from '@angular/common';
import { PlanEvaluateDialogComponent } from './plan-evaluate-dialog.component';
import { CommonUiModule } from '@common/ui/common-ui.module'; 

@NgModule({
    imports: [
      CommonModule,
      MatCardModule,
      MatButtonModule,
      MatInputModule,
      MatFormFieldModule,
      NgIf,
      CommonUiModule,
    ],
    declarations: [PlanEvaluateDialogComponent],
    exports: [PlanEvaluateDialogComponent]
  })
  export class EvaluatePlanDialogModule { }