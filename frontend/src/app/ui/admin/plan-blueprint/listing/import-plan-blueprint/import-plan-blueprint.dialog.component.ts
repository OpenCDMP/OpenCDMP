import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogTitle, MatDialogContent, MatDialogActions } from '@angular/material/dialog';
import { MatIconButton, MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { FileUploadComponent } from '../../../../../library/file-uploader/file-uploader.component';
import { MatFormField } from '@angular/material/form-field';
import { ReactiveAsteriskDirective } from '../../../../../../common/forms/reactive-asterisk-directive';
import { MatInput } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';


@Component({
    templateUrl: './import-plan-blueprint.dialog.component.html',
    styleUrls: ['./import-plan-blueprint.dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatDialogTitle, MatIconButton, MatIcon, CdkScrollable, MatDialogContent, FileUploadComponent, MatFormField, ReactiveAsteriskDirective, MatInput, FormsModule, MatDialogActions, MatButton, TranslatePipe]
})
export class ImportPlanBlueprintDialogComponent {
   

	sizeError = false;
	selectFile = false;
	maxFileSize: number = 1048576;
	selectedFileName: string;

	constructor(
		public dialogRef: MatDialogRef<ImportPlanBlueprintDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any
	) { }


	selectXML(event: File) {
		const file = event;
		if (!file) return;//no select closed with cancel . no file selected
		const size: number = file.size;  // Get file size.
		this.sizeError = size > this.maxFileSize;  // Checks if file size is valid.
		const formdata: FormData = new FormData();
		if (!this.sizeError) {
			this.data.file = file;
			this.selectFile = true;
			this.selectedFileName = file.name;
		}
		this.data.name = file.name;
	}

	cancel() {
		this.data.sucsess = false;
		this.dialogRef.close(this.data);
	}

	confirm() {
		this.data.name = this.data.name;
		this.data.sucsess = true;
		this.dialogRef.close(this.data);
	}

	hasBlueprint(): boolean {
		return (this.selectFile && !this.sizeError);
	}
	//remove selected file
	onRemove() {
		this.data.name = "";
		this.selectFile = false;
		this.selectedFileName = "";
	}
}
