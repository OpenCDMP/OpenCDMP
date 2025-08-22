import {NgModule} from '@angular/core';
import {CommonUiModule} from '@common/ui/common-ui.module';
import {PlanBlueprintsPreviewComponent} from "@app/ui/plan/plan-editor-blueprint/plan-blueprints-preview/plan-blueprints-preview.component";
import {DescriptionTemplatesDialogModule} from "@app/ui/plan/plan-editor-blueprint/description-templates-dialog/description-templates-dialog.module";
import { DescriptionTemplateTableSelectModule } from '../descriptions-template-table-select/description-template-table-select.module';
import {TextFilterModule} from "@common/modules/text-filter/text-filter.module";
import {ReactiveFormsModule} from "@angular/forms";


@NgModule({
	imports: [
		CommonUiModule,
		DescriptionTemplatesDialogModule,
		DescriptionTemplateTableSelectModule,
		TextFilterModule,
		ReactiveFormsModule
	],
	declarations: [
		PlanBlueprintsPreviewComponent
	],
	exports: [
		PlanBlueprintsPreviewComponent
	]
})
export class PlanBlueprintsPreviewModule {}
