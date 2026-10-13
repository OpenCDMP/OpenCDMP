import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogTitle, MatDialogContent, MatDialogActions, MatDialogClose } from '@angular/material/dialog';
import { MatIconButton, MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { ContactContentComponent } from '../contact-content/contact-content.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-contact-dialog',
    templateUrl: './contact-dialog.component.html',
    styleUrls: ['./contact-dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatDialogTitle, MatIconButton, MatIcon, CdkScrollable, MatDialogContent, ContactContentComponent, MatDialogActions, MatButton, MatDialogClose, TranslatePipe]
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
