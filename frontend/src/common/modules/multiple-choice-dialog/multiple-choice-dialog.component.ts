import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogTitle, MatDialogContent, MatDialogActions } from '@angular/material/dialog';
import { MatIcon } from '@angular/material/icon';
import { MatIconButton, MatButton } from '@angular/material/button';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { MatCheckbox } from '@angular/material/checkbox';
import { FormsModule } from '@angular/forms';
import { MatTooltip } from '@angular/material/tooltip';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-multiple-choice-dialog',
    templateUrl: './multiple-choice-dialog.component.html',
    styleUrls: ['./multiple-choice-dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatIcon, MatIconButton, MatDialogTitle, CdkScrollable, MatDialogContent, MatCheckbox, FormsModule, MatDialogActions, MatTooltip, MatButton, TranslatePipe]
})
export class MultipleChoiceDialogComponent {
   

	agreePrivacyPolicyNames = false;

	constructor(
		public dialogRef: MatDialogRef<MultipleChoiceDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any
	) {
	}

	close() {
		this.dialogRef.close(false);
	}

	apply(i: number) {
		this.dialogRef.close(i);
	}
}
