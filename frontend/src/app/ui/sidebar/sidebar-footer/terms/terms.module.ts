import { NgModule } from '@angular/core';
import { TermsComponent } from '@app/ui/sidebar/sidebar-footer/terms/terms.component';
import { TermsRoutingModule } from '@app/ui/sidebar/sidebar-footer/terms/terms.routing';
import { CommonUiModule } from '@common/ui/common-ui.module';

@NgModule({
	imports: [
		CommonUiModule,
		TermsRoutingModule
	],
	declarations: [
		TermsComponent
	],
})
export class TermsModule { }
