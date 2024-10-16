import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '@app/core/auth-guard.service';
import { LockListingComponent } from './lock-listing.component';

const routes: Routes = [
	{
		path: '',
		component: LockListingComponent,
		canActivate: [AuthGuard]
	},
	{ path: '**', loadChildren: () => import('@common/modules/page-not-found/page-not-found.module').then(m => m.PageNotFoundModule) },
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	providers: []
})
export class LockRoutingModule { }
