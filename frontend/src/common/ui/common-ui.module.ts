import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FormattingModule } from '@app/core/formatting.module';
import { NavigationBreadcrumbComponent } from '@app/ui/misc/breadcrumb/navigation-breadcrumb.component';
import { AdminPageHeaderComponent } from '@common/ui/admin-page-header/admin-page-header.component';
import { SecureImagePipe } from '@common/http/image/secure-image.pipe';
import { MaterialModule } from '@common/material/material.module';
import { TranslateModule } from '@ngx-translate/core';
import { TenantSharedChipComponent } from '@common/ui/tenant-shared/tenant-shared-chip.component';
import { TenantReadonlyBannerComponent } from '@common/ui/tenant-shared/tenant-readonly-banner.component';
import { AdminListingComponent } from '@common/modules/admin-listing/admin-listing.component';
import { ListingFilterChipComponent } from '@common/modules/admin-listing/listing-filter-chip.component';
import { NullifyValuePipe } from '@common/modules/admin-listing/pipes/nullify-value.pipe';

@NgModule({
    imports: [
        CommonModule,
        MaterialModule,
        TranslateModule,
        FormattingModule,
        RouterModule,
        TenantSharedChipComponent,
        TenantReadonlyBannerComponent,
        AdminListingComponent,
        ListingFilterChipComponent,
        NullifyValuePipe,
        SecureImagePipe,
        NavigationBreadcrumbComponent,
        AdminPageHeaderComponent
    ],
    exports: [
        CommonModule,
        MaterialModule,
        TranslateModule,
        FormattingModule,
        SecureImagePipe,
        NavigationBreadcrumbComponent,
        AdminPageHeaderComponent,
        TenantSharedChipComponent,
        TenantReadonlyBannerComponent,
        AdminListingComponent,
        ListingFilterChipComponent,
        NullifyValuePipe
    ]
})
export class CommonUiModule { }
