import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TermsComponent } from './terms.component';

const routes: Routes = [
	{
		path: '',
		component: TermsComponent,
	},
	{ path: '**', loadComponent: () => import('@common/modules/page-not-found/page-not-found.component').then(m => m.PageNotFoundComponent)}
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule]
})
export class TermsRoutingModule { }
