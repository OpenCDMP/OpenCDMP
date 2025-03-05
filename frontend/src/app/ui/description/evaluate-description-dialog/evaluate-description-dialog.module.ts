import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { NgIf } from '@angular/common';
import { EvaluateDescriptionDialogComponent } from './evaluate-description-dialog.component';
import { CommonUiModule } from '@common/ui/common-ui.module'; 

@NgModule({
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    NgIf,
    CommonUiModule
  ],
  declarations: [EvaluateDescriptionDialogComponent],
  exports: [EvaluateDescriptionDialogComponent]
})
export class EvaluateDescriptionDialogModule { }