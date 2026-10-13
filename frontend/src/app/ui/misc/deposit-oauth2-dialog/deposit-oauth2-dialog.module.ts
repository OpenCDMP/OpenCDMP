import { NgModule } from '@angular/core';

import { CommonUiModule } from '@common/ui/common-ui.module';
import { DepositOauth2DialogComponent } from './deposit-oauth2-dialog.component';
import { DepositOauth2DialogRoutingModule } from './deposit-oauth2-dialog-routing.module';
import { DepositOauth2DialogService } from './service/deposit-oauth2-dialog.service';


@NgModule({
    imports: [
        CommonUiModule,
        DepositOauth2DialogRoutingModule,
        DepositOauth2DialogComponent,
    ],
    providers: [
        DepositOauth2DialogService
    ]
})
export class DepositOauth2DialogModule { }
