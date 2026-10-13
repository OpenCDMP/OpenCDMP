import { NgModule } from '@angular/core';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { DescriptionTemplateLibraryRoutingModule } from './description-template-library.routing';
import { DescriptionTemplateLibraryListingComponent } from './listing/description-template-library-listing.component';
import { DescriptionTemplateLibraryViewComponent } from './view/description-template-library-view.component';
import { FormattingModule } from '@app/core/formatting.module';
import { TextFilterModule } from '@common/modules/text-filter/text-filter.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { DescriptionFormModule } from '@app/ui/description/editor/description-form/description-form.module';



@NgModule({
    imports: [
        CommonUiModule,
        DescriptionTemplateLibraryRoutingModule,
        FormattingModule,
        TextFilterModule,
        CommonFormsModule,
        DescriptionFormModule,
        DescriptionTemplateLibraryListingComponent,
        DescriptionTemplateLibraryViewComponent
    ]
})
export class DescriptionTemplateLibraryModule { }
