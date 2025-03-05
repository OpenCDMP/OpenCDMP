import { Component, Input, OnInit } from '@angular/core';
import { UntypedFormArray, UntypedFormControl } from '@angular/forms';
import { DescriptionTemplateFieldType } from '@app/core/common/enum/description-template-field-type';

@Component({
    selector: 'app-description-template-editor-default-value-component',
    templateUrl: './description-template-editor-default-value.component.html',
    styleUrls: ['./description-template-editor-default-value.component.scss'],
    standalone: false
})
export class DescriptionTemplateEditorDefaultValueComponent implements OnInit {

	@Input() fieldType: DescriptionTemplateFieldType;
	@Input() form: UntypedFormControl;
	@Input() formArrayOptions: UntypedFormArray;
	@Input() placeHolder: String;
	@Input() required: false;

	descriptionTemplateFieldTypeEnum = DescriptionTemplateFieldType;

	constructor() { }

	ngOnInit() {
	}

}
