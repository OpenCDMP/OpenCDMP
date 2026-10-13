import { NgModule } from '@angular/core';
import { FormattingModule } from "@app/core/formatting.module";
import { AnnotationDialogModule } from '@app/ui/annotations/annotation-dialog-component/annotation-dialog.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { FormFieldSetEditorDialogComponent } from './components/form-field-set/dialog-editor/form-fieldset-editor-dialog.component';
import { FormFieldSetTitleComponent } from './components/form-field-set/field-set-title/field-set-title.component';
import { FormFieldSetComponent } from './components/form-field-set/form-field-set.component';
import { FormFieldComponent } from './components/form-field/form-field.component';
import { FormSectionComponent } from './components/form-section/form-section.component';
import { FormFocusDirective } from '@common/forms/form-focus.directive';
import { FormScrollService } from './components/services/form-scroll.service';


@NgModule({
    imports: [
        CommonUiModule,
        CommonFormsModule,
        FormattingModule,
        AnnotationDialogModule,
        FormFocusDirective,
        FormFieldComponent,
        FormSectionComponent,
        FormFieldSetComponent,
        FormFieldSetTitleComponent,
        FormFieldSetEditorDialogComponent
    ],
    exports: [
        FormSectionComponent,
        FormFieldSetComponent,
        FormFieldSetEditorDialogComponent,
        FormFieldComponent
    ],
    providers: [
        FormScrollService,
    ]
})
export class FieldEditorModule { }