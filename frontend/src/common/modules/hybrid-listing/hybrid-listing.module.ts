import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BatchActionsDirective } from '@common/modules/hybrid-listing/directives/batch-actions.directive';
import { HybridListingFiltersDirective } from '@common/modules/hybrid-listing/directives/hybrid-listing-filters.directive';
import { UserFilterSettingsDirective } from '@common/modules/hybrid-listing/directives/user-filter-settings.directive';
import { HybridListingComponent } from '@common/modules/hybrid-listing/hybrid-listing.component';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { NgxDatatableModule } from '@swimlane/ngx-datatable';
import { MatPaginatorModule } from '@angular/material/paginator'
import { DownloadListingReportDirective } from '@common/modules/hybrid-listing/directives/download-listing-report.directive';
import { NullifyValuePipe } from './pipes/nullify-value.pipe';
import { FormattingModule } from '@app/core/formatting.module';

@NgModule({
    imports: [
        CommonUiModule,
        FormsModule,
        FormattingModule,
        NgxDatatableModule,
        MatPaginatorModule
    ],
    declarations: [
        HybridListingComponent,
        BatchActionsDirective,
        UserFilterSettingsDirective,
        HybridListingFiltersDirective,
        DownloadListingReportDirective,
        NullifyValuePipe
    ],
    exports: [
        HybridListingComponent,
        BatchActionsDirective,
        UserFilterSettingsDirective,
        HybridListingFiltersDirective,
        DownloadListingReportDirective,
        NullifyValuePipe
    ],
	providers:[
	]
})
export class HybridListingModule {
	constructor() { }
}
