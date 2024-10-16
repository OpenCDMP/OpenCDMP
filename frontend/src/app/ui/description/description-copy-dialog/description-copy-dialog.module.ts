import { NgModule } from '@angular/core';
import { CommonUiModule } from '@common/ui/common-ui.module';

import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { DescriptionCopyDialogComponent } from './description-copy-dialog.component';
import { RouterModule } from '@angular/router';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		AutoCompleteModule,
		RouterModule
	],
	declarations: [
		DescriptionCopyDialogComponent
	]
})
export class DescriptionCopyDialogModule { }
