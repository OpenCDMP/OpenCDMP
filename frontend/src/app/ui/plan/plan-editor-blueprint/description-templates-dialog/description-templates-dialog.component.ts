import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
	selector: 'app-description-templates-dialog',
	templateUrl: './description-templates-dialog.component.html',
	standalone: false
})
export class DescriptionTemplatesDialogComponent {


	agreePrivacyPolicyNames = false;

	constructor(
		public dialogRef: MatDialogRef<DescriptionTemplatesDialogComponent>,
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
