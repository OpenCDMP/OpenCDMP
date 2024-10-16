import { NgModule } from '@angular/core';
import { CookiesPolicyComponent } from '@app/ui/sidebar/sidebar-footer/cookies-policy/cookies-policy.component';
import { CookiesPolicyRoutingModule } from '@app/ui/sidebar/sidebar-footer/cookies-policy/cookies-policy.routing';
import { CommonUiModule } from '@common/ui/common-ui.module';

@NgModule({
	imports: [
		CommonUiModule,
		CookiesPolicyRoutingModule
	],
	declarations: [
		CookiesPolicyComponent
	],
})
export class CookiesPolicyModule { }
