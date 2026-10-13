import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogTitle, MatDialogContent, MatDialogActions, MatDialogClose } from '@angular/material/dialog';
import { MatIconButton, MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { FaqContentComponent } from '../faq-content/faq-content.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-faq-dialog',
    templateUrl: './faq-dialog.component.html',
    styleUrls: ['./faq-dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatDialogTitle, MatIconButton, MatIcon, CdkScrollable, MatDialogContent, FaqContentComponent, MatDialogActions, MatButton, MatDialogClose, TranslatePipe]
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
