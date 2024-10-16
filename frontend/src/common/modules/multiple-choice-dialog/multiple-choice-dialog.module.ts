import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { MultipleChoiceDialogComponent } from './multiple-choice-dialog.component';



@NgModule({
	imports: [CommonUiModule, FormsModule],
	declarations: [MultipleChoiceDialogComponent],
	exports: [MultipleChoiceDialogComponent]
})
export class MultipleChoiceDialogModule { 
	constructor() { }
}
