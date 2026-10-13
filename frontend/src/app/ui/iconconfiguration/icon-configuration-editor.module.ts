import { NgModule } from '@angular/core';
import { FormattingModule } from '@app/core/formatting.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { IconConfigurationEditorComponent } from './icon-configuration-editor.component';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { FileUploadComponent } from '@app/library/file-uploader/file-uploader.component';
import { CommonFormattingModule } from '@common/formatting/common-formatting.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';

@NgModule({
    imports: [
        CommonUiModule,
        CommonFormsModule,
        DragDropModule,
        FormattingModule,
        CommonFormattingModule,
        FileUploadComponent,
        IconConfigurationEditorComponent,
    ],
    exports: [
        IconConfigurationEditorComponent,
    ]
})
export class IconConfigurationEditorModule { }
