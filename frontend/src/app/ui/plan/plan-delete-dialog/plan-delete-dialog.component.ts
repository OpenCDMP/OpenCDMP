import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogTitle, MatDialogContent, MatDialogActions } from '@angular/material/dialog';
import { Description } from '@app/core/model/description/description';
import { MatIconButton, MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-plan-delete-dialog',
    templateUrl: './plan-delete-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatDialogTitle, MatIconButton, MatIcon, CdkScrollable, MatDialogContent, MatDialogActions, MatButton, TranslatePipe]
})
export class PlanDeleteDialogComponent {


	descriptions: Description[];
	constructor(
		public dialogRef: MatDialogRef<PlanDeleteDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any,
	) {
		this.descriptions = data.descriptions;
	}

	close() {
		this.dialogRef.close(false);
	}

	cancel() {
		this.dialogRef.close(false);
	}

	confirm() {
		this.dialogRef.close(true);
	}
}
