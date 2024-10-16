import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { CommonUiModule } from '@common/ui/common-ui.module';

@NgModule({
    imports: [CommonUiModule, FormsModule],
    declarations: [ConfirmationDialogComponent],
    exports: [ConfirmationDialogComponent]
})
export class ConfirmationDialogModule {
	constructor() { }
}
