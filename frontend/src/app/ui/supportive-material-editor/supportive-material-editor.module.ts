import { NgModule } from '@angular/core';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { EditorModule } from '@tinymce/tinymce-angular';
import { SupportiveMaterialEditorComponent } from './supportive-material-editor.component';
import { SupportiveMaterialEditorRoutingModule } from './supportive-material-editor.routing';


@NgModule({
	declarations: [SupportiveMaterialEditorComponent],
	imports: [
		CommonUiModule,
		CommonFormsModule,
		SupportiveMaterialEditorRoutingModule,
		EditorModule
	],
	providers: [
		EnumUtils
	],
})
export class SupportiveMaterialEditorModule { }
