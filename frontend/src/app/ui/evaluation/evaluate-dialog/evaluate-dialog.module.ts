import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { NgIf } from '@angular/common';
import { CommonUiModule } from '@common/ui/common-ui.module'; 
import { EvaluateDialogComponent } from './evaluate-dialog.component';

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
    declarations: [EvaluateDialogComponent],
    exports: [EvaluateDialogComponent]
  })
  export class EvaluateDialogModule { }