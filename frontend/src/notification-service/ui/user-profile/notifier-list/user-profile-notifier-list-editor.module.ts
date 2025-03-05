import { DragDropModule } from '@angular/cdk/drag-drop';
import { NgModule } from '@angular/core';
import { DragAndDropAccessibilityService } from '@app/core/services/accessibility/drag-and-drop-accessibility.service';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { UserProfileNotifierListEditorComponent } from '@notification-service/ui/user-profile/notifier-list/user-profile-notifier-list-editor.component';
import {CdkListboxModule} from '@angular/cdk/listbox';
@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		DragDropModule,
        CdkListboxModule
	],
	declarations: [
		UserProfileNotifierListEditorComponent
	],
	exports: [
		UserProfileNotifierListEditorComponent
	],
    providers: [
        DragAndDropAccessibilityService
    ]
})
export class UserProfileNotifierListModule { }
