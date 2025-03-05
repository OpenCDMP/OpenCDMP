import { NgModule } from '@angular/core';
import { FormattingModule } from "@app/core/formatting.module";
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { RichTextEditorModule } from "@app/library/rich-text-editor/rich-text-editor.module";
import { AnnotationDialogModule } from '@app/ui/annotations/annotation-dialog-component/annotation-dialog.module';
import { ReferenceFieldModule } from '@app/ui/reference/reference-field/reference-field.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { NgxDropzoneModule } from "ngx-dropzone";
import { FormFieldSetEditorDialogComponent } from './components/form-field-set/dialog-editor/form-fieldset-editor-dialog.component';
import { DescriptionFormFieldSetTitleComponent } from './components/form-field-set/field-set-title/field-set-title.component';
import { DescriptionFormFieldSetComponent } from './components/form-field-set/form-field-set.component';
import { DescriptionFormFieldComponent } from './components/form-field/form-field.component';
import { DescriptionFormSectionComponent } from './components/form-section/form-section.component';
import { DescriptionFormComponent } from './description-form.component';
import { FormAnnotationService } from '../../../annotations/annotation-dialog-component/form-annotation.service';
import { TagsFieldModule } from '@app/ui/tag/tags-field/tags-field.module';
import { DescriptionFormService } from './components/services/description-form.service';
import { FormFocusDirective } from '@common/forms/form-focus.directive';


@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		AutoCompleteModule,
		RichTextEditorModule,
		NgxDropzoneModule,
		FormattingModule,
		ReferenceFieldModule,
		AnnotationDialogModule,
		TagsFieldModule,
        FormFocusDirective,
	],
	declarations: [
		DescriptionFormComponent,
		DescriptionFormSectionComponent,
		DescriptionFormFieldSetComponent,
		DescriptionFormFieldComponent,
		DescriptionFormFieldSetTitleComponent,
		FormFieldSetEditorDialogComponent
	],
	exports: [
		DescriptionFormComponent,
		DescriptionFormFieldSetComponent
	],
	providers: [
		FormAnnotationService,
		DescriptionFormService,
	]
})
export class DescriptionFormModule { }

