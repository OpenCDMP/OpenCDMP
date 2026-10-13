import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PlanBlueprintListingComponent } from './listing/plan-blueprint-listing.component';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { AuthGuard } from '@app/core/auth-guard.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { PendingChangesGuard } from '@common/forms/pending-form-changes/pending-form-changes-guard.service';
import { PlanBlueprintEditorResolver } from './editor/plan-blueprint-editor.resolver';
import { PlanBlueprintEditorComponent } from './editor/plan-blueprint-editor.component';
import { Bp2VersionHistoryComponent } from '../structure-editor/version-history/version-history.component';
import { VersionHistorySource } from '../structure-editor/version-history/version-history-source';
import { BlueprintVersionSource } from './editor/blueprint-version-source';

const routes: Routes = [
	{
		path: '',
		component: PlanBlueprintListingComponent,
		canActivate: [AuthGuard]
	},
	{
		path: 'versions/:groupid',
		canActivate: [AuthGuard],
		component: Bp2VersionHistoryComponent,
		providers: [{ provide: VersionHistorySource, useClass: BlueprintVersionSource }],
		data: {
			...BreadcrumbService.generateRouteDataConfiguration({ skipNavigation: true })
		}
	},
	{
		path: 'new',
		canActivate: [AuthGuard],
		component: PlanBlueprintEditorComponent,
		canDeactivate: [PendingChangesGuard],
		data: {
			authContext: { permissions: [AppPermission.EditPlanBlueprint] },
			...BreadcrumbService.generateRouteDataConfiguration({ title: 'BREADCRUMBS.NEW-PLAN-BLUEPRINT' })
		}
	},
	{
		path: 'clone/:cloneid',
		canActivate: [AuthGuard],
		component: PlanBlueprintEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: { 'entity': PlanBlueprintEditorResolver },
		data: {
			...BreadcrumbService.generateRouteDataConfiguration({ skipNavigation: true }),
			authContext: { permissions: [AppPermission.EditPlanBlueprint] },
			action: 'clone'
		}
	},
	{
		path: 'new-version/:newversionid',
		canActivate: [AuthGuard],
		component: PlanBlueprintEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: { 'entity': PlanBlueprintEditorResolver },
		data: {
			...BreadcrumbService.generateRouteDataConfiguration({ skipNavigation: true }),
			authContext: { permissions: [AppPermission.EditPlanBlueprint] },
			action: 'new-version'
		}
	},
	{
		path: ':id',
		canActivate: [AuthGuard],
		component: PlanBlueprintEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: { 'entity': PlanBlueprintEditorResolver },
		data: {
			...BreadcrumbService.generateRouteDataConfiguration({ skipNavigation: true }),
			authContext: { permissions: [AppPermission.EditPlanBlueprint] }
		}
	},
	{ path: '**', loadComponent: () => import('@common/modules/page-not-found/page-not-found.component').then(m => m.PageNotFoundComponent)},
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	providers: [PlanBlueprintEditorResolver]
})
export class PlanBlueprintRoutingModule { }
