import { NgModule } from '@angular/core';
import { FormattingModule } from '@app/core/formatting.module';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { DescriptionRoutingModule } from '@app/ui/description/description.routing';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { ReferenceFieldComponent } from './reference-field.component';
import { ReferenceDialogEditorComponent } from './editor/reference-dialog-editor.component';
import { ReferenceFieldInfoDialogComponent } from './info-dialog/reference-field-info-dialog.component';
import { MatTooltipModule } from '@angular/material/tooltip';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		FormattingModule,
		DescriptionRoutingModule,
		AutoCompleteModule,
		MatTooltipModule
	],
	declarations: [
		ReferenceFieldComponent,
		ReferenceDialogEditorComponent,
		ReferenceFieldInfoDialogComponent
	],
	exports: [
		ReferenceFieldComponent,
		ReferenceDialogEditorComponent,
		ReferenceFieldInfoDialogComponent
	]
})
export class ReferenceFieldModule { }
