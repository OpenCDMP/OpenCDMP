import { NgModule } from '@angular/core';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { DefaultPlanBlueprintEditorComponent } from './default-plan-blueprint-editor.component';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
	],
	declarations: [
		DefaultPlanBlueprintEditorComponent
	],
	exports: [
		DefaultPlanBlueprintEditorComponent
	]
})
export class DefaultPlanBlueprintModule { }
