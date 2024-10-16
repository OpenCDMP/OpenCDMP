import { NgModule } from "@angular/core";
import { UnauthorizedComponent } from '@app/ui/misc/unauthorized/unauthorized.component';
import { UnauthorizedRoutingModule } from '@app/ui/misc/unauthorized/unauthorized.routes';
import { CommonUiModule } from '@common/ui/common-ui.module';

@NgModule({
	imports: [
		CommonUiModule,
		UnauthorizedRoutingModule
	],
	declarations: [
		UnauthorizedComponent,
	],
})
export class UnauthorizedModule { }