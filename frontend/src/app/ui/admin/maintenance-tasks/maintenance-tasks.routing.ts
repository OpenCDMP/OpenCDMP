import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { MaintenanceTasksComponent } from './maintenance-tasks.component';
import { AuthGuard } from '@app/core/auth-guard.service';


const routes: Routes = [
	{ path: '', component: MaintenanceTasksComponent, canActivate: [AuthGuard] },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MaintenanceTasksRoutingModule { }
