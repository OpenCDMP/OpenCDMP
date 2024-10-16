import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { NavbarComponent } from '@app/ui/navbar/navbar.component';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { LanguageModule } from '../language/language.module';
import { UserDialogComponent } from './user-dialog/user-dialog.component';
import { TenantModule } from '../tenant/tenant.module';

@NgModule({
    imports: [
        CommonUiModule,
        CommonFormsModule,
        RouterModule,
        LanguageModule,
        TenantModule
    ],
    declarations: [
        NavbarComponent,
		UserDialogComponent
    ],
    exports: [NavbarComponent]
})
export class NavbarModule { }
