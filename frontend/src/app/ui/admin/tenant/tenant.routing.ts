import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TenantListingComponent } from './listing/tenant-listing.component';
import { AuthGuard } from '@app/core/auth-guard.service';

const routes: Routes = [
	{
		path: '',
		component: TenantListingComponent,
		canActivate: [AuthGuard]
	},
	{ path: 'new', redirectTo: '', pathMatch: 'full' },
	{ path: ':id', redirectTo: '', pathMatch: 'full' },
	{ path: '**', loadComponent: () => import('@common/modules/page-not-found/page-not-found.component').then(m => m.PageNotFoundComponent)},
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	providers: []
})
export class TenantRoutingModule { }
