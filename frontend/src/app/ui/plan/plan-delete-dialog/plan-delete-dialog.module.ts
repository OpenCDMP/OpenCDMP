import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { PlanDeleteDialogComponent } from './plan-delete-dialog.component';

@NgModule({
    imports: [CommonUiModule, FormsModule],
    declarations: [PlanDeleteDialogComponent],
    exports: [PlanDeleteDialogComponent]
})
export class PlanDeleteDialogModule {
	constructor() { }
}
