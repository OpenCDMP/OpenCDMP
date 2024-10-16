import {NgModule} from "@angular/core";
import {CommonUiModule} from "@common/ui/common-ui.module";
import {CommonFormsModule} from "@common/forms/common-forms.module";
import {AngularEditorModule} from "@kolkov/angular-editor";
import {RichTextEditorComponent} from "@app/library/rich-text-editor/rich-text-editor.component";

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		AngularEditorModule
	],
	declarations: [
		RichTextEditorComponent
	],
	exports: [
		RichTextEditorComponent
	]
})
export class RichTextEditorModule { }
