import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonUiModule } from '@common/ui/common-ui.module';
import {DescriptionTemplatesDialogComponent} from "@app/ui/plan/plan-editor-blueprint/description-templates-dialog/description-templates-dialog.component";
import { DescriptionTemplateTableSelectModule } from '../descriptions-template-table-select/description-template-table-select.module';



@NgModule({
	imports: [CommonUiModule, FormsModule, DescriptionTemplateTableSelectModule],
	declarations: [DescriptionTemplatesDialogComponent],
	exports: [DescriptionTemplatesDialogComponent]
})
export class DescriptionTemplatesDialogModule {
	constructor() { }
}
