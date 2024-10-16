import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {TransitionGroupItemDirective} from "./transition-group-item.directive";
import {TransitionGroupComponent} from "./transition-group.component";

@NgModule({
	declarations: [TransitionGroupItemDirective, TransitionGroupComponent],
	imports: [CommonModule],
	exports: [TransitionGroupItemDirective, TransitionGroupComponent]
})
export class TransitionGroupModule {

}
