import { NgModule } from '@angular/core';
import { FormattingModule } from '@app/core/formatting.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { NgxDropzoneModule } from 'ngx-dropzone';
import { PlanUploadDialogComponent } from './plan-upload-dialog.component';
import { ReactiveFormsModule } from '@angular/forms';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { DescriptionTemplatePreviewDialogModule } from '@app/ui/admin/description-template/description-template-preview/description-template-preview-dialog.module';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		FormattingModule,
		ReactiveFormsModule, 
		AutoCompleteModule,
		NgxDropzoneModule,
		DescriptionTemplatePreviewDialogModule
	],
	declarations: [
		PlanUploadDialogComponent,
	],
	exports: [
		PlanUploadDialogComponent,
	]
})
export class PlanUploadDialogModule { }
