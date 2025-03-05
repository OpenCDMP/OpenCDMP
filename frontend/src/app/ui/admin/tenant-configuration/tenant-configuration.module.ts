import { DragDropModule } from '@angular/cdk/drag-drop';
import { NgModule } from "@angular/core";
import { AutoCompleteModule } from "@app/library/auto-complete/auto-complete.module";
import { CommonFormattingModule } from '@common/formatting/common-formatting.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { ConfirmationDialogModule } from '@common/modules/confirmation-dialog/confirmation-dialog.module';
import { HybridListingModule } from "@common/modules/hybrid-listing/hybrid-listing.module";
import { TextFilterModule } from "@common/modules/text-filter/text-filter.module";
import { UserSettingsModule } from "@common/modules/user-settings/user-settings.module";
import { CommonUiModule } from '@common/ui/common-ui.module';
import { NgxDropzoneModule } from "ngx-dropzone";
import { RichTextEditorModule } from '@app/library/rich-text-editor/rich-text-editor.module';
import { TenantConfigurationRoutingModule } from './tenant-configuration.routing';
import { TenantConfigurationEditorComponent } from './editor/tenant-configuration-editor.component';
import { CssColorsEditorComponent } from './editor/css-colors/css-colors-editor.component';
import { DefaultUserLocaleEditorComponent } from './editor/default-user-locale/default-user-locale-editor.component';
import { FormattingModule } from '@app/core/formatting.module';
import { DepositEditorComponent } from './editor/deposit/deposit-editor.component';
import { FileTransformerEditorComponent } from './editor/file-transformer/file-transformer-editor.component';
import { LogoEditorComponent } from './editor/logo/logo-editor.component';
import { NgxColorsModule } from 'ngx-colors';
import { NotifierListModule } from '@notification-service/ui/admin/tenant-configuration/notifier-list/notifier-list-editor.module';
import { PlanWorkflowEditorComponent } from './editor/plan-workflow/plan-workflow-editor/plan-workflow-editor.component';
import { DescriptionWorkflowEditorComponent } from './editor/description-workflow/description-workflow-editor/description-workflow-editor.component';
import { EvaluatorEditorComponent } from './editor/evaluator/evaluator-editor.component';
import { FeaturedEntitiesEditorComponent } from './editor/featured-entities/featured-entities-editor.component';
import { DefaultPlanBlueprintEditorComponent } from './editor/default-plan-blueprint/default-plan-blueprint-editor.component';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		ConfirmationDialogModule,
		TenantConfigurationRoutingModule,
		NgxDropzoneModule,
		DragDropModule,
        FormattingModule,
		AutoCompleteModule,
		HybridListingModule,
		TextFilterModule,
		UserSettingsModule,
		CommonFormattingModule,
		RichTextEditorModule,
		NgxColorsModule,
		NotifierListModule
	],
	declarations: [
		TenantConfigurationEditorComponent,
		CssColorsEditorComponent,
		DefaultUserLocaleEditorComponent,
		DepositEditorComponent,
		FileTransformerEditorComponent,
		LogoEditorComponent,
        PlanWorkflowEditorComponent,
        DescriptionWorkflowEditorComponent,
		EvaluatorEditorComponent,
		FeaturedEntitiesEditorComponent,
		DefaultPlanBlueprintEditorComponent
	]
})
export class TenantConfigurationModule { }
