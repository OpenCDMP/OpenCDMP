import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CookiesPolicyComponent } from './cookies-policy.component';

const routes: Routes = [
	{
		path: '',
		component: CookiesPolicyComponent,
	},
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule]
})
export class CookiesPolicyRoutingModule { }
