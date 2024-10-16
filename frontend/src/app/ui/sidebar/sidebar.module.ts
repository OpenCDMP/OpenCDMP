import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { ContactModule } from '../contact/contact.module';
import { FaqModule } from '../faq/faq.module';
import { GlossaryModule } from '../glossary/glossary.module';
import { SidebarFooterComponent } from './sidebar-footer/sidebar-footer.component';
import { SidebarComponent } from './sidebar.component';
import { UserGuideModule } from '../user-guide/user-guide.module';
import { LanguageModule } from '../language/language.module';

@NgModule({
    imports: [
        CommonUiModule,
        CommonFormsModule,
        GlossaryModule,
        FaqModule,
        RouterModule,
        ContactModule,
        UserGuideModule,
        LanguageModule
    ],
    declarations: [
        SidebarComponent,
        SidebarFooterComponent
    ],
    exports: [SidebarComponent]
})
export class SidebarModule { }
