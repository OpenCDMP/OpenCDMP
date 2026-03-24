import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UserGuideContentComponent } from './user-guide-content/user-guide-content.component';


const routes: Routes = [
	{
		path: '',
		component: UserGuideContentComponent,
	},
	{ path: '**', loadComponent: () => import('@common/modules/page-not-found/page-not-found.component').then(m => m.PageNotFoundComponent)}
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule]
})
export class UserGuideRoutingModule { }
