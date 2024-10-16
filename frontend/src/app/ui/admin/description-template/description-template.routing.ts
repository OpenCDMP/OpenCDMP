import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '@app/core/auth-guard.service';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { PendingChangesGuard } from '@common/forms/pending-form-changes/pending-form-changes-guard.service';
import { DescriptionTemplateEditorComponent } from './editor/description-template-editor.component';
import { DescriptionTemplateEditorResolver } from './editor/description-template-editor.resolver';
import { DescriptionTemplateListingComponent } from './listing/description-template-listing.component';

const routes: Routes = [
	{
		path: '',
		component: DescriptionTemplateListingComponent,
		canActivate: [AuthGuard]
	},
	{
		path: 'versions/:groupid',
		component: DescriptionTemplateListingComponent,
		canActivate: [AuthGuard],
		data: {
			mode: 'versions-listing'
		}
	},
	{
		path: 'new',
		canActivate: [AuthGuard],
		component: DescriptionTemplateEditorComponent,
		canDeactivate: [PendingChangesGuard],
		data: {
			authContext: {
				permissions: [AppPermission.EditDescriptionTemplate]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.NEW-DESCRIPTION-TEMPLATES'
			}),
			getFromTitleService: true,
			usePrefix: false
		}
	},
	{
		path: 'clone/:cloneid',
		canActivate: [AuthGuard],
		component: DescriptionTemplateEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': DescriptionTemplateEditorResolver
		},
		data: {
			authContext: {
				permissions: [AppPermission.EditDescriptionTemplate]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				skipNavigation: true
			}),
			getFromTitleService: true,
			usePrefix: false,
			action: 'clone'
		}
	},
	{
		path: 'new-version/:newversionid',
		canActivate: [AuthGuard],
		component: DescriptionTemplateEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': DescriptionTemplateEditorResolver
		},
		data: {
			authContext: {
				permissions: [AppPermission.EditDescriptionTemplate]
			},
			getFromTitleService: true,
			usePrefix: false,
			action: 'new-version'
		}
	},
	{
		path: ':id',
		canActivate: [AuthGuard],
		component: DescriptionTemplateEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': DescriptionTemplateEditorResolver
		},
		data: {
			getFromTitleService: true,
			usePrefix: false
		}

	},
	{ path: '**', loadChildren: () => import('@common/modules/page-not-found/page-not-found.module').then(m => m.PageNotFoundModule) },
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	providers: [DescriptionTemplateEditorResolver]
})
export class DescriptionTemplateRoutingModule { }
