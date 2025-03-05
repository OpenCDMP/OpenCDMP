import { Component, Input, OnInit } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';

@Component({
    selector: 'app-description-template-editor-reference-type-field-component',
    styleUrls: ['./description-template-editor-reference-type-field.component.scss'],
    templateUrl: './description-template-editor-reference-type-field.component.html',
    standalone: false
})
export class DescriptionTemplateEditorReferenceTypeFieldComponent implements OnInit {

	@Input() form: UntypedFormGroup;
	constructor(
		public referenceTypeService: ReferenceTypeService
	) { }

	ngOnInit() {

	}
}
