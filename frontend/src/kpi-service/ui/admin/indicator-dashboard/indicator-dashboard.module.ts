import { NgModule } from '@angular/core';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { ConfirmationDialogModule } from '@common/modules/confirmation-dialog/confirmation-dialog.module';
import { KPIDashboardComponent} from '@citesa/kpi-client';
import { IndicatorDashboardComponent } from './indicator-dashboard.component';
import { IndicatorDashboardRoutingModule } from './indicator-dashboard.routing';
import { KPIProviders } from 'kpi-service/core/providers/kpi-client.providers';
import { IndicatorDashboardService } from 'kpi-service/services/indicator-dashboard.service';
import { IndicatorPointService } from 'kpi-service/services/indicator-point.service';


@NgModule({
  declarations: [IndicatorDashboardComponent],
  imports: [
    CommonUiModule,
    CommonFormsModule,
    ConfirmationDialogModule,
    IndicatorDashboardRoutingModule,
    KPIDashboardComponent
  ],
  providers: [
    IndicatorPointService,
    KPIProviders.provideIndicatorPointService(),
    IndicatorDashboardService,
    KPIProviders.provideDashboardResolver(),
  ]
})
export class IndicatorDashboardModule { }