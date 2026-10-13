import { NgModule } from '@angular/core';
import { ReferenceTypeRoutingModule } from './reference-type.routing';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { ReferenceTypeEditorComponent } from './editor/reference-type-editor.component';
import { CommonModule } from '@angular/common';
import { TextFilterModule } from '@common/modules/text-filter/text-filter.module';
import { UserSettingsModule } from '@common/modules/user-settings/user-settings.module';
import { ReferenceTypeListingFiltersComponent } from './listing/filters/reference-type-listing-filters.component';
import { ConfirmationDialogModule } from '@common/modules/confirmation-dialog/confirmation-dialog.module';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { CommonFormattingModule } from '@common/formatting/common-formatting.module';
import { ReferenceTypeListingComponent } from './listing/reference-type-listing.component';
import { ExternalFetcherSourceModule } from '@app/ui/external-fetcher/external-fetcher-source.module';
import { MatDialogModule } from '@angular/material/dialog';
import { ReferenceTypeTestDialogComponent } from './reference-type-test-dialog/reference-type-test-dialog.component';
import { ReferenceFieldModule } from "../../reference/reference-field/reference-field.module";
import { Rt2SourceEditorComponent } from '@app/ui/external-fetcher/source-editor/source-editor.component';
import { ReferenceEntriesComponent } from './editor/entries/reference-entries.component';


@NgModule({
    imports: [
        CommonModule,
        CommonUiModule,
        CommonFormsModule,
        ConfirmationDialogModule,
        ReferenceTypeRoutingModule,
        TextFilterModule,
        UserSettingsModule,
        DragDropModule,
        AutoCompleteModule,
        CommonFormattingModule,
        ExternalFetcherSourceModule,
        MatDialogModule,
        ReferenceFieldModule,
        Rt2SourceEditorComponent,
        ReferenceEntriesComponent,
        ReferenceTypeEditorComponent,
        ReferenceTypeListingComponent,
        ReferenceTypeListingFiltersComponent,
        ReferenceTypeTestDialogComponent
    ]
})
export class ReferenceTypeModule { }
