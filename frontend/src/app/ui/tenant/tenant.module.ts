import { NgModule } from '@angular/core';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { TenantSwitchComponent } from './tenant-switch/tenant-switch.component';



@NgModule({
    imports: [
        CommonUiModule,
        TenantSwitchComponent
    ],
    exports: [
        TenantSwitchComponent,
    ]
})
export class TenantModule { }
