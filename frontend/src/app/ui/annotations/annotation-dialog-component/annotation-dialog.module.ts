import { NgModule } from '@angular/core';
import { FormattingModule } from '@app/core/formatting.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { AnnotationDialogComponent } from './annotation-dialog.component';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { TextAreaWithMentionsComponent } from '../components/text-area-with-mentions.component';
import { NgxMentionsModule } from 'ngx-mentions';
import { OverlayModule } from '@angular/cdk/overlay';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		FormattingModule,
		AutoCompleteModule,
		NgxMentionsModule,
		OverlayModule,
	],
	declarations: [
		AnnotationDialogComponent,
		TextAreaWithMentionsComponent,
	],
	exports: [
		AnnotationDialogComponent,
	],
})
export class AnnotationDialogModule { }
