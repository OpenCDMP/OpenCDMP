import { NgModule } from '@angular/core';
import { FormattingModule } from '@app/core/formatting.module';
import { DescriptionListingComponent } from '@app/ui/description/listing/description-listing.component';
import { DescriptionListingItemComponent } from '@app/ui/description/listing/listing-item/description-listing-item.component';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { DescriptionListingRoutingModule } from './description-listing.routing';
import { DescriptionCopyDialogModule } from '../description-copy-dialog/description-copy-dialog.module';
import { StartNewDescriptionDialogModule } from '../start-new-description-dialog/start-new-description-dialog.module';
import { DescriptionFilterDialogComponent } from './filtering/description-filter-dialogue/description-filter-dialog.component';
import { DescriptionFilterComponent } from './filtering/description-filter.component';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { DescriptionFilterService } from './filtering/description-filter.service';
import { TextFilterModule } from '@common/modules/text-filter/text-filter.module';
import { ActiveReferenceFiltersComponent } from '@app/ui/reference/reference-usage-filter/active-reference-filters.component';
import { ReferenceUsageFilterComponent } from '@app/ui/reference/reference-usage-filter/reference-usage-filter.component';

@NgModule({
    imports: [
        CommonUiModule,
        ReferenceUsageFilterComponent,
        ActiveReferenceFiltersComponent,
        CommonFormsModule,
        FormattingModule,
        DescriptionCopyDialogModule,
        StartNewDescriptionDialogModule,
        DescriptionListingRoutingModule,
        AutoCompleteModule,
        TextFilterModule,
        DescriptionListingComponent,
        DescriptionListingItemComponent,
        DescriptionFilterDialogComponent,
        DescriptionFilterComponent,
    ],
    exports: [
        DescriptionListingItemComponent,
        DescriptionFilterDialogComponent,
    ],
    providers: [
        DescriptionFilterService,
    ]
})
export class DescriptionListingModule { }
