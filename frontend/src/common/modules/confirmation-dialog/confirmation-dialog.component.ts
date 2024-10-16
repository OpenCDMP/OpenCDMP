import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
	selector: 'app-confirmation-dialog',
	templateUrl: './confirmation-dialog.component.html',
	styleUrls: ['./confirmation-dialog.component.scss']
})
export class ConfirmationDialogComponent {

	agreePrivacyPolicyNames = false;

	constructor(
		public dialogRef: MatDialogRef<ConfirmationDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any
	) {
	}

	close() {
		this.dialogRef.close(false);
	}

	cancel() {
		this.dialogRef.close(false);
	}

	confirm() {
		this.dialogRef.close(true);
	}
}
