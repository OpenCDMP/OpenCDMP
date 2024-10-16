import { Component, Input, OnInit } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';

@Component({
	selector: 'app-description-template-editor-label-and-multiplicity-field-component',
	styleUrls: ['./description-template-editor-label-and-multiplicity-field.component.scss'],
	templateUrl: './description-template-editor-label-and-multiplicity-field.component.html'
})
export class DescriptionTemplateEditorLabelAndMultiplicityFieldComponent implements OnInit {

	@Input() form: UntypedFormGroup;

	ngOnInit() {
	}
}
