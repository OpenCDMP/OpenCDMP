import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UnauthorizedComponent } from './unauthorized.component';

const routes: Routes = [
	{
		path: '',
		component: UnauthorizedComponent
	},
	{ path: '**', loadComponent: () => import('@common/modules/page-not-found/page-not-found.component').then(m => m.PageNotFoundComponent)}
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule]
})
export class UnauthorizedRoutingModule { }