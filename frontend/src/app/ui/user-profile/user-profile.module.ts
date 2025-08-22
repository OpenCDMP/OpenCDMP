import { NgModule } from '@angular/core';
import { FormattingModule } from '@app/core/formatting.module';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { FormValidationErrorsDialogModule } from '@common/forms/form-validation-errors-dialog/form-validation-errors-dialog.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { AddAccountDialogModule } from './add-account/add-account-dialog.module';
import { UserProfileComponent } from './user-profile.component';
import { UserProfileRoutingModule } from './user-profile.routing';
import { UserProfileNotifierListModule } from '@notification-service/ui/user-profile/notifier-list/user-profile-notifier-list-editor.module';
import { FormFocusDirective } from '@common/forms/form-focus.directive';
import { PluginEditorModule } from '../plugin/plugin-editor.module';

@NgModule({
    imports: [
        CommonUiModule,
        CommonFormsModule,
        FormattingModule,
        UserProfileRoutingModule,
        AutoCompleteModule,
        AddAccountDialogModule,
        FormValidationErrorsDialogModule,
        UserProfileNotifierListModule,
        FormFocusDirective,
        PluginEditorModule,
    ],
    declarations: [
        UserProfileComponent
    ]
})
export class UserProfileModule { }
