import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { OpensourceLicencesComponent } from './opensource-licences.component';

const routes: Routes = [
	{
		path: '',
		component: OpensourceLicencesComponent,
	},
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule]
})
export class OpensourceLicencesRoutingModule { }