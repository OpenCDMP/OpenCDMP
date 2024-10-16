import { Component, Input, OnInit } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { BaseComponent } from '@common/base/base.component';

@Component({
	selector: 'app-description-template-editor-section-component',
	templateUrl: './description-template-editor-section.component.html',
	styleUrls: ['./description-template-editor-section.component.scss']
})

export class DescriptionTemplateEditorSectionComponent extends BaseComponent implements OnInit {

	@Input() form: UntypedFormGroup;

	constructor() { super(); }

	ngOnInit() {

	}
}
