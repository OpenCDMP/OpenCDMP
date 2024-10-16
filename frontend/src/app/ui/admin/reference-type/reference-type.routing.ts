import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ReferenceTypeEditorComponent } from './editor/reference-type-editor.component';
import { ReferenceTypeListingComponent } from './listing/reference-type-listing.component';
import { AuthGuard } from '@app/core/auth-guard.service';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { PendingChangesGuard } from '@common/forms/pending-form-changes/pending-form-changes-guard.service';
import { ReferenceTypeEditorResolver } from './editor/reference-type-editor.resolver';


const routes: Routes = [
  	{
		path: '',
		component: ReferenceTypeListingComponent,
		canActivate: [AuthGuard]
	},
	{
		path: 'new',
		component: ReferenceTypeEditorComponent,
		canActivate: [AuthGuard],
		canDeactivate: [PendingChangesGuard],
		data: {
				authContext: {
					permissions: [AppPermission.EditReferenceType]
				},
				...BreadcrumbService.generateRouteDataConfiguration({
					title: 'BREADCRUMBS.NEW-REFERENCE-TYPE'
				}),
				getFromTitleService: true,
				usePrefix: false
			}
	},
 	{
		path: ':id',
		canActivate: [AuthGuard],
		component: ReferenceTypeEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': ReferenceTypeEditorResolver
		},
		data: {
			authContext: {
				permissions: [AppPermission.EditReferenceType]
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
  providers: [ReferenceTypeEditorResolver]
})
export class ReferenceTypeRoutingModule { }
