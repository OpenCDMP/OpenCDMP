import { Routes } from '@angular/router';
import { AuthGuard } from '@app/core/auth-guard.service';
import { MaintenancePageComponent } from './maintenance-page.component';

export const MAINTENANCE_ROUTES: Routes = [
	{ path: '', component: MaintenancePageComponent, canActivate: [AuthGuard] },
	{ path: '**', loadComponent: () => import('@common/modules/page-not-found/page-not-found.component').then(m => m.PageNotFoundComponent) }
];
