import { DragDropModule } from '@angular/cdk/drag-drop';
import { NgModule } from "@angular/core";
import { AutoCompleteModule } from "@app/library/auto-complete/auto-complete.module";
import { CommonFormattingModule } from '@common/formatting/common-formatting.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { ConfirmationDialogModule } from '@common/modules/confirmation-dialog/confirmation-dialog.module';
import { TextFilterModule } from "@common/modules/text-filter/text-filter.module";
import { UserSettingsModule } from "@common/modules/user-settings/user-settings.module";
import { CommonUiModule } from '@common/ui/common-ui.module';
import { RichTextEditorModule } from '@app/library/rich-text-editor/rich-text-editor.module';
import { LockRoutingModule } from './lock.routing';
import { LockListingFiltersComponent } from './filters/lock-listing-filters.component';
import { MatIconModule } from '@angular/material/icon';
import { LockListingComponent } from './lock-listing.component';

@NgModule({
    imports: [
        CommonUiModule,
        CommonFormsModule,
        ConfirmationDialogModule,
        LockRoutingModule,
        DragDropModule,
        AutoCompleteModule,
        TextFilterModule,
        UserSettingsModule,
        CommonFormattingModule,
        RichTextEditorModule,
        MatIconModule,
        LockListingComponent,
        LockListingFiltersComponent,
    ]
})
export class LockModule { }
