import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';


@Component({
	templateUrl: './import-description-template.dialog.component.html',
	styleUrls: ['./import-description-template.dialog.component.scss']
})
export class ImportDescriptionTemplateDialogComponent {

	sizeError = false;
	selectFile = false;
	maxFileSize: number = 1048576;
	selectedFileName: string;

	constructor(
		public dialogRef: MatDialogRef<ImportDescriptionTemplateDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any
	) { }


	selectXML(event) {
		let file: FileList = null;
		if (event.target && event.target.files) {
			file = event.target.files;
		} else if (event.addedFiles && event.addedFiles.length) {
			file = event.addedFiles;
		}
		if (!file) return;//no select closed with cancel . no file selected
		const size: number = file[0].size;  // Get file size.
		this.sizeError = size > this.maxFileSize;  // Checks if file size is valid.
		const formdata: FormData = new FormData();
		if (!this.sizeError) {
			this.data.file = file;
			this.selectFile = true;
			this.selectedFileName = file[0].name;
		}
		this.data.name = file[0].name;
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
