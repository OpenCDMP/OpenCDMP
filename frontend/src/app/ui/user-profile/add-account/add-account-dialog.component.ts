import { Component, Inject, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, FormGroup, UntypedFormGroup, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogTitle, MatDialogContent, MatDialogActions } from '@angular/material/dialog';
import { MatIconButton, MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { MatFormField, MatError } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-add-account-dialog-component',
    templateUrl: 'add-account-dialog.component.html',
    styleUrls: ['./add-account-dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatDialogTitle, MatIconButton, MatIcon, CdkScrollable, MatDialogContent, MatFormField, MatInput, FormsModule, ReactiveFormsModule, MatError, MatDialogActions, MatButton, TranslatePipe]
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
