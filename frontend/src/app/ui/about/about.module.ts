import { NgModule } from '@angular/core';
import { AboutComponent } from '@app/ui/about/about.component';
import { AboutRoutingModule } from '@app/ui/about/about.routing';
import { CommonUiModule } from '@common/ui/common-ui.module';

@NgModule({
	imports: [
		CommonUiModule,
		AboutRoutingModule
	],
	declarations: [
		AboutComponent
	]
})
export class AboutModule { }
