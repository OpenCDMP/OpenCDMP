import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { MaintenanceTasksComponent } from './maintenance-tasks.component';
import { AuthGuard } from '@app/core/auth-guard.service';


const routes: Routes = [
	{ path: '', component: MaintenanceTasksComponent, canActivate: [AuthGuard] },
  { path: '**', loadComponent: () => import('@common/modules/page-not-found/page-not-found.component').then(m => m.PageNotFoundComponent)}
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MaintenanceTasksRoutingModule { }
