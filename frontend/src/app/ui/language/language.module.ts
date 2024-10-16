import { NgModule } from '@angular/core';

import { LanguageComponent } from './language-content/language.component';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { LanguageDialogComponent } from './dialog/language-dialog.component';


@NgModule({
    declarations: [
        LanguageComponent,
        LanguageDialogComponent
    ],
    imports: [
        CommonUiModule
    ],
    exports: [
        LanguageComponent,
        LanguageDialogComponent
    ]
})
export class LanguageModule { }
