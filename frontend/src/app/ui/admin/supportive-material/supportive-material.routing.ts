import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SupportiveMaterialEditorComponent } from './editor/supportive-material-editor.component';
import { SupportiveMaterialListingComponent } from './listing/supportive-material-listing.component';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { AuthGuard } from '@app/core/auth-guard.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { PendingChangesGuard } from '@common/forms/pending-form-changes/pending-form-changes-guard.service';
import { SupportiveMaterialEditorResolver } from './editor/supportive-material-editor.resolver';

const routes: Routes = [
	{
		path: '',
		component: SupportiveMaterialListingComponent,
		canActivate: [AuthGuard]
	},
	{
		path: 'new',
		canActivate: [AuthGuard],
		component: SupportiveMaterialEditorComponent,
		canDeactivate: [PendingChangesGuard],
		data: {
			authContext: {
				permissions: [AppPermission.EditSupportiveMaterial]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.NEW-SUPPORTIVE-MATERIAL'
			})
		}
	},
	{
		path: ':id',
		canActivate: [AuthGuard],
		component: SupportiveMaterialEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': SupportiveMaterialEditorResolver
		},
		data: {
			authContext: {
				permissions: [AppPermission.EditSupportiveMaterial]
			}
		}

	},
	{ path: '**', loadChildren: () => import('@common/modules/page-not-found/page-not-found.module').then(m => m.PageNotFoundModule) },
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	providers: [SupportiveMaterialEditorResolver]
})
export class SupportiveMaterialRoutingModule { }
