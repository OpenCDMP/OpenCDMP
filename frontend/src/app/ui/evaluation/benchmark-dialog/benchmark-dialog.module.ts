import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { BenchmarkDialogComponent } from './benchmark-dialog.component';

@NgModule({
    imports: [CommonUiModule, FormsModule, ReactiveFormsModule ],
    declarations: [BenchmarkDialogComponent],
    exports: [BenchmarkDialogComponent]
})
export class BenchmarkDialogModule {
	constructor() { }
}
