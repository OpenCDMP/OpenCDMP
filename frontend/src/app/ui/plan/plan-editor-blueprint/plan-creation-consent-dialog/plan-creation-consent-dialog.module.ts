import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { PlanCreationConsentModule } from '../../plan-creation-consent/plan-creation-consent.module';
import { PlanCreationConsentDialogComponent } from './plan-creation-consent-dialog.component';

@NgModule({
    imports: [CommonUiModule, FormsModule, PlanCreationConsentModule, PlanCreationConsentDialogComponent],
    exports: [PlanCreationConsentDialogComponent]
})
export class PlanCreationConsentDialogModule {
	constructor() { }
}
