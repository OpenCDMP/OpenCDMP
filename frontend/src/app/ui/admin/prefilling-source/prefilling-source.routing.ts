import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PrefillingSourceEditorComponent } from './editor/prefilling-source-editor.component';
import { PrefillingSourceListingComponent } from './listing/prefilling-source-listing.component';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { AuthGuard } from '@app/core/auth-guard.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { PendingChangesGuard } from '@common/forms/pending-form-changes/pending-form-changes-guard.service';
import { PrefillingSourceEditorResolver } from './editor/prefilling-source-editor.resolver';

const routes: Routes = [
	{
		path: '',
		component: PrefillingSourceListingComponent,
		canActivate: [AuthGuard]
	},
	{
		path: 'new',
		canActivate: [AuthGuard],
		component: PrefillingSourceEditorComponent,
		canDeactivate: [PendingChangesGuard],
		data: {
			authContext: {
				permissions: [AppPermission.EditPrefillingSource]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.NEW-PREFILLING-SOURCE'
			}),
			getFromTitleService: true,
			usePrefix: false
		}
	},
	{
		path: ':id',
		canActivate: [AuthGuard],
		component: PrefillingSourceEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': PrefillingSourceEditorResolver
		},
		data: {
			authContext: {
				permissions: [AppPermission.EditPrefillingSource]
			},
			getFromTitleService: true,
			usePrefix: false
		}

	},
	{ path: '**', loadChildren: () => import('@common/modules/page-not-found/page-not-found.module').then(m => m.PageNotFoundModule) },
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	providers: [PrefillingSourceEditorResolver]
})
export class PrefillingSourceRoutingModule { }
