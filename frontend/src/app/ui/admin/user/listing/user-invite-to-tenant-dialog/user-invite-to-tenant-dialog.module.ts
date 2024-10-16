import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { UserInviteToTenantDialogComponent } from './user-invite-to-tenant-dialog.component';

@NgModule({
	imports: [CommonUiModule, FormsModule, ReactiveFormsModule],
	declarations: [UserInviteToTenantDialogComponent],
	exports: [UserInviteToTenantDialogComponent]
})
export class UserInviteToTenantDialogModule {
	constructor() { }
}