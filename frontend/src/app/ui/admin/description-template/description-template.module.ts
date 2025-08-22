import { DragDropModule } from '@angular/cdk/drag-drop';
import { NgModule } from "@angular/core";
import { AutoCompleteModule } from "@app/library/auto-complete/auto-complete.module";
import { RichTextEditorModule } from '@app/library/rich-text-editor/rich-text-editor.module';
import { CommonFormattingModule } from '@common/formatting/common-formatting.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { ConfirmationDialogModule } from '@common/modules/confirmation-dialog/confirmation-dialog.module';
import { HybridListingModule } from "@common/modules/hybrid-listing/hybrid-listing.module";
import { TextFilterModule } from "@common/modules/text-filter/text-filter.module";
import { UserSettingsModule } from "@common/modules/user-settings/user-settings.module";
import { CommonUiModule } from '@common/ui/common-ui.module';
import { DescriptionTemplateRoutingModule } from './description-template.routing';
import { DescriptionTemplateEditorFieldSetComponent } from './editor/components/field-set/description-template-editor-field-set.component';
import { DescriptionTemplateEditorDefaultValueComponent } from './editor/components/default-value/description-template-editor-default-value.component';
import { DescriptionTemplateEditorExternalDatasetsFieldComponent } from './editor/components/field-type/external-datasets/description-template-editor-external-datasets-field.component';
import { DescriptionTemplateEditorLabelAndMultiplicityFieldComponent } from './editor/components/field-type/label-and-multiplicity-field/description-template-editor-label-and-multiplicity-field.component';
import { DescriptionTemplateEditorLabelFieldComponent } from './editor/components/field-type/label-field/description-template-editor-label-field.component';
import { DescriptionTemplateEditorRadioBoxFieldComponent } from './editor/components/field-type/radio-box/description-template-editor-radio-box-field.component';
import { DescriptionTemplateEditorSelectFieldComponent } from './editor/components/field-type/select/description-template-editor-select-field.component';
import { DescriptionTemplateEditorUploadFieldComponent } from './editor/components/field-type/upload/description-template-editor-upload-field.component';
import { DescriptionTemplateEditorFieldComponent } from './editor/components/field/description-template-editor-field.component';
import { DescriptionTemplateEditorSectionFieldSetComponent } from './editor/components/section-fieldset/description-template-editor-section-fieldset.component';
import { DescriptionTemplateEditorSectionComponent } from './editor/components/section/description-template-editor-section.component';
import { DescriptionTemplateEditorRuleComponent } from './editor/components/visibility-rule/description-template-editor-visibility-rule.component';
import { DescriptionTemplateEditorComponent } from './editor/description-template-editor.component';
import { DescriptionTemplateTableOfContents } from './editor/table-of-contents/description-template-table-of-contents';
import { DescriptionTemplateTableOfContentsInternalSection } from './editor/table-of-contents/table-of-contents-internal-section/description-template-table-of-contents-internal-section';
import { DescriptionTemplateListingComponent } from './listing/description-template-listing.component';
import { DescriptionTemplateListingFiltersComponent } from "./listing/filters/description-template-listing-filters.component";
import { ImportDescriptionTemplateDialogComponent } from './listing/import-description-template/import-description-template.dialog.component';
import { DescriptionTemplateEditorReferenceTypeFieldComponent } from './editor/components/field-type/reference-type/description-template-editor-reference-type-field.component';
import { DescriptionFormModule } from '@app/ui/description/editor/description-form/description-form.module';
import { FinalPreviewComponent } from './editor/components/final-preview/final-preview.component';
import { DragulaModule } from 'ng2-dragula';
import { TransitionGroupModule } from '@app/ui/transition-group/transition-group.module';
import { FormFocusDirective } from '@common/forms/form-focus.directive';
import { FileUploadComponent } from '@app/library/file-uploader/file-uploader.component';
import { PluginEditorModule } from '@app/ui/plugin/plugin-editor.module';
import { CustomMatStepperHeaderComponent } from '@app/library/custom-mat-stepper-header/custom-mat-stepper-header.component';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		ConfirmationDialogModule,
		DescriptionTemplateRoutingModule,
		DragDropModule,
		AutoCompleteModule,
		HybridListingModule,
		TextFilterModule,
		UserSettingsModule,
		CommonFormattingModule,
		RichTextEditorModule,
		DescriptionFormModule,
		TransitionGroupModule,
		DragulaModule.forRoot(),
        FormFocusDirective,
        FileUploadComponent,
		PluginEditorModule,
        CustomMatStepperHeaderComponent
	],
	declarations: [
		DescriptionTemplateEditorComponent,
		DescriptionTemplateListingComponent,
		DescriptionTemplateListingFiltersComponent,
		ImportDescriptionTemplateDialogComponent,

		DescriptionTemplateTableOfContents,
		DescriptionTemplateTableOfContentsInternalSection,
		DescriptionTemplateEditorSectionFieldSetComponent,
		DescriptionTemplateEditorSectionComponent,
		DescriptionTemplateEditorFieldSetComponent,
		DescriptionTemplateEditorFieldComponent,
		DescriptionTemplateEditorDefaultValueComponent,
		DescriptionTemplateEditorRuleComponent,

		DescriptionTemplateEditorReferenceTypeFieldComponent,
		DescriptionTemplateEditorSelectFieldComponent,
		DescriptionTemplateEditorLabelFieldComponent,
		DescriptionTemplateEditorLabelAndMultiplicityFieldComponent,
		DescriptionTemplateEditorRadioBoxFieldComponent,
		DescriptionTemplateEditorExternalDatasetsFieldComponent,
		DescriptionTemplateEditorUploadFieldComponent,

		FinalPreviewComponent
	]
})
export class DescriptionTemplateModule { }
