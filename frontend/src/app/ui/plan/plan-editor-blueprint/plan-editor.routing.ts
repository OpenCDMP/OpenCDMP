import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { PendingChangesGuard } from '@common/forms/pending-form-changes/pending-form-changes-guard.service';
import { AuthGuard } from '@app/core/auth-guard.service';
import { PlanEditorComponent } from './plan-editor.component';
import { PlanEditorEntityResolver } from './resolvers/plan-editor-enitity.resolver';
import { PlanEditorPermissionsResolver } from './resolvers/plan-editor-permissions.resolver';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';

const routes: Routes = [
	{
		path: '',
		component: PlanEditorComponent,
		canActivate: [AuthGuard],
		canDeactivate: [PendingChangesGuard],
		data: {
			breadcrumb: true,
			authContext: {
				permissions: [AppPermission.NewPlan]
			}
		}
	},
	{
		path: ':id',
		canActivate: [AuthGuard],
		component: PlanEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': PlanEditorEntityResolver,
			'permissions': PlanEditorPermissionsResolver
		},
		data: {
			breadcrumb: true,
			getFromTitleService: true,
			usePrefix: false
		}
	},
	{
		path: ':id/f/:fieldId',
		canActivate: [AuthGuard],
		component: PlanEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': PlanEditorEntityResolver,
			'permissions': PlanEditorPermissionsResolver,
		},
		data: {
			breadcrumbs: true,
			getFromTitleService: true,
			usePrefix: false,
			...BreadcrumbService.generateRouteDataConfiguration({
				skipNavigation: true,
			}),
			title: 'PLAN-EDITOR.TITLE-EDIT',
			scrollToField: true,
		}
	},
	,
	{
		path: ':id/f/:fieldId/annotation',
		canActivate: [AuthGuard],
		component: PlanEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': PlanEditorEntityResolver,
			'permissions': PlanEditorPermissionsResolver,
		},
		data: {
			breadcrumbs: true,
			getFromTitleService: true,
			usePrefix: false,
			...BreadcrumbService.generateRouteDataConfiguration({
				skipNavigation: true,
			}),
			title: 'DESCRIPTION-EDITOR.TITLE-EDIT-DESCRIPTION',
			openAnnotation: true,
		}
	},
	{
		path: ':id/d/:descriptionId/f/:fieldId',
		canActivate: [AuthGuard],
		component: PlanEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': PlanEditorEntityResolver,
			'permissions': PlanEditorPermissionsResolver,
		},
		data: {
			breadcrumbs: true,
			getFromTitleService: true,
			usePrefix: false,
			...BreadcrumbService.generateRouteDataConfiguration({
				skipNavigation: true,
			}),
			title: 'PLAN-EDITOR.TITLE-EDIT',
			scrollToDescriptionField: true,
		}
	},
	{
		path: ':id/d/:descriptionId/f/:fieldId/annotation',
		canActivate: [AuthGuard],
		component: PlanEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': PlanEditorEntityResolver,
			'permissions': PlanEditorPermissionsResolver,
		},
		data: {
			breadcrumbs: true,
			getFromTitleService: true,
			usePrefix: false,
			...BreadcrumbService.generateRouteDataConfiguration({
				skipNavigation: true,
			}),
			title: 'PLAN-EDITOR.TITLE-EDIT',
			openDescriptionAnnotation: true,
		}
	},
    {
		path: ':id/d/:descriptionId/finalize',
		canActivate: [AuthGuard],
		component: PlanEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': PlanEditorEntityResolver,
			'permissions': PlanEditorPermissionsResolver,
		},
		data: {
			breadcrumbs: true,
			getFromTitleService: true,
			usePrefix: false,
			...BreadcrumbService.generateRouteDataConfiguration({
				skipNavigation: true,
			}),
			title: 'PLAN-EDITOR.TITLE-EDIT',
            finalizeDescription: true
		}
	}
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	providers: [PlanEditorEntityResolver, PlanEditorPermissionsResolver]
})
export class PlanEditorRoutingModule { }
