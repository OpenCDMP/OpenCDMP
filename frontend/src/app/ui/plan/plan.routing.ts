import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BreadcrumbService } from '../misc/breadcrumb/breadcrumb.service';
import { AuthGuard } from '@app/core/auth-guard.service';
import { InvitationAcceptedComponent } from './invitation/accepted/plan-invitation-accepted.component';

const routes: Routes = [
	{
		path: 'overview',
		loadChildren: () => import('./overview/plan-overview.module').then(m => m.PlanOverviewModule),
		canActivate:[AuthGuard],
		data: {
			breadcrumb: true,
			...BreadcrumbService.generateRouteDataConfiguration({
				hideNavigationItem: true
			}),
		}
	},
	{
		path: 'new',
		loadChildren: () => import('./plan-editor-blueprint/plan-editor.module').then(m => m.PlanEditorModule),
		canActivate:[AuthGuard],
		data: {
			breadcrumb: true,
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'PLAN-EDITOR.TITLE-NEW'
			}),
			title: 'PLAN-EDITOR.TITLE-NEW'
		}
	},
	{
		path: 'edit',
		loadChildren: () => import('./plan-editor-blueprint/plan-editor.module').then(m => m.PlanEditorModule),
		canActivate:[AuthGuard],
		data: {
			breadcrumb: true,
			...BreadcrumbService.generateRouteDataConfiguration({
				hideNavigationItem: true
			}),
			title: 'PLAN-EDITOR.TITLE-EDIT'
		}
	},
	{
		path: '',
		canActivate:[AuthGuard],
		loadChildren: () => import('./listing/plan-listing.module').then(m => m.PlanListingModule),
		data: {
			breadcrumb: true
		},
	},
	{ path: 'invitation/:token', component: InvitationAcceptedComponent },
];

const publicRoutes: Routes = [
	{
		path: 'overview',
		loadChildren: () => import('./overview/plan-overview.module').then(m => m.PlanOverviewModule),
		data: {
			breadcrumb: true,
			...BreadcrumbService.generateRouteDataConfiguration({
				hideNavigationItem: true
			}),
		}
	},
	{
		path: '',
		loadChildren: () => import('./listing/plan-listing.module').then(m => m.PlanListingModule),
		data: {
			breadcrumb: true,
			isPublic: true
		},
	},
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule]
})
export class PlanRoutingModule { }

@NgModule({
	imports: [RouterModule.forChild(publicRoutes)],
	exports: [RouterModule]
})
export class PublicPlanRoutingModule { }
