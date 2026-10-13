import { DragDropModule } from '@angular/cdk/drag-drop';
import { NgModule } from "@angular/core";
import { AutoCompleteModule } from "@app/library/auto-complete/auto-complete.module";
import { CommonFormattingModule } from '@common/formatting/common-formatting.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { ConfirmationDialogModule } from '@common/modules/confirmation-dialog/confirmation-dialog.module';
import { TextFilterModule } from "@common/modules/text-filter/text-filter.module";
import { UserSettingsModule } from "@common/modules/user-settings/user-settings.module";
import { CommonUiModule } from '@common/ui/common-ui.module';
import { TenantRoutingModule } from './tenant.routing';
import { TenantListingComponent } from './listing/tenant-listing.component';
import { TenantDrawerComponent } from './listing/tenant-drawer/tenant-drawer.component';
import { TenantListingFiltersComponent } from "./listing/filters/tenant-listing-filters.component";
import { RichTextEditorModule } from '@app/library/rich-text-editor/rich-text-editor.module';

@NgModule({
    imports: [
        CommonUiModule,
        CommonFormsModule,
        ConfirmationDialogModule,
        TenantRoutingModule,
        DragDropModule,
        AutoCompleteModule,
        TextFilterModule,
        UserSettingsModule,
        CommonFormattingModule,
        RichTextEditorModule,
        TenantDrawerComponent,
        TenantListingComponent,
        TenantListingFiltersComponent
    ]
})
export class TenantModule { }
