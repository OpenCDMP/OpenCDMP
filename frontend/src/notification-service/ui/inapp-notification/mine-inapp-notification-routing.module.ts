import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '@app/core/auth-guard.service';
import { MineInAppNotificationListingComponent } from './listing/mine-inapp-notification-listing.component';
import { InAppNotificationEditorComponent } from './editor/inapp-notification-editor.component';

const routes: Routes = [
	{
		path: '',
		component: MineInAppNotificationListingComponent,
		data: {
		},
		canActivate: [AuthGuard]
	},
	{
		path: 'dialog/:id',
		canActivate: [AuthGuard],
		data: {
			isFromDialog: true,
		},
		component: InAppNotificationEditorComponent
	},
	{
		path: ':id',
		canActivate: [AuthGuard],
		component: InAppNotificationEditorComponent
	},
	{ path: '**', loadComponent: () => import('@common/modules/page-not-found/page-not-found.component'). then(m => m.PageNotFoundComponent)}
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule]
})
export class MineInAppNotificationRoutingModule { }
