import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogTitle, MatDialogContent, MatDialogActions, MatDialogClose } from '@angular/material/dialog';
import { MatIconButton, MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { UserGuideContentComponent } from '../user-guide-content/user-guide-content.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-user-guide-dialog',
    templateUrl: './user-guide-dialog.component.html',
    styleUrls: ['./user-guide-dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatDialogTitle, MatIconButton, MatIcon, CdkScrollable, MatDialogContent, UserGuideContentComponent, MatDialogActions, MatButton, MatDialogClose, TranslatePipe]
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
