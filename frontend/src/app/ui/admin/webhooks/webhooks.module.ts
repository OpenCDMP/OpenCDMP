import { NgModule } from "@angular/core";
import { CommonFormattingModule } from '@common/formatting/common-formatting.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { ConfirmationDialogModule } from '@common/modules/confirmation-dialog/confirmation-dialog.module';
import { TextFilterModule } from "@common/modules/text-filter/text-filter.module";
import { UserSettingsModule } from "@common/modules/user-settings/user-settings.module";
import { CommonUiModule } from '@common/ui/common-ui.module';
import { WebhooksRoutingModule } from './webhooks.routing';
import { WebhookEditorComponent } from './editor/webhook-editor.component';
import { WebhookListingComponent } from './listing/webhook-listing.component';
import { WebhookListingFiltersComponent } from './listing/filters/webhook-listing-filters.component';

@NgModule({
    imports: [
        CommonUiModule,
        CommonFormsModule,
        ConfirmationDialogModule,
        WebhooksRoutingModule,
        TextFilterModule,
        UserSettingsModule,
        CommonFormattingModule,
        WebhookEditorComponent,
        WebhookListingComponent,
        WebhookListingFiltersComponent
    ]
})
export class WebhooksModule { }
