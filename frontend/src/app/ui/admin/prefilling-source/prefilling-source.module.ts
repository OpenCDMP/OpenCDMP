import { DragDropModule } from '@angular/cdk/drag-drop';
import { NgModule } from "@angular/core";
import { AutoCompleteModule } from "@app/library/auto-complete/auto-complete.module";
import { CommonFormattingModule } from '@common/formatting/common-formatting.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { ConfirmationDialogModule } from '@common/modules/confirmation-dialog/confirmation-dialog.module';
import { TextFilterModule } from "@common/modules/text-filter/text-filter.module";
import { UserSettingsModule } from "@common/modules/user-settings/user-settings.module";
import { CommonUiModule } from '@common/ui/common-ui.module';
import { PrefillingSourceRoutingModule } from './prefilling-source.routing';
import { RichTextEditorModule } from '@app/library/rich-text-editor/rich-text-editor.module';
import { PrefillingSourceListingComponent } from './listing/prefilling-source-listing.component';
import { PrefillingSourceListingFiltersComponent } from './listing/filters/prefilling-source-listing-filters.component';
import { ExternalFetcherSourceModule } from '@app/ui/external-fetcher/external-fetcher-source.module';
import { PrefillingSourceEditorComponent } from './editor/prefilling-source-editor.component';
import { Rt2SourceEditorComponent } from '@app/ui/external-fetcher/source-editor/source-editor.component';

@NgModule({
    imports: [
        CommonUiModule,
        CommonFormsModule,
        ConfirmationDialogModule,
        PrefillingSourceRoutingModule,
        DragDropModule,
        AutoCompleteModule,
        TextFilterModule,
        UserSettingsModule,
        CommonFormattingModule,
        RichTextEditorModule,
        ExternalFetcherSourceModule,
        Rt2SourceEditorComponent,
        PrefillingSourceEditorComponent,
        PrefillingSourceListingComponent,
        PrefillingSourceListingFiltersComponent
    ]
})
export class PrefillingSourceModule { }
