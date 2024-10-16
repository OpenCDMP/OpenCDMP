import { NgModule } from '@angular/core';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { FaqDialogComponent } from './dialog/faq-dialog.component';
import { FaqContentComponent } from './faq-content/faq-content.component';
import { FaqRoutingModule } from './faq.routing';

@NgModule({
    imports: [
        CommonUiModule,
        FaqRoutingModule
    ],
    declarations: [
        FaqContentComponent,
        FaqDialogComponent
    ],
    exports: [
        FaqDialogComponent
    ]
})
export class FaqModule { }
