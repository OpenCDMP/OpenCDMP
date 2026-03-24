import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FaqContentComponent } from './faq-content/faq-content.component';

const routes: Routes = [
	{
		path: '',
		component: FaqContentComponent,
	},
	{ path: '**', loadComponent: () => import('@common/modules/page-not-found/page-not-found.component').then(m => m.PageNotFoundComponent)}
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule]
})
export class FaqRoutingModule { }
