import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { PendingChangesGuard } from '@common/forms/pending-form-changes/pending-form-changes-guard.service';
import { WebhookEditorComponent } from './editor/webhook-editor.component';
import { WebhookEditorResolver } from './editor/webhook-editor.resolver';
import { WebhookListingComponent } from './listing/webhook-listing.component';

const routes: Routes = [
	{
		path: '',
		component: WebhookListingComponent
	},
	{
		path: 'new',
		component: WebhookEditorComponent,
		canDeactivate: [PendingChangesGuard],
		data: {
			authContext: {
				permissions: [AppPermission.EditWebhookSubscription]
			},
			...BreadcrumbService.generateRouteDataConfiguration({
				title: 'BREADCRUMBS.NEW-WEBHOOK'
			})
		}
	},
	{
		path: ':id',
		component: WebhookEditorComponent,
		canDeactivate: [PendingChangesGuard],
		resolve: {
			'entity': WebhookEditorResolver
		},
		data: {
			authContext: {
				permissions: [AppPermission.EditWebhookSubscription]
			}
		}
	},
	{ path: '**', loadComponent: () => import('@common/modules/page-not-found/page-not-found.component').then(m => m.PageNotFoundComponent)},
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	providers: [WebhookEditorResolver]
})
export class WebhooksRoutingModule { }
