import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogTitle, MatDialogContent } from '@angular/material/dialog';
import { MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { DescriptionTemplateTableSelectComponent } from '../descriptions-template-table-select/description-template-table-select.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-description-templates-dialog',
    templateUrl: './description-templates-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatDialogTitle, MatIconButton, MatIcon, CdkScrollable, MatDialogContent, DescriptionTemplateTableSelectComponent, TranslatePipe]
})
export class DescriptionTemplatesDialogComponent {


	agreePrivacyPolicyNames = false;

	constructor(
		public dialogRef: MatDialogRef<DescriptionTemplatesDialogComponent>,
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
