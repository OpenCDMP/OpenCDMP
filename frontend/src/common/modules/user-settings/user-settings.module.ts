import { DialogModule } from '@angular/cdk/dialog';
import { NgModule } from '@angular/core';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { ConfirmationDialogModule } from '@common/modules/confirmation-dialog/confirmation-dialog.module';
import { UserSettingsPickerComponent } from '@common/modules/user-settings/user-settings-picker/user-settings-picker.component';
import { UserSettingsSelectorComponent } from '@common/modules/user-settings/user-settings-selector/user-settings-selector.component';
import { CommonUiModule } from '@common/ui/common-ui.module';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		ConfirmationDialogModule,
		DialogModule,
		AutoCompleteModule
	],
	declarations: [
		UserSettingsSelectorComponent,
		UserSettingsPickerComponent,
	],
	exports: [
		UserSettingsSelectorComponent,
		UserSettingsPickerComponent
	]
})
export class UserSettingsModule {
	constructor() { }
}
