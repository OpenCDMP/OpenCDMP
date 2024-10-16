import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
	selector: 'app-language-dialog',
	templateUrl: './language-dialog.component.html',
	styleUrls: ['./language-dialog.component.scss']
})
export class LanguageDialogComponent {

	public isDialog: boolean;

	constructor(
		public dialogRef: MatDialogRef<LanguageDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any
	) {
		this.isDialog = data.isDialog;
	}

	cancel() {
		this.dialogRef.close();
	}

	send() {
		this.dialogRef.close(this.data);
	}

	close() {
		this.dialogRef.close(false);
	}
}
