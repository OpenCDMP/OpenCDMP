import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '@app/core/auth-guard.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { PendingChangesGuard } from '@common/forms/pending-form-changes/pending-form-changes-guard.service';
import { DescriptionEditorComponent } from './description-editor.component';
import { DescriptionEditorEntityResolver } from './resolvers/description-editor-entity.resolver';
import { DescriptionEditorPermissionsResolver } from './resolvers/description-editor-permissions.resolver';

const routes: Routes = [
	{
		path: ':id',
		canActivate: [AuthGuard],
		component: DescriptionEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': DescriptionEditorEntityResolver,
			'permissions': DescriptionEditorPermissionsResolver,
		},
		data: {
			breadcrumbs: true,
		}
	},
	{
		path: ':id/finalize',
		canActivate: [AuthGuard],
		component: DescriptionEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': DescriptionEditorEntityResolver,
			'permissions': DescriptionEditorPermissionsResolver,
		},
		data: {
			breadcrumbs: true,
			...BreadcrumbService.generateRouteDataConfiguration({
				hideNavigationItem: true
			}),
		}
	},
	{
		path: ':id/f/:fieldsetId',
		canActivate: [AuthGuard],
		component: DescriptionEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': DescriptionEditorEntityResolver,
			'permissions': DescriptionEditorPermissionsResolver,
		},
		data: {
			breadcrumbs: true,
			...BreadcrumbService.generateRouteDataConfiguration({
				skipNavigation: true,
			}),
			scrollToField: true,
		}
	},
	{
		path: ':id/f/:fieldsetId/annotation',
		canActivate: [AuthGuard],
		component: DescriptionEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': DescriptionEditorEntityResolver,
			'permissions': DescriptionEditorPermissionsResolver,
		},
		data: {
			breadcrumbs: true,
			...BreadcrumbService.generateRouteDataConfiguration({
				skipNavigation: true,
			}),
			scrollToField: true,
			openAnnotation: true,
		}
	},
	{
		path: ':planId/:planSectionId',
		canActivate: [AuthGuard],
		component: DescriptionEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': DescriptionEditorEntityResolver,
			'permissions': DescriptionEditorPermissionsResolver,
		},
		data: {
			breadcrumbs: true,
			title: 'DESCRIPTION-EDITOR.TITLE-NEW'
		}
	},
	{
		path: 'copy/:id/:copyPlanId/:planSectionId',
		canActivate: [AuthGuard],
		component: DescriptionEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': DescriptionEditorEntityResolver,
			'permissions': DescriptionEditorPermissionsResolver,
		},
		data: {
			breadcrumbs: true,
			title: 'DESCRIPTION-EDITOR.TITLE-NEW',
			...BreadcrumbService.generateRouteDataConfiguration({
				skipNavigation: true
			})
		}
	},
	{ path: '**', loadComponent: () => import('@common/modules/page-not-found/page-not-found.component').then(m => m.PageNotFoundComponent)}
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	providers: [DescriptionEditorEntityResolver, DescriptionEditorPermissionsResolver]
})
export class DescriptionEditorRoutingModule { }
