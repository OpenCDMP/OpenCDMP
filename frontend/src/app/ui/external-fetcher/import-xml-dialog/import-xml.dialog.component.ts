import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogTitle, MatDialogContent, MatDialogActions } from '@angular/material/dialog';
import { FileInputValue } from '@ngx-dropzone/cdk';
import { MatIconButton, MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { FileUploadComponent } from '../../../library/file-uploader/file-uploader.component';
import { TranslatePipe } from '@ngx-translate/core';

export interface ImportXmlDialogData {
	title: string;
	confirmButton: string;
	cancelButton: string;
}

@Component({
    templateUrl: './import-xml.dialog.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatDialogTitle, MatIconButton, MatIcon, CdkScrollable, MatDialogContent, FileUploadComponent, MatDialogActions, MatButton, TranslatePipe]
})
export class ImportXmlDialogComponent {

	file: File = null;

	constructor(
		public dialogRef: MatDialogRef<ImportXmlDialogComponent, File>,
		@Inject(MAT_DIALOG_DATA) public data: ImportXmlDialogData
	) { }

	selectFile(fileValue: FileInputValue): void {
		this.file = Array.isArray(fileValue) ? fileValue[0] : fileValue;
	}

	onRemove(): void {
		this.file = null;
	}

	cancel(): void {
		this.dialogRef.close(null);
	}

	confirm(): void {
		this.dialogRef.close(this.file);
	}
}
