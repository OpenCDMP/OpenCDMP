import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { BenchmarkDialogComponent } from './benchmark-dialog.component';

@NgModule({
    imports: [CommonUiModule, FormsModule, ReactiveFormsModule, MatButtonToggleModule, BenchmarkDialogComponent],
    exports: [BenchmarkDialogComponent]
})
export class BenchmarkDialogModule {
	constructor() { }
}
