import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ContactContentComponent } from './contact-content/contact-content.component';

const routes: Routes = [
	{
		path: '',
		component: ContactContentComponent,
	},
	{ path: '**', loadComponent: () => import('@common/modules/page-not-found/page-not-found.component').then(m => m.PageNotFoundComponent)}
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule]
})
export class ContactRoutingModule { }
