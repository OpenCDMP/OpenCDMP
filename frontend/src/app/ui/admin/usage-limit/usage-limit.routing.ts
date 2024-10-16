import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UsageLimitEditorComponent } from './editor/usage-limit-editor.component';
import { UsageLimitListingComponent } from './listing/usage-limit-listing.component';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { AuthGuard } from '@app/core/auth-guard.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { PendingChangesGuard } from '@common/forms/pending-form-changes/pending-form-changes-guard.service';
import { UsageLimitEditorResolver } from './editor/usage-limit-editor.resolver';

const routes: Routes = [
	{
		path: '',
		component: UsageLimitListingComponent,
		canActivate: [AuthGuard]
	},
	{
		path: 'new',
		canActivate: [AuthGuard],
		component: UsageLimitEditorComponent,
		canDeactivate: [PendingChangesGuard],
		data: {
			authContext: {
				permissions: [AppPermission.EditUsageLimit]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.NEW-USAGE-LIMIT'
			})
		}
	},
	{
		path: ':id',
		canActivate: [AuthGuard],
		component: UsageLimitEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': UsageLimitEditorResolver
		},
		data: {
			authContext: {
				permissions: [AppPermission.EditUsageLimit]
			}
		}

	},
	{ path: '**', loadChildren: () => import('@common/modules/page-not-found/page-not-found.module').then(m => m.PageNotFoundModule) },
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	providers: [UsageLimitEditorResolver]
})
export class UsageLimitRoutingModule { }
