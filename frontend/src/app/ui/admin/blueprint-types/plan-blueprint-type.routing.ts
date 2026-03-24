import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { AuthGuard } from "@app/core/auth-guard.service";
import { AppPermission } from "@app/core/common/enum/permission.enum";
import { BreadcrumbService } from "@app/ui/misc/breadcrumb/breadcrumb.service";
import { PendingChangesGuard } from "@common/forms/pending-form-changes/pending-form-changes-guard.service";
import { PlanBlueprintTypeEditorComponent } from "./editor/plan-blueprint-type-editor.component";
import { PlanBlueprintTypeListingComponent } from "./listing/plan-blueprint-type-listing.component";
import { PlanBlueprintTypeEditorResolver } from "./editor/plan-blueprint-type-editor.resolver";

const routes: Routes = [
	{
		path: '',
		component: PlanBlueprintTypeListingComponent,
		canActivate: [AuthGuard]
	},
	{
		path: 'new',
		canActivate: [AuthGuard],
		data: {
			authContext: {
				permissions: [AppPermission.EditPlanBlueprintType]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.NEW-DESCRIPTION-TEMPLATE-TYPE'
			}),
			getFromTitleService: true,
			usePrefix: false
		},
		component: PlanBlueprintTypeEditorComponent,
		canDeactivate: [PendingChangesGuard],
	},
	{
		path: ':id',
		canActivate: [AuthGuard],
		component: PlanBlueprintTypeEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': PlanBlueprintTypeEditorResolver
		},
		data: {
			getFromTitleService: true,
			usePrefix: false,
			authContext: {
				permissions: [AppPermission.EditPlanBlueprintType]
			}
		}

	},
	{ path: '**', loadComponent: () => import('@common/modules/page-not-found/page-not-found.component').then(m => m.PageNotFoundComponent)},
]

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	providers: [PlanBlueprintTypeEditorResolver]
})
export class PlanBlueprintTypesRoutingModule { }
