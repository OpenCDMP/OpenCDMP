import { NgModule } from "@angular/core";
import { AutoCompleteModule } from "@app/library/auto-complete/auto-complete.module";
import { CommonFormattingModule } from '@common/formatting/common-formatting.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { ConfirmationDialogModule } from '@common/modules/confirmation-dialog/confirmation-dialog.module';
import { TextFilterModule } from "@common/modules/text-filter/text-filter.module";
import { UserSettingsModule } from "@common/modules/user-settings/user-settings.module";
import { CommonUiModule } from '@common/ui/common-ui.module';
import { FileUploadComponent } from '@app/library/file-uploader/file-uploader.component';
import { PlanBlueprintRoutingModule } from './plan-blueprint.routing';
import { PlanBlueprintListingComponent } from './listing/plan-blueprint-listing.component';
import { PlanBlueprintListingFiltersComponent } from "./listing/filters/plan-blueprint-listing-filters.component";
import { ImportPlanBlueprintDialogComponent } from './listing/import-plan-blueprint/import-plan-blueprint.dialog.component';
import { PlanBlueprintEditorModule } from './editor/plan-blueprint-editor.module';

@NgModule({
    imports: [
        CommonUiModule,
        CommonFormsModule,
        ConfirmationDialogModule,
        PlanBlueprintRoutingModule,
        AutoCompleteModule,
        TextFilterModule,
        UserSettingsModule,
        CommonFormattingModule,
        FileUploadComponent,
        PlanBlueprintEditorModule,
        PlanBlueprintListingComponent,
        PlanBlueprintListingFiltersComponent,
        ImportPlanBlueprintDialogComponent
    ]
})
export class PlanBlueprintModule { }
