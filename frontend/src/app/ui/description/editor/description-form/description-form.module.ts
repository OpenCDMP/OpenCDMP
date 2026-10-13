import { NgModule } from '@angular/core';
import { FormattingModule } from "@app/core/formatting.module";
import { AnnotationDialogModule } from '@app/ui/annotations/annotation-dialog-component/annotation-dialog.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { DescriptionFormComponent } from './description-form.component';
import { FormScrollService } from './components/services/form-scroll.service';
import { FormFocusDirective } from '@common/forms/form-focus.directive';
import { FileUploadComponent } from '@app/library/file-uploader/file-uploader.component';
import { FieldEditorModule } from './field-editor.module';


@NgModule({
    imports: [
    CommonUiModule,
    CommonFormsModule,
    FormattingModule,
    AnnotationDialogModule,
    FormFocusDirective,
    FileUploadComponent,
    FieldEditorModule,
    DescriptionFormComponent
],
    exports: [
        DescriptionFormComponent,
    ],
    providers: [
        FormScrollService,
    ]
})
export class DescriptionFormModule { }

