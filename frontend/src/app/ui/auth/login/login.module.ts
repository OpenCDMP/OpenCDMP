import { NgModule } from '@angular/core';
import { LoginComponent } from '@app/ui/auth/login/login.component';
import { LoginRoutingModule } from '@app/ui/auth/login/login.routing';
import { LoginService } from '@app/ui/auth/login/utilities/login.service';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { MergeEmailConfirmation } from './merge-email-confirmation/merge-email-confirmation.component';
import { UnlinkEmailConfirmation } from './unlink-email-confirmation/unlink-email-confirmation.component';
import { UserInviteConfirmation } from './user-invite-confirmation/user-invite-confirmation.component';
import { MergeEmailLoaderDialogComponent } from './merge-email-confirmation/merge-email-loader-dialog/merge-email-loader-dialog.component';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		LoginRoutingModule,
	],
	declarations: [
		LoginComponent,
		MergeEmailConfirmation,
		UnlinkEmailConfirmation,
		UserInviteConfirmation,
		MergeEmailLoaderDialogComponent,
	],
	exports: [
		LoginComponent
	],
	providers: [LoginService]
})
export class LoginModule { }
