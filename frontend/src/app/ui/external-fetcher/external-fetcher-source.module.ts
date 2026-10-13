import { NgModule } from '@angular/core';
import { MatDialogModule } from '@angular/material/dialog';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { FileUploadComponent } from '@app/library/file-uploader/file-uploader.component';
import { ImportXmlDialogComponent } from './import-xml-dialog/import-xml.dialog.component';

@NgModule({
    imports: [
        CommonUiModule,
        FileUploadComponent,
        MatDialogModule,
        ImportXmlDialogComponent
    ],
    exports: [
        ImportXmlDialogComponent
    ]
})
export class ExternalFetcherSourceModule { }
