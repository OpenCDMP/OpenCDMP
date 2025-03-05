import { Component,Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
    selector: 'app-faq-dialog',
    templateUrl: './faq-dialog.component.html',
    styleUrls: ['./faq-dialog.component.scss'],
    standalone: false
})
export class FaqDialogComponent {
   

	public isDialog: boolean = false;

	constructor(
		public dialogRef: MatDialogRef<FaqDialogComponent>,
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
