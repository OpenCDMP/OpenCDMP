import { inject, NgModule } from '@angular/core';
import { Routes, RouterModule, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { IndicatorDashboardComponent } from './indicator-dashboard.component';
import { KpiAuthGuard } from 'kpi-service/core/kpi-auth-guard.service';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';


const routes: Routes = [
	{
        path: '', 
        component: IndicatorDashboardComponent, 
        canActivate: [(next: ActivatedRouteSnapshot, state: RouterStateSnapshot) => inject(KpiAuthGuard).kpiCanActivate(next, state)], 
        data: {
            authContext: {
                permissions: [AppPermission.ViewIndicatorDashboardPage]
            },
            breadcrumb: true
        }
    },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class IndicatorDashboardRoutingModule { }
