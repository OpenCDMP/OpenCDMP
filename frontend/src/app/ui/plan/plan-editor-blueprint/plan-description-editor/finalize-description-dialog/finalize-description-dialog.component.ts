import { Component, Inject, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { DescriptionEditorForm} from '@app/ui/description/editor/description-editor.model';
import { BaseComponent } from '@common/base/base.component';
import { TranslateService } from '@ngx-translate/core';
import { DescriptionEditorHelper } from '../plan-description-editor-helper';

@Component({
  selector: 'app-finalize-description-dialog',
  standalone: false,
  templateUrl: './finalize-description-dialog.component.html',
  styleUrl: './finalize-description-dialog.component.scss'
})
export class FinalizeDescriptionDialogComponent extends BaseComponent implements OnInit{
    formGroup: FormGroup<DescriptionEditorForm>;
    isValid: boolean; 
    errorFields: string[] = [];
    constructor(
        public dialogRef: MatDialogRef<FinalizeDescriptionDialogComponent>,
        private language: TranslateService,
        @Inject(MAT_DIALOG_DATA) public data: any
    ) {
        super();
        this.formGroup = data.formGroup;
        this.dialogRef.backdropClick().subscribe(() => this.dialogRef.close({
            isValid: this.isValid,
            cancelled: true
        } as DescriptionFinalizeDialogOutput))
    }

    ngOnInit(): void {
        this.isValid = this.formGroup.valid;
        this.errorFields = this.getErrors();
    }

    onClose(){
        this.dialogRef.close({
            isValid: this.isValid,
            cancelled: true
        } as DescriptionFinalizeDialogOutput)
    }

    onFinalize(){
        this.dialogRef.close({
            isValid: this.isValid,
            cancelled: false
        } as DescriptionFinalizeDialogOutput)
    }

    getErrors(): string[]{
        return DescriptionEditorHelper.getDescriptionErrors({
            formGroup: this.formGroup, 
            htmlMapping: (id: string) => `${id}-label`
        });
    }

    
        // public getErrors(formControl: AbstractControl, key?: string): string[] {
        //     const errors: string[] = [];
        //     if (formControl instanceof UntypedFormControl) {
        //         if (formControl.errors !== null) {
        //             let name: string;
        //             if ((<any>formControl).nativeElement !== undefined && (<any>formControl).nativeElement !== null) {
        //                 name = this.getPlaceHolder(formControl);
        //             } else {
        //                 name = key;
        //             }
        //             errors.push(name);
        //         }
        //     } else if (formControl instanceof UntypedFormGroup) {
        //         if (formControl.errors) { (errors.push(...Object.values(formControl.errors).map(x => x.message) as string[])); }
        //         Object.keys(formControl.controls).forEach(key => {
        //             const control = formControl.get(key);
        //             errors.push(...this.getErrors2(control, key));
        //         });
        //     } else if (formControl instanceof UntypedFormArray) {
        //         if (formControl.errors) { (errors.push(...Object.values(formControl.errors).map(x => x.message) as string[])); }
        //         formControl.controls.forEach(item => {
        //             errors.push(...this.getErrors2(item));
        //         });
        //     }
        //     return errors;
        // }

    
        // getPlaceHolder(formControl: any): string {
        //     if (formControl.nativeElement.localName === 'input' || formControl.nativeElement.localName === 'textarea'
        //         || formControl.nativeElement.localName === 'richTextarea') {
        //         return formControl.nativeElement.getAttribute('placeholder');
        //     } else if (formControl.nativeElement.localName === 'mat-select') {
        //         return formControl.nativeElement.getAttribute('placeholder');
        //     } else if (formControl.nativeElement.localName === 'app-single-auto-complete') {
        //         return (Array.from(formControl.nativeElement.firstChild.children).filter((x: any) => x.localName === 'input')[0] as any).getAttribute('placeholder');
        //     } else if (formControl.nativeElement.localName === 'app-multiple-auto-complete') {
        //         return (Array.from(formControl.nativeElement.firstChild.firstChild.firstChild.children).filter((x: any) => x.localName === 'input')[0] as any).getAttribute('placeholder');
        //     }
        // }
}
export interface DescriptionFinalizeDialogOutput{
    cancelled?: boolean;
    isValid?: boolean
}