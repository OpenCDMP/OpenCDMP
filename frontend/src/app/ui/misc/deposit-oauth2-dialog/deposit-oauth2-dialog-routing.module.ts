import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DepositOauth2DialogComponent } from './deposit-oauth2-dialog.component';

const routes: Routes = [
	{
		path: '',
		component: DepositOauth2DialogComponent,
		data: {
			showOnlyRouterOutlet: true
		}
	},
	{
		path: 'code-callaback',
		component: DepositOauth2DialogComponent,
		data: {
			showOnlyRouterOutlet: true
		}
	},
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	providers: []
})
export class DepositOauth2DialogRoutingModule { }
