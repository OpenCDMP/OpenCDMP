import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogTitle, MatDialogContent, MatDialogActions } from '@angular/material/dialog';
import { BenchmarkConfiguration, TestConfiguration } from '@app/core/model/evaluator/evaluator-configuration';
import { MatDivider } from '@angular/material/list';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { MatButtonToggleGroup, MatButtonToggle } from '@angular/material/button-toggle';
import { MatCheckbox } from '@angular/material/checkbox';
import { MatButton } from '@angular/material/button';
import { TranslatePipe } from '@ngx-translate/core';

export interface BenchmarkDialogResult {
	type: 'benchmarks' | 'tests';
	values: string[];
}

@Component({
    selector: 'app-benchmark-dialog',
    templateUrl: './benchmark-dialog.component.html',
    styleUrls: ['./benchmark-dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatDialogTitle, MatDivider, CdkScrollable, MatDialogContent, MatButtonToggleGroup, FormsModule, ReactiveFormsModule, MatButtonToggle, MatCheckbox, MatDialogActions, MatButton, TranslatePipe]
})
export class BenchmarkDialogComponent {

	evaluatorId: string;
	benchmarks: BenchmarkConfiguration[];
	tests: TestConfiguration[];
	formGroup: UntypedFormGroup;

	get hasBoth(): boolean {
		return this.benchmarks?.length > 0 && this.tests?.length > 0;
	}

	get showBenchmarks(): boolean {
		if (this.hasBoth) return this.formGroup.get('selectionType')?.value === 'benchmarks';
		return this.benchmarks?.length > 0;
	}

	get showTests(): boolean {
		if (this.hasBoth) return this.formGroup.get('selectionType')?.value === 'tests';
		return this.tests?.length > 0;
	}

	constructor(
		public dialogRef: MatDialogRef<BenchmarkDialogComponent>,
		private fb: UntypedFormBuilder,
		@Inject(MAT_DIALOG_DATA) public data: any,
	) {
		this.benchmarks = data.availableBenchmarks;
		this.tests = data.availableTests;
		this.evaluatorId = data.evaluatorId;

		const hasBoth = this.benchmarks?.length > 0 && this.tests?.length > 0;
		const defaultType = hasBoth ? 'benchmarks' : (this.tests?.length > 0 ? 'tests' : 'benchmarks');

		this.formGroup = this.fb.group({
			selectionType: this.fb.control(defaultType),
			benchmarks: this.fb.control([], this.benchmarks?.length > 0 ? Validators.required: null),
			tests: this.fb.control([], this.tests?.length > 0 ? Validators.required: null),
		});

		if (hasBoth) {
			this.onSelectionTypeChange(defaultType);
		}
	}

	onSelectionTypeChange(type: string) {
		const benchmarksCtrl = this.formGroup.get('benchmarks')!;
		const testsCtrl = this.formGroup.get('tests')!;
		benchmarksCtrl.setValue([]);
		testsCtrl.setValue([]);
		if (type === 'benchmarks') {
			benchmarksCtrl.setValidators(Validators.required);
			testsCtrl.clearValidators();
		} else {
			testsCtrl.setValidators(Validators.required);
			benchmarksCtrl.clearValidators();
		}
		benchmarksCtrl.updateValueAndValidity();
		testsCtrl.updateValueAndValidity();
	}

	isSelected(type: 'benchmarks' | 'tests', id: string): boolean {
		return ((this.formGroup.get(type)?.value as string[]) ?? []).includes(id);
	}

	toggleSelection(type: 'benchmarks' | 'tests', id: string, checked: boolean): void {
		const ctrl = this.formGroup.get(type);
		const current = [...((ctrl?.value as string[]) ?? [])];
		const idx = current.indexOf(id);
		if (checked && idx === -1) {
			current.push(id);
		} else if (!checked && idx > -1) {
			current.splice(idx, 1);
		}
		ctrl?.setValue(current);
		ctrl?.markAsTouched();
	}

	cancel() {
		this.dialogRef.close(null);
	}

	confirm() {
		if (this.showBenchmarks) {
			this.dialogRef.close({ type: 'benchmarks', values: this.formGroup.get('benchmarks')!.value } as BenchmarkDialogResult);
		} else {
			this.dialogRef.close({ type: 'tests', values: this.formGroup.get('tests')!.value } as BenchmarkDialogResult);
		}
	}
}
