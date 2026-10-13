import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogContent } from '@angular/material/dialog';
import { Reference } from '@app/core/model/reference/reference';
import { BaseComponent } from '@common/base/base.component';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { ReferenceManualFormComponent } from '../manual-form/reference-manual-form.component';
import { TranslatePipe } from '@ngx-translate/core';

// Thin dialog wrapper around the shared ReferenceManualFormComponent. The form holds all the logic;
// this just hosts it in a centered dialog and maps its events onto the dialog result.
@Component({
    templateUrl: 'reference-dialog-editor.component.html',
    styleUrls: ['./reference-dialog-editor.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [CdkScrollable, MatDialogContent, ReferenceManualFormComponent, TranslatePipe]
})
export class ReferenceDialogEditorComponent extends BaseComponent {
	label: string = null;

	constructor(
		public dialogRef: MatDialogRef<ReferenceDialogEditorComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any,
	) {
		super();
		this.label = data.label;
	}

	onCreated(reference: Reference) {
		this.dialogRef.close(reference);
	}

	onCancelled() {
		this.dialogRef.close(false);
	}
}
