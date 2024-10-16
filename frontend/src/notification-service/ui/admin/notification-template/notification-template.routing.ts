import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { AuthGuard } from '@app/core/auth-guard.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { PendingChangesGuard } from '@common/forms/pending-form-changes/pending-form-changes-guard.service';
import { NotificationTemplateListingComponent } from './listing/notification-template-listing.component';
import { NotificationTemplateEditorComponent } from './editor/notification-template-editor.component';
import { NotificationTemplateEditorResolver } from './editor/notification-template-editor.resolver';

const routes: Routes = [
	{
		path: '',
		component: NotificationTemplateListingComponent,
		canActivate: [AuthGuard]
	},
	{
		path: 'new',
		canActivate: [AuthGuard],
		component: NotificationTemplateEditorComponent,
		canDeactivate: [PendingChangesGuard],
		data: {
			authContext: {
				permissions: [AppPermission.EditNotificationTemplate]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.NEW-TENANT'
			}),
			getFromTitleService: true,
			usePrefix: false
		}
	},
	{
		path: ':id',
		canActivate: [AuthGuard],
		component: NotificationTemplateEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': NotificationTemplateEditorResolver
		},
		data: {
			authContext: {
				permissions: [AppPermission.EditNotificationTemplate]
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
	providers: [NotificationTemplateEditorResolver]
})
export class NotificationTemplateRoutingModule { }
