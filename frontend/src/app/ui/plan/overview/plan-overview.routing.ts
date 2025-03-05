import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PlanOverviewComponent } from './plan-overview.component';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';

const routes: Routes = [
	{
		path: ':id',
        children: [
            {
                path: '',
                component: PlanOverviewComponent,
                data: {
                    breadcrumb: true,
                    title: 'GENERAL.TITLES.PLAN-OVERVIEW'
                },
            },
            {
                path: 'descriptions',
                loadChildren: () => import('../../description/overview/description-overview.module').then(m => m.DescriptionOverviewModule),
                data: {
                    breadcrumb: true,
                    ...BreadcrumbService.generateRouteDataConfiguration({
                        skipNavigation: true,
                        title: 'GENERAL.TITLES.DESCRIPTIONS'
                    }),
                },
            }
        ]
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
