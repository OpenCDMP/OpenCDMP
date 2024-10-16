import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { AuthGuard } from "@app/core/auth-guard.service";
import { AppPermission } from "@app/core/common/enum/permission.enum";
import { BreadcrumbService } from "@app/ui/misc/breadcrumb/breadcrumb.service";
import { PendingChangesGuard } from "@common/forms/pending-form-changes/pending-form-changes-guard.service";
import { DescriptionTemplateTypeEditorComponent } from './editor/description-template-type-editor.component';
import { DescriptionTemplateTypeEditorResolver } from "./editor/description-template-type-editor.resolver";
import { DescriptionTemplateTypeListingComponent } from "./listing/description-template-type-listing.component";

const routes: Routes = [
	{
		path: '',
		component: DescriptionTemplateTypeListingComponent,
		canActivate: [AuthGuard]
	},
	{
		path: 'new',
		canActivate: [AuthGuard],
		data: {
			authContext: {
				permissions: [AppPermission.EditDescriptionTemplateType]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.NEW-DESCRIPTION-TEMPLATE-TYPE'
			}),
			getFromTitleService: true,
			usePrefix: false
		},
		component: DescriptionTemplateTypeEditorComponent,
		canDeactivate: [PendingChangesGuard],
	},
	{
		path: ':id',
		canActivate: [AuthGuard],
		component: DescriptionTemplateTypeEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': DescriptionTemplateTypeEditorResolver
		},
		data: {
			getFromTitleService: true,
			usePrefix: false,
			authContext: {
				permissions: [AppPermission.EditDescriptionTemplateType]
			}
		}

	},
	{ path: '**', loadChildren: () => import('@common/modules/page-not-found/page-not-found.module').then(m => m.PageNotFoundModule) },
]

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	providers: [DescriptionTemplateTypeEditorResolver]
})
export class DescriptionTemplateTypesRoutingModule { }
