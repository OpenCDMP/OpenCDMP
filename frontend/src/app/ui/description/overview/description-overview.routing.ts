import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DescriptionOverviewComponent } from './description-overview.component';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';

const routes: Routes = [
	{
		path: ':id',
		component: DescriptionOverviewComponent,
		data: {
			breadcrumb: true,
			title: 'GENERAL.TITLES.DESCRIPTION-OVERVIEW'
		},
	},
	{
		path: 'public/:publicId',
		component: DescriptionOverviewComponent,
		data: {
			breadcrumb: true,
			title: 'GENERAL.TITLES.DESCRIPTION-OVERVIEW'
		},
	}
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	providers: []
})
export class DescriptionOverviewRoutingModule { }
