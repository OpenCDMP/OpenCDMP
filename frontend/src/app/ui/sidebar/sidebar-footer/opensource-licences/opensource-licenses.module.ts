import { NgModule } from '@angular/core';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { OpensourceLicencesRoutingModule } from './opensource-licences.routing';
import { OpensourceLicencesComponent } from './opensource-licences.component';

@NgModule({
	imports: [
		CommonUiModule,
		OpensourceLicencesRoutingModule
	],
	declarations: [
		OpensourceLicencesComponent
	],
})
export class OpensourceLicencesModule { }