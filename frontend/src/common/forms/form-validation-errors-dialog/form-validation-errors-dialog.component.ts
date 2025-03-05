import { Component, Inject } from '@angular/core';
import { AbstractControl, UntypedFormArray, UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';

@Component({
    selector: 'app-form-validation-errors-dialog',
    templateUrl: './form-validation-errors-dialog.component.html',
    styleUrls: ['./form-validation-errors-dialog.component.scss'],
    standalone: false
})
export class FormValidationErrorsDialogComponent {
   

	formGroup: UntypedFormGroup;
	errorMessages: string[] = [];

	constructor(public dialogRef: MatDialogRef<FormValidationErrorsDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any,
		private language: TranslateService
	) {
		if (data.formGroup !== undefined && data.formGroup !== null) {
			this.formGroup = data.formGroup;
			this.errorMessages = this.getErrors(this.formGroup);
		} else if (data.errorMessages !== undefined && data.errorMessages !== null) {
			this.errorMessages = data.errorMessages;
		}
	}

	onClose(): void {
		this.dialogRef.close();
	}

	public getErrors(formControl: AbstractControl, key?: string): string[] {
		const errors: string[] = [];
		if (formControl instanceof UntypedFormControl) {
			if (formControl.errors !== null) {
				let name: string;
				if ((<any>formControl).nativeElement !== undefined && (<any>formControl).nativeElement !== null) {
					name = this.getPlaceHolder(formControl);
				} else {
					name = key;
				}
				errors.push(...this.getErrorMessage(formControl, name));
			}
		} else if (formControl instanceof UntypedFormGroup) {
			if (formControl.errors) { (errors.push(...Object.values(formControl.errors).map(x => x.message) as string[])); }
			Object.keys(formControl.controls).forEach(key => {
				const control = formControl.get(key);
				errors.push(...this.getErrors(control, key));
			});
		} else if (formControl instanceof UntypedFormArray) {
			if (formControl.errors) { (errors.push(...Object.values(formControl.errors).map(x => x.message) as string[])); }
			formControl.controls.forEach(item => {
				errors.push(...this.getErrors(item));
			});
		}
		return errors;
	}

	getErrorMessage(formControl: UntypedFormControl, name: string): string[] {
		const errors: string[] = [];
		Object.keys(formControl.errors).forEach(key => {
			if (key === 'required') { errors.push(this.language.instant(name + ": " + this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.REQUIRED'))); }
			// if (key === 'required') { errors.push(this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.THIS-FIELD') + ' "' + this.getPlaceHolder(formControl) + '" ' + this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.HAS-ERROR') + ', ' + this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.REQUIRED')); }
			else if (key === 'email') { errors.push(this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.THIS-FIELD') + ' "' + name + '" ' + this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.HAS-ERROR') + ', ' + this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.EMAIL')); }
			else if (key === 'min') { errors.push(this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.THIS-FIELD') + ' "' + name + '" ' + this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.HAS-ERROR') + ', ' + this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.MIN-VALUE', { 'min': formControl.getError('min').min })); }
			else if (key === 'max') { errors.push(this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.THIS-FIELD') + ' "' + name + '" ' + this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.HAS-ERROR') + ', ' + this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.MAX-VALUE', { 'max': formControl.getError('max').max })); }
			else { errors.push(name + ': ' + formControl.errors[key].message); }
		});
		return errors;
	}

	getPlaceHolder(formControl: any): string {
		if (formControl.nativeElement.localName === 'input' || formControl.nativeElement.localName === 'textarea'
			|| formControl.nativeElement.localName === 'richTextarea') {
			return formControl.nativeElement.getAttribute('placeholder');
		} else if (formControl.nativeElement.localName === 'mat-select') {
			return formControl.nativeElement.getAttribute('placeholder');
		} else if (formControl.nativeElement.localName === 'app-single-auto-complete') {
			return (Array.from(formControl.nativeElement.firstChild.children).filter((x: any) => x.localName === 'input')[0] as any).getAttribute('placeholder');
		} else if (formControl.nativeElement.localName === 'app-multiple-auto-complete') {
			return (Array.from(formControl.nativeElement.firstChild.firstChild.firstChild.children).filter((x: any) => x.localName === 'input')[0] as any).getAttribute('placeholder');
		}
	}
}
