import { Component, Input, OnInit } from '@angular/core';
import { UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { DescriptionTemplateExternalSelectHttpMethodType } from '@app/core/common/enum/description-template-external-select-http-method-type';
import { DescriptionTemplateExternalSelectAuthType } from '@app/core/common/enum/description-template-external-select-auth-type';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';

@Component({
	selector: 'app-description-template-editor-external-select-field-component',
	styleUrls: ['./description-template-editor-external-select-field.component.scss'],
	templateUrl: './description-template-editor-external-select-field.component.html'
})
export class DescriptionTemplateEditorExternalSelectFieldComponent implements OnInit {

	@Input() form: UntypedFormGroup;
	@Input() validationErrorModel: ValidationErrorModel;
	@Input() validationRootPath: string;
	
	methodTypeValues = this.enumUtils.getEnumValues<DescriptionTemplateExternalSelectHttpMethodType>(DescriptionTemplateExternalSelectHttpMethodType);
	authTypeValues = this.enumUtils.getEnumValues<DescriptionTemplateExternalSelectAuthType>(DescriptionTemplateExternalSelectAuthType);

	constructor (
		public enumUtils: EnumUtils
	) {}

	ngOnInit() {
	}

	addSource() {
		const externalDataset = new DescriptionTemplateExternalSelectSourceEditorModel(this.validationErrorModel);
		const externalSelectArray = this.form.get('data').get('sources') as UntypedFormArray;
		externalSelectArray.push(externalDataset.buildForm({rootPath: this.validationRootPath + 'data.sources[' + externalSelectArray.length + '].'}));
	}

	removeSource(index: number) {
		(<UntypedFormArray>this.form.get('data').get('sources')).removeAt(index);
		DescriptionTemplateExternalSelectDataEditorModel.reapplyValidators({
			formGroup: this.form?.get('data') as UntypedFormGroup,
			rootPath: `${this.validationRootPath}data.`,
			validationErrorModel: this.validationErrorModel
		});

		this.form.get('data').get('sources').markAsDirty();
	}
}
