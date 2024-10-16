import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FaqContentComponent } from './faq-content/faq-content.component';

const routes: Routes = [
	{
		path: '',
		component: FaqContentComponent,
	},
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule]
})
export class FaqRoutingModule { }
