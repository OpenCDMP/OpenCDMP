import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UserProfileComponent } from './user-profile.component';
import { AuthGuard } from '@app/core/auth-guard.service';

const routes: Routes = [
	{
		path: '',
		component: UserProfileComponent,
		canActivate: [AuthGuard],
		data: {
			breadcrumb: true
		},
	},
	{ path: '**', loadComponent: () => import('@common/modules/page-not-found/page-not-found.component').then(m => m.PageNotFoundComponent)}
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule]
})
export class UserProfileRoutingModule { }
