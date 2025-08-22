import { DragDropModule } from '@angular/cdk/drag-drop';
import { NgModule } from '@angular/core';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { ViewPreferencesEditorComponent } from './view-preferences-editor.component';
import { DragAndDropAccessibilityService } from '@app/core/services/accessibility/drag-and-drop-accessibility.service';
import { CdkListboxModule } from '@angular/cdk/listbox';
import { ViewPreferencesReferenceEditorComponent } from './view-preferences-reference/view-preferences-reference-editor.component';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		DragDropModule,
        CdkListboxModule
	],
	declarations: [
		ViewPreferencesEditorComponent,
		ViewPreferencesReferenceEditorComponent
	],
	exports: [
		ViewPreferencesEditorComponent,
	],
    providers: [
        DragAndDropAccessibilityService
    ]
})
export class ViewPreferencesModule { }
