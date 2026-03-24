import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '@app/core/auth-guard.service';
import { DescriptionListingComponent } from './description-listing.component';

const routes: Routes = [
	{
		path: '',
		component: DescriptionListingComponent,
		data: {
			breadcrumb: true
		},
	},
	{
		path: 'plan/:planId',
		component: DescriptionListingComponent,
		canActivate: [AuthGuard],
		data: {
			breadcrumb: true
		},
	},
	{ path: '**', loadComponent: () => import('@common/modules/page-not-found/page-not-found.component').then(m => m.PageNotFoundComponent)}
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	providers: []
})
export class DescriptionListingRoutingModule { }
