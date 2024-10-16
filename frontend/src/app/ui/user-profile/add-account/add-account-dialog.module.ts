import { NgModule } from '@angular/core';
import { FormattingModule } from '@app/core/formatting.module';
import { LoginModule } from '@app/ui/auth/login/login.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { AddAccountDialogComponent } from './add-account-dialog.component';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		FormattingModule,
		LoginModule
	],
	declarations: [
		AddAccountDialogComponent
	]
})
export class AddAccountDialogModule { }
