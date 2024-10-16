import { NgModule } from "@angular/core";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { CommonUiModule } from "@common/ui/common-ui.module";
import { NoopCheckboxComponent } from "./noop-checkbox/noop-checkbox.component";

@NgModule({
  imports: [
    CommonUiModule,
		FormsModule,
    ReactiveFormsModule,
  ],
  declarations: [
    NoopCheckboxComponent,
  ],
  exports: [
    NoopCheckboxComponent,
  ]
})
export class CheckboxFieldModule {
	constructor() { }
}