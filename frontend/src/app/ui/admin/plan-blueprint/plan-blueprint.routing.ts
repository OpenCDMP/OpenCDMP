import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PlanBlueprintEditorComponent } from './editor/plan-blueprint-editor.component';
import { PlanBlueprintListingComponent } from './listing/plan-blueprint-listing.component';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { AuthGuard } from '@app/core/auth-guard.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { PendingChangesGuard } from '@common/forms/pending-form-changes/pending-form-changes-guard.service';
import { PlanBlueprintEditorResolver } from './editor/plan-blueprint-editor.resolver';

const routes: Routes = [
	{
		path: '',
		component: PlanBlueprintListingComponent,
		canActivate: [AuthGuard]
	},
	{
		path: 'versions/:groupid',
		component: PlanBlueprintListingComponent,
		canActivate: [AuthGuard],
		data: {
			mode: 'versions-listing'
		}
	},
	{
		path: 'new',
		canActivate: [AuthGuard],
		component: PlanBlueprintEditorComponent,
		canDeactivate: [PendingChangesGuard],
		data: {
			authContext: {
				permissions: [AppPermission.EditPlanBlueprint]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.NEW-PLAN-BLUEPRINT'
			}),
			getFromTitleService: true,
			usePrefix: false
		}
	},
	{
		path: 'clone/:cloneid',
		canActivate: [AuthGuard],
		component: PlanBlueprintEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': PlanBlueprintEditorResolver
		},
		data: {
			...BreadcrumbService.generateRouteDataConfiguration({
				skipNavigation: true
			}),
			authContext: {
				permissions: [AppPermission.EditPlanBlueprint]
			},
			getFromTitleService: true,
			usePrefix: false,
			action: 'clone'
		}
	},
	{
		path: 'new-version/:newversionid',
		canActivate: [AuthGuard],
		component: PlanBlueprintEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': PlanBlueprintEditorResolver
		},
		data: {
			...BreadcrumbService.generateRouteDataConfiguration({
				skipNavigation: true
			}),
			authContext: {
				permissions: [AppPermission.EditPlanBlueprint]
			},
			getFromTitleService: true,
			usePrefix: false,
			action: 'new-version'
		}
	},
	{
		path: ':id',
		canActivate: [AuthGuard],
		component: PlanBlueprintEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': PlanBlueprintEditorResolver
		},
		data: {
			authContext: {
				permissions: [AppPermission.EditPlanBlueprint]
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
	providers: [PlanBlueprintEditorResolver]
})
export class PlanBlueprintRoutingModule { }
