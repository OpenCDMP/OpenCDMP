import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LogoutComponent } from './logout.component';

const routes: Routes = [
	{
		path: '',
		component: LogoutComponent
	},
	{ path: '**', loadChildren: () => import('@common/modules/page-not-found/page-not-found.module').then(m => m.PageNotFoundModule) },
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule]
})
export class LogoutRoutingModule { }
