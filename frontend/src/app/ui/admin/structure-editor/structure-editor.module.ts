import { DragDropModule } from '@angular/cdk/drag-drop';
import { OverlayModule } from '@angular/cdk/overlay';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { RelativeTimeFormatPipe } from '@app/core/pipes/relative-time.pipe';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { RichTextEditorModule } from '@app/library/rich-text-editor/rich-text-editor.module';
import { IconConfigurationEditorModule } from '@app/ui/iconconfiguration/icon-configuration-editor.module';
import { PluginEditorModule } from '@app/ui/plugin/plugin-editor.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { FormFocusDirective } from '@common/forms/form-focus.directive';
import { ConfirmationDialogModule } from '@common/modules/confirmation-dialog/confirmation-dialog.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { DefinitionEditorModule } from '../description-template/description-template.module';
import { TcExportSettingsComponent } from '../tenant-configuration/sections/integrations/export-settings/tc-export-settings.component';
import { Bp2CountPipe } from './count.pipe';
import { Bp2KeyPartsPipe } from './key-parts.pipe';
import { AboutCardComponent } from './details/about-card/about-card.component';
import { ExportCardComponent } from './details/export-card/export-card.component';
import { IconPickerComponent } from './details/icon-picker/icon-picker.component';
import { CompareVersionsDialogComponent } from './dialogs/compare-versions/compare-versions.dialog';
import { FinalizePreflightDialogComponent } from './dialogs/finalize-preflight/finalize-preflight.dialog';
import { InsertFromDialogComponent } from './dialogs/insert-from/insert-from.dialog';
import { MoveToDialogComponent } from './dialogs/move-to/move-to.dialog';
import { UnsavedChangesDialogComponent } from './dialogs/unsaved-changes/unsaved-changes.dialog';
import { Bp2ProblemsInPipe } from './problems-in.pipe';
import { DownloadOutlineComponent } from './review/download-outline/download-outline.component';
import { SemanticsCardComponent } from './review/semantics-card/semantics-card.component';
import { PluginReviewComponent } from './review/plugin-review/plugin-review.component';
import { PreviewMappingsDirective } from './review/preview-mappings.directive';
import { KeysViewComponent } from './structure/keys-view/keys-view.component';
import { ObjectFieldComponent } from './structure/object-field/object-field.component';
import { EditorShellComponent } from './shell/editor-shell.component';
import { Bp2InputRowComponent } from './structure/input-row/input-row.component';
import { Bp2OptionsEditorComponent } from './structure/options-editor/options-editor.component';
import { Bp2OutlineComponent } from './structure/outline/outline.component';
import { Bp2QuestionCardComponent } from './structure/question-card/question-card.component';
import { Bp2QuestionReadComponent } from './structure/question-read/question-read.component';
import { Bp2RulesEditorComponent } from './structure/rules-editor/rules-editor.component';
import { Bp2SectionPaneComponent } from './structure/section-pane/section-pane.component';
import { Bp2SemanticsPickerComponent } from './structure/semantics-picker/semantics-picker.component';
import { Bp2StructureTabComponent } from './structure/structure-tab.component';
import { Bp2UploadSettingsComponent } from './structure/upload-settings/upload-settings.component';
import { Bp2VersionHistoryComponent } from './version-history/version-history.component';

const SHARED = [
	EditorShellComponent,
	Bp2StructureTabComponent,
	Bp2OutlineComponent,
	Bp2SectionPaneComponent,
	Bp2QuestionCardComponent,
	Bp2InputRowComponent,
	Bp2QuestionReadComponent,
	Bp2SemanticsPickerComponent,
	Bp2RulesEditorComponent,
	Bp2UploadSettingsComponent,
	Bp2OptionsEditorComponent,
	AboutCardComponent,
	IconPickerComponent,
	ExportCardComponent,
	SemanticsCardComponent,
	PluginReviewComponent,
	PreviewMappingsDirective,
	KeysViewComponent,
	ObjectFieldComponent,
	DownloadOutlineComponent,
	FinalizePreflightDialogComponent,
	UnsavedChangesDialogComponent,
	MoveToDialogComponent,
	CompareVersionsDialogComponent,
	InsertFromDialogComponent,
	Bp2VersionHistoryComponent,
	Bp2CountPipe,
	Bp2KeyPartsPipe,
	Bp2ProblemsInPipe
];

/**
 * What the plan blueprint and description template editors share: the editor frame, the Structure tab
 * with everything in it, the common Details cards, the dialogs and the version history page. Every component
 * reads its editor through `StructureEditorBase`, which each editor provides as itself.
 */
@NgModule({
    imports: [
        CommonUiModule,
        CommonFormsModule,
        RouterModule,
        DragDropModule,
        OverlayModule,
        AutoCompleteModule,
        RichTextEditorModule,
        IconConfigurationEditorModule,
        PluginEditorModule,
        TcExportSettingsComponent,
        ConfirmationDialogModule,
        DefinitionEditorModule,
        FormFocusDirective,
        RelativeTimeFormatPipe,
        ...SHARED
    ],
    exports: [...SHARED, CommonFormsModule, RichTextEditorModule, AutoCompleteModule, DragDropModule, OverlayModule, FormFocusDirective, RelativeTimeFormatPipe]
})
export class StructureEditorModule { }
