import { AfterContentChecked, Directive, Optional } from "@angular/core";
import { AbstractControl } from "@angular/forms";
import { MatFormField } from "@angular/material/form-field";
import { MatInput } from "@angular/material/input";
import { MatSelect } from "@angular/material/select";
import { MultipleAutoCompleteComponent } from "@app/library/auto-complete/multiple/multiple-auto-complete.component";
import { SingleAutoCompleteComponent } from "@app/library/auto-complete/single/single-auto-complete.component";
import { MarkedValidatorFn } from "./validation/custom-validator";

/**
 * Input/Select into FormField consider Validator.required from reactive form if the [required] attribute is missing in the template
 */
@Directive({
    selector: 'mat-form-field:has(input:not([required])), mat-form-field:has(mat-select:not([required])), mat-form-field:has(app-multiple-auto-complete:not([required])), mat-form-field:has(app-single-auto-complete:not([required]))',
    standalone: false
})
export class ReactiveAsteriskDirective implements AfterContentChecked {
	private readonly requiredValidatornames = ['RequiredWithVisibilityRulesValidator', 'required'];
	constructor(@Optional() private matFormField: MatFormField) { }

	ngAfterContentChecked() {
		if (!this.matFormField) return;
		const ctrl = this.matFormField._control;
		const abstractControl = ctrl?.ngControl?.control;
		const validators = (abstractControl as AbstractControl & { _rawValidators: MarkedValidatorFn[] })?._rawValidators;
		if (!validators) return;

		if (ctrl instanceof MatInput ||
			ctrl instanceof MatSelect ||
			ctrl instanceof SingleAutoCompleteComponent ||
			ctrl instanceof MultipleAutoCompleteComponent
		) {
			ctrl.required = validators.some(validator => this.requiredValidatornames.includes(validator.type));
		}
	}
}