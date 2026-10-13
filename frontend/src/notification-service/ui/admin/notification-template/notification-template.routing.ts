import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '@app/core/auth-guard.service';

const routes: Routes = [
	{
		path: '',
		loadComponent: () => import('./templates/notification-templates.component').then(m => m.NotificationTemplatesComponent),
		canActivate: [AuthGuard]
	},
	// the old one-template editor; every template is edited from the list now
	{ path: '**', redirectTo: '' }
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule]
})
export class NotificationTemplateRoutingModule { }
