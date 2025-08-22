import { Component, computed, HostBinding, Inject } from "@angular/core";
import { UntypedFormGroup } from "@angular/forms";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { DescriptionTemplateFieldSet } from "@app/core/model/description-template/description-template";
import { VisibilityRulesService } from "../../../visibility-rules/visibility-rules.service";
import { DescriptionTemplateFieldSetPersist } from "@app/core/model/description-template/description-template-persist";

@Component({
    selector: 'app-description-form-fieldset-editor-dialog',
    styleUrls: ['form-fieldset-editor-dialog.component.scss'],
    templateUrl: 'form-fieldset-editor-dialog.component.html',
    standalone: false
})
export class FormFieldSetEditorDialogComponent {
   
	visibilityRulesService: VisibilityRulesService;
	numberingText: string;
	fieldSet: DescriptionTemplateFieldSet;
	propertiesFormGroup: UntypedFormGroup;

    currentFieldsetValue: DescriptionTemplateFieldSetPersist;

	constructor(
		private dialogRef: MatDialogRef<FormFieldSetEditorDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any,
	) {
		this.fieldSet = data.fieldSet;
		this.propertiesFormGroup = data.propertiesFormGroup;
        this.currentFieldsetValue = data.propertiesFormGroup?.getRawValue();
		this.numberingText = data.numberingText;
		this.visibilityRulesService = data.visibilityRulesService;
	}

	cancel() {
        this.propertiesFormGroup.patchValue(this.currentFieldsetValue, {emitEvent: false});
		this.dialogRef.close();
	}

	save() {
		this.dialogRef.close({value: this.propertiesFormGroup});
	}
}
