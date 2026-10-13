import { NgModule } from "@angular/core";
import { AutoCompleteModule } from "@app/library/auto-complete/auto-complete.module";
import { CommonFormattingModule } from '@common/formatting/common-formatting.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { ConfirmationDialogModule } from '@common/modules/confirmation-dialog/confirmation-dialog.module';
import { TextFilterModule } from "@common/modules/text-filter/text-filter.module";
import { UserSettingsModule } from "@common/modules/user-settings/user-settings.module";
import { CommonUiModule } from '@common/ui/common-ui.module';
import { FileUploadComponent } from '@app/library/file-uploader/file-uploader.component';
import { DescriptionTemplateRoutingModule } from './description-template.routing';
import { DescriptionTemplateEditorDefaultValueComponent } from './editor/components/default-value/description-template-editor-default-value.component';
import { DescriptionTemplateListingComponent } from './listing/description-template-listing.component';
import { DescriptionTemplateListingFiltersComponent } from "./listing/filters/description-template-listing-filters.component";
import { ImportDescriptionTemplateDialogComponent } from './listing/import-description-template/import-description-template.dialog.component';
import { DescriptionFormModule } from '@app/ui/description/editor/description-form/description-form.module';
import { FinalPreviewComponent } from './editor/components/final-preview/final-preview.component';
import { FieldEditorModule } from '@app/ui/description/editor/description-form/field-editor.module';

@NgModule({
    imports: [
        CommonUiModule,
        CommonFormsModule,
        DescriptionFormModule,
        FieldEditorModule,
        DescriptionTemplateEditorDefaultValueComponent,
        FinalPreviewComponent
    ],
    exports: [
        DescriptionTemplateEditorDefaultValueComponent,
        FinalPreviewComponent
    ]
})
export class DefinitionEditorModule { }

@NgModule({
    imports: [
        CommonUiModule,
        CommonFormsModule,
        ConfirmationDialogModule,
        DescriptionTemplateRoutingModule,
        AutoCompleteModule,
        TextFilterModule,
        UserSettingsModule,
        CommonFormattingModule,
        FileUploadComponent,
        DescriptionTemplateListingComponent,
        DescriptionTemplateListingFiltersComponent,
        ImportDescriptionTemplateDialogComponent
    ]
})
export class DescriptionTemplateModule { }
