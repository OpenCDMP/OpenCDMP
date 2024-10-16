import { DragDropModule } from '@angular/cdk/drag-drop';
import { NgModule } from '@angular/core';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { NotifierListEditorComponent } from './notifier-list-editor.component';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		DragDropModule,
	],
	declarations: [
		NotifierListEditorComponent
	],
	exports: [
		NotifierListEditorComponent
	]
})
export class NotifierListModule { }
