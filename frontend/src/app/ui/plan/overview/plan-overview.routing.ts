import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PlanOverviewComponent } from './plan-overview.component';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';

const routes: Routes = [
	{
		path: ':id',
		component: PlanOverviewComponent,
		data: {
			breadcrumb: true,
			title: 'GENERAL.TITLES.PLAN-OVERVIEW'
		},
	},
	{
		path: 'public/:publicId',
		component: PlanOverviewComponent,
		data: {
			breadcrumb: true,
			title: 'GENERAL.TITLES.PLAN-OVERVIEW'
		},
	}
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	providers: []
})
export class PlanOverviewRoutingModule { }
