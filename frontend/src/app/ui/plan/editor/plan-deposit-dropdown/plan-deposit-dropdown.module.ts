import { NgModule } from '@angular/core';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { PlanDepositDropdown } from './plan-deposit-dropdown.component';

@NgModule({
    imports: [
        CommonUiModule,
        PlanDepositDropdown,
    ],
    exports: [PlanDepositDropdown]
})
export class PlanDepositDropdownModule { }
