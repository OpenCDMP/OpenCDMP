import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
    selector: 'app-user-guide-dialog',
    templateUrl: './user-guide-dialog.component.html',
    styleUrls: ['./user-guide-dialog.component.scss'],
    standalone: false
})
export class UserGuideDialogComponent {
   
	public isDialog: boolean = false;

	constructor(
		public dialogRef: MatDialogRef<UserGuideDialogComponent>,
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
