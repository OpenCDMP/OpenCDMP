import { NgModule } from '@angular/core';
import { FormattingModule } from '@app/core/formatting.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { StartNewDescriptionDialogComponent } from './start-new-description-dialog.component';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { RouterModule } from '@angular/router';

@NgModule({
    imports: [
        CommonUiModule,
        CommonFormsModule,
        FormattingModule,
        AutoCompleteModule,
        RouterModule,
        StartNewDescriptionDialogComponent
    ],
    exports: [
        StartNewDescriptionDialogComponent,
    ]
})
export class StartNewDescriptionDialogModule { }
