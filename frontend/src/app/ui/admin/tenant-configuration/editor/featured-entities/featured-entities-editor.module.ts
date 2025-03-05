import { DragDropModule } from '@angular/cdk/drag-drop';
import { NgModule } from '@angular/core';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { FeaturedEntitiesEditorComponent } from './featured-entities-editor.component';
import { DragAndDropAccessibilityService } from '@app/core/services/accessibility/drag-and-drop-accessibility.service';
import { CdkListboxModule } from '@angular/cdk/listbox';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
		DragDropModule,
        CdkListboxModule
	],
	declarations: [
		FeaturedEntitiesEditorComponent
	],
	exports: [
		FeaturedEntitiesEditorComponent
	],
    providers: [
        DragAndDropAccessibilityService
    ]
})
export class FeatureEntitiesModule { }
