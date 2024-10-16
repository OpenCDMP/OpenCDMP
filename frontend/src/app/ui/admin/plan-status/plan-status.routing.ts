import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { AuthGuard } from '@app/core/auth-guard.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { PendingChangesGuard } from '@common/forms/pending-form-changes/pending-form-changes-guard.service';
import { PlanStatusListingComponent } from './listing/plan-status-listing/plan-status-listing.component';
import { PlanStatusEditorComponent } from './editor/plan-status-editor/plan-status-editor.component';
import { PlanStatusEditorResolver } from './editor/plan-status-editor.resolver';

const routes: Routes = [
	{
		path: '',
		component: PlanStatusListingComponent,
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
	    component: PlanStatusEditorComponent,
		canDeactivate: [PendingChangesGuard],
		data: {
			authContext: {
				permissions: [AppPermission.EditPlanStatus]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'PLAN-STATUS-EDITOR.TITLE.NEW'
			}),
			getFromTitleService: true,
			usePrefix: false
		}
	},
	{
		path: ':id',
		canActivate: [AuthGuard],
		component: PlanStatusEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': PlanStatusEditorResolver
		},
		data: {
			authContext: {
				permissions: [AppPermission.EditPlanStatus]
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
    providers: [PlanStatusEditorResolver]
})
export class PlanStatusRoutingModule { }
