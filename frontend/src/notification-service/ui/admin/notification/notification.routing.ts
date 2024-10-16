import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '@app/core/auth-guard.service';
import { NotificationListingComponent } from './notification-listing.component';

const routes: Routes = [
	{
		path: '',
		component: NotificationListingComponent,
		canActivate: [AuthGuard]
	},
	{ path: '**', loadChildren: () => import('@common/modules/page-not-found/page-not-found.module').then(m => m.PageNotFoundModule) },
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	providers: []
})
export class NotificationRoutingModule { }
