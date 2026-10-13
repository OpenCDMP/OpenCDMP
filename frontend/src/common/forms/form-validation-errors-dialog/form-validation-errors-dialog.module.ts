import { NgModule } from '@angular/core';
import { FormValidationErrorsDialogComponent } from '@common/forms/form-validation-errors-dialog/form-validation-errors-dialog.component';
import { FormValidationErrorsDirective } from '@common/forms/form-validation-errors-dialog/form-validation-errors-directive';
import { CommonUiModule } from '@common/ui/common-ui.module';

@NgModule({
    imports: [
        CommonUiModule,
        FormValidationErrorsDialogComponent,
        FormValidationErrorsDirective,
    ],
    exports: [
        FormValidationErrorsDialogComponent,
        FormValidationErrorsDirective
    ],
    providers: [
        FormValidationErrorsDirective
    ]
})
export class FormValidationErrorsDialogModule { }
