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
import { PlanBlueprintRoutingModule } from './plan-blueprint.routing';
import { PlanBlueprintEditorComponent } from './editor/plan-blueprint-editor.component';
import { PlanBlueprintListingComponent } from './listing/plan-blueprint-listing.component';
import { PlanBlueprintListingFiltersComponent } from "./listing/filters/plan-blueprint-listing-filters.component";
import { ImportPlanBlueprintDialogComponent } from './listing/import-plan-blueprint/import-plan-blueprint.dialog.component';
import { DescriptionTemplatePreviewDialogModule } from '../description-template/description-template-preview/description-template-preview-dialog.module';
import { CheckboxFieldModule } from '@common/modules/checkbox-field/checkbox-field.module';
import { RichTextEditorModule } from '@app/library/rich-text-editor/rich-text-editor.module';
import { FormFocusDirective } from '@common/forms/form-focus.directive';
import { PlanBlueprintTocComponent } from './editor/table-of-content/plan-blueprint-toc.component';
import { CustomMatStepperHeaderComponent } from '@app/library/custom-mat-stepper-header/custom-mat-stepper-header.component';
import { PlanInternalBlueprintEditorModule } from '@app/ui/plan/plan-editor-blueprint/internal-blueprint-editor/internal-blueprint-editor.module';
import { PlanBlueprintSectionFieldEditorComponent } from './editor/section-field-editor/plan-blueprint-section-field-editor.component';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		ConfirmationDialogModule,
		PlanBlueprintRoutingModule,
		NgxDropzoneModule,
		DragDropModule,
		AutoCompleteModule,
		HybridListingModule,
		TextFilterModule,
		UserSettingsModule,
		CommonFormattingModule,
		DescriptionTemplatePreviewDialogModule,
		CheckboxFieldModule,
        RichTextEditorModule,
        FormFocusDirective,
        CustomMatStepperHeaderComponent,
        PlanInternalBlueprintEditorModule
    ],
	declarations: [
		PlanBlueprintEditorComponent,
		PlanBlueprintListingComponent,
		PlanBlueprintListingFiltersComponent,
		ImportPlanBlueprintDialogComponent,
        PlanBlueprintTocComponent,
        PlanBlueprintSectionFieldEditorComponent
	]
})
export class PlanBlueprintModule { }
