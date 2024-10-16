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
			getFromTitleService: true,
			usePrefix: false

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
			title: 'DESCRIPTION-EDITOR.TITLE-EDIT-DESCRIPTION',
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
			getFromTitleService: true,
			usePrefix: false,
			...BreadcrumbService.generateRouteDataConfiguration({
				skipNavigation: true,
			}),
			title: 'DESCRIPTION-EDITOR.TITLE-EDIT-DESCRIPTION',
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
			getFromTitleService: true,
			usePrefix: false,
			...BreadcrumbService.generateRouteDataConfiguration({
				skipNavigation: true,
			}),
			title: 'DESCRIPTION-EDITOR.TITLE-EDIT-DESCRIPTION',
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
			title: 'DESCRIPTION-EDITOR.TITLE-NEW',
			getFromTitleService: true,
			usePrefix: false
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
			getFromTitleService: true,
			usePrefix: false,
			...BreadcrumbService.generateRouteDataConfiguration({
				skipNavigation: true
			})
		}
	},
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	providers: [DescriptionEditorEntityResolver, DescriptionEditorPermissionsResolver]
})
export class DescriptionEditorRoutingModule { }
