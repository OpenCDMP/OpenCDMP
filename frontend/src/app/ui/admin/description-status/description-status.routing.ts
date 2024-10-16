import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { AuthGuard } from '@app/core/auth-guard.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { PendingChangesGuard } from '@common/forms/pending-form-changes/pending-form-changes-guard.service';
import { DescriptionStatusEditorComponent } from './editor/description-status-editor/description-status-editor.component';
import { DescriptionStatusListingComponent } from './listing/description-status-listing/description-status-listing.component';
import { DescriptionStatusEditorResolver } from './editor/description-status-editor/description-status-editor.resolver';

const routes: Routes = [
	{
		path: '',
		component: DescriptionStatusListingComponent,
		canActivate: [AuthGuard],
        data: {
			breadcrumb: true,
			...BreadcrumbService.generateRouteDataConfiguration({
				hideNavigationItem: true
			}),
		}
	},
	{
		path: 'new',
		canActivate: [AuthGuard],
	    component: DescriptionStatusEditorComponent,
		canDeactivate: [PendingChangesGuard],
		data: {
			authContext: {
				permissions: [AppPermission.EditDescriptionStatus]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'DESCRIPTION-STATUS-EDITOR.TITLE.NEW'
			}),
			getFromTitleService: true,
			usePrefix: false
		}
	},
	{
		path: ':id',
		canActivate: [AuthGuard],
		component: DescriptionStatusEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': DescriptionStatusEditorResolver
		},
		data: {
			authContext: {
				permissions: [AppPermission.EditDescriptionStatus]
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
    providers: [DescriptionStatusEditorResolver]
})
export class DescriptionStatusRoutingModule { }
