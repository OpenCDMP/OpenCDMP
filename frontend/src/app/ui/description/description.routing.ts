import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '@app/core/auth-guard.service';
import { BreadcrumbService } from '../misc/breadcrumb/breadcrumb.service';

const routes: Routes = [
	{
		path: 'overview',
		loadChildren: () => import('./overview/description-overview.module').then(m => m.DescriptionOverviewModule),
		canActivate: [AuthGuard],
		data: {
			breadcrumb: true,
			...BreadcrumbService.generateRouteDataConfiguration({
				hideNavigationItem: true
			}),
		}
	},
	// {
	// 	path: 'edit',
	// 	loadChildren: () => import('./editor/description-editor.module').then(m => m.DescriptionEditorModule),
	// 	canActivate: [AuthGuard],
	// 	data: {
	// 		breadcrumb: true,
	// 		...BreadcrumbService.generateRouteDataConfiguration({
	// 			hideNavigationItem: true
	// 		}),
	// 	}
	// },
	// {
	// 	path: '',
	// 	canActivate: [AuthGuard],
	// 	loadChildren: () => import('./listing/description-listing.module').then(m => m.DescriptionListingModule),
	// 	data: {
	// 		breadcrumb: true
	// 	},
	// },
];

const publicRoutes: Routes = [
	{
		path: 'overview',
		loadChildren: () => import('./overview/description-overview.module').then(m => m.DescriptionOverviewModule),
		data: {
			breadcrumb: true,
			...BreadcrumbService.generateRouteDataConfiguration({
				hideNavigationItem: true
			}),
		}
	},
	{
		path: '',
		loadChildren: () => import('./listing/description-listing.module').then(m => m.DescriptionListingModule),
		data: {
			breadcrumb: true,
			isPublic: true
		},
	},
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	providers: []
})
export class DescriptionRoutingModule { }

@NgModule({
	imports: [RouterModule.forChild(publicRoutes)],
	exports: [RouterModule],
	providers: []
})
export class PublicDescriptionRoutingModule { }
