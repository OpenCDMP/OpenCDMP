import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TenantEditorComponent } from './editor/tenant-editor.component';
import { TenantListingComponent } from './listing/tenant-listing.component';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { AuthGuard } from '@app/core/auth-guard.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { PendingChangesGuard } from '@common/forms/pending-form-changes/pending-form-changes-guard.service';
import { TenantEditorResolver } from './editor/tenant-editor.resolver';

const routes: Routes = [
	{
		path: '',
		component: TenantListingComponent,
		canActivate: [AuthGuard]
	},
	{
		path: 'new',
		canActivate: [AuthGuard],
		component: TenantEditorComponent,
		canDeactivate: [PendingChangesGuard],
		data: {
			authContext: {
				permissions: [AppPermission.EditTenant]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.NEW-TENANT'
			})
		}
	},
	{
		path: ':id',
		canActivate: [AuthGuard],
		component: TenantEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': TenantEditorResolver
		},
		data: {
			authContext: {
				permissions: [AppPermission.EditTenant]
			}
		}

	},
	{ path: '**', loadChildren: () => import('@common/modules/page-not-found/page-not-found.module').then(m => m.PageNotFoundModule) },
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	providers: [TenantEditorResolver]
})
export class TenantRoutingModule { }
