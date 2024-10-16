import { Component, Inject } from "@angular/core";
import { UntypedFormGroup } from "@angular/forms";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { DescriptionTemplateFieldSet } from "@app/core/model/description-template/description-template";
import { VisibilityRulesService } from "../../../visibility-rules/visibility-rules.service";

@Component({
	selector: 'app-description-form-fieldset-editor-dialog',
	styleUrls: ['form-fieldset-editor-dialog.component.scss'],
	templateUrl: 'form-fieldset-editor-dialog.component.html'
})
export class FormFieldSetEditorDialogComponent {

	visibilityRulesService: VisibilityRulesService;
	numberingText: string;
	fieldSet: DescriptionTemplateFieldSet;
	propertiesFormGroup: UntypedFormGroup;

	constructor(
		private dialogRef: MatDialogRef<FormFieldSetEditorDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any
	) {
		this.fieldSet = data.fieldSet;
		this.propertiesFormGroup = data.propertiesFormGroup;
		this.numberingText = data.numberingText;
		this.visibilityRulesService = data.visibilityRulesService;
	}

	cancel() {
		this.dialogRef.close();
	}

	save() {
		this.dialogRef.close(this.propertiesFormGroup);
	}

	public close() {
		this.dialogRef.close(false);
	}
}
