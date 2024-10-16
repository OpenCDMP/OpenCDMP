import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { AuthGuard } from '@app/core/auth-guard.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { PendingChangesGuard } from '@common/forms/pending-form-changes/pending-form-changes-guard.service';
import { StatusListingComponent } from './listing/status-listing.component';
import { StatusEditorResolver } from './editor/status-editor.resolver';
import { StatusEditorComponent } from './editor/status-editor.component';

const routes: Routes = [
	{
		path: '',
		component: StatusListingComponent,
		canActivate: [AuthGuard]
	},
	{
		path: 'new',
		canActivate: [AuthGuard],
		component: StatusEditorComponent,
		canDeactivate: [PendingChangesGuard],
		data: {
			authContext: {
				permissions: [AppPermission.EditStatus]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.NEW-TENANT'
			}),
			getFromTitleService: true,
			usePrefix: false
		}
	},
	{
		path: ':id',
		canActivate: [AuthGuard],
		component: StatusEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': StatusEditorResolver
		},
		data: {
			authContext: {
				permissions: [AppPermission.EditStatus]
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
	providers: [StatusEditorResolver]
})
export class StatusRoutingModule { }
