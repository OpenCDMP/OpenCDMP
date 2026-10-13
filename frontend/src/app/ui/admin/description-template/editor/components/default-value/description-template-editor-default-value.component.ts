import { Component, Input, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormArray, UntypedFormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FieldType } from '@app/core/common/enum/field-type';
import { MatFormField, MatLabel, MatError, MatSuffix } from '@angular/material/form-field';
import { MatSelect, MatOption } from '@angular/material/select';
import { MatInput } from '@angular/material/input';
import { MatDatepickerInput, MatDatepickerToggle, MatDatepicker } from '@angular/material/datepicker';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-description-template-editor-default-value-component',
    templateUrl: './description-template-editor-default-value.component.html',
    styleUrls: ['./description-template-editor-default-value.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatFormField, MatLabel, MatSelect, FormsModule, ReactiveFormsModule, MatOption, MatError, MatInput, MatDatepickerInput, MatDatepickerToggle, MatSuffix, MatDatepicker, TranslatePipe]
})
export class DescriptionTemplateEditorDefaultValueComponent implements OnInit {

	@Input() fieldType: FieldType;
	@Input() form: UntypedFormControl;
	@Input() formArrayOptions: UntypedFormArray;
	@Input() placeHolder: string;
	@Input() required: false;

	descriptionTemplateFieldTypeEnum = FieldType;

	constructor() { }

	ngOnInit() {
	}

}
