import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogTitle, MatDialogContent, MatDialogActions } from '@angular/material/dialog';
import { BaseComponent } from '@common/base/base.component';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { MatCheckbox } from '@angular/material/checkbox';
import { FormsModule } from '@angular/forms';
import { MatButton } from '@angular/material/button';
import { TranslatePipe } from '@ngx-translate/core';

export interface TermsAcceptanceDialogData {
	content: string;
}

@Component({
    selector: 'app-terms-acceptance-dialog',
    templateUrl: './terms-acceptance-dialog.component.html',
    styleUrls: ['./terms-acceptance-dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatDialogTitle, CdkScrollable, MatDialogContent, MatDialogActions, MatCheckbox, FormsModule, MatButton, TranslatePipe]
})
export class TermsAcceptanceDialogComponent extends BaseComponent {

	safeContent: SafeHtml;
	accepted = false;

	constructor(
		public dialogRef: MatDialogRef<TermsAcceptanceDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: TermsAcceptanceDialogData,
		private sanitizer: DomSanitizer,
	) {
		super();
		dialogRef.disableClose = true;
		this.safeContent = this.sanitizer.bypassSecurityTrustHtml(data?.content ?? '');
	}

	confirm() {
		if (this.accepted) {
			this.dialogRef.close(true);
		}
	}
}
