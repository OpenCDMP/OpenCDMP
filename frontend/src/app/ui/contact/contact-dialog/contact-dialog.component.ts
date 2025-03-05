import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormGroup } from '@angular/forms';

@Component({
    selector: 'app-contact-dialog',
    templateUrl: './contact-dialog.component.html',
    styleUrls: ['./contact-dialog.component.scss'],
    standalone: false
})
export class ContactDialogComponent {

	constructor(
		public dialogRef: MatDialogRef<ContactDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any
	) { }

	cancel() {
		this.dialogRef.close();
	}

	send() {
		this.dialogRef.close(this.data.formGroup);
	}

	close() {
		this.dialogRef.close(false);
	}
}
