import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ReferenceEditorComponent } from './editor/reference-editor.component';
import { ReferenceListingComponent } from './listing/reference-listing.component';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { AuthGuard } from '@app/core/auth-guard.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { PendingChangesGuard } from '@common/forms/pending-form-changes/pending-form-changes-guard.service';
import { ReferenceEditorResolver } from './editor/reference-editor.resolver';

const routes: Routes = [
	{
		path: '',
		component: ReferenceListingComponent,
		canActivate: [AuthGuard],
		data: {
			breadcrumb: true
		}
	},
	{
		path: 'new',
		canActivate: [AuthGuard],
		component: ReferenceEditorComponent,
		canDeactivate: [PendingChangesGuard],
		data: {
			authContext: {
				permissions: [AppPermission.EditReference]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.NEW-REFERENCE'
			}),
			getFromTitleService: true,
			usePrefix: false
		}
	},
	{
		path: ':id',
		canActivate: [AuthGuard],
		component: ReferenceEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': ReferenceEditorResolver
		},
		data: {
			breadcrumb: true,
			authContext: {
				permissions: [AppPermission.EditReference]
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
	providers: [ReferenceEditorResolver]
})
export class ReferenceRoutingModule { }
