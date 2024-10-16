import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
	selector: 'app-glossary-dialog',
	templateUrl: './glossary-dialog.component.html',
	styleUrls: ['./glossary-dialog.component.scss']
})
export class GlossaryDialogComponent {

	public isDialog: boolean;

	constructor(
		public dialogRef: MatDialogRef<GlossaryDialogComponent>,
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
