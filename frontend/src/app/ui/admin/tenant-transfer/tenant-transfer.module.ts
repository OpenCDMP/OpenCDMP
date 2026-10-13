import { NgModule } from '@angular/core';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { FileUploadComponent } from '@app/library/file-uploader/file-uploader.component';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { ConfirmationDialogModule } from '@common/modules/confirmation-dialog/confirmation-dialog.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { TenantTransferComponent } from './tenant-transfer.component';
import { TenantTransferRoutingModule } from './tenant-transfer.routing';

@NgModule({
    imports: [
        CommonUiModule,
        CommonFormsModule,
        ConfirmationDialogModule,
        AutoCompleteModule,
        FileUploadComponent,
        TenantTransferRoutingModule,
        TenantTransferComponent
    ]
})
export class TenantTransferModule { }
