import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './login.component';
import { MergeEmailConfirmation } from './merge-email-confirmation/merge-email-confirmation.component';
import { UnlinkEmailConfirmation } from './unlink-email-confirmation/unlink-email-confirmation.component';
import { AuthGuard } from '@app/core/auth-guard.service';
import { UserInviteConfirmation } from './user-invite-confirmation/user-invite-confirmation.component';

const routes: Routes = [
	{ path: '', component: LoginComponent },
	{
		path: 'merge/confirmation/:token',
		component: MergeEmailConfirmation,
		canActivate: [AuthGuard]
	},
	{ path: 'unlink/confirmation/:token', component: UnlinkEmailConfirmation },
	{ path: 'invitation/confirmation/:token', component: UserInviteConfirmation },

];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule]
})
export class LoginRoutingModule { }
