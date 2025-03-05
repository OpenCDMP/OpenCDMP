import { Component, computed, HostBinding, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Description } from '@app/core/model/description/description';

@Component({
    selector: 'app-plan-delete-dialog',
    templateUrl: './plan-delete-dialog.component.html',
    styleUrls: ['./plan-delete-dialog.component.scss'],
    standalone: false
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
