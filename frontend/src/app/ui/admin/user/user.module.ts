import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { CommonFormattingModule } from '@common/formatting/common-formatting.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { TextFilterModule } from '@common/modules/text-filter/text-filter.module';
import { UserSettingsModule } from '@common/modules/user-settings/user-settings.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { UserListingFiltersComponent } from './listing/filters/user-listing-filters.component';
import { UserInviteToTenantDialogModule } from './listing/user-invite-to-tenant-dialog/user-invite-to-tenant-dialog.module';
import { UserListingComponent } from './listing/user-listing.component';
import { UserDrawerComponent } from './listing/user-drawer/user-drawer.component';
import { TenantUsersRoutingModule, UsersRoutingModule } from './user.routing';

@NgModule({
    imports: [
        CommonModule,
        CommonUiModule,
        CommonFormsModule,
        CommonFormattingModule,
        UsersRoutingModule,
        AutoCompleteModule,
        TextFilterModule,
        UserSettingsModule,
        UserInviteToTenantDialogModule,
        UserDrawerComponent,
        UserListingComponent,
        UserListingFiltersComponent
    ],
    exports: [
        UserListingComponent,
        UserListingFiltersComponent
    ]
})
export class UsersModule { }

@NgModule({
	declarations: [
	],
	imports: [
		CommonModule,
		CommonUiModule,
		CommonFormsModule,
		CommonFormattingModule,
		TenantUsersRoutingModule,
		AutoCompleteModule,
		TextFilterModule,
		UserSettingsModule,
		UserInviteToTenantDialogModule,
		UsersModule
	]
})
export class TenantUsersModule { }
