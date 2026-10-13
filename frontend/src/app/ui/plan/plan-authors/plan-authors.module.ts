import { NgModule } from "@angular/core";
import { AutoCompleteModule } from "@app/library/auto-complete/auto-complete.module";
import { CommonFormsModule } from "@common/forms/common-forms.module";
import { UserLogoPipe } from "@common/http/image/user-logo.pipe";
import { FallbackAvatarDirective } from "@common/http/image/fallback-avatar.directive";
import { CommonUiModule } from "@common/ui/common-ui.module";
import { PlanAuthorsEditorDialogComponent } from "./plan-authors-editor-dialog/plan-authors-editor-dialog.component";
import { PlanAuthorsComponent } from "./plan-authors.component";
import { InvitePlanUserComponent } from "./invite-plan-user/invite-plan-user.component";
import { ExistingPlanUsersEditorComponent } from "./existing-plan-users-editor/existing-plan-users-editor.component";
import { PlanUsersEditorWrapperComponent } from "./plan-users-editor-wrapper/plan-users-editor-wrapper.component";

@NgModule({
    imports: [
        CommonUiModule,
        AutoCompleteModule,
        CommonFormsModule,
        UserLogoPipe,
        FallbackAvatarDirective,
        PlanAuthorsEditorDialogComponent,
        PlanAuthorsComponent,
        InvitePlanUserComponent,
        ExistingPlanUsersEditorComponent,
        PlanUsersEditorWrapperComponent
    ],
    exports: [
        PlanAuthorsComponent,
        PlanUsersEditorWrapperComponent
    ],
    providers: []
})
export class PlanAuthorsModule { }