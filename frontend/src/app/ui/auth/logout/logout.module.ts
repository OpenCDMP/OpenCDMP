import { NgModule } from '@angular/core';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { LogoutRoutingModule } from './logout-routing.module';
import { LogoutComponent } from './logout.component';

@NgModule({
    imports: [
        CommonUiModule,
        LogoutRoutingModule,
        LogoutComponent,
    ]
})
export class LogoutModule { }
