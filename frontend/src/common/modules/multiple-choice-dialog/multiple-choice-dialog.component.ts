import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
    selector: 'app-multiple-choice-dialog',
    templateUrl: './multiple-choice-dialog.component.html',
    styleUrls: ['./multiple-choice-dialog.component.scss'],
    standalone: false
})
export class MultipleChoiceDialogComponent {
   

	agreePrivacyPolicyNames = false;

	constructor(
		public dialogRef: MatDialogRef<MultipleChoiceDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any
	) {
	}

	close() {
		this.dialogRef.close(false);
	}

	apply(i: number) {
		this.dialogRef.close(i);
	}
}
