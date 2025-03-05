import { Component,  Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, UntypedFormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
    selector: 'app-add-account-dialog-component',
    templateUrl: 'add-account-dialog.component.html',
    styleUrls: ['./add-account-dialog.component.scss'],
    standalone: false
})
export class AddAccountDialogComponent implements OnInit {
   
	descriptionTemplateDefinitionModel: any;
	descriptionTemplateDefinitionFormGroup: UntypedFormGroup;
	progressIndication = false;
	form: FormGroup;

	constructor(
		private formBuilder: FormBuilder,
		public dialogRef: MatDialogRef<AddAccountDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any
	) {	}

	ngOnInit(): void {
		this.form = this.formBuilder.group({
			email: [this.data.email, [Validators.required, Validators.email]]
		});
	}

	add(): void {
		this.closeDialog(this.form.value);
	}

	cancel(): void {
		this.closeDialog();
	}

	closeDialog(data?: any): void {
		this.dialogRef.close(data);
	}
}
