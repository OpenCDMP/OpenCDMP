import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '@app/core/auth-guard.service';

const routes: Routes = [
	{
		path: '',
		loadComponent: () => import('./help-pages/help-pages.component').then(m => m.HelpPagesComponent),
		canActivate: [AuthGuard]
	},
	// the old one-material editor; every page is edited from the grid now
	{ path: '**', redirectTo: '' }
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule]
})
export class SupportiveMaterialRoutingModule { }
