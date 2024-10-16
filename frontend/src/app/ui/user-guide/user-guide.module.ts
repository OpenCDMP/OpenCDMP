import { NgModule } from '@angular/core';

import { UserGuideRoutingModule } from './user-guide.routing';
import { UserGuideContentComponent } from './user-guide-content/user-guide-content.component';
import { UserGuideDialogComponent } from './dialog/user-guide-dialog.component';
import { CommonUiModule } from '@common/ui/common-ui.module';


@NgModule({
    declarations: [
        UserGuideContentComponent,
        UserGuideDialogComponent
    ],
    imports: [
        CommonUiModule,
        UserGuideRoutingModule
    ],
    exports: [
        UserGuideDialogComponent
    ]
})
export class UserGuideModule { }
