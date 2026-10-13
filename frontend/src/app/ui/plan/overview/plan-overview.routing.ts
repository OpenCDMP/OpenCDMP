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
                path: 'assets',
                loadChildren: () => import('../../description/overview/description-overview.module').then(m => m.DescriptionOverviewModule),
                data: {
                    breadcrumb: true,
                    ...BreadcrumbService.generateRouteDataConfiguration({
                        skipNavigation: true,
                        title: 'GENERAL.TITLES.DESCRIPTIONS'
                    }),
                },
            },
            // Legacy redirect: nested "descriptions" route renamed to "assets".
            { path: 'descriptions', redirectTo: 'assets', pathMatch: 'prefix' }
        ]
	},
	{
		path: 'public/:publicId',
		component: PlanOverviewComponent,
		data: {
			breadcrumb: true,
			title: 'GENERAL.TITLES.PLAN-OVERVIEW',
			isPublic: true
		},
	},
    { path: '**', loadComponent: () => import('@common/modules/page-not-found/page-not-found.component').then(m => m.PageNotFoundComponent)}
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	providers: []
})
export class PlanOverviewRoutingModule { }
