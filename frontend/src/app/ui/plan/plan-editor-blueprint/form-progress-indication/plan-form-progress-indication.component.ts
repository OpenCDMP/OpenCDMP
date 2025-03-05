import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { AbstractControl, UntypedFormArray, UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { BaseComponent } from '@common/base/base.component';
import { takeUntil } from 'rxjs/operators';

@Component({
    selector: 'app-plan-form-progress-indication',
    templateUrl: './plan-form-progress-indication.component.html',
    styleUrls: ['./plan-form-progress-indication.component.scss'],
    standalone: false
})
export class PlanFormProgressIndicationComponent extends BaseComponent implements OnInit, OnChanges {
	@Input() formGroup: UntypedFormGroup;

	@Input() public progressValueAccuracy = 2;
	progressSoFar: number;
	total: number;
	percent: number;

	constructor(private visibilityRulesService: VisibilityRulesService) { super(); }

	public value = 0;
	ngOnInit() {
		this.init();
	}

	ngOnChanges(changes: SimpleChanges) {
		if (changes.formGroup) {
			this.init();
		}
	}

	init() {
		setTimeout(() => { this.calculateValueForProgressbar(); });
		this.formGroup
			.valueChanges
			.pipe(takeUntil(this._destroyed))
			.subscribe(control => {
				setTimeout(() => { this.calculateValueForProgressbar(); });
			});
	}

	calculateValueForProgressbar() {
		this.progressSoFar = this.countFormControlsValidForProgress(this.formGroup);
		this.total = this.countFormControlsRequiredFieldsForTotal(this.formGroup);
		this.percent = (this.progressSoFar / this.total) * 100;
		this.value = Number.parseFloat(this.percent.toPrecision(this.progressValueAccuracy));
	}

	countFormControlsValidForProgress(formControl: AbstractControl): number {
		let valueCurrent = 0;
		if (formControl instanceof UntypedFormControl) {
			if (this.controlRequired(formControl) && this.controlEnabled(formControl) && formControl.valid) {
				valueCurrent++;
			}
		} else if (formControl instanceof UntypedFormGroup) {
			Object.keys(formControl.controls).forEach(item => {
				const control = formControl.get(item);
				valueCurrent = valueCurrent + this.countFormControlsValidForProgress(control);
			});
		} else if (formControl instanceof UntypedFormArray) {
			formControl.controls.forEach(item => {
				valueCurrent = valueCurrent + this.countFormControlsValidForProgress(item);
			});
		}
		return valueCurrent;
	}

	countFormControlsRequiredFieldsForTotal(formControl: AbstractControl, checkVisibility = false): number {
		let valueCurrent = 0;
		if (formControl instanceof UntypedFormControl) {
			if (this.controlRequired(formControl) && this.controlEnabled(formControl)) {
				valueCurrent++;
			}
		} else if (formControl instanceof UntypedFormGroup) {
			if (!checkVisibility || (!formControl.get('id')?.value || (this.visibilityRulesService.isVisibleMap[formControl.get('id').value] ?? true))) {
				Object.keys(formControl.controls).forEach(item => {
					const control = formControl.get(item);
					valueCurrent = valueCurrent + this.countFormControlsRequiredFieldsForTotal(control, checkVisibility);
				});
			}
		} else if (formControl instanceof UntypedFormArray) {
			formControl.controls.forEach(item => {
				valueCurrent = valueCurrent + this.countFormControlsRequiredFieldsForTotal(item, checkVisibility);
			});
		}
		return valueCurrent;
	}

	controlRequired(formControl: AbstractControl) {
		if (formControl.validator) {
			const validator = formControl.validator({} as AbstractControl);
			if (validator && validator.required) {
				return true;
			}
		} else { return false }
	}

	controlEnabled(formControl: AbstractControl) {
		if (formControl.enabled) {
			return true;
		} else { return false }
	}
}
