import { Component, Inject } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { BenchmarkConfiguration } from '@app/core/model/evaluator/evaluator-configuration';

@Component({
    selector: 'app-benchmark-dialog',
    templateUrl: './benchmark-dialog.component.html',
    styleUrls: ['./benchmark-dialog.component.scss'],
    standalone: false
})
export class BenchmarkDialogComponent {
   

	benchmarks: BenchmarkConfiguration[];
	formGroup: UntypedFormGroup;

	constructor(
		public dialogRef: MatDialogRef<BenchmarkDialogComponent>,
		private fb: UntypedFormBuilder,
		@Inject(MAT_DIALOG_DATA) public data: any,
	) {
		this.benchmarks = data.availableBenchmarks;
		this.formGroup = this.fb.group(
			{
				benchmarks: this.fb.control([], Validators.required),
			});
	}

	cancel() {
		this.dialogRef.close(null);
	}

	confirm() {
		this.dialogRef.close(this.formGroup.get('benchmarks').value);
	}
}
