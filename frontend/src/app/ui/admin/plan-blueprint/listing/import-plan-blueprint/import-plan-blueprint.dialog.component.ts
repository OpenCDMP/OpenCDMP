import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';


@Component({
    templateUrl: './import-plan-blueprint.dialog.component.html',
    styleUrls: ['./import-plan-blueprint.dialog.component.scss'],
    standalone: false
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
		let file = event;
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
