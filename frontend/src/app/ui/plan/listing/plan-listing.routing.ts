import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PlanListingComponent } from './plan-listing.component';

const routes: Routes = [
	{
		path: '',
		component: PlanListingComponent,
		data: {
			breadcrumb: true
		},
	},
	{
		path: 'versions/:groupId',
		component: PlanListingComponent,
		data: {
			breadcrumb: true,
			mode: 'versions-listing'
		},
	},
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	providers: []
})
export class PlanListingRoutingModule { }
