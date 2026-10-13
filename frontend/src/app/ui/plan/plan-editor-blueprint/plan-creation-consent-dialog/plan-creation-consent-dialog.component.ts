import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatIconButton, MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { PlanCreationConsentComponent } from '../../plan-creation-consent/plan-creation-consent.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-plan-creation-consent-dialog',
    templateUrl: './plan-creation-consent-dialog.component.html',
    styleUrls: ['./plan-creation-consent-dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatIconButton, MatIcon, PlanCreationConsentComponent, MatButton, TranslatePipe]
})
export class PlanCreationConsentDialogComponent {
   
	planCreationConsentAccepted: boolean = false;

	constructor(
		public dialogRef: MatDialogRef<PlanCreationConsentDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any
	) {
	}

	onPlanCreationConsentChange(checked: boolean): void {
		this.planCreationConsentAccepted = checked;
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
