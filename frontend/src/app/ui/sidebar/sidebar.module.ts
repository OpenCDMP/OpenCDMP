import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { ContactModule } from '../contact/contact.module';
import { FaqModule } from '../faq/faq.module';
import { GlossaryModule } from '../glossary/glossary.module';
import { SidebarComponent } from './sidebar.component';
import { UserGuideModule } from '../user-guide/user-guide.module';
import { LanguageModule } from '../language/language.module';
import { StartNewDescriptionDialogModule } from '../description/start-new-description-dialog/start-new-description-dialog.module';
import { StartNewPlanDialogModule } from '../plan/new/start-new-plan-dialogue/start-new-plan-dialog.module';

@NgModule({
    imports: [
        CommonUiModule,
        CommonFormsModule,
        GlossaryModule,
        FaqModule,
        RouterModule,
        ContactModule,
        UserGuideModule,
        LanguageModule,
        StartNewDescriptionDialogModule,
        StartNewPlanDialogModule,
        SidebarComponent
    ],
    exports: [SidebarComponent]
})
export class SidebarModule { }
