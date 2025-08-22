import { NgModule } from '@angular/core';
import { FormattingModule } from '@app/core/formatting.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { PlanUploadDialogComponent } from './plan-upload-dialog.component';
import { ReactiveFormsModule } from '@angular/forms';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { DescriptionTemplatePreviewDialogModule } from '@app/ui/admin/description-template/description-template-preview/description-template-preview-dialog.module';
import { FileUploadComponent } from '@app/library/file-uploader/file-uploader.component';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		FormattingModule,
		ReactiveFormsModule, 
		AutoCompleteModule,
		DescriptionTemplatePreviewDialogModule,
        FileUploadComponent
	],
	declarations: [
		PlanUploadDialogComponent,
	],
	exports: [
		PlanUploadDialogComponent,
	]
})
export class PlanUploadDialogModule { }
