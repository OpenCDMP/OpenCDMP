import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogTitle, MatDialogContent, MatDialogActions, MatDialogClose } from '@angular/material/dialog';
import { MatIconButton, MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { GlossaryContentComponent } from '../glossary-content/glossary-content.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-glossary-dialog',
    templateUrl: './glossary-dialog.component.html',
    styleUrls: ['./glossary-dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatDialogTitle, MatIconButton, MatIcon, CdkScrollable, MatDialogContent, GlossaryContentComponent, MatDialogActions, MatButton, MatDialogClose, TranslatePipe]
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
