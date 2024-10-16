import { NgModule } from '@angular/core';
import { PrivacyComponent } from '@app/ui/sidebar/sidebar-footer/privacy/privacy.component';
import { PrivacyRoutingModule } from '@app/ui/sidebar/sidebar-footer/privacy/privacy.routing';
import { CommonUiModule } from '@common/ui/common-ui.module';

@NgModule({
  imports: [
    CommonUiModule,
    PrivacyRoutingModule
  ],
  declarations: [
    PrivacyComponent
  ]
})
export class PrivacyModule { }
