import {NgModule} from '@angular/core';
import {CommonUiModule} from '@common/ui/common-ui.module';
import {DescriptionTemplateTableSelectComponent} from "@app/ui/plan/plan-editor-blueprint/descriptions-template-table-select/description-template-table-select.component";
import {TextFilterModule} from "@common/modules/text-filter/text-filter.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatInputModule} from "@angular/material/input";
import {MatFormFieldModule} from "@angular/material/form-field";
import {AsyncPipe, CommonModule} from "@angular/common";
import {MatChipsModule} from "@angular/material/chips";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatCardModule} from "@angular/material/card";
import { MatMenuModule } from '@angular/material/menu';

@NgModule({
	imports: [
        CommonModule,
		CommonUiModule,
		TextFilterModule,
		FormsModule,
		MatFormFieldModule,
		MatInputModule,
		MatAutocompleteModule,
		ReactiveFormsModule,
		AsyncPipe,		
        MatChipsModule,     
        MatPaginatorModule,
		MatCardModule,
        MatMenuModule
	],
	declarations: [
		DescriptionTemplateTableSelectComponent
	],
	exports: [
		DescriptionTemplateTableSelectComponent
	]
})
export class DescriptionTemplateTableSelectModule {
}
