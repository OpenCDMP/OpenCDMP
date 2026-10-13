import { Component, Inject, ChangeDetectionStrategy } from "@angular/core";
import { FormGroup, UntypedFormGroup, FormsModule, ReactiveFormsModule } from "@angular/forms";
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogTitle, MatDialogContent, MatDialogActions } from "@angular/material/dialog";
import { BaseFieldSet } from "@app/core/model/description-template/description-template";
import { VisibilityRulesService } from "../../../visibility-rules/visibility-rules.service";
import { FieldSetPersist } from "@app/core/model/description-template/description-template-persist";
import { DescriptionEditorForm } from "@app/ui/description/editor/description-editor.model";
import { PlanEditorForm } from "@app/ui/plan/plan-editor-blueprint/plan-editor.model";
import { MatIconButton, MatButton } from "@angular/material/button";
import { MatIcon } from "@angular/material/icon";
import { CdkScrollable } from "@angular/cdk/scrolling";
import { FormFieldSetTitleComponent } from "../field-set-title/field-set-title.component";
import { FormFieldComponent } from "../../form-field/form-field.component";
import { TranslatePipe } from "@ngx-translate/core";

@Component({
    selector: 'app-description-form-fieldset-editor-dialog',
    templateUrl: 'form-fieldset-editor-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatDialogTitle, MatIconButton, MatIcon, CdkScrollable, MatDialogContent, FormFieldSetTitleComponent, FormFieldComponent, FormsModule, ReactiveFormsModule, MatDialogActions, MatButton, TranslatePipe]
})
export class FormFieldSetEditorDialogComponent {

	visibilityRulesService: VisibilityRulesService;
	numberingText: string;
	fieldSet: BaseFieldSet;
	propertiesFormGroup: UntypedFormGroup;
    formGroup: FormGroup<PlanEditorForm | DescriptionEditorForm>;

    currentFieldsetValue: FieldSetPersist;

	constructor(
		private dialogRef: MatDialogRef<FormFieldSetEditorDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any,
	) {
		this.fieldSet = data.fieldSet;
		this.propertiesFormGroup = data.propertiesFormGroup;
        this.formGroup = data.formGroup;
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
