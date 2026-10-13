import {NgModule} from '@angular/core';
import {CommonUiModule} from '@common/ui/common-ui.module';
import {ReactiveFormsModule} from "@angular/forms";
import { PlanCreationConsentComponent } from './plan-creation-consent.component';


@NgModule({
    imports: [
        CommonUiModule,
        ReactiveFormsModule,
        PlanCreationConsentComponent
    ],
    exports: [
        PlanCreationConsentComponent
    ]
})
export class PlanCreationConsentModule {}
