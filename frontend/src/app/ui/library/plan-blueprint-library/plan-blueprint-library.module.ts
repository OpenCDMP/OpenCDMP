import { NgModule } from '@angular/core';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { PlanBlueprintLibraryRoutingModule } from './plan-blueprint-library.routing';
import { PlanBlueprintLibraryListingComponent } from './listing/plan-blueprint-library-listing.component';
import { PlanBlueprintLibraryViewComponent } from './view/plan-blueprint-library-view.component';
import { FormattingModule } from '@app/core/formatting.module';
import { TextFilterModule } from '@common/modules/text-filter/text-filter.module';
import { PlanBlueprintsPreviewModule } from '@app/ui/plan/plan-editor-blueprint/plan-blueprints-preview/plan-blueprint-preview.module';



@NgModule({
    imports: [
        CommonUiModule,
        PlanBlueprintLibraryRoutingModule,
        FormattingModule,
        TextFilterModule,
        PlanBlueprintsPreviewModule,
        PlanBlueprintLibraryListingComponent,
        PlanBlueprintLibraryViewComponent
    ]
})
export class PlanBlueprintLibraryModule { }
