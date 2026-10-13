import {NgModule} from '@angular/core';
import {CommonUiModule} from '@common/ui/common-ui.module';
import {PlanBlueprintsPreviewComponent} from "@app/ui/plan/plan-editor-blueprint/plan-blueprints-preview/plan-blueprints-preview.component";
import {DescriptionTemplatesDialogModule} from "@app/ui/plan/plan-editor-blueprint/description-templates-dialog/description-templates-dialog.module";
import { DescriptionTemplateTableSelectModule } from '../descriptions-template-table-select/description-template-table-select.module';
import {TextFilterModule} from "@common/modules/text-filter/text-filter.module";
import {ReactiveFormsModule} from "@angular/forms";
import { PlanCreationConsentModule } from '../../plan-creation-consent/plan-creation-consent.module';
import { RouterModule } from '@angular/router';
import { CommonFormattingModule } from '@common/formatting/common-formatting.module';
import { PlanUploadDialogModule } from '../../new/upload-dialogue/plan-upload-dialog.module';


@NgModule({
    imports: [
        CommonUiModule,
        DescriptionTemplatesDialogModule,
        DescriptionTemplateTableSelectModule,
        TextFilterModule,
        ReactiveFormsModule,
        PlanCreationConsentModule,
        RouterModule,
        CommonFormattingModule,
        PlanUploadDialogModule,
        PlanBlueprintsPreviewComponent
    ],
    exports: [
        PlanBlueprintsPreviewComponent
    ]
})
export class PlanBlueprintsPreviewModule {}
