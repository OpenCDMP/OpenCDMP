import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GlossaryContentComponent } from './glossary-content/glossary-content.component';

const routes: Routes = [
	{
		path: '',
		component: GlossaryContentComponent,
	},
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule]
})
export class GlossaryRoutingModule { }
