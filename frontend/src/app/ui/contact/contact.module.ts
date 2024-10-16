import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ContactContentComponent } from '@app/ui/contact/contact-content/contact-content.component';
import { ContactDialogComponent } from '@app/ui/contact/contact-dialog/contact-dialog.component';
import { ContactRoutingModule } from '@app/ui/contact/contact.routing';
import { CommonUiModule } from '@common/ui/common-ui.module';

@NgModule({
    imports: [
        CommonUiModule,
        ContactRoutingModule,
        ReactiveFormsModule
    ],
    declarations: [
        ContactContentComponent,
        ContactDialogComponent
    ],
    exports: [
        ContactDialogComponent
    ]
})
export class ContactModule { }
