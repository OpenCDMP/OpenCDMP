import { DragDropModule } from '@angular/cdk/drag-drop';
import { NgModule } from '@angular/core';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { NotifierListEditorComponent } from './notifier-list-editor.component';
import { DragAndDropAccessibilityService } from '@app/core/services/accessibility/drag-and-drop-accessibility.service';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		DragDropModule
	],
	declarations: [
		NotifierListEditorComponent
	],
	exports: [
		NotifierListEditorComponent
	],
    providers: [
        DragAndDropAccessibilityService
    ]
})
export class NotifierListModule { }
