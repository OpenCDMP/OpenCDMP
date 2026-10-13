import { NgModule } from '@angular/core';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { GlossaryDialogComponent } from './dialog/glossary-dialog.component';
import { GlossaryContentComponent } from './glossary-content/glossary-content.component';
import { GlossaryRoutingModule } from './glossary.routing';

@NgModule({
    imports: [
        CommonUiModule,
        GlossaryRoutingModule,
        GlossaryContentComponent,
        GlossaryDialogComponent
    ],
    exports: [
        GlossaryDialogComponent
    ]
})
export class GlossaryModule { }
