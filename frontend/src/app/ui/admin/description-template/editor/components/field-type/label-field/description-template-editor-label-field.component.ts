import { Component, Input, OnInit } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';

@Component({
	selector: 'app-description-template-editor-label-field-component',
	styleUrls: ['./description-template-editor-label-field.component.scss'],
	templateUrl: './description-template-editor-label-field.component.html'
})
export class DescriptionTemplateEditorLabelFieldComponent implements OnInit {

	@Input() form: UntypedFormGroup;

	ngOnInit() {
	}
}
