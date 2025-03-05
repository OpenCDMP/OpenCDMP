import { Component, Input, OnInit } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { DescriptionTemplateFieldDataExternalDatasetType } from '@app/core/common/enum/description-template-field-data-external-dataset-type';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';

@Component({
    selector: 'app-description-template-editor-external-datasets-field-component',
    styleUrls: ['./description-template-editor-external-datasets-field.component.scss'],
    templateUrl: './description-template-editor-external-datasets-field.component.html',
    standalone: false
})
export class DescriptionTemplateEditorExternalDatasetsFieldComponent implements OnInit {

	@Input() form: UntypedFormGroup;
	externalDatasetTypeEnumValues = this.enumUtils.getEnumValues<DescriptionTemplateFieldDataExternalDatasetType>(DescriptionTemplateFieldDataExternalDatasetType);
	constructor(
		private enumUtils: EnumUtils
	) {}

	ngOnInit() {

	}
}
