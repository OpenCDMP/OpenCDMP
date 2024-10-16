import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { PlanContactPrefillDialogComponent } from './plan-contact-prefill-dialog.component';

@NgModule({
    imports: [CommonUiModule, FormsModule, ReactiveFormsModule, AutoCompleteModule],
    declarations: [PlanContactPrefillDialogComponent],
    exports: [PlanContactPrefillDialogComponent]
})
export class PlanContactPrefillDialogModule {
	constructor() { }
}
